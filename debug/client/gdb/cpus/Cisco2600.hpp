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

#ifndef CISCO2600_HPP
#define CISCO2600_HPP

#include "PowerPC.hpp"
#include "../Transport.hpp"
#include "../BreakpointHandler.hpp"

#include <vector>

/**
 * CPU description class for the Cisco 2600
 */
class Cisco2600 : public PowerPC, public BreakpointHandler {
#ifdef UNITTESTING
public:
#else
 protected:
#endif
  // Maximum number of bytes that can be read from the target process memory in
  //one go
  unsigned int getMaximumReadSize() const {
    return 0x100;
  }

  // Returns the greet message of the CISCO GDB server
  std::string getGreetMessage() const;

  // Determines whether the target GDB server needs to be restarted sometimes
  bool needsRestarting() const {
    return !isSuspended();
  }

  // Returns the restart message of the target platform
  std::string getRestartMessage() const {
    return "\r\ngdb kernel\r\n\r\n";
  }

  // Returns the index of the PC register
  unsigned int getInstructionPointerIndex() const;

 public:
  /**
   * Creates a new Cisco2600 object.
   *
   * @param transport The transport object used to communicate with the GDB
   * server.
   */
  Cisco2600(Transport* transport)
      : PowerPC(transport),
        BreakpointHandler(getBreakpointData()) {
  }

  bool hasRegularBreakpointMessage() const {
    return false;
  }

  // Returns the address size of the Cisco 2600
  unsigned int getAddressSize() const;

  // Determines whether a string is a Cisco 2600 GDB Breakpoint message
  bool isBreakpointMessage(const std::string& msg) const;

  // Returns the descriptions of the Cisco 2600 registers.
  std::vector<RegisterDescription> getRegisterNames() const;

  NaviError parseRegistersString(std::vector<RegisterValue>& registers,
                                 const std::string& regString) const;

  // Decodes GDB messages using Cisco's own algorithm
  bool RunlengthDecode(std::string& encoded) const;

  // Detaches from the target process
  NaviError detach(IEventCallback* cb) const;

  // Returns the debugger options supported by the Cisco 2600 debug client
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

  std::vector<char> getBreakpointData() const {
    return PowerPC::getBreakpointData();
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
