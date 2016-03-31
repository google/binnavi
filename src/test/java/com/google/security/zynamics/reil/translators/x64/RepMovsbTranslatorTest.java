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
import com.google.security.zynamics.reil.translators.x64.MovsGenerator;
import com.google.security.zynamics.reil.translators.x64.RepTranslator;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class RepMovsbTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironmentx64 environment = new StandardEnvironmentx64();

  private final RepTranslator translator = new RepTranslator(new MovsGenerator(), OperandSize.BYTE);

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testDFCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsi", BigInteger.valueOf(0x1000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", BigInteger.valueOf(0x2000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(0x3), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("DF", BigInteger.valueOf(0), OperandSize.BYTE,
        ReilRegisterStatus.DEFINED);

    interpreter.getMemory().store(0x1000, 0x98765432, 4);

    final MockInstruction instruction =
        new MockInstruction("rep lodsb", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x12345678), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.valueOf(0x1003), interpreter.getVariableValue("rsi"));
    assertEquals(BigInteger.valueOf(0x2003), interpreter.getVariableValue("rdi"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("rcx"));

    assertEquals(BigInteger.valueOf(7L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(BigInteger.valueOf(0x765432l), interpreter.getMemory().load(0x2000, 3));
  }

  @Test
  public void testDFSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsi", BigInteger.valueOf(0x1003), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", BigInteger.valueOf(0x2003), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(0x3), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("DF", BigInteger.valueOf(1), OperandSize.BYTE,
        ReilRegisterStatus.DEFINED);

    interpreter.getMemory().store(0x1000, 0x98765432, 4);

    final MockInstruction instruction =
        new MockInstruction("rep lodsb", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    System.out.println(instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x12345678), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.valueOf(0x1000), interpreter.getVariableValue("rsi"));
    assertEquals(BigInteger.valueOf(0x2000), interpreter.getVariableValue("rdi"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("rcx"));

    assertEquals(BigInteger.valueOf(7L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(BigInteger.valueOf(0x987654l), interpreter.getMemory().load(0x2001, 3));
  }
}
