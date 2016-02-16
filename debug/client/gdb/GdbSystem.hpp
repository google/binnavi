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

#ifndef GDBSYSTEM_HPP
#define GDBSYSTEM_HPP

#include <boost/foreach.hpp>
#include <string>
#include <map>
#include <vector>

#include "../defs.hpp"
#include "../BaseSystem.hpp"
#include "cpus/GdbCpu.hpp"
#include "Transport.hpp"

struct DebuggerOptions;

/**
 * The GDB System class is a connection class for the debug client that
 * allows connections to a gdbserver through various transport modes.
 */
class GdbSystem : public BaseSystem, IEventCallback {
#ifdef UNITTESTING
public:
#else
 private:
#endif
  // Description of the target CPU the target program runs on.
  GdbCpu* cpu;

  // The active threads in the current debugging session.
  std::vector<Thread> knownThreads;

  unsigned int threadFromBreakpointMessage(const std::string& message);

  NaviError processBreakpointMessage(const std::string& msg);

  // Parses instruction pointer from a stop-reply packet
  NaviError getInstructionPointerFromStopMessage(const std::string& msg,
                                                 unsigned int index,
                                                 CPUADDRESS& address) const;

  void synchronizeThreadState();

#ifdef UNITTESTING
public:
#else
 protected:
#endif
  // Attaches to a running process
  NaviError attachToProcess();

  // We do not support starting a process via Gdb
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
  NaviError removeBreakpoint(const BREAKPOINT& bp, bool moreToCome = false);

  // Executes the next instruction in thread of the target process
  NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address);

  NaviError suspendThread(unsigned int tid);

  NaviError resumeProcess();

  // Resumes a thread in the target process
  NaviError resumeThread(unsigned int tid);

  // Halts the target process
  NaviError halt();

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

  NaviError writeMemory(CPUADDRESS address, const std::vector<char>& data);

  NaviError readProcessList(ProcessListContainer& processList);

  NaviError getFileSystems(std::vector<boost::filesystem::path>& roots) const;

  NaviError getSystemRoot(boost::filesystem::path& root) const;

  // Finds the memory region an offset belongs to
  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from, CPUADDRESS& to);

  // Find all allocated memory sections of the target process
  NaviError getMemmap(std::vector<CPUADDRESS>& addresses);

  // Handles a breakpoint hit
  NaviError handleBreakpointHit(CPUADDRESS address);

  DebugExceptionContainer getPlatformExceptions() const;

 public:

  /**
   * Creates a new GDB system object
   *
   * @param pid The process ID of the target process.
   */
  GdbSystem(unsigned int pid)
      : BaseSystem(pid) {
  }

  /**
   * Creates a new GDB System object
   *
   * @param path The path to the target process executable
   */
  GdbSystem(const char* path, const std::vector<const char*>& commands)
      : BaseSystem(path, commands) {
  }

  GdbCpu* getCpu() const {
    return cpu;
  }

  // Reads new debug events from the target process
  NaviError readDebugEvents();

  // Returns the register names that are available on the target architecture
  std::vector<RegisterDescription> getRegisterNames() const;

  // Returns the address size of the target architecture
  unsigned int getAddressSize() const;

  // Returns the debugger options that are supported by the debug client
  DebuggerOptions getDebuggerOptions() const;

  // Initializes the CPU-specific objects
  NaviError initTarget(const std::string& connection, const std::string& cpu);

  // Processes a stop-reply packet
  NaviError processMessage(const std::string& message);

};

#endif
