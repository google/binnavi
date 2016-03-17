// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

include "WinDynamoRioSystem.hpp"

#include <Windows.h>

#include <cassert>
#include <chrono>
#include <exception>
#include <memory>
#include <thread>

#include "../windowscommon/WindowsCommon.hpp"

WinDynamoRioSystem::WinDynamoRioSystem(const std::string& client_dll_path,
                                       const std::string& drrun_path)
    : client_dll_path_(client_dll_path),
      drrun_path_(drrun_path),
      dr_pipe_(INVALID_HANDLE_VALUE) {
  initializeRegisterList();
}

WinDynamoRioSystem::WinDynamoRioSystem(
    const std::string& client_dll_path, const std::string& drrun_path,
    const NATIVE_STRING path, const std::vector<const NATIVE_STRING>& cmd_line)
    : BaseSystem(path, cmd_line),
      client_dll_path_(client_dll_path),
      drrun_path_(drrun_path),
      dr_pipe_(INVALID_HANDLE_VALUE) {
  initializeRegisterList();
}

WinDynamoRioSystem::~WinDynamoRioSystem() {
  if (dr_pipe_ != INVALID_HANDLE_VALUE) {
    CloseHandle(dr_pipe_);
  }
}

NaviError WinDynamoRioSystem::readDebugEvents() {
  if (dr_pipe_ == INVALID_HANDLE_VALUE) {
    // No connection yet, nothing to read.
    // We have to return SUCCESS, because DebugClient calls this function before
    // starting the debuggee.
    return NaviErrors::SUCCESS;
  }
  security::drdebug::Command cmd;
  cmd.set_command(security::drdebug::Command_CommandType_GET_DEBUG_EVENTS);
  security::drdebug::Response resp;

  // TODO(mkow): Make sendCommandToDr() result more verbose after fixing DR bug:
  // https://code.google.com/p/dynamorio/issues/detail?id=297
  if (!sendCommandToDr(cmd, &resp)) {
    return NaviErrors::CONNECTION_CLOSED;
  }
  if (resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Client sent an error response when reading debug events %s:%d.",
        __FUNCTION__, __LINE__);
    return NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
  }

  security::drdebug::GetDebugEventsResult events = resp.get_debug_events_result();

  if (events.debug_event_size() > 0)
    printf("Read %d debug events\n", events.debug_event_size());

  for (security::drdebug::DebugEvent event : events.debug_event()) {
    switch (event.type()) {
      case security::drdebug::DebugEvent_EventType_BREAKPOINT_HIT: {
        auto address = event.breakpoint_hit_info().address();
        auto id = event.breakpoint_hit_info().bp_id();
        auto thread_id = event.breakpoint_hit_info().thread_id();
        setActiveThread(thread_id);
        registers_from_bphit_event = event.breakpoint_hit_info().register_();
        auto res =
            breakpointHit(address, thread_id, true /* resume on echo bp */);
        if (res) {
          msglog->log(LOG_ALWAYS, "Error: breakpointHit() failed with code: %d",
                      res);
        }
        break;
      }
      case security::drdebug::DebugEvent_EventType_THREAD_CREATED: {
        threadCreated(event.thread_created_info().thread_id(), RUNNING);
        break;
      }
      case security::drdebug::DebugEvent_EventType_THREAD_EXITED: {
        threadExit(event.thread_exited_info().thread_id());
        break;
      }
      case security::drdebug::DebugEvent_EventType_MODULE_LOADED: {
        CPUADDRESS baseAddress = event.module_loaded_info().address();
        CPUADDRESS size = event.module_loaded_info().size();
        std::string name = event.module_loaded_info().name();
        std::string path = "";
        unsigned int thread = event.module_loaded_info().thread();
        Module module(name, path, baseAddress, size);
        module_map_[baseAddress] = module;
        moduleLoaded(module, thread);
        break;
      }
      case security::drdebug::DebugEvent_EventType_MODULE_UNLOADED: {
        auto addr =
            static_cast<CPUADDRESS>(event.module_unloaded_info().address());
        auto cit = module_map_.find(addr);
        if (cit == module_map_.end()) {
          moduleUnloaded(
              Module("<unknown module>", "" /* path */, addr, 0 /* size */));
        } else {
          moduleUnloaded(cit->second);
        }
        break;
      }
      case security::drdebug::DebugEvent_EventType_PROCESS_STARTED: {
        Module module(getTargetApplicationPath().filename().string(),
                      getTargetApplicationPath().string(),
                      event.process_started_info().base_address(),
                      event.process_started_info().image_size());
        Thread thread(event.process_started_info().thread_id(), RUNNING);
        processStart(module, thread);
        module_map_.emplace(module.baseAddress, module);
        break;
      }
      case security::drdebug::DebugEvent_EventType_PROCESS_EXITED: {
        printf("Received PROCESS_EXITED!\n");
        processExit();
        break;
      }
      case security::drdebug::DebugEvent_EventType_EXCEPTION_THROWN: {
        auto thread_id = event.exception_thrown_info().thread_id();
        auto address = event.exception_thrown_info().address();
        auto code = event.exception_thrown_info().exc_code();
        setActiveThread(thread_id);
        switch (GetExceptionAction(code)) {
          case PASS_TO_APP:
            // Nothing to do, exception was passed to application by dr client.
            break;
          case SKIP_APP_HANDLER:
            // Nothing to do, exception was silenced by dr client.
            break;
          case HALT:
            exceptionRaised(thread_id, address, code);
            break;
        }
        break;
      }
      default:
        // Shold never be reached
        msglog->log(LOG_ALWAYS,
                    "Error (%s:%d): Client sent invalid debug event: %d",
                    __FUNCTION__, __LINE__, event.type());
        throw std::runtime_error("Unknown or not implemented event type!");
    }
  }

  return NaviErrors::SUCCESS;
}

std::vector<RegisterDescription> WinDynamoRioSystem::getRegisterNames() const {
  return register_names_;
}

unsigned int WinDynamoRioSystem::getAddressSize() const {
  // TODO(mkow): this should be retrieved from client DLL
  return 32;
}

DebuggerOptions WinDynamoRioSystem::getDebuggerOptions() const {
  DebuggerOptions options;

  SYSTEM_INFO system_info;
  GetSystemInfo(&system_info);

  options.canAttach = false;
  options.canDetach = false;
  options.canTerminate = true;
  options.canMemmap = true;       // TODO(mkow): implement this
  options.canValidMemory = false;  // TODO(mkow): implement this
  options.canMultithread = true;
  options.canSoftwareBreakpoint = true;
  options.canHalt = false;
  options.haltBeforeCommunicating = false;
  options.hasStack = true;
  options.pageSize = system_info.dwPageSize;
  options.canTraceCount = false;           // TODO(mkow): implement this
  options.canBreakOnModuleUnload = false;  // TODO(mkow): implement this
  options.canBreakOnModuleLoad = false;    // TODO(mkow): implement this

  options.exceptions = getPlatformExceptions();

  return options;
}

NaviError WinDynamoRioSystem::attachToProcess() {
  throw std::runtime_error("Not supported");
}

NaviError WinDynamoRioSystem::startProcess(
    const NATIVE_STRING path,
    const std::vector<const NATIVE_STRING>& commands) {
  pipe_name_ = randomPipeName();
  int buf_size = 10 * 1024;
  dr_pipe_ = CreateNamedPipeA(pipe_name_.c_str(), PIPE_ACCESS_DUPLEX,
                              PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
                              PIPE_UNLIMITED_INSTANCES, buf_size, buf_size,
                              0,       /* nDefaultTimeOut */
                              nullptr  /* lpSecurityAttributes */ );
  if (dr_pipe_ == INVALID_HANDLE_VALUE) {
    msglog->log(LOG_ALWAYS, "Error: CreateNamedPipeA failed %s:%d.",
                __FUNCTION__, __LINE__);
    return NaviErrors::GENERIC_ERROR;
  }

  std::string cmd_line = "\"" + drrun_path_ + "\"";
#ifdef _DEBUG
// TODO(mkow): Uncomment this after fixing mutexes usage in dr client. Current
// client version fails on asserts with "-debug".
  // cmd_line += " -debug";
#endif
  cmd_line += " -c \"" + client_dll_path_ + "\"";
  cmd_line += " \"" + pipe_name_ + "\"";
  cmd_line += " -- ";
  cmd_line += "\"" + std::string(path) + "\"";
  for (const auto& arg : commands) {
    cmd_line += " ";
    cmd_line += arg;
  }
  STARTUPINFOA si;
  PROCESS_INFORMATION pi;
  memset(&si, 0, sizeof(si));
  si.cb = sizeof(si);
  // c_str() is guaranteed to point to the internal string buffer (from C++11)
  auto cmd_line_cstr = const_cast<char*>(cmd_line.c_str());
  if (!CreateProcessA(nullptr,        // lpApplicationName
                      cmd_line_cstr,  // lpCommandLine
                      nullptr,        // lpProcessAttributes
                      nullptr,        // lpThreadAttributes
                      FALSE,          // bInheritHandles
                      0,              // dwCreationFlags
                      nullptr,        // lpEnvironment
                      nullptr,        // lpCurrentDirectory
                      &si, &pi)) {
    msglog->log(LOG_ALWAYS, "Error: CreateProcessA failed %s:%d.", __FUNCTION__,
                __LINE__);
    return NaviErrors::GENERIC_ERROR;
  }

  client_process_id_ = pi.dwProcessId;
  client_process_ = pi.hProcess;

  // Wait for client to connect back to pipe.
  if (!ConnectNamedPipe(dr_pipe_, nullptr /* lpOverlapped */)) {
    return false;
  }

  if (pingDynamoDll()) {
    printf("DynamoRIO client sucessfully pinged!\n");
  } else {
    return NaviErrors::CONNECTION_CLOSED;
  }

  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::detach() {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::terminateProcess() {
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  printf("Terminating process\n");
  cmd.set_command(security::drdebug::Command_CommandType_TERMINATE_PROCESS);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_TERMINATE_TARGET_PROCESS;
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::storeOriginalData(const BREAKPOINT& bp) {
  // Empty implementation for compatibility with DebugClient.
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::setBreakpoint(const BREAKPOINT& breakpoint,
                                            bool moreToCome) {
  security::drdebug::Command cmd;
  cmd.set_command(security::drdebug::Command_CommandType_ADD_BREAKPOINT);
  auto bp_args = cmd.mutable_add_breakpoint_args();
  bp_args->set_address(breakpoint.addr);
  // Use bpx_type as bp id to make echo_bp/simple_bp/stepping_bp look
  // different.
  bp_args->set_id(breakpoint.bpx_type);
  // Don't pause on echo breakpoints since we only want to record the event
  // and let the process continue.
  if (breakpoint.bpx_type == BPX_echo) {
    bp_args->set_auto_resume(true);
    bp_args->set_send_registers(true);
  }
  delayed_add_bp_commands_.push_back(cmd);
  if (!moreToCome) {
    std::vector<security::drdebug::Response> responses;
    bool res = sendCommandsToDr(delayed_add_bp_commands_, &responses);
    delayed_add_bp_commands_.clear();
    if (!res) {
      return NaviErrors::COULDNT_SET_BREAKPOINT;
    }
    for (const auto& resp : responses) {
      if (resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
        return NaviErrors::COULDNT_SET_BREAKPOINT;
      }
    }
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::removeBreakpoint(const BREAKPOINT& breakpoint,
                                               bool moreToCome) {
  printf("BP removed from %08x (id: %d)\n", static_cast<int>(breakpoint.addr),
         static_cast<int>(breakpoint.bpx_type));

  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_REMOVE_BREAKPOINT);
  auto bp_args = cmd.mutable_remove_breakpoint_args();
  bp_args->set_address(breakpoint.addr);
  bp_args->set_id(breakpoint.bpx_type);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_REMOVE_BREAKPOINT;
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::doSingleStep(unsigned int& tid,
                                           CPUADDRESS& address) {
  puts(__FUNCTION__ " is not implemented!");
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::resumeProcess() {
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_RESUME_FROM_BP);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_RESUME_THREAD;
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::suspendThread(unsigned int tid) {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::resumeThread(unsigned int tid) {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::halt() {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::getInstructionPointer(unsigned int tid,
                                                    CPUADDRESS& addr) {
  // Reading IP is not yet implemented, but DebugClient's forces us to implement
  // this method.
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::setInstructionPointer(unsigned int tid,
                                                    CPUADDRESS address) {
  // We can't easily modify instruction pointer, but DebugClient's forces us to
  // implement this method.
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::readRegisters(RegisterContainer& registers) {
  unsigned int threadId = getActiveThread();
  Thread thread = Thread(threadId, RUNNING);

  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_READ_REGISTERS);
  for (const auto& regDescr : register_names_)
    cmd.mutable_read_register_args()->add_name(regDescr.name);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_READ_REGISTERS;
  }

  for (const auto& regValue : resp.read_register_result().register_()) {
    thread.registers.push_back(makeRegisterValue(
        regValue.name(), zylib::zycon::toHexString(regValue.value()),
        zylib::zycon::toLower(regValue.name()) == "eip",
        zylib::zycon::toLower(regValue.name()) == "esp"));
  }
  registers.addThread(thread);
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::setRegister(unsigned int tid, unsigned int index,
                                          CPUADDRESS value) {
  if (tid != getActiveThread())
    throw std::runtime_error(
        "Writing to another thread's registers is not implemented!");

  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_WRITE_REGISTERS);
  auto reg = cmd.mutable_write_register_args()->add_register_();
  reg->set_name(register_names_[index].name);
  reg->set_value(value);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_WRITE_REGISTERS;
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                                             CPUADDRESS& to) {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::getMemmap(std::vector<CPUADDRESS>& addresses) {
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_LIST_MEMORY);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_LIST_MEMORY;
  }
  for (const auto& block : resp.list_memory_result().memory_block()) {
    // This format of memory blocks list is really bad, because they're
    // represented as a flat list of addresses which doesn't correspond to the
    // logical structure of "memory regions list"
    // TODO(mkow): change to a list of pairs/structs
    addresses.push_back(block.start());
    addresses.push_back(block.start() + block.size() - 1);
  }
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::readMemoryData(char* buffer, CPUADDRESS address,
                                             CPUADDRESS size) {
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_READ_MEMORY);
  cmd.mutable_read_memory_args()->set_start_address(address);
  cmd.mutable_read_memory_args()->set_size(size);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::COULDNT_READ_MEMORY;
  }
  if (size != resp.read_memory_result().data().size()) {
    printf(
        "Read memory from 0x%08x returned wrong data size! (returned: 0x%08x, "
        "should be: 0x08x)\n",
        address, resp.read_memory_result().data().size(), size);
    return NaviErrors::COULDNT_READ_MEMORY;
  }
  memcpy(buffer, resp.read_memory_result().data().c_str(), size);
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::writeMemory(CPUADDRESS address,
                                          const std::vector<char>& data) {
  throw std::runtime_error("Not implemented");
}

NaviError WinDynamoRioSystem::readProcessList(
    ProcessListContainer& processList) {
  // We don't allow attaching, so we don't return anything in processList
  // argument.
  return NaviErrors::SUCCESS;
}

NaviError WinDynamoRioSystem::getFileSystems(
    std::vector<boost::filesystem::path>& roots) const {
  return windowscommon::getFileSystems(roots);
}

NaviError WinDynamoRioSystem::getSystemRoot(
    boost::filesystem::path& root) const {
  return windowscommon::getSystemRoot(root);
}

DebugExceptionContainer WinDynamoRioSystem::getPlatformExceptions() const {
  return windowscommon::exception_list;
}

NaviError WinDynamoRioSystem::SetExceptionAction(CPUADDRESS exc_code,
                                     DebugExceptionHandlingAction action) {
  BaseSystem::SetExceptionAction(exc_code, action);
  // Inform client about exception action change
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_SET_EXCEPTION_ACTION);
  cmd.mutable_set_exception_action_args()->set_exc_code(exc_code);
  cmd.mutable_set_exception_action_args()->set_action(
      DebugExceptionHandlingActionToExceptionAction(action));
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return NaviErrors::GENERIC_ERROR;
  }
  return NaviErrors::SUCCESS;
}

// private methods and fields

void WinDynamoRioSystem::initializeRegisterList() {
  // TODO(mkow): this should be retrieved from client DLL, but DebugClient is
  // wrongly implemented (first asks for that and then runs the process).
  // name, size in bytes, is_editable
  register_names_.emplace_back("EAX", 4, true);
  register_names_.emplace_back("EBX", 4, true);
  register_names_.emplace_back("ECX", 4, true);
  register_names_.emplace_back("EDX", 4, true);
  register_names_.emplace_back("ESI", 4, true);
  register_names_.emplace_back("EDI", 4, true);
  register_names_.emplace_back("EBP", 4, true);
  register_names_.emplace_back("ESP", 4, true);
  register_names_.emplace_back("EIP", 4, true);
  register_names_.emplace_back("EFLAGS", 4, false);
  register_names_.emplace_back("CF", 0, true);
  register_names_.emplace_back("PF", 0, true);
  register_names_.emplace_back("AF", 0, true);
  register_names_.emplace_back("ZF", 0, true);
  register_names_.emplace_back("SF", 0, true);
  register_names_.emplace_back("OF", 0, true);
}

std::string WinDynamoRioSystem::randomPipeName() {
  int length = 30;
  const char charset[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  // We want a system-wide unique name
  // TODO(mkow): Improve time resolution here
  std::mt19937 gen(std::chrono::system_clock::now().time_since_epoch().count());
  std::uniform_int_distribution<int> distr(0, (sizeof(charset) - 2));
  auto randchar =
      [&gen, &distr, &charset]() -> char { return charset[distr(gen)]; };
  std::string str(length, 'X');
  std::generate_n(str.begin(), length, randchar);
  return "\\\\.\\pipe\\" + str;
}

bool WinDynamoRioSystem::sendCommandToDr(
    const security::drdebug::Command& command,
    security::drdebug::Response* response) {
  std::vector<security::drdebug::Command> commands;
  std::vector<security::drdebug::Response> responses;
  commands.push_back(command);
  if (!sendCommandsToDr(commands, &responses))
    return false;
  *response = responses[0];
  return true;
}

bool WinDynamoRioSystem::sendCommandsToDr(
    const std::vector<security::drdebug::Command>& commands,
    std::vector<security::drdebug::Response>* responses) {
  assert(dr_pipe_ != INVALID_HANDLE_VALUE);
  // Send commands.
  security::drdebug::CommandPacket packet;
  for (const auto& command : commands) {
    *(packet.add_command()) = command;
  }
  int size = packet.ByteSize();
  std::vector<char> out_buf(size);
  DWORD bytes_written, bytes_read;

  if (!WriteFile(dr_pipe_, &size, sizeof(size), &bytes_written,
    nullptr /* lpOverlapped */) ||
    bytes_written != sizeof(size))
    return false;

  packet.SerializeToArray(&(out_buf[0]), size);
  for (int buf_pos = 0; buf_pos < size;) {
    if (!WriteFile(dr_pipe_, &(out_buf[buf_pos]), size - buf_pos,
      &bytes_written, nullptr /* lpOverlapped */))
      return false;
    buf_pos += bytes_written;
  }

  // Wait for and read response.
  if (!ReadFile(dr_pipe_, &size, sizeof(size), &bytes_read,
    nullptr /* lpOverlapped */) ||
    bytes_read != sizeof(size))
    return false;
  std::vector<char> in_buf(size);
  for (int buf_pos = 0; buf_pos < size;) {
    if (!ReadFile(dr_pipe_, &(in_buf[buf_pos]), size - buf_pos, &bytes_read,
      nullptr /* lpOverlapped */))
      return false;
    buf_pos += bytes_read;
  }

  security::drdebug::ResponsePacket resp;
  resp.ParseFromArray(in_buf.data(), in_buf.size());
  // check if we received exactly one response for every command sent
  if (resp.response_size() != commands.size())
    return false;

  for (const auto& resp : resp.response()) {
    responses->push_back(resp);
  }

  return true;
}

bool WinDynamoRioSystem::pingDynamoDll() {
  security::drdebug::Command cmd;
  security::drdebug::Response resp;
  cmd.set_command(security::drdebug::Command_CommandType_PING);
  if (!sendCommandToDr(cmd, &resp) ||
      resp.error_code() != security::drdebug::Response_ErrorCode_SUCCESS) {
    return false;
  }
  return true;
}

security::drdebug::ExceptionAction
WinDynamoRioSystem::DebugExceptionHandlingActionToExceptionAction(
    DebugExceptionHandlingAction action) {
  switch (action) {
    // TODO(mkow): fix naming differences
    case PASS_TO_APP:
      return security::drdebug::ExceptionAction::PASS_TO_APP;
    case HALT:
      return security::drdebug::ExceptionAction::HALT;
    case SKIP_APP_HANDLER:
      return security::drdebug::ExceptionAction::SKIP_APP_HANDLER;
    default:
      throw new std::runtime_error("Invalid enum value!");
  }
}

DebugExceptionHandlingAction
WinDynamoRioSystem::ExceptionActionToDebugExceptionHandlingAction(
    security::drdebug::ExceptionAction action) {
  switch (action) {
    // TODO(mkow): fix naming differences
    case security::drdebug::ExceptionAction::PASS_TO_APP:
      return PASS_TO_APP;
    case security::drdebug::ExceptionAction::HALT:
      return HALT;
    case security::drdebug::ExceptionAction::SKIP_APP_HANDLER:
      return SKIP_APP_HANDLER;
    default:
      throw new std::runtime_error("Invalid enum value!");
  }
}

NaviError WinDynamoRioSystem::echoBreakpointHit(const BREAKPOINT& bp,
                                                unsigned int tid,
                                                bool /* correctPc */,
                                                bool /* doResume */) {
  const bool breakpointExists = hasBreakpoint(bp.addr, BPX_echo);
  if (!breakpointExists) {
    msglog->log(LOG_ALWAYS,
                "Error: Non-existing echo breakpoint at address 0x%08X was hit",
                bp.addr);
    return NaviErrors::NO_BREAKPOINT_AT_ADDRESS;
  }
  RegisterContainer registers;
  Thread thread(tid, RUNNING);
  for (const auto& regValue : registers_from_bphit_event) {
    thread.registers.push_back(makeRegisterValue(
        regValue.name(), zylib::zycon::toHexString(regValue.value()),
        zylib::zycon::toLower(regValue.name()) == "eip",
        zylib::zycon::toLower(regValue.name()) == "esp"));
  }
  registers.addThread(thread);
  DBGEVT dbgevt;
  dbgevt.bp = bp;
  dbgevt.tid = tid;
  dbgevt.type = dbgevt_bpe_hit;
  dbgevt.registerString = createRegisterString(registers);
  addDebugEvent(dbgevt);

  return NaviErrors::SUCCESS;
}
