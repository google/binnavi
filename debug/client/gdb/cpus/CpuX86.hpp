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

#ifndef CPUX86_HPP
#define CPUX86_HPP

#include "GdbCpu.hpp"
#include "../Transport.hpp"
#include "../BreakpointHandler.hpp"
#include "../../DebuggerOptions.hpp"

#include <map>

/**
 * CPU description class for x86 CPUs.
 */
class CpuX86 : public GdbCpu, public BreakpointHandler {
 private:

  // Returns the opcode of a breakpoint interrupt
  std::vector<char> getBreakpointData() const;

 protected:

  // Returns the register index of the instruction pointer.
  unsigned int getInstructionPointerIndex() const;

  // Converts a register index between BinNavi and GDB server
  unsigned int naviIndexToGdbIndex(unsigned int index) const;

 public:
  /**
   * Creates a new X86 CPU object,
   *
   * @param transport The transport object used to communicate with the GDB
   * server.
   */
  CpuX86(Transport* transport)
      : GdbCpu(transport),
        BreakpointHandler(getBreakpointData()) {
  }

  // Returns the size of the addressable memory of the target memory.
  unsigned int getAddressSize() const;

  // We gotta limit this because of an overflow bug in the GDB server code; send
  // $m8048000,fff#2f to see it crash
  unsigned int getMaximumReadSize() const {
    return 1024;
  }

  // Returns the descriptions of the target platform registers.
  std::vector<RegisterDescription> getRegisterNames() const;

  NaviError parseRegistersString(std::vector<RegisterValue>& registers,
                                 const std::string& regString) const;

  // Returns the debugger options supported by the X86 GDB debugger
  DebuggerOptions getDebuggerOptions() const;

  // Converts a breakpoint exception address to a breakpoint instruction
  // address
  CPUADDRESS correctBreakpointAddress(CPUADDRESS address) const;

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

  // Sets a register value of a thread in the target process.
  NaviError setRegister(unsigned int tid, unsigned int index,
                        CPUADDRESS address, IEventCallback* cb) const;
};

#endif
