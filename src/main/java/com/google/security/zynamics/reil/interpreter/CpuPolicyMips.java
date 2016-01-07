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

public class CpuPolicyMips implements ICpuPolicy {
  private static String[] registers = new String[] {"$pc", "Status", "EPC", "HI", "Cause", "LO",
      "BadVAddr", "$zero", "$r0", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3", "$t0", "$t1",
      "$t2", "$t3", "$t4", "$t6", "$t7", "$s0", "$s1", "$s2", "$s3", "$s4", "$s6", "$s7", "$t8",
      "$t9", "$k0", "$k1", "$gp", "$s8", "$ra", "$sp", "FIR", "FCSR", "FCCR", "FEXR", "FENR",
      "FP0", "FP1", "FP2", "FP3", "FP4", "FP5", "FP6", "FP7", "FP8", "FP9", "FP10", "FP11", "FP12",
      "FP13", "FP14", "FP15", "FP16", "FP17", "FP18", "FP19", "FP20", "FP21", "FP22", "FP23",
      "FP24", "FP25", "FP26", "FP27", "FP28", "FP29", "FP30", "FP31", "T"};

  private static String[] flags = new String[] {};

  @Override
  public String[] getFlags() {
    return flags.clone();
  }

  @Override
  public String getProgramCounter() {
    return "$pc";
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
    return "Ssp";
  }

  public String getTRegister() {
    return "$t8";
  }

  @Override
  public void start(final ReilInterpreter interpreter) {
  }
}
