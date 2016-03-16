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

import com.google.common.collect.Lists;
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
import com.google.security.zynamics.reil.translators.x86.Helpers;
import com.google.security.zynamics.reil.translators.x86.ShldTranslator;
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
public class ShldTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX86(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ShldTranslator translator = new ShldTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testNull() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("eax", BigInteger.valueOf(0x2000), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ebx", BigInteger.valueOf(0xFFFF), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "ebx"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("shld", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    System.out.println(instructions);

    long counter = 0x10000;

    for (final ReilInstruction inst : instructions) {
      assertEquals(counter, inst.getAddress().toLong());

      counter++;
    }

    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testOne() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("eax", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ebx", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "ebx"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("shld", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    long counter = 0x10000;

    for (final ReilInstruction inst : instructions) {
      assertEquals(counter, inst.getAddress().toLong());

      counter++;
    }

    assertEquals(7, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(1L), interpreter.getVariableValue("eax"));
    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("ebx"));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.ZERO_FLAG));
    assertEquals(BigInteger.valueOf(1L), interpreter.getVariableValue(Helpers.OVERFLOW_FLAG));
    assertEquals(BigInteger.valueOf(1L), interpreter.getVariableValue(Helpers.CARRY_FLAG));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.SIGN_FLAG));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testProper() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("eax", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ebx", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "ebx"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("shld", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    System.out.println(instructions);

    long counter = 0x10000;

    for (final ReilInstruction inst : instructions) {
      assertEquals(counter, inst.getAddress().toLong());

      counter++;
    }

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(2L), interpreter.getVariableValue("eax"));
    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("ebx"));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.ZERO_FLAG));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.CARRY_FLAG));
    assertEquals(BigInteger.valueOf(0), interpreter.getVariableValue(Helpers.SIGN_FLAG));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testProperMemory() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("ebx", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setMemory(100, 0xF0, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree1.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "100"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "ebx"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("shld", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    System.out.println(instructions);

    long counter = 0x10000;

    for (final ReilInstruction inst : instructions) {
      assertEquals(counter, inst.getAddress().toLong());

      counter++;
    }

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(0x3C2, interpreter.getMemory().load(100, 4));
    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("ebx"));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.ZERO_FLAG));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.CARRY_FLAG));
    assertEquals(BigInteger.valueOf(0L), interpreter.getVariableValue(Helpers.SIGN_FLAG));

    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testTooLarge() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("eax", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ebx", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "bx"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "17"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("shld", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    System.out.println(instructions);

    long counter = 0x10000;

    for (final ReilInstruction inst : instructions) {
      assertEquals(counter, inst.getAddress().toLong());

      counter++;
    }

    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("ebx"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
