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

#ifndef POWERPC603E_HPP
#define POWERPC603E_HPP

#include "GdbCpu.hpp"
#include "../Transport.hpp"
#include "../../DebuggerOptions.hpp"

#include <map>

/**
 * CPU description class for PowerPC 603e CPUs.
 */
class PPC603e : public GdbCpu {
 private:

  // Returns the opcode of a breakpoint interrupt
  std::vector<char> getBreakpointData() const;

 protected:

  // Returns the register index of the instruction pointer.
  unsigned int getInstructionPointerIndex() const;

 public:
  /**
   * Creates a new PPC 603e CPU object,
   *
   * @param transport The transport object used to communicate with the GDB
   server.
   */
  PPC603e(Transport* transport)
      : GdbCpu(transport) {
  }

  // Returns the size of the addressable memory of the target memory.
  unsigned int getAddressSize() const;

  // Returns the descriptions of the target platform registers.
  std::vector<RegisterDescription> getRegisterNames() const;

  // Sets a breakpoint in the target process.
  NaviError setBreakpoint(CPUADDRESS address, IEventCallback* cb) const;

  // Removes a breakpoint from the target process.
  NaviError removeBreakpoint(CPUADDRESS address, IEventCallback* cb);

  NaviError storeOriginalData(CPUADDRESS address, IEventCallback* cb);

  NaviError parseRegistersString(std::vector<RegisterValue>& registers,
                                 const std::string& regString) const;

  // Returns the debugger options supported by the PPC603e GDB debugger
  DebuggerOptions getDebuggerOptions() const;

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
