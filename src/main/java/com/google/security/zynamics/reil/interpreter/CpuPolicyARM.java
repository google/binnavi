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

public class CpuPolicyARM implements ICpuPolicy {
  private static String[] registers = new String[] {"R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7",
      "R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "SP", "PC", "LR", "CPSR", "SPSR",
      "CPSR_GE", "CPSR_M"};

  // [ N | Z | C | V | Q | resserved[2] | J | reserved[4] | GE[4] | reserved[6] | E | A | I | F | T
  // | M[5] ]


  private static String[] flags = new String[] {"N", // negative
      "Z", // zero
      "C", /*
            * carry A carry occurs if the result of an addition is greater than or equal to 2^32, if
            * the result of a subtraction is positive, or as the result of an inline barrel shifter
            * operation in a move or logical instruction.
            */
      "V", /*
            * overflow Overflow occurs if the result of an add, subtract, or compare is greater than
            * or equal to 2^31, or less than minus 2^31.
            */
      "Q", // sticky
      "E", // Endianess
      "T", "J",
      /*
       * the t and j flag describe the current instruction set used by the processor
       * 
       * t | j | instruction set 0 | 0 | ARM (32) 1 | 0 | THUMB 0 | 1 | Jazelle 1 | 1 | Reserved
       */
      "A", "I", "F", "CPSR_GE_0", "CPSR_GE_1", "CPSR_GE_2", "CPSR_GE_3"


  };

  @Override
  public String[] getFlags() {
    return flags.clone();
  }

  public String getLinkRegister() {
    return "LR";
  }

  @Override
  public String getProgramCounter() {
    return "PC";
  }

  @Override
  public String[] getRegisters() {
    return registers.clone();
  }

  @Override
  public OperandSize getRegisterSize(final String value) {
    return OperandSize.DWORD;
  }

  public String getStackPointer() {
    return "SP";
  }

  @Override
  public void start(final ReilInterpreter interpreter) {
  }

}
