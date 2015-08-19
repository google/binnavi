/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.interpreter.CpuPolicyX86;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

@RunWith(JUnit4.class)
public class InterpreterTest {
  @Test
  public void testAdd() throws InterpreterException {
    final ReilInterpreter interpreter =
        new ReilInterpreter(Endianness.LITTLE_ENDIAN, new CpuPolicyX86(),
            new EmptyInterpreterPolicy());

    final HashMap<BigInteger, List<ReilInstruction>> instructions =
        new HashMap<BigInteger, List<ReilInstruction>>();

    instructions.put(BigInteger.ZERO, Lists.newArrayList(ReilHelpers.createAdd(0,
        OperandSize.DWORD, "2147483647", OperandSize.DWORD, "1", OperandSize.QWORD, "t0")));

    interpreter.interpret(instructions, BigInteger.ZERO);

    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("t0"));
  }

  @Test
  public void testAdd2() throws InterpreterException {
    final ReilInterpreter interpreter =
        new ReilInterpreter(Endianness.LITTLE_ENDIAN, new CpuPolicyX86(),
            new EmptyInterpreterPolicy());

    final HashMap<BigInteger, List<ReilInstruction>> instructions =
        new HashMap<BigInteger, List<ReilInstruction>>();

    instructions
        .put(BigInteger.ZERO, Lists.newArrayList(ReilHelpers.createAdd(0, OperandSize.DWORD,
            "2147483648", OperandSize.DWORD, "4294967295", OperandSize.QWORD, "t0")));

    interpreter.interpret(instructions, BigInteger.ZERO);

    assertEquals(BigInteger.valueOf(0x17FFFFFFFL), interpreter.getVariableValue("t0"));
  }
}
