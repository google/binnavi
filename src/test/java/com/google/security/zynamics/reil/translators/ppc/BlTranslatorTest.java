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
package com.google.security.zynamics.reil.translators.ppc;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyPPC;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.ppc.BlTranslator;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class BlTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final BlTranslator translator = new BlTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter.setRegister("XEROV", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERSO", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERCA", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0EQ", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0GT", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0LT", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0SO", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("LR", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
  }

  @Test
  public void testOverflow() throws InternalTranslationException, InterpreterException {

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        String.valueOf(0xFFFFL)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("bl", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0xFFFFL), interpreter.getVariableValue("pc"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0SO"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XEROV"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XERSO"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XERCA"));
    assertEquals(BigInteger.valueOf(0x104), interpreter.getVariableValue("lr"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
