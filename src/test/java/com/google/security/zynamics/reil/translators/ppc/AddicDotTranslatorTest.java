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
import com.google.security.zynamics.reil.translators.ppc.AddicDotTranslator;
import com.google.security.zynamics.reil.translators.ppc.Helpers;
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
public class AddicDotTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final AddicDotTranslator translator = new AddicDotTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter
        .setRegister("XERCA", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("XERSO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("XEROV", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR0EQ", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR0LT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR0GT", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter
        .setRegister("CR0SO", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
  }

  @Test
  public void testCarryPropagate() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r1", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "65535"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("addic.", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(2147483647L), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue(Helpers.XER_CARRY_BIT));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_EQUAL));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue(Helpers.CR0_GREATER_THEN));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_LESS_THEN));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CRO_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimple() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r1", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "32767"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("addic.", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(2147516415L), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("%r1"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_CARRY_BIT));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_EQUAL));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_GREATER_THEN));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue(Helpers.CR0_LESS_THEN));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CRO_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSingExtend() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r1", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "32767"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("addic.", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x00008000L), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_CARRY_BIT));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.XER_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_EQUAL));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue(Helpers.CR0_GREATER_THEN));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CR0_LESS_THEN));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue(Helpers.CRO_SUMMARY_OVERFLOW));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
