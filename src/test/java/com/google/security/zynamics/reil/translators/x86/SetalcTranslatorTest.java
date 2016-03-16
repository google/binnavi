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
package com.google.security.zynamics.reil.translators.x86;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyX86;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.x86.SetalcTranslator;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class SetalcTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX86(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final SetalcTranslator translator = new SetalcTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testCFCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.valueOf(0), OperandSize.BYTE,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0x80808080L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("setalc", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x80808000L), interpreter.getVariableValue("eax"));
  }

  @Test
  public void testCFSet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.valueOf(1), OperandSize.BYTE,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(0x80808080L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockInstruction instruction =
        new MockInstruction("setalc", new ArrayList<MockOperandTree>());

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x808080FFL), interpreter.getVariableValue("eax"));
  }
}
