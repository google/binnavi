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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;

import com.google.security.zynamics.reil.OperandSize;

public class DumpPolicy implements IInterpreterPolicy {
  private BufferedWriter writer = null;

  public DumpPolicy(final String filename) throws IOException {
    writer = new BufferedWriter(new FileWriter(new File(filename)));
  }

  private void printRegisters(final ReilInterpreter interpreter, final ICpuPolicy cpuPolicy,
      final String[] registers, final String[] flags) throws IOException {
    for (final String register : registers) {
      final BigInteger value =
          interpreter.isDefined(register) ? interpreter.getVariableValue(register)
              : BigInteger.ZERO;

      final OperandSize registerSize = cpuPolicy.getRegisterSize(register);

      final String mask = "%0" + (2 * registerSize.getByteSize()) + "X";

      writer.write(String.format("%s: " + mask + " ", register.toUpperCase(), value));
    }

    long value = 0;
    int i = 0;

    for (final String flag : flags) {
      final int flagValue =
          interpreter.isDefined(flag) ? interpreter.getVariableValue(flag).intValue() : 0;

      value = value + (flagValue << (31 - i));
      i++;
    }

    writer.write(String.format("%s: %08X ", "FOO", value));
    writer.write("\n");
  }

  @Override
  public void end() {
    try {
      writer.close();
    } catch (final Exception e) {
      assert false;
    }
  }

  @Override
  public void nextInstruction(final ReilInterpreter interpreter) {
    final ICpuPolicy cpuPolicy = interpreter.getCpuPolicy();

    final String[] registers = cpuPolicy.getRegisters();
    final String[] flags = cpuPolicy.getFlags();

    try {
      printRegisters(interpreter, cpuPolicy, registers, flags);
    } catch (final IOException e) {
      System.out.println(e);
      assert false;
    }
  }

  @Override
  public void start() {
  }
}
