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

#ifndef CISCO3600_HPP
#define CISCO3600_HPP

#include "GdbCpu.hpp"
#include "../Transport.hpp"
#include "../BreakpointHandler.hpp"
#include "../../DebuggerOptions.hpp"

#include <map>

/**
 * CPU description class for MIPS CPUs.
 */
class MIPS : public GdbCpu, public BreakpointHandler {
 private:

  // Returns the opcode of a breakpoint interrupt
  std::vector<char> getBreakpointData() const;

 protected:

  // Maximum number of bytes that can be read from the target process memory in
  //one go
  unsigned int getMaximumReadSize() const {
    return 199 /* 200+ does not work */;
  }

  // Returns the register index of the instruction pointer.
  unsigned int getInstructionPointerIndex() const;

  std::string getGreetMessage() const;

  // Determines whether the target GDB server needs to be restarted sometimes
  bool needsRestarting() const {
    return !isSuspended();
  }

  // Returns the restart message of the target platform
  std::string getRestartMessage() const {
    return "\r\ngdb kernel\r\n\r\n";
  }

 public:
  /**
   * Creates a new MIPS CPU object,
   *
   * @param transport The transport object used to communicate with the GDB
   * server.
   */
  MIPS(Transport* transport)
      : GdbCpu(transport),
        BreakpointHandler(getBreakpointData()) {
  }

  bool hasRegularBreakpointMessage() const {
    return false;
  }

  // Returns the size of the addressable memory of the target memory.
  unsigned int getAddressSize() const;

  bool isBreakpointMessage(const std::string& msg) const;

  bool RunlengthDecode(std::string& encoded) const;

  // Returns the descriptions of the target platform registers.
  std::vector<RegisterDescription> getRegisterNames() const;

  NaviError parseRegistersString(std::vector<RegisterValue>& registers,
                                 const std::string& regString) const;

  // Returns the debugger options supported by the MIPS GDB debugger
  DebuggerOptions getDebuggerOptions() const;

  // We can thank C++ for the following function forwarders
  NaviError setBreakpoint(CPUADDRESS address, IEventCallback* cb) const {
    return BreakpointHandler::setBreakpoint(address, cb);
  }
  NaviError removeBreakpoint(CPUADDRESS address, IEventCallback* cb) {
    return BreakpointHandler::removeBreakpoint(address, cb);
  }
  NaviError storeOriginalData(CPUADDRESS address, IEventCallback* cb) {
    return BreakpointHandler::storeOriginalData(address, cb);
  }

  NaviError readMemoryData(char* buffer, CPUADDRESS from, CPUADDRESS to,
                           IEventCallback* cb) const {
    return GdbCpu::readMemoryData(buffer, from, to, cb);
  }
  NaviError writeMemoryData(const char* buffer, CPUADDRESS from,
                            unsigned int size, IEventCallback* cb) const {
    return GdbCpu::writeMemoryData(buffer, from, size, cb);
  }
};

#endif
