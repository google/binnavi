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

#include "WindowsSystem.hpp"

#include <algorithm>
#include <iostream>
#include <tlhelp32.h>

#include <zycon/src/zycon.h>
#include <zywin/src/toolhelp.h>

#include "../errors.hpp"
#include "../logger.hpp"
#include "../DebuggerOptions.hpp"
#include "NTHeader.h"

#include "../windowscommon/WindowsCommon.hpp"

const unsigned int PROTECTION_CHANGE_FAILED = 1;
const unsigned int PROTECTION_CHANGED = 2;
const unsigned int PROTECTION_CHANGE_UNNECESSARY = 3;
const unsigned int PROTECTION_CHANGE_IMPOSSIBLE = 4;

void printLastError() {
  unsigned int err = GetLastError();
  LPVOID message;
  FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
                nullptr, err, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                (LPTSTR)&message, 0, nullptr);
  msglog->log(LOG_ALWAYS, "Last error: %s (Error Code: %d)", message, err);
  LocalFree(message);
}

void dbg_echo_MEMORY_BASIC_INFORMATION(MEMORY_BASIC_INFORMATION *m) {
  msglog->log(LOG_VERBOSE, "Base:              %lx", m->BaseAddress);
  msglog->log(LOG_VERBOSE, "AllocationBase:    %lx", m->AllocationBase);
  msglog->log(LOG_VERBOSE, "AllocationProtect: %lx", m->AllocationProtect);
  msglog->log(LOG_VERBOSE, "RegionSize:        %lx", m->RegionSize);
  msglog->log(LOG_VERBOSE, "state:             %lx", m->State);
  msglog->log(LOG_VERBOSE, "Protect:           %lx", m->Protect);
  msglog->log(LOG_VERBOSE, "Type:              %lx", m->Type);
}

NaviError WindowsSystem::fillModules(HANDLE process_handle,
                                     std::vector<Module> &modules) const {
  std::vector<std::pair<HMODULE, std::string>> internalModules;
  if (!listProcessModules(process_handle, internalModules)) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Listing modules of the target process failed - aborting.");
    return NaviErrors::COULDNT_LIST_PROCESSES;
  }
  msglog->log(LOG_VERBOSE, "Found %d modules in the process",
              internalModules.size());
  for (const auto& module : internalModules) {
    HMODULE module_handle = module.first;
    const std::string& path = module.second;
    std::string name =
        boost::filesystem::path(module.second).filename().string();
    MODULEINFO moduleInfo;
    CPUADDRESS size = 0;
    CPUADDRESS base = (CPUADDRESS)module_handle;
    if (GetModuleInformation(process_handle, module_handle, &moduleInfo,
                             sizeof(MODULEINFO))) {
      base = (CPUADDRESS)moduleInfo.lpBaseOfDll;
      size = moduleInfo.SizeOfImage;
    } else {
      msglog->log(LOG_VERBOSE, "Could not determine module size of module %s",
                  name.c_str());
    }
    Module m(name, path, base, size);
    msglog->log(LOG_VERBOSE, "Found module %s loaded from %s (%X : %X)",
                name.c_str(), path.c_str(), module_handle, size);
    modules.push_back(m);
  }
  return NaviErrors::SUCCESS;
}

// Checks the Windows operating system version number.
BOOL isWindowsXPOrLater() {
  OSVERSIONINFOEX osvi;
  DWORDLONG dwlConditionMask = 0;
  BYTE op = VER_GREATER_EQUAL;
  ZeroMemory(&osvi, sizeof(OSVERSIONINFOEX));
  osvi.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
  osvi.dwMajorVersion = 5;
  osvi.dwMinorVersion = 1;
  osvi.wServicePackMajor = 0;
  osvi.wServicePackMinor = 0;
  VER_SET_CONDITION(dwlConditionMask, VER_MAJORVERSION, op);
  VER_SET_CONDITION(dwlConditionMask, VER_MINORVERSION, op);
  VER_SET_CONDITION(dwlConditionMask, VER_SERVICEPACKMAJOR, op);
  VER_SET_CONDITION(dwlConditionMask, VER_SERVICEPACKMINOR, op);
  return VerifyVersionInfo(&osvi,
                           VER_MAJORVERSION | VER_MINORVERSION |
                               VER_SERVICEPACKMAJOR | VER_SERVICEPACKMINOR,
                           dwlConditionMask);
}

// Makes a page of the target process memory writable. Returns the old
// protection as an out argument.
unsigned int makePageWritable(HANDLE hProcess, CPUADDRESS offset,
                              DWORD &oldProtection, bool silent) {
  MEMORY_BASIC_INFORMATION mem;
  memset(&mem, 0, sizeof(mem));
  int returned = VirtualQueryEx(hProcess, (void *)offset, &mem, sizeof(mem));
  if (returned != sizeof(mem)) {
    if (!silent) {
      msglog->log(LOG_ALWAYS, "Error: Invalid return value of VirtualQueryEx");
    }
    return PROTECTION_CHANGE_FAILED;
  }
  if ((!(mem.Protect & PAGE_READWRITE)) || (mem.Protect & PAGE_GUARD)) {
    if (!VirtualProtectEx(hProcess, (void *)offset, 4, PAGE_EXECUTE_READWRITE,
                          &oldProtection)) {
      msglog->log(LOG_VERBOSE,
                  "%s: VirtualProtectEx on offset %x failed (%d) !",
                  __FUNCTION__, offset, GetLastError());
      return PROTECTION_CHANGE_FAILED;
    } else {
      return PROTECTION_CHANGED;
    }
  }
  return PROTECTION_CHANGE_UNNECESSARY;
}

bool isReadable(DWORD protect) {
  DWORD lower = protect & 0xFF;
  return (lower & PAGE_EXECUTE_READ) || (lower & PAGE_EXECUTE_READWRITE) ||
         (lower & PAGE_READONLY) || (lower & PAGE_READWRITE);
}

DWORD getReadableProtection(DWORD protect) {
  switch (protect & 0xFF) {
  case PAGE_EXECUTE:
    return PAGE_EXECUTE_READ;
  case PAGE_NOACCESS:
    return PAGE_READONLY;
  case PAGE_WRITECOPY:
    return PAGE_READWRITE;
  case PAGE_EXECUTE_WRITECOPY:
    return PAGE_EXECUTE_READWRITE;
  }
  return protect & 0xFF;
}

// Makes a single page readable. Returns the old protection as out argument.
unsigned int makePageReadable(HANDLE hProcess, CPUADDRESS offset,
                              DWORD &oldProtection, bool silent) {
  MEMORY_BASIC_INFORMATION mem;
  memset(&mem, 0, sizeof(mem));
  int returned = VirtualQueryEx(hProcess, (void *)offset, &mem, sizeof(mem));
  if (returned != sizeof(mem)) {
    if (!silent) {
      msglog->log(LOG_ALWAYS, "Error: Invalid return value of VirtualQueryEx");
    }
    return PROTECTION_CHANGE_FAILED;
  }
  if (!mem.Protect) {
    return PROTECTION_CHANGE_IMPOSSIBLE;
  }
  if (!isReadable(mem.Protect) || (mem.Protect & PAGE_GUARD)) {
    if (!VirtualProtectEx(hProcess, (void *)offset, 4,
                          getReadableProtection(mem.Protect), &oldProtection)) {
      msglog->log(LOG_ALL, "%s: VirtualProtectEx on offset %x failed (%d) !",
                  __FUNCTION__, offset, GetLastError());
      return PROTECTION_CHANGE_FAILED;
    } else {
      return PROTECTION_CHANGED;
    }
  }
  return PROTECTION_CHANGE_UNNECESSARY;
}

// Restores the old protection level of a page of the target process memory.
NaviError restoreMemoryProtection(HANDLE hProcess, CPUADDRESS offset,
                                  DWORD oldProtection) {
  DWORD oldProtection2 = 0;
  if (!VirtualProtectEx(hProcess, (void *)offset, 4, oldProtection,
                        &oldProtection2)) {
    msglog->log(LOG_VERBOSE,
                "%s: Resetting VirtualProtectEx state failed (%d) !",
                __FUNCTION__, GetLastError());
    return false;
  }
  return true;
}

// Reads a single byte from the process memory of the target process.
NaviError readByte(HANDLE hProcess, CPUADDRESS offset, char &b) {
  SIZE_T writtenbytes = 0;
  return ReadProcessMemory(hProcess, (void *)offset, &b, 1, &writtenbytes)
             ? NaviErrors::SUCCESS
             : NaviErrors::COULDNT_READ_MEMORY;
}

// Writes a single byte to the process memory of the target process.
NaviError writeByte(HANDLE hProcess, CPUADDRESS offset, char b) {
  DWORD oldProtection = 0;
  unsigned int result =
      makePageWritable(hProcess, offset, oldProtection, false);
  if (result == PROTECTION_CHANGE_FAILED) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't make page read/writable");
    return NaviErrors::PAGE_NOT_WRITABLE;
  }
  SIZE_T writtenbytes = 0;
  if (!WriteProcessMemory(hProcess, (void *)offset, &b, 1, &writtenbytes)) {
    msglog->log(LOG_VERBOSE, "WriteProcessMemory failed in %s ...",
                __FUNCTION__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }
  if (result == PROTECTION_CHANGED) {
    restoreMemoryProtection(hProcess, offset, oldProtection);
  }
  return NaviErrors::SUCCESS;
}

// Writes multiple bytes to the process memory of the target process
NaviError writeBytes(HANDLE hProcess, CPUADDRESS offset,
                     std::vector<char> data) {
  DWORD oldProtection = 0;
  unsigned int result =
      makePageWritable(hProcess, offset, oldProtection, false);
  if (result == PROTECTION_CHANGE_FAILED) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't make page read/writable");
    return NaviErrors::PAGE_NOT_WRITABLE;
  }
  SIZE_T writtenbytes = 0;
  if (!WriteProcessMemory(hProcess, (void *)offset, &data[0], data.size(),
                          &writtenbytes)) {
    msglog->log(LOG_VERBOSE, "WriteProcessMemory failed in %s ...",
                __FUNCTION__);
    return NaviErrors::COULDNT_WRITE_MEMORY;
  }
  if (result == PROTECTION_CHANGED) {
    restoreMemoryProtection(hProcess, offset, oldProtection);
  }
  return NaviErrors::SUCCESS;
}

// Changes the step mode of the given thread in the debugged process.
NaviError changeStepMode(unsigned int threadId, bool enterSingleStep) {
  CONTEXT threadContext;
  HANDLE thread = OpenThread(THREAD_ALL_ACCESS, FALSE, threadId);
  if (!thread) {
    msglog->log(LOG_ALWAYS,
                "Error: Opening the thread handle of thread %d failed.",
                threadId);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  threadContext.ContextFlags = CONTEXT_FULL;
  if (GetThreadContext(thread, &threadContext) == FALSE) {
    msglog->log(LOG_ALWAYS, "Error: GetThreadContext failed %s:%d.",
                __FUNCTION__, __LINE__);
    printLastError();
    CloseHandle(thread);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  const unsigned int TRAP_FLAG = 0x100;
  if (enterSingleStep) {
    threadContext.EFlags |= TRAP_FLAG;
  } else {
    threadContext.EFlags &= ~TRAP_FLAG;
  }
  NaviError result = SetThreadContext(thread, &threadContext)
                         ? NaviErrors::SUCCESS
                         : NaviErrors::COULDNT_WRITE_REGISTERS;
  CloseHandle(thread);
  return result;
}

bool SetDebugPrivilege(HANDLE hProcess) {
  LUID luid;
  TOKEN_PRIVILEGES privs;
  HANDLE hToken = nullptr;
  DWORD dwBufLen = 0;
  char buf[1024] = {0};
  ZeroMemory(&luid, sizeof(luid));
  msglog->log(LOG_VERBOSE, "Looking up privilege value");
  if (!LookupPrivilegeValue(nullptr, SE_DEBUG_NAME, &luid)) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't look up privilege value");
    return false;
  }
  privs.PrivilegeCount = 1;
  privs.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
  memcpy(&privs.Privileges[0].Luid, &luid, sizeof(privs.Privileges[0].Luid));
  msglog->log(LOG_VERBOSE, "Opening process token");
  if (!OpenProcessToken(hProcess, TOKEN_ALL_ACCESS, &hToken)) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't open process token");
    return false;
  }
  msglog->log(LOG_VERBOSE, "Adjusting token privilege");
  if (!AdjustTokenPrivileges(hToken, FALSE, &privs, sizeof(buf),
                             (PTOKEN_PRIVILEGES)buf, &dwBufLen)) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't adjust token privileges");
    return false;
  }
  msglog->log(LOG_VERBOSE,
              "Successfully set debug privilege on target process");
  return true;
}

bool WindowsSystem::Win32_resume(unsigned int tid, DWORD mode) {
  msglog->log(LOG_VERBOSE,
              "Resuming thread 0x%X in process 0x%X with mode 0x%X", tid,
              getPID(), mode);
  if (!ContinueDebugEvent(getPID(), tid, mode)) {
    LPVOID message;
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM,
                  nullptr, GetLastError(),
                  MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPTSTR)&message,
                  0, nullptr);
    msglog->log(LOG_ALWAYS,
                "Error: ContinueDebugEvent() failed with error: %s\n", message);
    LocalFree(message);
    return false;
  }
  return true;
}

// Update internal list of threads and generate debug event accordingly.
NaviError WindowsSystem::addThread(const Thread &thread) {
  tids.push_back(thread);
  return threadCreated(thread.tid, RUNNING);
}

void WindowsSystem::handleCreateThread(const DEBUG_EVENT &dbg_evt) {
  msglog->log(LOG_VERBOSE, "Created new thread with TID %d",
              dbg_evt.dwThreadId);
  NaviError ctResult = addThread(Thread(dbg_evt.dwThreadId, RUNNING));
  if (ctResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't handle thread created event");
  }
  Win32_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);
}

void WindowsSystem::handleExitThread(const DEBUG_EVENT &dbg_evt) {
  tids.erase(std::remove_if(tids.begin(), tids.end(),
                            Thread::MakeThreadIdComparator(dbg_evt.dwThreadId)),
             tids.end());
  NaviError ctResult = threadExit(dbg_evt.dwThreadId);
  if (ctResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't handle thread exit event");
  }
  Win32_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);
}

void WindowsSystem::handleProcessStart(const DEBUG_EVENT &dbg_evt) {
  Module module = getProcessModule(&dbg_evt.u.CreateProcessInfo);
  Thread thread = Thread(dbg_evt.dwThreadId, SUSPENDED);
  msglog->log(
      LOG_VERBOSE,
      "Created process at image base address 0x%X; initial thread has id 0x%X",
      module.baseAddress, thread.tid);
  processStart(module, thread);
  moduleMap.insert(std::make_pair(module.baseAddress, module));
  tids.push_back(thread);
}

void WindowsSystem::handleLoadDll(const DEBUG_EVENT &dbg_evt) {
  HANDLE handle = dbg_evt.u.LoadDll.hFile;
  std::string filename;
  if (GetFileNameFromHandle(handle, filename)) {
    DWORD high;
    DWORD low = GetFileSize(handle, &high);
    CPUADDRESS size = (high << 8) + low;
    size_t lastSlash = filename.find_last_of('\\');
    Module module(lastSlash == std::string::npos
                      ? filename
                      : filename.substr(lastSlash + 1),
                  filename, (CPUADDRESS)dbg_evt.u.LoadDll.lpBaseOfDll, size);
    addModule(module, dbg_evt.dwThreadId);
  } else {
    msglog->log(LOG_ALWAYS, "Error: Could not determine name of DLL file");
  }
  msglog->log(LOG_VERBOSE, "Breaking on dll load (%s) in thread 0x%X",
              filename.c_str(), dbg_evt.dwThreadId);
}

void WindowsSystem::handleUnloadDll(const DEBUG_EVENT &dbg_evt) {
  auto unloaded_module =
      moduleMap.find((CPUADDRESS)dbg_evt.u.UnloadDll.lpBaseOfDll);
  if (unloaded_module == moduleMap.end()) {
    moduleUnloaded(
        Module("", "", (CPUADDRESS)dbg_evt.u.UnloadDll.lpBaseOfDll, 0));
  } else {
    moduleUnloaded(unloaded_module->second);
  }
}

bool WindowsSystem::handleExceptionEvent(const DEBUG_EVENT &debug_event) {
  DWORD code = debug_event.u.Exception.ExceptionRecord.ExceptionCode;
  CPUADDRESS address =
      (CPUADDRESS)debug_event.u.Exception.ExceptionRecord.ExceptionAddress;
  int thread_id = debug_event.dwThreadId;
  switch (code) {
  case EXCEPTION_BREAKPOINT:
    if (!initialDebugBreakPassed) {
      initialDebugBreakPassed = true;
      Win32_resume(thread_id, DBG_CONTINUE);
      return false;
    }
    if (breakpointHit(address, thread_id, true /* resume on echo bp */)) {
      msglog->log(LOG_ALWAYS, "Error: Breakpoint handler failed");
      Win32_resume(thread_id, DBG_EXCEPTION_NOT_HANDLED);
    }
    return true;
  default:
    switch (GetExceptionAction(code)) {
    case PASS_TO_APP:
      Win32_resume(thread_id, DBG_EXCEPTION_NOT_HANDLED);
      return false;
    case HALT:
      exceptionRaised(thread_id, address, code);
      threadContinueState.setThreadState(thread_id, DBG_EXCEPTION_NOT_HANDLED);
      return true;
    case SKIP_APP_HANDLER:
      Win32_resume(thread_id, DBG_CONTINUE);
      return false;
    }
    break;
  }
  return false;
}

bool WindowsSystem::Win32_is_dbg_event_available() {
  DEBUG_EVENT dbg_evt;
  while (WaitForDebugEvent(&dbg_evt, 10)) {
    nextContinue = DBG_CONTINUE;
    setActiveThread(dbg_evt.dwThreadId);
    switch (dbg_evt.dwDebugEventCode) {
    case CREATE_PROCESS_DEBUG_EVENT: {
      try {
        handleProcessStart(dbg_evt);
      } catch (const std::runtime_error &e) {
        // TODO(jannewger): this error is not as critical as it seems here.
        // rewrite ntheader such that we do not need handle the exception
        // here.
        msglog->log(
            LOG_ALWAYS,
            "Error while extracting module information for the debuggee: %s",
            e.what());
      }
      CloseHandle(dbg_evt.u.CreateProcessInfo.hFile);
      return true;
    }
    case EXCEPTION_DEBUG_EVENT:
      return handleExceptionEvent(dbg_evt);
    case CREATE_THREAD_DEBUG_EVENT:
      handleCreateThread(dbg_evt);
      return true;
    case LOAD_DLL_DEBUG_EVENT:
      handleLoadDll(dbg_evt);
      return true;
    case UNLOAD_DLL_DEBUG_EVENT:
      handleUnloadDll(dbg_evt);
      return true;
    case EXIT_THREAD_DEBUG_EVENT:
      handleExitThread(dbg_evt);
      return true;
    case EXIT_PROCESS_DEBUG_EVENT:
      Win32_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);
      processExit();
      return true;
    default:
      Win32_resume(dbg_evt.dwThreadId, DBG_EXCEPTION_NOT_HANDLED);
    }
  }
  return false;
}

NaviError WindowsSystem::attachToProcess() {
  wasAttached = true;
  if (!SetDebugPrivilege(GetCurrentProcess())) {
    msglog->log(LOG_ALWAYS, "Error: could not set debug privilege aborting.");
    return NaviErrors::COULDNT_ENTER_DEBUG_MODE;
  }
  msglog->log(LOG_VERBOSE, "Opening process (PID: %d)", getPID());
  hProcess = OpenProcess(PROCESS_ALL_ACCESS, 0, getPID());
  if (hProcess == nullptr) {
    msglog->log(LOG_ALWAYS,
                "Error: failed to OpenProcess() aborting... (GetLastError: %d)",
                GetLastError());
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }
  msglog->log(LOG_VERBOSE, "Debugging process (PID: %d)", getPID());
  if (!DebugActiveProcess(getPID())) {
    CloseHandle(hProcess);
    msglog->log(LOG_ALWAYS, "Error: DebugActiceProcess() failed aborting.");
    return NaviErrors::COULDNT_DEBUG_TARGET_PROCESS;
  }
  return NaviErrors::SUCCESS;
}

NaviError
WindowsSystem::startProcess(const NATIVE_STRING path,
                            const std::vector<const char *> &arguments) {
  wasAttached = false;
  if (!SetDebugPrivilege(GetCurrentProcess())) {
    msglog->log(LOG_ALWAYS, "Error: could not set debug privilege aborting.");
    return NaviErrors::COULDNT_ENTER_DEBUG_MODE;
  }
  msglog->log(LOG_VERBOSE, "Getting startup information");
  STARTUPINFO startupInfo;
  GetStartupInfo(&startupInfo);
  PROCESS_INFORMATION pi;
  std::string commandLine = path;
  commandLine += " ";
  for (unsigned int i = 0; i < arguments.size(); i++) {
    commandLine += arguments[i];
    commandLine += " ";
  }
  char *cmdl = new char[commandLine.length() + 1];
  strcpy(cmdl, commandLine.c_str());
  msglog->log(LOG_VERBOSE, "Starting %s with arguments %s", path, cmdl);
  if (!CreateProcess(0, cmdl, nullptr, nullptr, FALSE,
                     DEBUG_PROCESS | DEBUG_ONLY_THIS_PROCESS, nullptr, nullptr,
                     &startupInfo, &pi)) {
    delete[] cmdl;
    msglog->log(LOG_ALWAYS, "Error: Couldn't start target process (Code: %d)",
                GetLastError());
    return NaviErrors::COULDNT_OPEN_TARGET_PROCESS;
  }
  delete[] cmdl;
  hProcess = pi.hProcess;
  setPID(pi.dwProcessId);
  setActiveThread(pi.dwThreadId);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::detach() {
  resumeProcess();
  if (isWindowsXPOrLater()) {
    typedef BOOL(WINAPI * d_DebugActiveProcessStop)(DWORD);
    typedef BOOL(WINAPI * d_DebugSetProcessKillOnExit)(BOOL);
    HMODULE kernel32 = GetModuleHandle("kernel32.dll");
    d_DebugActiveProcessStop f_DebugActiveProcessStop =
        (d_DebugActiveProcessStop)GetProcAddress(kernel32,
                                                 "DebugActiveProcessStop");
    d_DebugSetProcessKillOnExit f_DebugSetProcessKillOnExit =
        (d_DebugSetProcessKillOnExit)GetProcAddress(
            kernel32, "DebugSetProcessKillOnExit");
    if (f_DebugActiveProcessStop && f_DebugSetProcessKillOnExit) {
      f_DebugSetProcessKillOnExit(false);
      f_DebugActiveProcessStop(getPID());
    }
  }
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::terminateProcess() {
  if (isWindowsXPOrLater()) {
    typedef BOOL(WINAPI * d_DebugActiveProcessStop)(DWORD);
    HMODULE kernel32 = GetModuleHandle("kernel32.dll");
    d_DebugActiveProcessStop f_DebugActiveProcessStop =
        (d_DebugActiveProcessStop)GetProcAddress(kernel32,
                                                 "DebugActiveProcessStop");
    if (f_DebugActiveProcessStop) {
      f_DebugActiveProcessStop(getPID());
    }
  }
  return TerminateProcess(hProcess, 0)
             ? NaviErrors::SUCCESS
             : NaviErrors::COULDNT_TERMINATE_TARGET_PROCESS;
}

NaviError WindowsSystem::storeOriginalData(const BREAKPOINT &bp) {
  std::string addressString = cpuAddressToString(bp.addr);
  if (originalBytes.find(addressString) != originalBytes.end()) {
    return NaviErrors::SUCCESS;
  }
  char b;
  NaviError result = readByte(hProcess, bp.addr, b);
  if (result) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't read byte from address %s",
                addressString.c_str());
    return result;
  }
  originalBytes[addressString] = b;
  return NaviErrors::SUCCESS;
}

// Sets a breakpoint in the target process.
NaviError WindowsSystem::setBreakpoint(const BREAKPOINT &breakpoint, bool) {
  const unsigned char BREAKPOINT_OPCODE = 0xCC;
  return writeByte(hProcess, breakpoint.addr, BREAKPOINT_OPCODE);
}

// Removes a breakpoint from the target process.
NaviError WindowsSystem::removeBreakpoint(const BREAKPOINT &bp, bool) {
  std::string addressString = cpuAddressToString(bp.addr);
  if (originalBytes.find(addressString) == originalBytes.end()) {
    msglog->log(
        LOG_ALWAYS,
        "Error: Trying to restore a breakpoint with unknown original byte");
    return NaviErrors::ORIGINAL_DATA_NOT_AVAILABLE;
  }
  char b = originalBytes[addressString];
  msglog->log(LOG_VERBOSE, "Writing byte %lx to address %s\n", (unsigned int)b,
              addressString.c_str());
  return writeByte(hProcess, bp.addr, b);
}

// The alreadyStepped set keeps track of all threads that were already single-
// stepped "recently".
//
// This is necessary because of the following scenario:
//
// - Thread A hits breakpoint
// - Thread A is stepped over the breakpoint
// - Before Thread A stepping is complete, Thread B hits breakpoint
// - Thread B is stepped over the breakpoint
// - Before Thread B stepping is complete, Thread A stepping is complete
//
// What has to happen now is that the step-loop of Thread A has to
// be stopped but Thread A does not know this because it was stepped in
// the step-loop of Thread B and the two loops can not communicate with
// each other directly. Therefore alreadyStepped is used.
//
// You can make this scenario arbitrarily difficult by adding more threads.
std::set<unsigned int> alreadyStepped;

NaviError WindowsSystem::doSingleStep(unsigned int &tid, CPUADDRESS &address) {
  NaviError stepEnter = changeStepMode(tid, true);
  if (stepEnter) {
    msglog->log(LOG_ALWAYS,
                "Error: Could not enter single step mode in thread (0x%X)\n",
                tid);
    return NaviErrors::COULDNT_ENTER_SINGLE_STEP_MODE;
  }
  for (const auto &thread_id : tids) {
    if (thread_id.tid != tid) {
      NaviError suspendError = suspendThread(thread_id.tid);
      if (suspendError) {
        msglog->log(LOG_ALWAYS, "Error: Unable to suspend thread (0x%X)\n",
                    thread_id.tid);
        return NaviErrors::COULDNT_SUSPEND_THREAD;
      }
    }
  }

  getInstructionPointer(tid, address);
  const auto breakpoints(getBreakpoints(address));
  for (const auto &breakpoint : breakpoints) {
    removeBreakpoint(breakpoint);
  }

  NaviError resumeResult = resumeProcess();
  if (resumeResult) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't resume process");
    return resumeResult;
  }

  DEBUG_EVENT dbg_evt;
  do {
    if (!WaitForDebugEvent(&dbg_evt, INFINITE)) {
      msglog->log(LOG_ALWAYS, "Error: WaitForDebugEvent failed with error 0x%X",
                  GetLastError());
      return NaviErrors::COULDNT_SINGLE_STEP;
    }
    switch (dbg_evt.dwDebugEventCode) {
    case EXIT_THREAD_DEBUG_EVENT:
      handleExitThread(dbg_evt);
      continue;
    case CREATE_THREAD_DEBUG_EVENT:
      handleCreateThread(dbg_evt);
      continue;
    case LOAD_DLL_DEBUG_EVENT:
      handleLoadDll(dbg_evt);
      continue;
    case UNLOAD_DLL_DEBUG_EVENT:
      handleUnloadDll(dbg_evt);
      continue;
    case EXCEPTION_DEBUG_EVENT:
      switch (dbg_evt.u.Exception.ExceptionRecord.ExceptionCode) {
      case EXCEPTION_BREAKPOINT:
        setActiveThread(dbg_evt.dwThreadId);
        if (breakpointHit((CPUADDRESS)
                          dbg_evt.u.Exception.ExceptionRecord.ExceptionAddress,
                          dbg_evt.dwThreadId,
                          false /* do not resume on echo bp */)) {
          msglog->log(LOG_ALWAYS, "Error: Breakpoint handler failed");
          ContinueDebugEvent(dbg_evt.dwProcessId, dbg_evt.dwThreadId,
                             DBG_EXCEPTION_NOT_HANDLED);
        }
        if (alreadyStepped.find(tid) != alreadyStepped.end()) {
          alreadyStepped.erase(tid);
        }
        break;
      case EXCEPTION_SINGLE_STEP:
        if (dbg_evt.dwThreadId != tid) {
          alreadyStepped.insert(tid);
          Win32_resume(dbg_evt.dwThreadId, DBG_CONTINUE);
        }
        break;
      default:
        msglog->log(LOG_ALWAYS,
                    "Error: expected EXCEPTION_SINGLE_STEP, but got %d.",
                    dbg_evt.u.Exception.ExceptionRecord.ExceptionCode);
        return NaviErrors::COULDNT_SINGLE_STEP;
      }
      break;
    default:
      msglog->log(
          LOG_ALWAYS,
          "Error: expected to get an EXCEPTION_DEBUG_EVENT, but got %d.",
          dbg_evt.dwDebugEventCode);
      return NaviErrors::COULDNT_SINGLE_STEP;
    }
  } while (dbg_evt.dwThreadId != tid ||
           !(dbg_evt.dwDebugEventCode == EXCEPTION_DEBUG_EVENT &&
             dbg_evt.u.Exception.ExceptionRecord.ExceptionCode ==
                 EXCEPTION_SINGLE_STEP));

  for (const auto &breakpoint : breakpoints) {
    setBreakpoint(breakpoint);
  }

  for (const auto &thread_id : tids) {
    if (thread_id.tid != tid) {
      NaviError resumeError = resumeThread(thread_id.tid);
      if (resumeError) {
        msglog->log(LOG_ALWAYS, "Error: Unable to resume thread (0x%X)\n");
        return resumeError;
      }
    }
  }

  address = (CPUADDRESS)dbg_evt.u.Exception.ExceptionRecord.ExceptionAddress;
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::resumeProcess() {
  if (threadContinueState.hasThreadState(getActiveThread())) {
    DWORD continueAction =
        threadContinueState.getThreadState<DWORD>(getActiveThread());
    threadContinueState.removeThreadState(getActiveThread());
    return Win32_resume(getActiveThread(), continueAction)
               ? NaviErrors::SUCCESS
               : NaviErrors::COULDNT_RESUME_THREAD;
  } else {
    return Win32_resume(getActiveThread(), nextContinue)
               ? NaviErrors::SUCCESS
               : NaviErrors::COULDNT_RESUME_THREAD;
  }
}

NaviError WindowsSystem::suspendThread(unsigned int tid) {
  HANDLE handle = OpenThread(THREAD_SUSPEND_RESUME, FALSE, tid);
  if (!handle) {
    printLastError();
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }
  if (SuspendThread(handle) == -1) {
    printLastError();
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }
  CloseHandle(handle);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::resumeThread(unsigned int tid) {
  HANDLE handle = OpenThread(THREAD_SUSPEND_RESUME, FALSE, tid);
  if (!handle) {
    printLastError();
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }
  if (ResumeThread(handle) == -1) {
    printLastError();
    return NaviErrors::COULDNT_SUSPEND_THREAD;
  }
  CloseHandle(handle);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::getInstructionPointer(unsigned int threadId,
                                               CPUADDRESS &addr) {
  CONTEXT threadContext;
  HANDLE thread = OpenThread(THREAD_ALL_ACCESS, FALSE, threadId);
  threadContext.ContextFlags = CONTEXT_FULL;
  if (GetThreadContext(thread, &threadContext) == FALSE) {
    msglog->log(LOG_ALWAYS, "Error: GetThreadContext failed %s:%d.",
                __FUNCTION__, __LINE__);
    CloseHandle(thread);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
#ifdef CPU_386
  addr = threadContext.Eip;
#elif CPU_AMD64
  addr = threadContext.Rip;
#else
#error Unknown architecture
#endif
  CloseHandle(thread);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::setInstructionPointer(unsigned int tid,
                                               CPUADDRESS address) {
  CONTEXT threadContext;
  HANDLE thread = OpenThread(THREAD_ALL_ACCESS, FALSE, tid);
  threadContext.ContextFlags = CONTEXT_FULL;
  if (GetThreadContext(thread, &threadContext) == FALSE) {
    msglog->log(LOG_ALWAYS, "Error: GetThreadContext failed %s:%d.",
                __FUNCTION__, __LINE__);
    CloseHandle(thread);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
#ifdef CPU_386
  threadContext.Eip = address;
#elif CPU_AMD64
  threadContext.Rip = address;
#else
#error Unknown architecture
#endif
  NaviError result = SetThreadContext(thread, &threadContext)
                         ? NaviErrors::SUCCESS
                         : NaviErrors::COULDNT_WRITE_REGISTERS;
  CloseHandle(thread);
  return result;
}

std::vector<char> WindowsSystem::readPointedMemory(CPUADDRESS address) {
  unsigned int currentSize = 128;
  while (currentSize != 0) {
    std::vector<char> memory(currentSize, 0);
    if (readMemoryDataInternal(&memory[0], address, memory.size(), true) ==
        NaviErrors::SUCCESS) {
      return memory;
    }
    currentSize /= 2;
  }
  return std::vector<char>();
}

// Fills a given register container structure with information about the
// current values of the CPU registers in the active thread.
NaviError WindowsSystem::readRegisters(RegisterContainer &registers) {
  unsigned int threadId = getActiveThread();
  Thread thread = Thread(threadId, RUNNING);
  HANDLE hThread = OpenThread(THREAD_ALL_ACCESS, FALSE, threadId);
  if (!hThread) {
    DWORD lastError = GetLastError();
    if (isThreadDead(threadId)) {
      return NaviErrors::SUCCESS;
    } else {
      msglog->log(LOG_ALWAYS, "Opening thread %d failed (Error: 0x%X)",
                  threadId, lastError);
      return NaviErrors::COULDNT_OPEN_THREAD;
    }
  }
  CONTEXT ctx;
  ctx.ContextFlags = CONTEXT_FULL;
  if (GetThreadContext(hThread, &ctx) == FALSE) {
    msglog->log(LOG_ALWAYS,
                "Error: %s: GetThreadContext failed (Code: %d, thread: 0x%X).",
                __FUNCTION__, GetLastError(), threadId);
    CloseHandle(hThread);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
  msglog->log(LOG_VERBOSE, "Assigning register values of thread %X", threadId);

#ifdef CPU_386
  thread.registers.push_back(makeRegisterValue(
      "EAX", zylib::zycon::toHexString(ctx.Eax), readPointedMemory(ctx.Eax)));
  thread.registers.push_back(makeRegisterValue(
      "EBX", zylib::zycon::toHexString(ctx.Ebx), readPointedMemory(ctx.Ebx)));
  thread.registers.push_back(makeRegisterValue(
      "ECX", zylib::zycon::toHexString(ctx.Ecx), readPointedMemory(ctx.Ecx)));
  thread.registers.push_back(makeRegisterValue(
      "EDX", zylib::zycon::toHexString(ctx.Edx), readPointedMemory(ctx.Edx)));
  thread.registers.push_back(makeRegisterValue(
      "ESI", zylib::zycon::toHexString(ctx.Esi), readPointedMemory(ctx.Esi)));
  thread.registers.push_back(makeRegisterValue(
      "EDI", zylib::zycon::toHexString(ctx.Edi), readPointedMemory(ctx.Edi)));
  thread.registers.push_back(
      makeRegisterValue("ESP", zylib::zycon::toHexString(ctx.Esp),
                        readPointedMemory(ctx.Esp), false, true));
  thread.registers.push_back(makeRegisterValue(
      "EBP", zylib::zycon::toHexString(ctx.Ebp), readPointedMemory(ctx.Ebp)));
  thread.registers.push_back(
      makeRegisterValue("EIP", zylib::zycon::toHexString(ctx.Eip),
                        readPointedMemory(ctx.Eip), true));
  thread.registers.push_back(
      makeRegisterValue("EFLAGS", zylib::zycon::toHexString(ctx.EFlags)));
  thread.registers.push_back(
      makeRegisterValue("CF", zylib::zycon::toHexString(ctx.EFlags & 1)));
  thread.registers.push_back(makeRegisterValue(
      "PF", zylib::zycon::toHexString((ctx.EFlags >> 2) & 1)));
  thread.registers.push_back(makeRegisterValue(
      "AF", zylib::zycon::toHexString((ctx.EFlags >> 4) & 1)));
  thread.registers.push_back(makeRegisterValue(
      "ZF", zylib::zycon::toHexString((ctx.EFlags >> 6) & 1)));
  thread.registers.push_back(makeRegisterValue(
      "SF", zylib::zycon::toHexString((ctx.EFlags >> 7) & 1)));
  thread.registers.push_back(makeRegisterValue(
      "OF", zylib::zycon::toHexString((ctx.EFlags >> 11) & 1)));
#elif CPU_AMD64 // TODO(timkornau): add all registers specified by platform.
  thread.registers.push_back(
      makeRegisterValue("RAX", zylib::zycon::toHexString(ctx.Rax)));
  thread.registers.push_back(
      makeRegisterValue("RBX", zylib::zycon::toHexString(ctx.Rbx)));
  thread.registers.push_back(
      makeRegisterValue("RCX", zylib::zycon::toHexString(ctx.Rcx)));
  thread.registers.push_back(
      makeRegisterValue("RDX", zylib::zycon::toHexString(ctx.Rdx)));
  thread.registers.push_back(
      makeRegisterValue("RSI", zylib::zycon::toHexString(ctx.Rsi)));
  thread.registers.push_back(
      makeRegisterValue("RDI", zylib::zycon::toHexString(ctx.Rdi)));
  thread.registers.push_back(
      makeRegisterValue("RSP", zylib::zycon::toHexString(ctx.Rsp)));
  thread.registers.push_back(
      makeRegisterValue("RBP", zylib::zycon::toHexString(ctx.Rbp)));
  thread.registers.push_back(
      makeRegisterValue("RIP", zylib::zycon::toHexString(ctx.Rip), true));
  thread.registers.push_back(
      makeRegisterValue("EFLAGS", zylib::zycon::toHexString(ctx.EFlags)));
#else
#error Unknown architecture
#endif
  CloseHandle(hThread);
  registers.addThread(thread);
  return NaviErrors::SUCCESS;
}

// Updates the value of a given register in a given thread.
NaviError WindowsSystem::setRegister(unsigned int tid, unsigned int index,
                                     CPUADDRESS address) {
  CONTEXT threadContext;
  HANDLE thread = OpenThread(THREAD_ALL_ACCESS, FALSE, tid);
  threadContext.ContextFlags = CONTEXT_FULL;
  if (GetThreadContext(thread, &threadContext) == FALSE) {
    msglog->log(LOG_ALWAYS, "Error: GetThreadContext failed %s:%d.",
                __FUNCTION__, __LINE__);
    return NaviErrors::COULDNT_READ_REGISTERS;
  }
#ifdef CPU_386
  switch (index) {
  case 0:
    threadContext.Eax = address;
    break;
  case 1:
    threadContext.Ebx = address;
    break;
  case 2:
    threadContext.Ecx = address;
    break;
  case 3:
    threadContext.Edx = address;
    break;
  case 4:
    threadContext.Esi = address;
    break;
  case 5:
    threadContext.Edi = address;
    break;
  case 6:
    threadContext.Esp = address;
    break;
  case 7:
    threadContext.Ebp = address;
    break;
  case 8:
    threadContext.Eip = address;
    break;
  case 10:
    threadContext.EFlags = (threadContext.EFlags & 0xFFFFFFFE) | (address & 1);
    break;
  case 11:
    threadContext.EFlags =
        (threadContext.EFlags & 0xFFFFFFFB) | ((address & 1) << 2);
    break;
  case 12:
    threadContext.EFlags =
        (threadContext.EFlags & 0xFFFFFFEF) | ((address & 1) << 4);
    break;
  case 13:
    threadContext.EFlags =
        (threadContext.EFlags & 0xFFFFFFBF) | ((address & 1) << 6);
    break;
  case 14:
    threadContext.EFlags =
        (threadContext.EFlags & 0xFFFFFF7F) | ((address & 1) << 7);
    break;
  case 15:
    threadContext.EFlags =
        (threadContext.EFlags & 0xFFFFF7FF) | ((address & 1) << 11);
    break;
  default:
    return NaviErrors::INVALID_REGISTER_INDEX;
  }
#elif CPU_AMD64
  switch (index) {
  case 0:
    threadContext.Rax = address;
    break;
  case 1:
    threadContext.Rbx = address;
    break;
  case 2:
    threadContext.Rcx = address;
    break;
  case 3:
    threadContext.Rdx = address;
    break;
  case 4:
    threadContext.Rsi = address;
    break;
  case 5:
    threadContext.Rdi = address;
    break;
  case 6:
    threadContext.Rsp = address;
    break;
  case 7:
    threadContext.Rbp = address;
    break;
  case 8:
    threadContext.Rip = address;
    break;
  default:
    return NaviErrors::INVALID_REGISTER_INDEX;
  }
#else
#error Unknown architecture
#endif
  NaviError result = SetThreadContext(thread, &threadContext)
                         ? NaviErrors::SUCCESS
                         : NaviErrors::COULDNT_WRITE_REGISTERS;
  CloseHandle(thread);
  return result;
}

// Given an address, this function returns the first and last offset of the
// memory region the address belongs to.
NaviError WindowsSystem::getValidMemory(CPUADDRESS start, CPUADDRESS &from,
                                        CPUADDRESS &to) {
  unsigned int PAGE_SIZE = 0x1000;
  CPUADDRESS startOffset = start & (~(PAGE_SIZE - 1));
  CPUADDRESS current = startOffset;
  CPUADDRESS low = (unsigned int)current;
  CPUADDRESS high = (unsigned int)current;
  MEMORY_BASIC_INFORMATION mem;
  for (;;) {
    if (!VirtualQueryEx(hProcess, (void *)current, &mem,
                        sizeof(MEMORY_BASIC_INFORMATION))) {
      break;
    }
    if (mem.State != MEM_COMMIT) {
      break;
    }
    current -= PAGE_SIZE;
  }
  if (current == startOffset) {
    return NaviErrors::NO_VALID_MEMORY;
  }
  low = current + PAGE_SIZE;
  current = startOffset;
  for (;;) {
    if (!VirtualQueryEx(hProcess, (void *)current, &mem,
                        sizeof(MEMORY_BASIC_INFORMATION))) {
      break;
    }
    if (mem.State != MEM_COMMIT) {
      break;
    }
    current += PAGE_SIZE;
  }
  high = current;
  from = low;
  to = high;
  return low != high ? NaviErrors::SUCCESS : NaviErrors::NO_VALID_MEMORY;
}

// Returns a list of all memory regions that are available in the target
// process.
NaviError WindowsSystem::getMemmap(std::vector<CPUADDRESS> &addresses) {
  MEMORY_BASIC_INFORMATION mem;
  CPUADDRESS offset = 0;
  unsigned int consecutiveRegions = 0;
  while (VirtualQueryEx(hProcess, (void *)offset, &mem, sizeof(mem))) {
    if (mem.State == MEM_COMMIT) {
      ++consecutiveRegions;
      if (consecutiveRegions == 1) {
        msglog->log(LOG_ALL, "Found memory section between %X and %X",
                    (CPUADDRESS)mem.BaseAddress,
                    (CPUADDRESS)mem.BaseAddress + mem.RegionSize - 1);
        msglog->log(LOG_ALL, "With Protection %X", mem.Protect);
        addresses.push_back((CPUADDRESS)mem.BaseAddress);
        addresses.push_back(((CPUADDRESS)mem.BaseAddress + mem.RegionSize - 1));
      } else {
        msglog->log(LOG_ALL, "Extending memory section to %X",
                    addresses.back() +
                        (CPUADDRESS)mem.RegionSize);
        msglog->log(LOG_ALL, "With Protection %X", mem.Protect);
        addresses.back() += (CPUADDRESS)mem.RegionSize;
      }
    } else {
      consecutiveRegions = 0;
    }
	offset = reinterpret_cast<CPUADDRESS>(mem.BaseAddress) + mem.RegionSize;
    if (offset == 0) {
      break;
    }
  }

  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::readMemoryDataInternal(char *buffer,
                                                CPUADDRESS address,
                                                CPUADDRESS size, bool silent) {
  SIZE_T outlen;
  msglog->log(LOG_ALL, "Trying to read %d bytes from memory address %X", size,
              address);
  DWORD oldProtection = 0;
  unsigned int result =
      makePageReadable(hProcess, address, oldProtection, silent);
  if (result == PROTECTION_CHANGE_FAILED ||
      result == PROTECTION_CHANGE_IMPOSSIBLE) {
    return NaviErrors::COULDNT_READ_MEMORY;
  }
  if (ReadProcessMemory(hProcess, (void *)address, buffer, size, &outlen) ==
      0) {
    if (!silent) {
      msglog->log(LOG_ALWAYS,
                  "Error: ReadProcessMemory failed (Error Code: %d)",
                  GetLastError());
    }
    return NaviErrors::COULDNT_READ_MEMORY;
  }
  if (result == PROTECTION_CHANGED) {
    restoreMemoryProtection(hProcess, address, oldProtection);
  }
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::readMemoryData(char *buffer, CPUADDRESS address,
                                        CPUADDRESS size) {
  return readMemoryDataInternal(buffer, address, size, false);
}

NaviError WindowsSystem::writeMemory(CPUADDRESS address,
                                     const std::vector<char> &data) {
  return writeBytes(hProcess, address, data);
}

NaviError WindowsSystem::readProcessList(ProcessListContainer &processList) {
  HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
  if (hSnapshot == INVALID_HANDLE_VALUE) {
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }
  PROCESSENTRY32 pe;
  pe.dwSize = sizeof(PROCESSENTRY32);
  BOOL retval = Process32First(hSnapshot, &pe);
  while (retval) {
    DWORD pid = pe.th32ProcessID;
    std::string name = pe.szExeFile;
    ProcessDescription process(pid, name);
    processList.push_back(process);
    pe.dwSize = sizeof(PROCESSENTRY32);
    retval = Process32Next(hSnapshot, &pe);
  }
  CloseHandle(hSnapshot);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::getAttachedProcessInfo(DWORD processId,
                                                std::string &exeName,
                                                std::string &exePath,
                                                CPUADDRESS &imageSize) const {
  HANDLE hSnapshot = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, processId);
  if (hSnapshot == INVALID_HANDLE_VALUE) {
    return NaviErrors::COULDNT_GET_PROCESSLIST;
  }
  MODULEENTRY32 me;
  me.dwSize = sizeof(MODULEENTRY32);
  BOOL retval = Module32First(hSnapshot, &me);
  while (retval) {
    if (processId == me.th32ProcessID) {
      exeName = me.szModule;
      exePath = me.szExePath;
      imageSize = me.modBaseSize;
      break;
    }
    me.dwSize = sizeof(MODULEENTRY32);
    retval = Module32Next(hSnapshot, &me);
  }
  CloseHandle(hSnapshot);
  return NaviErrors::SUCCESS;
}

NaviError WindowsSystem::readDebugEvents() {
  return Win32_is_dbg_event_available()
             ? NaviErrors::SUCCESS
             : NaviErrors::WAITING_FOR_DEBUG_EVENTS_FAILED;
}

std::vector<RegisterDescription> WindowsSystem::getRegisterNames() const {
  std::vector<RegisterDescription> regNames;
#ifdef CPU_386
  RegisterDescription eax("EAX", 4, true);
  RegisterDescription ebx("EBX", 4, true);
  RegisterDescription ecx("ECX", 4, true);
  RegisterDescription edx("EDX", 4, true);
  RegisterDescription esi("ESI", 4, true);
  RegisterDescription edi("EDI", 4, true);
  RegisterDescription ebp("EBP", 4, true);
  RegisterDescription esp("ESP", 4, true);
  RegisterDescription eip("EIP", 4, true);
  RegisterDescription eflags("EFLAGS", 4, false);
  RegisterDescription cf("CF", 0, true);
  RegisterDescription pf("PF", 0, true);
  RegisterDescription af("AF", 0, true);
  RegisterDescription zf("ZF", 0, true);
  RegisterDescription sf("SF", 0, true);
  RegisterDescription of("OF", 0, true);

  regNames.push_back(eax);
  regNames.push_back(ebx);
  regNames.push_back(ecx);
  regNames.push_back(edx);
  regNames.push_back(esi);
  regNames.push_back(edi);
  regNames.push_back(esp);
  regNames.push_back(ebp);
  regNames.push_back(eip);
  regNames.push_back(eflags);
  regNames.push_back(cf);
  regNames.push_back(pf);
  regNames.push_back(af);
  regNames.push_back(zf);
  regNames.push_back(sf);
  regNames.push_back(of);
#elif CPU_AMD64
  RegisterDescription rax("RAX", 8, false);
  RegisterDescription rbx("RBX", 8, false);
  RegisterDescription rcx("RCX", 8, false);
  RegisterDescription rdx("RDX", 8, false);
  RegisterDescription rsi("RSI", 8, false);
  RegisterDescription rdi("RDI", 8, false);
  RegisterDescription rbp("RBP", 8, false);
  RegisterDescription rsp("RSP", 8, false);
  RegisterDescription rip("RIP", 8, false);
  RegisterDescription eflags("EFLAGS", 4, false);
  regNames.push_back(rax);
  regNames.push_back(rbx);
  regNames.push_back(rcx);
  regNames.push_back(rdx);
  regNames.push_back(rsi);
  regNames.push_back(rdi);
  regNames.push_back(rsp);
  regNames.push_back(rbp);
  regNames.push_back(rip);
  regNames.push_back(eflags);
#else
#error Unknown architecture
#endif
  return regNames;
}

unsigned int WindowsSystem::getAddressSize() const {
#ifdef CPU_386
  return 32;
#elif CPU_AMD64 || CPU_IA64
  return 64;
#else
#error Unknown architecture
#endif
}

DebuggerOptions WindowsSystem::getDebuggerOptions() const {
  DebuggerOptions empty;
  empty.canDetach = isWindowsXPOrLater() == TRUE ? true : false;
  empty.canMultithread = true;
  empty.pageSize = 4096;
  empty.exceptions = getPlatformExceptions();
  empty.canTraceCount = true;
  return empty;
}

DebugExceptionContainer WindowsSystem::getPlatformExceptions() const {
  return windowscommon::exception_list;
}

// Create a module instance for the executable image which was launched by the
// debug API.
Module WindowsSystem::getProcessModule(
    const CREATE_PROCESS_DEBUG_INFO *dbgInfo) const {
  if (wasAttached) {
    std::string path, name;
    CPUADDRESS imageSize = 0;
    getAttachedProcessInfo(getPID(), name, path, imageSize);
    return Module(name, path, (CPUADDRESS)dbgInfo->lpBaseOfImage, imageSize);
  } else {
    CPUADDRESS imageBase = (CPUADDRESS)dbgInfo->lpBaseOfImage;
    NTHeader ntHeader(getTargetApplicationPath().string());
    CPUADDRESS imageSize = ntHeader.getImageSize();
    return Module(getTargetApplicationPath().filename().string(),
                  getTargetApplicationPath().string(), imageBase, imageSize);
  }
}

NaviError WindowsSystem::getFileSystems(
    std::vector<boost::filesystem::path> &roots) const {
  return windowscommon::getFileSystems(roots);
}

NaviError WindowsSystem::getSystemRoot(boost::filesystem::path &root) const {
  return windowscommon::getSystemRoot(root);
}

// Add module to internal module map and generate debug event.
void WindowsSystem::addModule(const Module &module, unsigned int threadId) {
  moduleMap.insert(std::make_pair(module.baseAddress, module));
  moduleLoaded(module, threadId);
}

bool WindowsSystem::isThreadDead(unsigned int threadId) const {
  for (const Thread &t : tids) {
    if (t.tid == threadId) {
      return false;
    }
  }
  return true;
}