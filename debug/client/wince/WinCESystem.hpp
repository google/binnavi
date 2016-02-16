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

#ifndef WINCESYSTEM_HPP
#define WINCESYSTEM_HPP

#define NOMINMAX

#include <windows.h>
#include <Psapi.h>
#include <string>
#include <map>

#include "../defs.hpp"
#include "../BaseSystem.hpp"

/**
 * System policy for Windows systems.
 */
class WinCESystem : public BaseSystem {
 private:

  /**
   * Handle of the target process.
   */
  HANDLE hProcess;

  /**
   * Handle of the target process' thread.
   */
  HANDLE hThread;

  /**
   * Id of the last thread that reported a debug event
   */
  //		DWORD lastThread;
  /**
   * Handle of the exit event.
   */
  HANDLE hEventExit;

  /*
   * Load address of target process.
   */
  unsigned int baseOfImage;

  unsigned int baseOfCoreDll;

  /*
   * Flag indicating the current step mode
   */
  //bool singleStepMode;
  /**
   * Map that is used to keep track of the memory values
   * that are overwritten by breakpoint bytes.
   */
  std::map<std::string, unsigned int> originalDWORDs;

  /**
   * Keeps track of the threads of the target process
   */
  std::vector<Thread> tids;

  /**
   * Keeps track of the modules of the target process
   */
  std::vector<Module> modules;

  DWORD nextContinue;

  /**
   * Resumes the target process after a debug exception
   */
  bool WinCE_arm_resume(unsigned int tid, DWORD mode);

  /**
   * Tests whether a debug event is available in the target process
   */
  bool WinCE_arm_is_dbg_event_available();
  bool WinCE_arm_wait_for_debug_event(LPDEBUG_EVENT lpDebugEvent,
                                      DWORD dwMilliseconds);

  NaviError getNextInstructionAddress(unsigned int tid, CPUADDRESS& address);
  NaviError WinCESystem::getRegisterTable(unsigned int* registers);

  //NaviError readDWORD(HANDLE hProcess, CPUADDRESS offset, unsigned int &d);
  //NaviError writeDWORD(HANDLE hProcess, CPUADDRESS offset, unsigned int d);

 protected:

  // Break Point functions
  NaviError setBreakpoint(BREAKPOINT& breakpoint, bool moreToCome = false);
  NaviError resumeThread(unsigned int tid);
  NaviError resumeProcess();
  NaviError removeBreakpoint(const BREAKPOINT& bp, bool moreToCome = false);

  NaviError setInstructionPointer(unsigned int tid, CPUADDRESS address);

  NaviError storeOriginalData(const BREAKPOINT& bp);

  NaviError readRegisters(RegisterContainer& registers);
  NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to);

  NaviError readProcessList(ProcessListContainer& processList);

  NaviError readFiles(FileListContainer& fileList);

  NaviError readFiles(FileListContainer& fileList, const std::string& path);

  NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address);

  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS address);

  NaviError getInstructionPointer(unsigned int tid, CPUADDRESS& addr);

  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from, CPUADDRESS& to);

  NaviError attachToProcess(std::vector<Thread>& tids,
                            std::vector<Module>& modules);

  NaviError startProcess(const wchar_t* path,
                         const std::vector<const wchar_t*>& commands,
                         std::vector<Thread>& tids,
                         std::vector<Module>& modules);

  NaviError getMemmap(std::vector<CPUADDRESS>& addresses);

  NaviError terminateProcess();

  NaviError detach();

  NaviError halt() {
    return NaviErrors::UNSUPPORTED;
  }

  NaviError dllDebugEventHandler(LPVOID moduleBaseAddress, DWORD processID,
                                 CPUADDRESS imageNamePtr, int isUnicode,
                                 bool load);

  NaviError PtrToString(DWORD dwProcessId, CPUADDRESS address, int len, int uni,
                        TCHAR* tbuf);

  NaviError writeMemory(CPUADDRESS address, const std::vector<char>& data);

  NaviError suspendThread(unsigned int tid);

  std::vector<char> readPointedMemory(CPUADDRESS address);

  NaviError readMemoryDataInternal(char* buffer, CPUADDRESS address,
                                   CPUADDRESS size, bool silent);

 public:

  /**
   * Creates a new WindowsSystem object
   *
   * @param pid The process ID of the target process.
   */
  WinCESystem(unsigned int pid)
      : BaseSystem(pid) {
    hEventExit = CreateEvent(NULL, true, false, L"binnavi_exit");
  }

  WinCESystem(const wchar_t* path, const std::vector<const wchar_t*>& commands)
      : BaseSystem(path, commands) {
  }

  std::vector<RegisterDescription> getRegisterNames() const;

  NaviError readDebugEvents();

  unsigned int getAddressSize() const;

  DebuggerOptions getDebuggerOptions() const;

  unsigned int getBaseOfImage() const {
    return baseOfImage;
  }
  void setBaseOfImage(unsigned int baseOfImage) {
    this->baseOfImage = baseOfImage;
  }

};

#endif
