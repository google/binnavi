/*
Copyright 2015 Google Inc. All Rights Reserved.

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

public class CpuPolicyX64 implements ICpuPolicy {
  private static String[] registers =
      new String[] {"rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rip","r8","r9","r10","r11","r12","r13","r14","r15"};
  private static String[] flags = new String[] {"CF", "SF", "ZF", "OF"};

  @Override
  public String[] getFlags() {
    return flags.clone();
  }

  @Override
  public String getProgramCounter() {
    return "rip";
  }

  @Override
  public String[] getRegisters() {
    return registers.clone();
  }

  @Override
  public OperandSize getRegisterSize(final String value) {

    if (value.equals("al") || value.equals("ah") || value.equals("bl") || value.equals("bh")
        || value.equals("cl") || value.equals("ch") || value.equals("dl") || value.equals("dh")
        || value.equals("r8b") || value.equals("r9b") || value.equals("r10b") || value.equals("r11b") 
        || value.equals("r12b") || value.equals("r13b") || value.equals("r14b") || value.equals("r15b")) {
      return OperandSize.BYTE;
    } else if (value.equals("ax") || value.equals("bx") || value.equals("cx") || value.equals("dx")
        || value.equals("si") || value.equals("di") || value.equals("sp") || value.equals("bp")
        || value.equals("ip")
        || value.equals("r8w") || value.equals("r9w") || value.equals("r10w") || value.equals("r11w") 
        || value.equals("r12w") || value.equals("r13w") || value.equals("r14w") || value.equals("r15w")) {
      return OperandSize.WORD;
    } else if (value.equals("eax") || value.equals("ebx") || value.equals("ecx")
        || value.equals("edx") || value.equals("esi") || value.equals("edi") || value.equals("esp")
        || value.equals("ebp") || value.equals("eip")
        || value.equals("r8d") || value.equals("r9d") || value.equals("r10d") || value.equals("r11d") 
        || value.equals("r12d") || value.equals("r13d") || value.equals("r14d") || value.equals("r15d")) {
      return OperandSize.DWORD;
    } else if (value.equals("rax") || value.equals("rbx") || value.equals("rcx")
            || value.equals("rdx") || value.equals("rsi") || value.equals("rdi") || value.equals("rsp")
            || value.equals("rbp") || value.equals("rip")
            || value.equals("r8") || value.equals("r9") || value.equals("r10") || value.equals("r11") 
            || value.equals("r12") || value.equals("r13") || value.equals("r14") || value.equals("r15")) {
          return OperandSize.QWORD;
    }

    assert false;
    return null;

  }

  @Override
  public void start(final ReilInterpreter interpreter) {
  }

}
