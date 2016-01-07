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

public class CpuPolicyPPC implements ICpuPolicy {
  private static String[] registers = new String[] {"%r0", "%r1", "%r2", "%r3", "%r4", "%r5",
      "%r6", "%r7", "%r8", "%r9", "%r10", "%r11", "%r12", "%r13", "%r14", "%r15", "%r16", "%r17",
      "%r18", "%r19", "%r20", "%r21", "%r22", "%r23", "%r24", "%r25", "%r26", "%r27", "%r28",
      "%r29", "%r30", "%r31", "CR0", "CR1", "CR2", "CR3", "CR4", "CR5", "CR6", "CR7", "MSR", "XER",
      "LR", "CTR", "sp", "rtoc", "XERBC", "TBL", "TBU"};
  /*
   * Note about the CR Register:
   * 
   * General structure = [ CR0 | CR1 | CR2 | CR3 | CR4 | CR5 | CR6 | CR6 | CR7 ] each CRi = [ LT |
   * GT | EQ | SO ]
   * 
   * Should think about the way i defined the flags maybe its more useful to use the registers not
   * as flags
   */

  private static String[] flags = new String[] {"CR0LT", "CR0GT", "CR0EQ", "CR0SO", "CR1LT",
      "CR1GT", "CR1EQ", "CR1SO", "CR2LT", "CR2GT", "CR2EQ", "CR2SO", "CR3LT", "CR3GT", "CR3EQ",
      "CR3SO", "CR4LT", "CR4GT", "CR4EQ", "CR4SO", "CR5LT", "CR5GT", "CR5EQ", "CR5SO", "CR6LT",
      "CR6GT", "CR6EQ", "CR6SO", "CR7LT", "CR7GT", "CR7EQ", "CR7SO", "XERSO", "XEROV", "XERCA"};

  @Override
  public String[] getFlags() {
    return flags.clone();
  }

  @Override
  public String getProgramCounter() {
    return "pc";
  }

  @Override
  public String[] getRegisters() {
    return registers.clone();
  }

  @Override
  public OperandSize getRegisterSize(final String value) {
    return OperandSize.DWORD;
  }

  @Override
  public void start(final ReilInterpreter interpreter) {
  }

}
