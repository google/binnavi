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

#include "Cisco3600.hpp"

#include <algorithm>
#include <cassert>

#include <zycon/src/zycon.h>

#include "../../defs.hpp"
#include "../../logger.hpp"
#include "../utils.hpp"

/**
 * Returns the breakpoint interrupt opcode of MIPS CPUs.
 *
 * @return The breakpoint interrupt opcode of MIPS CPUs.
 */
std::vector<char> MIPS::getBreakpointData() const {
  char buffer[4] = { 0x00, 0x00, 0x00, 0x0D };

  std::vector<char> data(buffer, buffer + 4);

  return data;
}

/**
 * Returns the index of the EIP register in the GDB register array.
 *
 * @return The index of the EIP register in the GDB register array.
 */
unsigned int MIPS::getInstructionPointerIndex() const {
  return 37;
}

/**
 * Returns the greet message of the MIPS GDB server.
 */
std::string MIPS::getGreetMessage() const {
  return "||||";
}

bool MIPS::isBreakpointMessage(const std::string& msg) const {
  // On MIPS, breakpoint messages equal greet messages.

  return msg == getGreetMessage();
}

///
/// \brief Cisco's version of Run-length encoding
///
/// Cisco uses two instead of one character as the repeat count. This overload
/// deals with the situation. You must use the GdbProtoCisco version when
/// dealing with devices from this vendor, since they make heavy use of the
/// encoding when sending memory or register contents.
///
bool MIPS::RunlengthDecode(std::string& encoded) const {
  std::string expanded;
  char to_repeat[2];
  char to_conv[3];
  unsigned int repeat;

  to_conv[2] = to_repeat[1] = 0;

  for (unsigned int i = 0; i < encoded.length(); i++) {
    if (encoded.at(i) == '*') {
      // "*7" is not allowed
      if (0 == i)
        return false;

      // "lalala*1" not allowed
      if ((encoded.length() - 2) == i)
        return false;

      to_repeat[0] = encoded.at(i - 1);

      // convert
      to_conv[0] = encoded.at(i + 1);
      to_conv[1] = encoded.at(i + 2);
      repeat = strtoul(to_conv, NULL, 16);

      // 0 repeat theoretically possible but very unlikely, generate error
      if (0 == repeat)
        return false;

      for (unsigned int j = 0; j < repeat; j++)
        expanded.append(to_repeat);

      // skip over multiplier
      i += 2;
    } else {
      to_repeat[0] = encoded.at(i);
      expanded.append(to_repeat);
    }
  }

  encoded.clear();
  encoded.append(expanded);

  return true;
}

/**
 * Returns the address size of the target architecture.
 *
 * @return The address size of the target architecture.
 */
unsigned int MIPS::getAddressSize() const {
  // MIPS is a 32bit architecture
  return 32;
}

/**
 * Returns descriptions of the registers that can be accessed through gdbserver.
 *
 * @param List of register descriptions.
 */
std::vector<RegisterDescription> MIPS::getRegisterNames() const {
  std::vector < RegisterDescription > regNames;

  RegisterDescription zr("zr", 4, true);
  RegisterDescription at("at", 4, true);
  RegisterDescription v0("v0", 4, true);
  RegisterDescription v1("v1", 4, true);
  RegisterDescription a0("a0", 4, true);
  RegisterDescription a1("a1", 4, true);
  RegisterDescription a2("a2", 4, true);
  RegisterDescription a3("a3", 4, true);
  RegisterDescription t0("t0", 4, false);
  RegisterDescription t1("t1", 4, true);
  RegisterDescription t2("t2", 4, true);
  RegisterDescription t3("t3", 4, true);
  RegisterDescription t4("t4", 4, true);
  RegisterDescription t5("t5", 4, true);
  RegisterDescription t6("t6", 4, true);
  RegisterDescription t7("t7", 4, true);
  RegisterDescription s0("s0", 4, true);
  RegisterDescription s1("s1", 4, true);
  RegisterDescription s2("s2", 4, true);
  RegisterDescription s3("s3", 4, true);
  RegisterDescription s4("s4", 4, true);
  RegisterDescription s5("s5", 4, true);
  RegisterDescription s6("s6", 4, true);
  RegisterDescription s7("s7", 4, true);
  RegisterDescription t8("t8", 4, true);
  RegisterDescription t9("t9", 4, true);
  RegisterDescription k0("k0", 4, true);
  RegisterDescription k1("k1", 4, true);
  RegisterDescription gp("gp", 4, true);
  RegisterDescription sp("sp", 4, true);
  RegisterDescription fp("fp", 4, true);
  RegisterDescription ra("ra", 4, true);
  RegisterDescription status("status", 4, true);
  RegisterDescription q1("lo", 4, true);
  RegisterDescription q2("hi", 4, true);
  RegisterDescription q3("??3", 4, true);
  RegisterDescription q4("??4", 4, true);
  RegisterDescription pc("pc", 4, true);

  regNames.push_back(zr);
  regNames.push_back(at);
  regNames.push_back(v0);
  regNames.push_back(v1);
  regNames.push_back(a0);
  regNames.push_back(a1);
  regNames.push_back(a2);
  regNames.push_back(a3);
  regNames.push_back(t0);
  regNames.push_back(t1);
  regNames.push_back(t2);
  regNames.push_back(t3);
  regNames.push_back(t4);
  regNames.push_back(t5);
  regNames.push_back(t6);
  regNames.push_back(t7);
  regNames.push_back(s0);
  regNames.push_back(s1);
  regNames.push_back(s2);
  regNames.push_back(s3);
  regNames.push_back(s4);
  regNames.push_back(s5);
  regNames.push_back(s6);
  regNames.push_back(s7);
  regNames.push_back(t8);
  regNames.push_back(t9);
  regNames.push_back(k0);
  regNames.push_back(k1);
  regNames.push_back(gp);
  regNames.push_back(sp);
  regNames.push_back(fp);
  regNames.push_back(ra);
  regNames.push_back(status);
  regNames.push_back(q1);
  regNames.push_back(q2);
  regNames.push_back(q3);
  regNames.push_back(q4);
  regNames.push_back(pc);

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
NaviError MIPS::parseRegistersString(std::vector<RegisterValue>& registers,
                                     const std::string& regString) const {
  registers.push_back(
      makeRegisterValue("zr", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("at", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("v0", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("v1", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("a0", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("a1", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("a2", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("a3", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t0", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t1", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t2", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t3", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t4", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t5", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t6", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t7", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s0", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s1", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s2", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s3", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s4", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s5", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s6", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("s7", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t8", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("t9", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("k0", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("k1", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("gp", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("sp", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("fp", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("ra", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("status", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("lo", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("hi", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("??3", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("??4", (regString.substr(registers.size() * 8, 8))));
  registers.push_back(
      makeRegisterValue("pc", (regString.substr(registers.size() * 8, 8)),
                        true));

  return NaviErrors::SUCCESS;
}

/**
 * Returns information about the debugger options supported
 * by the MIPS debug client.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
DebuggerOptions MIPS::getDebuggerOptions() const {
  DebuggerOptions options;

  // It's not possible to terminate the router
  options.canTerminate = false;

  // The router is single-threaded
  options.canMultithread = false;

  // The MIPS GDB server does not provide memory maps
  options.canMemmap = false;

  // It's not possible to find out whether a memory region
  // is valid because the serial connection is too slow for
  // that.
  options.canValidMemory = false;

  options.hasStack = false;

  options.pageSize = 4096;

  return options;
}
