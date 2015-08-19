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
import com.google.security.zynamics.reil.translators.ppc.CrandcTranslator;
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
public class CrandcTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final CrandcTranslator translator = new CrandcTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter.setRegister("CR0EQ", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0LT", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0GT", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0SO", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR1EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR1LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR1GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR1SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR2EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR2LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR2GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR2SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR3EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR3LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR3GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR3SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("CR4EQ", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR4LT", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR4GT", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR4SO", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR5EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR5LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR5GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR5SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR6EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR6LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR6GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR6SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter
        .setRegister("CR7EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR7LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR7GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR7SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
  }

  @Test
  public void testCR0Bits() throws InternalTranslationException, InterpreterException {
    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "byte");
    operandTree1.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("crandc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0GT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3SO"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4EQ"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4LT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4GT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7SO"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(33, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testCR4Bits() throws InternalTranslationException, InterpreterException {

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "byte");
    operandTree1.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "16"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "17"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "18"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("crandc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0GT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR0SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR1SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR2SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR3SO"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR4LT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4GT"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CR4SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR5SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR6SO"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7GT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR7SO"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(33, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
