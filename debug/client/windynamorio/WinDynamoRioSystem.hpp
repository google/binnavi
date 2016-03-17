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

#ifndef WINDYNAMORIO_HPP_
#define WINDYNAMORIO_HPP_

#include "../BaseSystem.hpp"
#include "../DebuggerOptions.hpp"

#include <algorithm>
#include <random>
#include <vector>
#include <Windows.h>

#include "drdebug.pb.h"

class WinDynamoRioSystem : public BaseSystem {
 public:
  WinDynamoRioSystem(const std::string& client_dll_path,
                     const std::string& drrun_path);
  // Creates a new WinDynamoRioSystem object
  // path: The path to the target process executable
  // cmd_line: command line arguments
  WinDynamoRioSystem(const std::string& client_dll_path,
                     const std::string& drrun_path, const NATIVE_STRING path,
                     const std::vector<const NATIVE_STRING>& cmd_line);

  ~WinDynamoRioSystem();

  // Finds out whether debug events occurred in the target process.
  NaviError readDebugEvents() override;

  // Returns a list of the names of the registers of the underlying platform.
  std::vector<RegisterDescription> getRegisterNames() const override;

  // Returns the maximum size of a memory address of the target machine.
  unsigned int getAddressSize() const override;

  // Returns the debugger options that are supported by the debug client.
  DebuggerOptions getDebuggerOptions() const override;

 protected:
  // Attaches to the target process.
  NaviError attachToProcess() override;

  // Starts a new process for debugging.
  NaviError startProcess(
      const NATIVE_STRING path,
      const std::vector<const NATIVE_STRING>& commands) override;

  // Detaches from the target process.
  NaviError detach() override;

  // Terminates the target process.
  NaviError terminateProcess() override;

  // Stores the original data that is replaced by a given breakpoint.
  NaviError storeOriginalData(const BREAKPOINT& bp) override;

  // Sets a breakpoint in the target process.
  NaviError setBreakpoint(const BREAKPOINT& breakpoint,
                          bool moreToCome = false) override;

  // Removes a breakpoint from the target process.
  NaviError removeBreakpoint(const BREAKPOINT& breakpoint,
                             bool moreToCome = false) override;

  // Executes a single instruction.
  NaviError doSingleStep(unsigned int& tid, CPUADDRESS& address) override;

  // Resumes the debugged process.
  NaviError resumeProcess() override;

  // Suspends the thread with the given thread ID.
  NaviError suspendThread(unsigned int tid) override;

  NaviError resumeThread(unsigned int tid) override;

  // Halts the target process.
  NaviError halt() override;

  // Retrieves the value of the instruction pointer in a given thread.
  NaviError getInstructionPointer(unsigned int tid, CPUADDRESS& addr) override;

  // Not available in current implementation. Does nothing.
  NaviError setInstructionPointer(unsigned int tid,
                                  CPUADDRESS address) override;

  // Fills a given register container structure with information about the
  // current values of the CPU registers.
  NaviError readRegisters(RegisterContainer& registers) override;

  // Updates the value of a given register in a given thread.
  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS value) override;

  // Given a start address, this function returns the first and last offset of
  // the memory region the start address belongs to.
  NaviError getValidMemory(CPUADDRESS start, CPUADDRESS& from,
                           CPUADDRESS& to) override;

  // Returns a list of all memory regions that are available in the target
  // process
  NaviError getMemmap(std::vector<CPUADDRESS>& addresses) override;

  // Fills a buffer with memory data from the current process.
  NaviError readMemoryData(char* buffer, CPUADDRESS address,
                           CPUADDRESS size) override;

  // Overwrites the target process memory at the given address with the given
  // data
  NaviError writeMemory(CPUADDRESS address,
                        const std::vector<char>& data) override;

  // Fills the process list argument with a list of currently running processes
  // on the target system.
  NaviError readProcessList(ProcessListContainer& processList) override;

  // Fill the given vector with the roots of all available file systems
  // e.g.drives on Windows).
  NaviError getFileSystems(
      std::vector<boost::filesystem::path>& roots) const override;

  // Retrieves the (system dependent) system root or system drive.
  NaviError getSystemRoot(boost::filesystem::path& root) const;

  DebugExceptionContainer getPlatformExceptions() const;

  NaviError SetExceptionAction(CPUADDRESS exc_code,
                               DebugExceptionHandlingAction action) override;

  NaviError echoBreakpointHit(const BREAKPOINT& bp, unsigned int tid,
                              bool correctPc, bool doResume) override;

 private:
  void initializeRegisterList();

  std::string client_dll_path_;
  std::string drrun_path_;
  std::string pipe_name_;
  HANDLE dr_pipe_;
  int client_process_id_;
  HANDLE client_process_;
  std::map<CPUADDRESS, Module> module_map_;
  // Register names available on current platform
  std::vector<RegisterDescription> register_names_;
  google::protobuf::RepeatedPtrField<security::drdebug::RegValue>
      registers_from_bphit_event;
  // All breakpoints with "moreToCome" flag set will be stacked here and send
  // later.
  std::vector<security::drdebug::Command> delayed_add_bp_commands_;

  std::string randomPipeName();
  bool sendCommandToDr(const security::drdebug::Command& command,
                           security::drdebug::Response* out_response);
  bool sendCommandsToDr(
      const std::vector<security::drdebug::Command>& commands,
      std::vector<security::drdebug::Response>* responses);
  bool pingDynamoDll();

  // Converts the internal representation of an exception action to a protobuf
  // enum.
  security::drdebug::ExceptionAction
  DebugExceptionHandlingActionToExceptionAction(
      DebugExceptionHandlingAction action);

  // Converts a protobuf enum to the internal representation of an exception
  // action.
  DebugExceptionHandlingAction ExceptionActionToDebugExceptionHandlingAction(
    security::drdebug::ExceptionAction action);
};

#endif  // WINDYNAMORIO_HPP_
