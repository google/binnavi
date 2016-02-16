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

#include "ARMLittleEndian.hpp"

#include <algorithm>
#include <cassert>

#include <zycon/src/zycon.h>

#include "../../defs.hpp"
#include "../../logger.hpp"
#include "../utils.hpp"

std::vector<char> ARMLittleEndian::getBreakpointData() const {
  // ARM does not only have ARM software breakpoints but also THUMB and THUMB-2
  // software breakpoints
  // this must be handled ones this code works.
  // But why do we have to handle this ourselves anyways.

  char buffer[4] = { char(0x01), char(0x00), char(0x9f), char(0xef) };

  std::vector<char> data(buffer, buffer + 4);

  return data;
}

bool ARMLittleEndian::isLittleEndian() const {
  return true;
}

unsigned int ARMLittleEndian::getInstructionPointerIndex() const {
  return 15;
}

unsigned int ARMLittleEndian::getAddressSize() const {
  return 32;
}

std::vector<RegisterDescription> ARMLittleEndian::getRegisterNames() const {
  std::vector < RegisterDescription > regNames;

  RegisterDescription r0("R0", 4, true);
  RegisterDescription r1("R1", 4, true);
  RegisterDescription r2("R2", 4, true);
  RegisterDescription r3("R3", 4, true);
  RegisterDescription r4("R4", 4, true);
  RegisterDescription r5("R5", 4, true);
  RegisterDescription r6("R6", 4, true);
  RegisterDescription r7("R7", 4, true);
  RegisterDescription r8("R8", 4, true);
  RegisterDescription r9("R9", 4, true);
  RegisterDescription r10("R10", 4, true);
  RegisterDescription r11("R11", 4, true);
  RegisterDescription r12("R12", 4, true);
  RegisterDescription SP("SP", 4, true);
  RegisterDescription LR("LR", 4, true);
  RegisterDescription PC("PC", 4, true);

  RegisterDescription PSR("PSR", 4, true);

  RegisterDescription mode("MODE", 1, true);

  RegisterDescription tFlag("T", 0, true);
  RegisterDescription fFlag("F", 0, true);
  RegisterDescription iFlag("I", 0, true);
  RegisterDescription aFlag("A", 0, true);
  RegisterDescription eFlag("E", 0, true);
  RegisterDescription itFlag("IT", 0, true);
  RegisterDescription geFlag("GE", 0, true);
  RegisterDescription jFlag("J", 0, true);
  RegisterDescription qFlag("Q", 0, true);
  RegisterDescription vFlag("V", 0, true);
  RegisterDescription cFlag("C", 0, true);
  RegisterDescription zFlag("Z", 0, true);
  RegisterDescription nFlag("N", 0, true);

  regNames.push_back(r0);
  regNames.push_back(r1);
  regNames.push_back(r2);
  regNames.push_back(r3);
  regNames.push_back(r4);
  regNames.push_back(r5);
  regNames.push_back(r6);
  regNames.push_back(r7);
  regNames.push_back(r8);
  regNames.push_back(r9);
  regNames.push_back(r10);
  regNames.push_back(r11);
  regNames.push_back(r12);
  regNames.push_back(SP);
  regNames.push_back(LR);
  regNames.push_back(PC);

  return regNames;
}

NaviError ARMLittleEndian::parseRegistersString(
    std::vector<RegisterValue>& registers, const std::string& regString) const {
  unsigned int registerValues[16];

  for (unsigned int i = 0; i < sizeof(registerValues) / sizeof(*registerValues);
      i++) {
    std::string val = flipBytesInString(regString.substr(i * 8, 8));
    registerValues[i] = strtoul(val.c_str(), 0, 16);
  }

  registers.push_back(
      makeRegisterValue("R0", zylib::zycon::toHexString(registerValues[0]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R1", zylib::zycon::toHexString(registerValues[3]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R2", zylib::zycon::toHexString(registerValues[1]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R3", zylib::zycon::toHexString(registerValues[2]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R4", zylib::zycon::toHexString(registerValues[6]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R5", zylib::zycon::toHexString(registerValues[7]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R6", zylib::zycon::toHexString(registerValues[4]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R7", zylib::zycon::toHexString(registerValues[5]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R8", zylib::zycon::toHexString(registerValues[8]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R9", zylib::zycon::toHexString(registerValues[9]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R10", zylib::zycon::toHexString(registerValues[10]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R11", zylib::zycon::toHexString(registerValues[11]),
                        false, false));
  registers.push_back(
      makeRegisterValue("R12", zylib::zycon::toHexString(registerValues[12]),
                        false, false));
  registers.push_back(
      makeRegisterValue("SP", zylib::zycon::toHexString(registerValues[13]),
                        false, true));
  registers.push_back(
      makeRegisterValue("LR", zylib::zycon::toHexString(registerValues[14]),
                        false, false));
  registers.push_back(
      makeRegisterValue("PC", zylib::zycon::toHexString(registerValues[15]),
                        true, false));

  return NaviErrors::SUCCESS;
}

DebuggerOptions ARMLittleEndian::getDebuggerOptions() const {
  DebuggerOptions options;
  // currently must be false as the gdb agent implementation does
  // not handle memory mapping over the gdb protocol.
  options.canMemmap = false;
  // here we selected no multi threading as the code path for multi
  // threading seems seriously broken.
  options.canMultithread = false;
  // for linux clients something like options.canMultiProcess would really be a
  // cool thing to have.
  // looked this up for ARM it is just a reasonable default ...
  options.pageSize = 4096;
  options.hasStack = true;
  options.canHalt = false;
  options.breakpointCount = -1;
  options.canTraceCount = false;

  return options;
}

