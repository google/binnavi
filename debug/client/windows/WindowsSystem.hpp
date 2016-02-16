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

#ifndef WINDOWSSYSTEM_HPP
#define WINDOWSSYSTEM_HPP

#define _WIN32_WINNT 0x0500
#define NOMINMAX

#include "../defs.hpp"
#include "../BaseSystem.hpp"

#include <windows.h>
#include <Psapi.h>
#include <string>
#include <map>
#include "ThreadStateInformation.hpp"

struct DebuggerOptions;

/**
 * System policy for Windows systems.
 */
class WindowsSystem : public BaseSystem {
 private:

  /**
   * Handle of the target process.
   */
  HANDLE hProcess;

  /**
   * Map that is used to keep track of the memory values
   * that are overwritten by breakpoint bytes.
   */
  std::map<std::string, char> originalBytes;

  std::map<CPUADDRESS, Module> moduleMap;

  /**
   * Keeps track of the threads of the target process.
   */
  std::vector<Thread> tids;

  std::vector<Module> modules;

  DWORD nextContinue;

  // specifies whether the initial debug break event was already passed.
  bool initialDebugBreakPassed;

  // Specifies whether the debugger was attached to the process or if the
  // debugger started the process directly.
  bool wasAttached;

  // Resumes the target process after a debug exception
  bool Win32_resume(unsigned int tid, DWORD mode);

  // Attaches to process identified by a PID
  bool Win32_attach_to_process(unsigned int ulPID);

  // Tests whether a debug event is available in the target process
  bool Win32_is_dbg_event_available();

  // Handles DLL load events
  void handleDllLoad();

  NaviError readMemoryDataInternal(char* buffer, CPUADDRESS address,
                                   CPUADDRESS size, bool silent);

  std::vector<char> readPointedMemory(CPUADDRESS address);

  void handleCreateThread(const DEBUG_EVENT& dbg_evt);

  void handleExitThread(const DEBUG_EVENT& dbg_evt);

  void handleLoadDll(const DEBUG_EVENT& dbg_evt);

  // Add module to internal module map and generate debug event.
  void addModule(const Module& module, unsigned int threadId);

  void handleUnloadDll(const DEBUG_EVENT& dbg_evt);

  // Handles the debug exception in the main debug loop.
  bool handleExceptionEvent(const DEBUG_EVENT& debug_event);

  // saves the continue state of a thread when the debugger is halted after an
  // exception occurred
  ThreadStateInformation<DWORD> threadContinueState;

  Module getProcessModule(const CREATE_PROCESS_DEBUG_INFO* dbgInfo) const;

  void handleProcessStart(const DEBUG_EVENT& dbg_evt);

  // Determine all currently loaded modules.
  NaviError fillModules(HANDLE process_handle,
                        std::vector<Module>& modules) const;

  NaviError addThread(const Thread& thread);

  NaviError getAttachedProcessInfo(DWORD processId, std::string& exeName,
                                   std::string& exePath,
                                   CPUADDRESS& imageSize) const;

  // Test if thread with the given id is not existing in the debuggee.
  bool isThreadDead(unsigned int threadId) const;

 protected:

  // Attaches to a running process
  NaviError attachToProcess();

  // Starts a new process for debugging
  NaviError startProcess(const char* path,
                         const std::vector<const char*>& commands);

  // Detaches from the target process
  NaviError detach();

  // Terminates the target process
  NaviError terminateProcess();

  // Stores the original data that is replaced by a breakpoint
  NaviError storeOriginalData(const BREAKPOINT& bp);

  // Sets a breakpoint in the target process
  NaviError setBreakpoint(const BREAKPOINT& breakpoint, bool moreToCome = false);

  // Removes a breakpoint from the target process
  NaviError removeBreakpoint(const BREAKPOINT& bp, bool moreToCome = false);

  // Executes the next instruction in thread of the target process
  NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address);

  NaviError resumeProcess();

  NaviError suspendThread(unsigned int tid);

  // Resumes a thread in the target process
  NaviError resumeThread(unsigned int tid);

  // Returns the current instruction pointer of a thread of the target process
  NaviError getInstructionPointer(unsigned int tid, CPUADDRESS& addr);

  // Sets the instruction pointer of a thread in the target process
  NaviError setInstructionPointer(unsigned int tid, CPUADDRESS address);

  // Reads the register values of all threads
  NaviError readRegisters(RegisterContainer& registers);

  // Sets the value of a register in the target process
  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS address);

  // Reads a part of the target process memory
  NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to);

  NaviError writeMemory(CPUADDRESS address, const std::vector<char>& data);

  // Finds the memory region an offset belongs to
  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from, CPUADDRESS& to);

  // Find all allocated memory sections of the target process
  NaviError getMemmap(std::vector<CPUADDRESS>& addresses);

  // Halts the target process
  NaviError halt() {
    return NaviErrors::UNSUPPORTED;
  }

  NaviError readProcessList(ProcessListContainer& processList);

  NaviError readFiles(FileListContainer& fileList);

  NaviError readFiles(FileListContainer& fileList, const std::string& path);

  // Returns a list of exceptions known by the debugger.
  DebugExceptionContainer getPlatformExceptions() const;

  // Resolves an exception code to the corresponding string representation.
  std::string getExceptionName(CPUADDRESS exceptionCode) const;

  NaviError getFileSystems(std::vector<boost::filesystem::path>& roots) const;

  NaviError WindowsSystem::getSystemRoot(boost::filesystem::path& root) const;

 public:

  WindowsSystem()
      : BaseSystem(),
        nextContinue(DBG_CONTINUE),
        hProcess(0),
        initialDebugBreakPassed(false),
        wasAttached(false) {
  }

  /**
   * Creates a new WindowsSystem object
   *
   * @param pid The process ID of the target process.
   */
  WindowsSystem(unsigned int pid)
      : BaseSystem(pid),
        nextContinue(DBG_CONTINUE),
        hProcess(0) {
  }

  /**
   * Creates a new WindowsSystem object
   *
   * @param path The path to the target process executable
   */
  WindowsSystem(const NATIVE_STRING path,
                const std::vector<const NATIVE_STRING>& commands)
      : BaseSystem(path, commands),
        nextContinue(DBG_CONTINUE),
        hProcess(0) {
  }

  ~WindowsSystem() {
    if (hProcess) {
      CloseHandle(hProcess);
    }
  }

  // Reads new debug events from the target process
  NaviError readDebugEvents();

  // Returns the register names that are available on the target architecture
  std::vector<RegisterDescription> getRegisterNames() const;

  // Returns the address size of the target architecture
  unsigned int getAddressSize() const;

  // Returns the debugger options that are supported by the debug client
  DebuggerOptions getDebuggerOptions() const;
};

#endif
