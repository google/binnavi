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
import com.google.security.zynamics.reil.translators.x64.LeaTranslator;
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
public class LeaTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final LeaTranslator translator = new LeaTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testR16R16() throws InternalTranslationException, InterpreterException {
    // Operand size == 16 and memory size == 16

    // lea ax, word [bx + 4 * cx]

    interpreter.setRegister("rax", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(7000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(15000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "bx"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "*"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "4"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "cx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("lea", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x123405B8), interpreter.getVariableValue("rax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testR16R32() throws InternalTranslationException, InterpreterException {
    // Operand size == 16 and memory size == 16

    // lea ax, word [ebx + 4 * ecx]

    interpreter.setRegister("rax", BigInteger.valueOf(0x12340000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(7000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(15000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "ebx"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "*"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "4"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "ecx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("lea", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0x123405B8), interpreter.getVariableValue("rax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testR32R32() throws InternalTranslationException, InterpreterException {
    // Operand size == 16 and memory size == 16

    // lea eax, dword [ebx + 4 * ecx]

    interpreter.setRegister("rax", BigInteger.valueOf(0x12340000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(7000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(15000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "ebx"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "*"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "4"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "ecx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("lea", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(67000), interpreter.getVariableValue("rax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
  
  @Test
  public void testR64R64() throws InternalTranslationException, InterpreterException {
    // Operand size == 16 and memory size == 16

    // lea rax, qword [rbx + 4 * rcx]

    interpreter.setRegister("rax", BigInteger.valueOf(0x12340000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", BigInteger.valueOf(7000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", BigInteger.valueOf(111115000L), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "rbx"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "*"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "4"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "rcx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("lea", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(444467000), interpreter.getVariableValue("rax"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
