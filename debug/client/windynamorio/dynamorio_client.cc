//// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

// Implements main DynamoRIO debugger functionality, i.e.:
// - DynamoRIO callbacks
// - Broker <-> client message loop

#include <stddef.h> /* for offsetof */

#include <atomic>
#include <algorithm>
#include <map>
#include <memory>
#include <set>
#include <sstream>
#include <string>
#include <vector>

#include "AbstractCommandReader.h"
#include "BitReference.h"
#include "BreakpointInfo.h"
#include "common.h"
#include "DebugState.h"
#include "drdebug.pb.h"
#include "dr_api.h"
#include "ScopedLocker.h"
#include "WinPipeCommandReader.h"

namespace {

// TODO(mkow): Unglobal this after fixing SuperUglyHack(tm).
WinPipeCommandReader* g_reader;

void* g_wait_for_event_loop_exit_mutex;  // Held by event loop until its end.
void* g_resume_from_bp_mutex;  // Held by event loop, unlocked on "resume from
                               // BP" command.
std::atomic<bool> g_exiting = false;  // Set to true to kill message loop.
std::atomic<bool> g_terminate_on_request =
    false;  // true if we received terminate request from broker.
std::string g_pipe_name;
dr_mcontext_t g_bp_hit_context;
size_t g_pagesize;

// Will be initialized during dr_init since it relies on the DynamoRIO runtime
// being initialized.
std::unique_ptr<DebugState> g_debug_state = nullptr;

// Debug events queue
void* event_queue_rwlock;
std::queue<security::drdebug::DebugEvent> event_queue;

const char* registers_to_send_on_bp[] = {
    "EAX", "EBX",    "ECX", "EDX", "ESI", "EDI", "EBP", "ESP",
    "EIP", "EFLAGS", "CF",  "PF",  "AF",  "ZF",  "SF",  "OF"};

// Forward declarations
void QueueEventLock(security::drdebug::DebugEvent_EventType type, void* info);
dr_emit_flags_t EventBasicBlock(void* drcontext, void* tag,
                                instrlist_t* basic_block, bool for_trace,
                                bool translating);
void EventModuleLoad(void* drcontext, const module_data_t* info, bool loaded);
void EventModuleUnload(void* drcontext, const module_data_t* info);

void __declspec(noreturn) FatalError(std::string fmt, ...) {
  va_list va;
  va_start(va, fmt);
  static char buf[1000];
  dr_vsnprintf(buf, sizeof(buf), fmt.c_str(), va);
  dr_messagebox("Fatal error: %s\n", buf);
  dr_abort();
  va_end(va);
}

// TODO(mkow): We don't know when it actually invalidates it,
//  single stepping may fail because of this.
void InvalidateAddress(app_pc address) {
  dr_delay_flush_region((app_pc)address, 1 /* size */, 0 /* flush id */,
                        nullptr);
}

ErrorCode AddBreakpoint(app_pc address, const BreakpointInfo& breakpoint_info) {
  for (const auto& bp_info : g_debug_state->breakpoints()[address]) {
    if (bp_info->id() == breakpoint_info.id()) {
      return ErrorCode::BREAKPOINT_ALREADY_PRESENT;
    }
  }
  g_debug_state->breakpoint_addresses().insert(address);
  g_debug_state->breakpoints()[address].push_back(
      std::unique_ptr<BreakpointInfo>(new BreakpointInfo(breakpoint_info)));
  InvalidateAddress(address);
  return ErrorCode::SUCCESS;
}

ErrorCode RemoveBreakpoint(app_pc address, breakpoint_id_t id) {
  auto& breakpoints_at_addr = g_debug_state->breakpoints()[address];
  auto it = std::find_if(breakpoints_at_addr.begin(), breakpoints_at_addr.end(),
                         BreakpointInfo::MakeBreakpointIdComparator(id));
  if (it == breakpoints_at_addr.end()) {
    return ErrorCode::NO_SUCH_BREAKPOINT;
  }
  g_debug_state->breakpoint_addresses().erase(address);
  vector_fast_erase(&breakpoints_at_addr, it);
  InvalidateAddress(address);
  return ErrorCode::SUCCESS;
}

// Gets a reference to register in saved context (g_bp_hit_context)
// Letter case in name is ignored
BitReference<reg_t> GetRegister(const std::string& name,
                                dr_mcontext_t& context) {
  std::string lowercase_name;
  lowercase_name.resize(name.size());
  std::transform(name.begin(), name.end(), lowercase_name.begin(), tolower);
  if (lowercase_name == "eax") {
    return BitReference<reg_t>(&context.eax);
  } else if (lowercase_name == "ebx") {
    return BitReference<reg_t>(&context.ebx);
  } else if (lowercase_name == "ecx") {
    return BitReference<reg_t>(&context.ecx);
  } else if (lowercase_name == "edx") {
    return BitReference<reg_t>(&context.edx);
  } else if (lowercase_name == "esi") {
    return BitReference<reg_t>(&context.esi);
  } else if (lowercase_name == "edi") {
    return BitReference<reg_t>(&context.edi);
  } else if (lowercase_name == "ebp") {
    return BitReference<reg_t>(&context.ebp);
  } else if (lowercase_name == "esp") {
    return BitReference<reg_t>(&context.esp);
  } else if (lowercase_name == "eip") {
    return BitReference<reg_t>(
      &(reinterpret_cast<reg_t &>(context.eip)));
  } else if (lowercase_name == "eflags") {
    return BitReference<reg_t>(&context.eflags);
  } else if (lowercase_name == "cf") {
    return BitReference<reg_t>(&context.eflags, 0, 1);
  } else if (lowercase_name == "pf") {
    return BitReference<reg_t>(&context.eflags, 2, 1);
  } else if (lowercase_name == "af") {
    return BitReference<reg_t>(&context.eflags, 4, 1);
  } else if (lowercase_name == "zf") {
    return BitReference<reg_t>(&context.eflags, 6, 1);
  } else if (lowercase_name == "sf") {
    return BitReference<reg_t>(&context.eflags, 7, 1);
  } else if (lowercase_name == "of") {
    return BitReference<reg_t>(&context.eflags, 11, 1);
  } else {
    FatalError("Unknown register used: %s", lowercase_name.c_str());
  }
}

// Reads debugee memory
bool ReadMemory(const void* addr, size_t size, std::string* out_buf) {
  auto buf = std::unique_ptr<char>(new char[size]);
  size_t read;
  if (!buf) {
    FatalError("Out of memory!");
  }
  if (!dr_safe_read(addr, size, buf.get(), &read) || read != size) {
    return false;
  }
  *out_buf = std::string(buf.get(), size);
  return true;
}

bool IsEventQueueEmptyLock() {
  bool res;
  dr_rwlock_read_lock(event_queue_rwlock);
  res = event_queue.size() == 0;
  dr_rwlock_read_unlock(event_queue_rwlock);
  return res;
}

typedef void(*CommandHandler)(const security::drdebug::Command& command,
                              security::drdebug::Response* response);

void HandleGetDebugEvents(const security::drdebug::Command& command,
                          security::drdebug::Response* response) {
  auto* dbg_events = response->mutable_get_debug_events_result();
  dr_rwlock_write_lock(event_queue_rwlock);
  while (event_queue.size() > 0) {
    *(dbg_events->add_debug_event()) = event_queue.front();
    event_queue.pop();
  }
  dr_rwlock_write_unlock(event_queue_rwlock);
}

void HandleResumeFromBP(const security::drdebug::Command& command,
                        security::drdebug::Response* response) {
  if (g_debug_state->state() != DebugState::WAITING) {
    response->set_error_code(
        security::drdebug::Response_ErrorCode_INVALID_OPERATION);
    response->set_error_explanation(
        "Cannot resume, process is not waiting on any breakpoint/exception");
  } else {
    dr_mutex_unlock(g_resume_from_bp_mutex);
  }
}

void HandleSetExceptionAction(const security::drdebug::Command& command,
                              security::drdebug::Response* response) {
  auto exc_code = command.set_exception_action_args().exc_code();
  auto action = command.set_exception_action_args().action();
  g_debug_state->SetExceptionAction(
      static_cast<exception_code_t>(exc_code), action);
}

void HandleAddBreakpoint(const security::drdebug::Command& command,
                         security::drdebug::Response* response) {
  AddBreakpoint((app_pc)command.add_breakpoint_args().address(),
                BreakpointInfo(command.add_breakpoint_args().id(),
                               command.add_breakpoint_args().auto_resume(),
                               command.add_breakpoint_args().send_registers()));
}

void HandleRemoveBreakpoint(const security::drdebug::Command& command,
                            security::drdebug::Response* response) {
  RemoveBreakpoint((app_pc)command.remove_breakpoint_args().address(),
                   command.remove_breakpoint_args().id());
}

void HandleListThreads(const security::drdebug::Command& command,
                       security::drdebug::Response* response) {
  for (auto thread_id : g_debug_state->debuggee_threads()) {
    response->mutable_list_threads_result()->add_thread_id(thread_id);
  }
}

void HandleReadRegisters(const security::drdebug::Command& command,
                         security::drdebug::Response* response) {
  auto* regs = response->mutable_read_register_result();
  for (std::string reg_name : command.read_register_args().name()) {
    security::drdebug::RegValue reg_val;
    reg_val.set_name(reg_name);
    reg_val.set_value(GetRegister(reg_name, g_bp_hit_context).value());
    *(regs->add_register_()) = reg_val;
  }
}

void HandleWriteRegisters(const security::drdebug::Command& command,
                          security::drdebug::Response* response) {
  if (g_debug_state->state() != DebugState::WAITING &&
      g_debug_state->state() != DebugState::HALTED) {
    response->set_error_code(
      security::drdebug::Response_ErrorCode_INVALID_OPERATION);
    response->set_error_explanation(
      "Cannot access registers when process is running!");
  } else {
    for (const auto& regVal : command.write_register_args().register_()) {
      GetRegister(regVal.name(), g_bp_hit_context)
          .set_value(static_cast<reg_t>(regVal.value()));
    }
  }
}

void HandleListMemory(const security::drdebug::Command& command,
                      security::drdebug::Response* response) {
  auto* mem_res = response->mutable_list_memory_result();
  const byte* addr = 0;
  while (true) {
    dr_mem_info_t info;
    dr_query_memory_ex(addr, &info);
    if (info.type == DR_MEMTYPE_ERROR) {
      // Works only for nightly dr version (>= r2850)
      // (bug: https://code.google.com/p/dynamorio/issues/detail?id=1538)
      // TODO(mkow): File another bug with address 0x7FF1000.
      // FatalError("dr_query_memory_ex(%08p, [..]) failed!", addr);
      break;
    }
    if (info.type != DR_MEMTYPE_FREE &&
        info.type != DR_MEMTYPE_RESERVED &&
        info.type != DR_MEMTYPE_ERROR_WINKERNEL &&
        !dr_memory_is_dr_internal(addr) &&
        !dr_memory_is_in_client(addr)) {
      auto* block = mem_res->add_memory_block();
      block->set_start((cpuaddress_t)info.base_pc);
      block->set_size(info.size);
      auto* protection = block->mutable_protection();
      protection->set_readable(info.prot & DR_MEMPROT_READ);
      protection->set_writable(info.prot & DR_MEMPROT_WRITE ||
                               info.prot & DR_MEMPROT_PRETEND_WRITE);
      protection->set_executable(info.prot & DR_MEMPROT_EXEC);
    }
    if (info.type == DR_MEMTYPE_ERROR_WINKERNEL) {
      // TODO(mkow): File bug for not setting info.size on kernel memory
      break;
    } else {
      addr = info.base_pc + info.size;
    }
    // TODO(mkow): After overflow (we assume that page size if a power of
    // two, so it will overflow to zero). Pointer overflow is undefined,
    // we should fix that somehow.
    if (addr == 0) {
      break;
    }
  }
}

void HandleReadMemory(const security::drdebug::Command& command,
                      security::drdebug::Response* response) {
  auto* mem_res = response->mutable_read_memory_result();
  ReadMemory((const void*)command.read_memory_args().start_address(),
             static_cast<size_t>(command.read_memory_args().size()),
             mem_res->mutable_data());
}

void HandleNotImplemented(const security::drdebug::Command& command,
                          security::drdebug::Response* response) {
  FatalError("Command with id = %d not implemented", command.command());
}

// Waits for commands on pipe, reads them, executes proper handler and sends a
// response.
//
// Exits when g_exiting is set and it finished processing all incoming debug
// events. Right before returning it unlocks g_wait_for_event_loop_exit_mutex.
//
// Takes ownership over arg_cmd_reader.
void MessageLoop(void* arg_cmd_reader) {
  // Don't suspend this thread on BPs and other events
  dr_client_thread_set_suspendable(false);

  auto cmd_reader = std::unique_ptr<AbstractCommandReader>(
    reinterpret_cast<AbstractCommandReader*>(arg_cmd_reader));

  while (!g_exiting || !IsEventQueueEmptyLock()) {
    auto command = cmd_reader->WaitForCommand();
    if (command == nullptr) {
      FatalError("WaitForCommand() failed!");
    }

    {
      ScopedLocker state_lock(g_debug_state.get());
      security::drdebug::Response resp;
      resp.set_error_code(
        security::drdebug::Response_ErrorCode::Response_ErrorCode_SUCCESS);
      switch (command->command()) {
        case security::drdebug::Command_CommandType_PING: {
          break;
        }
        case security::drdebug::Command_CommandType_GET_DEBUG_EVENTS: {
          HandleGetDebugEvents(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_RESUME_FROM_BP: {
          HandleResumeFromBP(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_RESUME_FROM_HALT: {
          // TODO(mkow): Implement this.
          HandleNotImplemented(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_TERMINATE_PROCESS: {
          g_terminate_on_request = true;
          if (!cmd_reader->SendResponse(resp)) {
            FatalError("Sending response to TERMINATE_PROCESS failed!");
          }
          // TODO(mkow): We have to resume thread waiting on a BP, because
          // dr_exit_process() can't kill threads which are waiting on a mutex.
          // Currently it's only a hack, because the thread can fall into another
          // breakpoint before we manage to call dr_exit_process().
          // Without this hack process hangs when terminating in WAITING state
          // (wait after trapping on a breakpoint)
          if (g_debug_state->state() == DebugState::WAITING) {
            dr_mutex_unlock(g_resume_from_bp_mutex);
          }

          dr_exit_process(0);  // no return
        }
        case security::drdebug::Command_CommandType_SET_EXCEPTION_ACTION: {
          HandleSetExceptionAction(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_ADD_BREAKPOINT: {
          HandleAddBreakpoint(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_REMOVE_BREAKPOINT: {
          HandleRemoveBreakpoint(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_LIST_THREADS: {
          HandleListThreads(*command, &resp);
          break;
        }
        // Suspend and resume are stacked:
        // Double suspend needs double resume to really resume a thread
        // but of course you can't resume running thread.
        case security::drdebug::Command_CommandType_SUSPEND_THREAD: {
          // TODO(mkow): Implement this.
          HandleNotImplemented(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_RESUME_THREAD: {
          // TODO(mkow): Implement this.
          HandleNotImplemented(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_LIST_REGISTERS: {
          // TODO(mkow): Implement this.
          HandleNotImplemented(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_READ_REGISTERS: {
          HandleReadRegisters(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_WRITE_REGISTERS: {
          HandleWriteRegisters(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_LIST_MEMORY: {
          HandleListMemory(*command, &resp);
          break;
        }
        case security::drdebug::Command_CommandType_READ_MEMORY: {
          HandleReadMemory(*command, &resp);
          break;
        }
        default:
          HandleNotImplemented(*command, &resp);
      }
      if (!cmd_reader->SendResponse(resp)) {
        FatalError("Sending response failed!");
      }

    }
    // TODO(mkow): Change to auto wake-up.
    // Prevents high cpu usage when idling.
    //dr_sleep(10);
  }
  dr_mutex_unlock(g_wait_for_event_loop_exit_mutex);
}

/*******************************************************************************
 * DynamoRIO callbacks and things that run from non-client threads
 ******************************************************************************/

// Turns all threads (excl message loop) into sleep and waits for resume message
// from user.
void WaitForResume() {
  void** contexts;
  uint num_suspended, num_unsuspended;
  if (!dr_suspend_all_other_threads(&contexts, &num_suspended,
    &num_unsuspended)) {
    FatalError("Failed to suspend other threads on BP hit!");
  }
  // No race-condition here, because other threads should be already suspended.
  g_debug_state->set_state(DebugState::WAITING);
  dr_mutex_lock(g_resume_from_bp_mutex);
  g_debug_state->set_state(DebugState::RUNNING);
  if (!dr_resume_all_other_threads(contexts, num_suspended)) {
    FatalError("Failed to resume other threads after resume from BP!");
  }
  // TODO(mkow): Free contents of contexts
}

// Push event of certain type to event_queue. Uses event_queue_rwlock mutex.
//
// This function simplifies pushing new events by selecting proper
// set_allocated_* method and setting proper field in dbg_event.
//
// Takes ownership over info.
void QueueEventLock(security::drdebug::DebugEvent_EventType type, void* info) {
  bool succeded = false;
  while (!succeded) {
    dr_rwlock_write_lock(event_queue_rwlock);
    // Prevent high memory consuption when tracing
    if (event_queue.size() < 10000) {

      security::drdebug::DebugEvent dbg_event;
      dbg_event.set_type(type);

      switch (type) {
        case security::drdebug::DebugEvent_EventType_BREAKPOINT_HIT:
          dbg_event.set_allocated_breakpoint_hit_info(
            (security::drdebug::BreakpointHitInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_THREAD_CREATED:
          dbg_event.set_allocated_thread_created_info(
            (security::drdebug::ThreadCreatedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_THREAD_EXITED:
          dbg_event.set_allocated_thread_exited_info(
            (security::drdebug::ThreadExitedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_MODULE_LOADED:
          dbg_event.set_allocated_module_loaded_info(
            (security::drdebug::ModuleLoadedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_MODULE_UNLOADED:
          dbg_event.set_allocated_module_unloaded_info(
            (security::drdebug::ModuleUnloadedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_PROCESS_STARTED:
          dbg_event.set_allocated_process_started_info(
            (security::drdebug::ProcessStartedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_PROCESS_EXITED:
          dbg_event.set_allocated_process_exited_info(
            (security::drdebug::ProcessExitedInfo*)info);
          break;
        case security::drdebug::DebugEvent_EventType_EXCEPTION_THROWN:
          dbg_event.set_allocated_exception_thrown_info(
            (security::drdebug::ExceptionThrownInfo*)info);
          break;
        default:
          FatalError("Unknown event type passed to queue!");
      }
      event_queue.push(dbg_event);
      succeded = true;
    }
    dr_rwlock_write_unlock(event_queue_rwlock);
  }
}

// Called on every breakpoint hit.
void BreakpointHitCallback(app_pc addr, BreakpointInfo* bp_info) {
  dr_mcontext_t context;
  if (!bp_info->auto_resume()) {
    // TODO(mkow): We should suspend other threads here (not later in
    // WaitForResume()) otherwise we have a race condition when two threads
    // reach this code at the same time.
    // Save context to a global variable.
    g_bp_hit_context.size = sizeof(g_bp_hit_context);
    g_bp_hit_context.flags = dr_mcontext_flags_t(DR_MC_INTEGER | DR_MC_CONTROL);
    if (!dr_get_mcontext(dr_get_current_drcontext(), &g_bp_hit_context)) {
      FatalError("Couldn't read thread context!");
    }
    g_bp_hit_context.eip = addr;
  } else {
    // Save context to a local variable.
    context.size = sizeof(context);
    context.flags = dr_mcontext_flags_t(DR_MC_INTEGER | DR_MC_CONTROL);
    if (!dr_get_mcontext(dr_get_current_drcontext(), &context)) {
      FatalError("Couldn't read thread context!");
    }
    context.eip = addr;
  }

  auto* info = new security::drdebug::BreakpointHitInfo();
  info->set_address((google::protobuf::uint64)addr);
  info->set_bp_id(bp_info->id());
  info->set_thread_id(dr_get_thread_id(dr_get_current_drcontext()));
  if (bp_info->send_registers()) {
    for (const char* reg_name : registers_to_send_on_bp) {
      security::drdebug::RegValue reg_val;
      reg_val.set_name(reg_name);
      reg_val.set_value(GetRegister(reg_name, bp_info->auto_resume()
                                                  ? context
                                                  : g_bp_hit_context).value());
      *(info->add_register_()) = reg_val;
    }
  }

  QueueEventLock(security::drdebug::DebugEvent_EventType_BREAKPOINT_HIT, info);

  if (!bp_info->auto_resume()) {
    WaitForResume();
    // Load context which could have been changed by broker.
    dr_set_mcontext(dr_get_current_drcontext(), &g_bp_hit_context);
  }
}

void EventExit() {
  {
    ScopedLocker state_lock(g_debug_state.get());
    g_debug_state->set_state(DebugState::EXITING);
  }
  // Broker doesn't listen after sending "terminate" to us.
  if (!g_terminate_on_request) {
    auto* info = new security::drdebug::ProcessExitedInfo();
    // We can't get exit code easily using current dynamorio api.
    // Related issue: https://code.google.com/p/dynamorio/issues/detail?id=1260
    // info->set_exit_code(0);
    QueueEventLock(security::drdebug::DebugEvent_EventType_PROCESS_EXITED,
                   info);
    // We rely on memory barrier created before every access by std::atomic to
    // ensure that the event was sent.
    g_exiting = true;

    // <SuperUglyHack>
    // We have to start message loop again, because DynamoRIO kills every thread
    // (including message loop) on exit, and only after then it call exit
    // callback. By now, there's no option to tell dr to not kill specific
    // thread on exit request.
    //
    // We assume here that the loop can be called again after it was killed.
    // That assumption has many problems:
    // - If message loop was killed right after reading only a half of message
    // bytes from a pipe, this call will start with reading the message from the
    // middle.
    // - Message loop was forcibly terminated. State of all global structures is
    // unpredictable, e.g. some mutexes can be still locked and following call
    // will hang.
    //
    // We can't just skip this loop, because we have to wait for broker to send
    // us GET_DEBUG_EVENTS message and reply with PROCESS_EXITED message. That's
    // the only way to distinguish e.g. a crash in client code from normal exit.

    MessageLoop(g_reader);
    // </SuperUglyHack>

    // Wait for event loop to finish.
    dr_mutex_lock(g_wait_for_event_loop_exit_mutex);
  }
  g_debug_state.release();
}

dr_emit_flags_t EventBasicBlock(void* drcontext, void* tag,
                                instrlist_t* basic_block, bool for_trace,
                                bool translating) {
  instr_t* first = instrlist_first(basic_block);
  instr_t* last = instrlist_last(basic_block);
  app_pc block_start = instr_get_app_pc(first);
  app_pc block_end = instr_get_app_pc(last);  // inclusive

  // Check if there's any breakpoint in this basic block.
  // TODO(mkow): [Performace improvement] Don't lock if not necesary (no bps
  // here).
  {
    ScopedLocker state_lock(g_debug_state.get());
    auto bp_it_begin =
      g_debug_state->breakpoint_addresses().lower_bound(block_start);
    auto bp_it_end =
      g_debug_state->breakpoint_addresses().upper_bound(block_end);  // exclusive

    if (bp_it_begin != bp_it_end) {
      // Insert calls to BP hit handler.
      instr_t* next_instr;
      auto next_bp = bp_it_begin;

      for (instr_t* instr = first; instr != NULL && next_bp != bp_it_end;
           instr = next_instr) {
        next_instr = instr_get_next(instr);
        // Place all breakpoints that points to the current instruction.
        while (next_bp != bp_it_end && instr_get_app_pc(instr) <= *next_bp &&
               (next_instr == NULL || *next_bp < instr_get_app_pc(next_instr))) {
          // For every breakpoint at this address (*next_bp) add separate clean
          // call with a pointer to BP info.
          // This way BP hit handler doesn't need to lock global bp list and
          // doesn't have to use slow breakpoint map.
          for (std::unique_ptr<BreakpointInfo>& bp_info :
               g_debug_state->breakpoints()[*next_bp]) {
            dr_insert_clean_call(
              drcontext, basic_block, instr,
              reinterpret_cast<void*>(BreakpointHitCallback),
              false /*no fp save*/, 2 /*num args*/,
              OPND_CREATE_INTPTR(*next_bp),
              OPND_CREATE_INTPTR(bp_info.get()));
          }
          next_bp++;
        }
      }

      // Uncomment for additional debug, will be removed soon
      // file_t file = dr_open_file("C:\\Users\\mkow\\dynamo_log.txt",
      // DR_FILE_WRITE_APPEND);
      // instrlist_disassemble(drcontext, (app_pc)tag, basic_block, file);
      // dr_close_file(file);
      // dr_messagebox("Dump saved!");
    }
  }
  return DR_EMIT_DEFAULT;
}

void EventModuleLoad(void* drcontext, const module_data_t* info,
                     bool /* loaded */) {
  auto* module_info = new security::drdebug::ModuleLoadedInfo();
  module_info->set_address((google::protobuf::uint64)(info->start));
  module_info->set_size(info->module_internal_size);
  module_info->set_name(info->names.file_name);
  module_info->set_path(info->full_path);
  module_info->set_thread(dr_get_thread_id(drcontext));
  QueueEventLock(security::drdebug::DebugEvent_EventType_MODULE_LOADED,
                 module_info);
}

void EventModuleUnload(void* drcontext, const module_data_t* info) {
  auto* module_info = new security::drdebug::ModuleUnloadedInfo();
  module_info->set_address((google::protobuf::uint64)(info->start));
  QueueEventLock(security::drdebug::DebugEvent_EventType_MODULE_UNLOADED,
                 module_info);
}

bool EventExceptionThrown(void* drcontext, dr_exception_t* exception) {
  // Save context to global variable.
  memcpy(&g_bp_hit_context, exception->mcontext, sizeof(g_bp_hit_context));

  auto* info = new security::drdebug::ExceptionThrownInfo();
  auto code = exception->record->ExceptionCode;
  info->set_thread_id(dr_get_thread_id(drcontext));
  info->set_exc_code(code);
  info->set_address(
      (google::protobuf::uint64)exception->record->ExceptionAddress);
  QueueEventLock(security::drdebug::DebugEvent_EventType_EXCEPTION_THROWN,
                 info);

  security::drdebug::ExceptionAction action;
  {
    ScopedLocker state_lock(g_debug_state.get());
    action = g_debug_state->GetExceptionAction(code);
  }

  switch (action) {
    case security::drdebug::ExceptionAction::HALT:
      WaitForResume();
      // Load context which could have been changed by broker.
      memcpy(exception->mcontext, &g_bp_hit_context,
             sizeof(exception->mcontext));
      // true - pass exception to application with modified mcontext.
      return true;
    case security::drdebug::ExceptionAction::PASS_TO_APP:
      return true;
    case security::drdebug::ExceptionAction::SKIP_APP_HANDLER:
      // Silence exception and continue execution.
      return false;
    default:
      FatalError("Unknown exception action (%d)!", static_cast<int>(action));
  }
}

void EventThreadInit(void* drcontext) {
  static bool first_thread_started = false;
  if (!first_thread_started) {
    // Broker expects us to not send this event for the first thread.
    first_thread_started = true;
    return;
  }

  {
    ScopedLocker state_lock(g_debug_state.get());
    g_debug_state->debuggee_threads().emplace_back(dr_get_thread_id(drcontext));
  }

  auto* info = new security::drdebug::ThreadCreatedInfo();
  info->set_thread_id(dr_get_thread_id(drcontext));
  QueueEventLock(security::drdebug::DebugEvent_EventType_THREAD_CREATED, info);
}

void EventThreadExit(void* drcontext) {
  {
    ScopedLocker state_lock(g_debug_state.get());

    auto it = std::find(g_debug_state->debuggee_threads().begin(),
                        g_debug_state->debuggee_threads().end(),
                        dr_get_thread_id(drcontext));
    if (it == g_debug_state->debuggee_threads().end()) {
      dr_messagebox("Warning: Received exit thread event from unknown thread");
      return;
    }
    vector_fast_erase(&(g_debug_state->debuggee_threads()), it);
  }

  auto* info = new security::drdebug::ThreadExitedInfo();
  info->set_thread_id(dr_get_thread_id(drcontext));
  // We can't fill exit code, it's not yet implemented in DynamoRIO.
  QueueEventLock(security::drdebug::DebugEvent_EventType_THREAD_EXITED, info);
}

} // namespace

// Entry point (called by DynamoRIO right after loading client)
//
// - Initializes globals
// - Parses command line arguments
// - Registers all DynamoRIO event handlers
// - Sends PROCESS_STARTED message to broker
// - Starts message loop in a separate thread
DR_EXPORT void dr_init(client_id_t id) {
  g_debug_state = std::unique_ptr<DebugState>(new DebugState());
  g_wait_for_event_loop_exit_mutex = dr_mutex_create();
  g_resume_from_bp_mutex = dr_mutex_create();
  event_queue_rwlock = dr_rwlock_create();
  SYSTEM_INFO system_info;
  GetSystemInfo(&system_info);
  g_pagesize = system_info.dwPageSize;
  g_debug_state->set_state(DebugState::RUNNING);

  // Cmd line parsing
  std::stringstream args = std::stringstream(std::string(dr_get_options(id)));
  args >> g_pipe_name;
  g_reader = new WinPipeCommandReader();
  if (!g_reader->Connect(g_pipe_name)) {
    FatalError("Connecting failed!\n");
  }

  dr_register_exit_event(EventExit);
  dr_register_bb_event(EventBasicBlock);
  dr_register_module_load_event(EventModuleLoad);
  dr_register_module_unload_event(EventModuleUnload);
  dr_register_exception_event(EventExceptionThrown);
  dr_register_thread_init_event(EventThreadInit);
  dr_register_thread_exit_event(EventThreadExit);

  // Inform broker that the process was created.
  auto* startup_info = new security::drdebug::ProcessStartedInfo();
  _module_data_t* main_module = dr_get_main_module();
  startup_info->set_base_address((google::protobuf::uint64)main_module->start);
  startup_info->set_image_size(main_module->module_internal_size);
  startup_info->set_thread_id(
      (google::protobuf::uint64)dr_get_thread_id(dr_get_current_drcontext()));
  QueueEventLock(security::drdebug::DebugEvent_EventType_PROCESS_STARTED,
                 startup_info);
  dr_free_module_data(main_module);

  // Lock mutexes for event loop, it takes ownership over them.
  dr_mutex_lock(g_wait_for_event_loop_exit_mutex);
  dr_mutex_lock(g_resume_from_bp_mutex);
  if (!dr_create_client_thread(MessageLoop, g_reader)) {
    FatalError("Couldn't create message loop thread!");
  }

  // Let's say that's some kind of initial bp...
  g_debug_state->set_state(DebugState::WAITING);
  dr_mutex_lock(g_resume_from_bp_mutex);
  g_debug_state->set_state(DebugState::RUNNING);
}
