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
package com.google.security.zynamics.reil.translators.x64;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyX64;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironmentx64;
import com.google.security.zynamics.reil.translators.x64.CmpsdTranslator;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class CmpsdTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironmentx64 environment = new StandardEnvironmentx64();

  private final CmpsdTranslator translator = new CmpsdTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testDFSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("DF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("rsi", BigInteger.valueOf(0x1000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", BigInteger.valueOf(0x2000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    interpreter.getMemory().store(0x1000, 0x12345678, 4);
    interpreter.getMemory().store(0x2000, 0x12345678, 4);

    final MockInstruction instruction =
        new MockInstruction("cmpsd", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.valueOf(0x0FFC), interpreter.getVariableValue("rsi"));
    assertEquals(BigInteger.valueOf(0x1FFC), interpreter.getVariableValue("rdi"));

    assertEquals(BigInteger.valueOf(8L), BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testEqualValues() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("DF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("rsi", BigInteger.valueOf(0x1000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", BigInteger.valueOf(0x2000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    interpreter.getMemory().store(0x1000, 0x12345678, 4);
    interpreter.getMemory().store(0x2000, 0x12345678, 4);

    final MockInstruction instruction =
        new MockInstruction("cmpsd", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.valueOf(0x1004), interpreter.getVariableValue("rsi"));
    assertEquals(BigInteger.valueOf(0x2004), interpreter.getVariableValue("rdi"));

    assertEquals(BigInteger.valueOf(8L), BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
