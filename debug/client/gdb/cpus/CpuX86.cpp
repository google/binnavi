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

#include "CpuX86.hpp"

#include <algorithm>
#include <cassert>

#include <zycon/src/zycon.h>

#include "../../defs.hpp"
#include "../../logger.hpp"
#include "../utils.hpp"

/**
 * Returns the breakpoint interrupt opcode of x86 CPUs.
 *
 * @return The breakpoint interrupt opcode of x86 CPUs.
 */
std::vector<char> CpuX86::getBreakpointData() const {
  // 0xCC = Breakpoint interrupt opcode
  return std::vector<char>(1, 0xCC);
}

/**
 * Returns the index of the EIP register in the GDB register array.
 *
 * @return The index of the EIP register in the GDB register array.
 */
unsigned int CpuX86::getInstructionPointerIndex() const {
  return 8;
}

/**
 * Maps between registers as displayed in BinNavi and registers as positioned
 * in GDB register strings.
 *
 * @param index The index of a register in BinNavi
 *
 * @return The index of the same register in the GDB server.
 */
unsigned int CpuX86::naviIndexToGdbIndex(unsigned int index) const {
  // GDB: EAX, ECX, EDX, EBX, ESP, EBP, ESI, EDI, EIP, EFLAGS
  // NAV: EAX, EBX, ECX, EDX, ESI, EDI, ESP, EBP, EIP, EFLAGS
  unsigned int indices[] = { 0, 2, 3, 1, 6, 7, 4, 5, 8 };

  assert(index < sizeof(indices) / sizeof(*indices));

  return indices[index];
}

/**
 * Returns the address size of the target architecture.
 *
 * @return The address size of the target architecture.
 */
unsigned int CpuX86::getAddressSize() const {
  // x86 is a 32bit architecture
  return 32;
}

/**
 * Returns descriptions of the registers that can be accessed through gdbserver.
 *
 * @param List of register descriptions.
 */
std::vector<RegisterDescription> CpuX86::getRegisterNames() const {
  std::vector < RegisterDescription > regNames;

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

  return regNames;
}

/**
 * Parses a GDB register string and fills the registers parameter with the
 * information.
 *
 * @param register Register container that is filled with the information from
 * the string.
 * @param regString The register string to parse.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError CpuX86::parseRegistersString(std::vector<RegisterValue>& registers,
                                       const std::string& regString) const {
  unsigned int registerValues[10];

  for (unsigned int i = 0; i < sizeof(registerValues) / sizeof(*registerValues);
      i++) {
    std::string val = flipBytesInString(regString.substr(i * 8, 8));
    registerValues[i] = strtoul(val.c_str(), 0, 16);
  }

  unsigned int eflags = registerValues[9];

  registers.push_back(
      makeRegisterValue("EAX", zylib::zycon::toHexString(registerValues[0])));
  registers.push_back(
      makeRegisterValue("EBX", zylib::zycon::toHexString(registerValues[3])));
  registers.push_back(
      makeRegisterValue("ECX", zylib::zycon::toHexString(registerValues[1])));
  registers.push_back(
      makeRegisterValue("EDX", zylib::zycon::toHexString(registerValues[2])));
  registers.push_back(
      makeRegisterValue("ESI", zylib::zycon::toHexString(registerValues[6])));
  registers.push_back(
      makeRegisterValue("EDI", zylib::zycon::toHexString(registerValues[7])));
  registers.push_back(
      makeRegisterValue("ESP", zylib::zycon::toHexString(registerValues[4])));
  registers.push_back(
      makeRegisterValue("EBP", zylib::zycon::toHexString(registerValues[5])));
  registers.push_back(
      makeRegisterValue("EIP", zylib::zycon::toHexString(registerValues[8]),
                        true));
  registers.push_back(
      makeRegisterValue("EFLAGS", zylib::zycon::toHexString(eflags)));
  registers.push_back(
      makeRegisterValue("CF", zylib::zycon::toHexString(eflags & 1)));
  registers.push_back(
      makeRegisterValue("PF", zylib::zycon::toHexString((eflags >> 2) & 1)));
  registers.push_back(
      makeRegisterValue("AF", zylib::zycon::toHexString((eflags >> 4) & 1)));
  registers.push_back(
      makeRegisterValue("ZF", zylib::zycon::toHexString((eflags >> 6) & 1)));
  registers.push_back(
      makeRegisterValue("SF", zylib::zycon::toHexString((eflags >> 7) & 1)));
  registers.push_back(
      makeRegisterValue("OF", zylib::zycon::toHexString((eflags >> 11) & 1)));

  return NaviErrors::SUCCESS;
}

/**
 * Returns information about the debugger options supported
 * by the Cisco 2600 debug client.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
DebuggerOptions CpuX86::getDebuggerOptions() const {
  DebuggerOptions options;

  // It's not possible to terminate the target process
  options.canTerminate = false;

  // The GDB server does not provide a memory map
  options.canMemmap = false;

  // It's not possible to find out whether a memory region
  // is valid because the GDB server too slow for that.
  options.canValidMemory = false;

  options.canHalt = true;

  options.haltBeforeCommunicating = true;

  options.hasStack = false;

  options.canBreakOnModuleLoad = options.canBreakOnModuleUnload = false;

  options.canTraceCount = false;

  return options;
}

/**
 * Corrects a breakpoint address.
 *
 * On some platforms, breakpoint exceptions have the address of the breakpoint
 * instruction. On others, breakpoint exceptions have the address of the
 * instruction
 * after the breakpoint instruction. This function standardizes this behaviour by
 * returning the address of the breakpoint instruction for a given breakpoint
 * exception address.
 *
 * @param The breakpoint exception address.
 *
 * @return The breakpoint instruction address.
 */
CPUADDRESS CpuX86::correctBreakpointAddress(CPUADDRESS address) const {
  // The exception address is the instruction address +
  // sizeof(breakpoint instruction) which is instruction
  // address + 1.

  return address - 1;
}

/**
 * Sets the value of a given register in the given thread of the target process.
 *
 * @param tid Thread ID of the target thread.
 * @param index Index of the register.
 * @param value New value of the register.
 * @param cb The callback object that handles incoming stop-reply messages.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError CpuX86::setRegister(unsigned int tid, unsigned int index,
                              CPUADDRESS value, IEventCallback* cb) const {


  if (index <= 9) {
    return GdbCpu::setRegister(tid, index, value, cb);
  } else {
    HANDLE_NAVI_ERROR(switchThread(tid, "g", cb),
                      "Error: Couldn't switch thread (Code %d)");

    invalidateCachedRegisterString();

    // To set the value of a register, it is necessary to read the values of all
    // registers, replace
    // the old register value with the new value in the register string and to
    // send the register string
    // back to the GDB server.

    // Read the old register values
    std::string regString;
    HANDLE_NAVI_ERROR(readRegisterString(tid, regString, cb),
                      "Error: Couldn't read registers (Code: %d)")

    std::vector < RegisterValue > container;
    parseRegistersString(container, regString);

    std::string eflagsString = flipBytesInString(regString.substr(9 * 8, 8));
    unsigned int eflags = strtoul(eflagsString.c_str(), 0, 16);

    switch (index) {
      case 10:
        eflags = (eflags & 0xFFFFFFFE) | (value & 1);
        break;
      case 11:
        eflags = (eflags & 0xFFFFFFFB) | ((value & 1) << 2);
        break;
      case 12:
        eflags = (eflags & 0xFFFFFFEF) | ((value & 1) << 4);
        break;
      case 13:
        eflags = (eflags & 0xFFFFFFBF) | ((value & 1) << 6);
        break;
      case 14:
        eflags = (eflags & 0xFFFFFF7F) | ((value & 1) << 7);
        break;
      case 15:
        eflags = (eflags & 0xFFFFF7FF) | ((value & 1) << 11);
        break;
    }

    // Create the Set Register GDB command and packetify it
    // TODO: This will stop working for non 32-bit registers
    std::string command = "G" + createRegisterString(regString, 9, value);
    std::string packet = packetify(command);

    // Send the Set Register command to the GDB server and wait for the ACK/NACK
    HANDLE_NAVI_ERROR(sendAndWaitForAck(packet, cb),
                      "Error: Couldn't send Set Register command to the GDB "
                      "server (Code %d)");

    // Wait for the message that contains the result of the memory write
    // operation.
    std::string message;
    HANDLE_NAVI_ERROR(
        waitForOKMessage(message, cb),
        "Error: Couldn't receive packet reply from GDB server (Code %d)");

    // Acknowledge the reception of the message.
    HANDLE_NAVI_ERROR(sendAck(),
                      "Error: Couldn't acknowledge message (Code %d)");

    // Handle the possible reply messages: OK, UNSUPPORTED, ERROR
    return handleStandardReply(message,
                               "Error: Set Register operation unsupported",
                               "Error: Couldn't set register value (Code %s)");
  }
}
