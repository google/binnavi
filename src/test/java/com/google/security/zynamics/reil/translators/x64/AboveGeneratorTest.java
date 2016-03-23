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
import com.google.security.zynamics.reil.translators.x64.AboveGenerator;
import com.google.security.zynamics.zylib.general.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public class AboveGeneratorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironmentx64 environment = new StandardEnvironmentx64();

  private final AboveGenerator generator = new AboveGenerator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testFailCondition1() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    final Pair<OperandSize, String> result = generator.generate(environment, 0x100, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(1));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(result.second()));
  }

  @Test
  public void testFailCondition2() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    final Pair<OperandSize, String> result = generator.generate(environment, 0x100, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(1));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(result.second()));
  }

  @Test
  public void testFailCondition3() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    final Pair<OperandSize, String> result = generator.generate(environment, 0x100, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(1));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(result.second()));
  }

  @Test
  public void testHitCondition() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    final Pair<OperandSize, String> result = generator.generate(environment, 0x100, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(1));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue(result.second()));
  }
}
