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

#ifndef LINUXSYSTEM_HPP
#define LINUXSYSTEM_HPP

#include <map>
#include <string>

#include "../defs.hpp"
#include "../BaseSystem.hpp"

class DebuggerOptions;

/**
 * System policy for Linux systems.
 */
class LinuxSystem : public BaseSystem {
 private:

  // Original bytes that are replaced by breakpoints
  std::map<std::string, int> originalBytes;

  /**
   * Keeps track of the threads of the target process.
   */
  std::vector<Thread> tids;

  std::map<std::string, Module> modules;

  unsigned int lastMapFileSize;

  void processOtherEvents();

  typedef std::map<std::string, Module> ModulesMap;

  NaviError fillModules(pid_t pid, ModulesMap& modules);

  NaviError getExecutablePath(pid_t pid, std::string& path) const;

  NaviError resumeAfterStepping(unsigned int threadId, CPUADDRESS address);

  bool handleCloneEvent(pid_t pid) const;

 protected:

  // Attaches to a running process
  NaviError attachToProcess();

  // Starts a new process for debugging
  NaviError startProcess(const NATIVE_STRING path,
                         const std::vector<const NATIVE_STRING>& commands);

  // Detaches from the target process
  NaviError detach();

  // Terminates the target process
  NaviError terminateProcess();

  // Stores the original data that is replaced by a breakpoint
  NaviError storeOriginalData(const BREAKPOINT& bp);

  // Sets a breakpoint in the target process
  NaviError setBreakpoint(const BREAKPOINT& breakpoint,
                          bool moreToCome = false);

  // Removes a breakpoint from the target process
  NaviError removeBreakpoint(const BREAKPOINT &bp, bool moreToCome = false);

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

  // Reads the register values of a thread
  NaviError readRegisters(RegisterContainer& registers);

  // Sets the value of a register in the target process
  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS address);

  // Reads a part of the target process memory
  NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to);

  std::vector<char> readPointedMemory(CPUADDRESS address);

  NaviError writeMemory(CPUADDRESS address, const std::vector<char>& data);

  // Finds the memory region an offset belongs to
  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from, CPUADDRESS& to);

  // Find all allocated memory sections of the target process
  NaviError getMemmap(std::vector<CPUADDRESS>& addresses);

  // Halts the target process
  NaviError halt();

  NaviError readProcessList(ProcessListContainer& processList);

  DebugExceptionContainer getPlatformExceptions() const;

  NaviError getFileSystems(std::vector<boost::filesystem::path>& roots) const;

  NaviError getSystemRoot(boost::filesystem::path& root) const;

 public:
  LinuxSystem()
      : BaseSystem() {
  }

  /**
   * Creates a new LinuxSystem object to debug a process with a given PID.
   *
   * @param pid The process identifier of the target process
   */
  LinuxSystem(unsigned int pid)
      : BaseSystem(pid) {
  }

  /**
   * Creates a new LinuxSystem object to start a new target process.
   *
   * @param path The path to the target executable
   */
  LinuxSystem(const char* path,
              const std::vector<const NATIVE_STRING>& commands)
      : BaseSystem(path, commands) {
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
