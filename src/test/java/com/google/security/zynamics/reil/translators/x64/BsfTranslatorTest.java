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

import com.google.common.collect.Lists;
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
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.x64.BsfTranslator;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class BsfTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final BsfTranslator translator = new BsfTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testFirst() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(0x0000000000000001L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rbx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("bsf", operands);

    translator.translate(environment, instruction, instructions);

    for (final ReilInstruction mockOperandTree : instructions) {
      System.out.println(mockOperandTree);
    }

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.valueOf(0x00000001L), interpreter.getVariableValue("rbx"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testInputZero() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(0x00000000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rbx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("bsf", operands);

    translator.translate(environment, instruction, instructions);

    for (final ReilInstruction mockOperandTree : instructions) {
      System.out.println(mockOperandTree);
    }

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("rbx"));
    assertEquals(BigInteger.valueOf(0x00000001L), interpreter.getVariableValue("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testLast() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", TranslationHelpers.getUnsignedBigIntegerValue(0x8000000000000000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rbx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("bsf", operands);

    translator.translate(environment, instruction, instructions);

    for (final ReilInstruction mockOperandTree : instructions) {
      System.out.println(mockOperandTree);
    }

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(63), interpreter.getVariableValue("rax"));
    assertEquals(TranslationHelpers.getUnsignedBigIntegerValue(0x8000000000000000L), interpreter.getVariableValue("rbx"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testSecond() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(0x00000002L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rbx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("bsf", operands);

    translator.translate(environment, instruction, instructions);

    for (final ReilInstruction mockOperandTree : instructions) {
      System.out.println(mockOperandTree);
    }

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(1), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.valueOf(0x00000002L), interpreter.getVariableValue("rbx"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
