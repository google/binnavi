/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.reil.interpreter;

import com.google.security.zynamics.reil.OperandSize;

public class CpuPolicyX86 implements ICpuPolicy {
  private static String[] registers =
      new String[] {"eax", "ebx", "ecx", "edx", "esi", "edi", "eip"};
  private static String[] flags = new String[] {"CF", "SF", "ZF", "OF"};

  @Override
  public String[] getFlags() {
    return flags.clone();
  }

  @Override
  public String getProgramCounter() {
    return "eip";
  }

  @Override
  public String[] getRegisters() {
    return registers.clone();
  }

  @Override
  public OperandSize getRegisterSize(final String value) {

    if (value.equals("al") || value.equals("ah") || value.equals("bl") || value.equals("bh")
        || value.equals("cl") || value.equals("ch") || value.equals("dl") || value.equals("dh")) {
      return OperandSize.BYTE;
    } else if (value.equals("ax") || value.equals("bx") || value.equals("cx") || value.equals("dx")
        || value.equals("si") || value.equals("di") || value.equals("sp") || value.equals("bp")
        || value.equals("ip")) {
      return OperandSize.WORD;
    } else if (value.equals("eax") || value.equals("ebx") || value.equals("ecx")
        || value.equals("edx") || value.equals("esi") || value.equals("edi") || value.equals("esp")
        || value.equals("ebp") || value.equals("eip")) {
      return OperandSize.DWORD;
    }

    assert false;
    return null;

  }

  @Override
  public void start(final ReilInterpreter interpreter) {
  }

}
