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
package com.google.security.zynamics.reil.translators.arm;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyARM;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.arm.ARMLdrbTranslator;
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
public class ARMLdrbTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyARM(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ARMLdrbTranslator translator = new ARMLdrbTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  final OperandSize dw = OperandSize.DWORD;
  final OperandSize wd = OperandSize.WORD;
  final OperandSize bt = OperandSize.BYTE;


  @Before
  public void setUp() {
  }

  @Test
  public void testLdrAPostIndexedImmediate() throws InternalTranslationException,
      InterpreterException {
    // LDRB r2,[r1],#1

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8124L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x817BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x8124L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x96F0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0xFFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x8124L, 0x73726946L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, String.valueOf(1)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x00008125L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00008124L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000096F0L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrBPostIndexedRegister() throws InternalTranslationException,
      InterpreterException {
    // LDRB r4,[r1],r3

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8125L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x46L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x8124L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x96F0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0xFFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x8124L, 0x73726946L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R4"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x0045AB27L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000096F0L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrCPostIndexedLSL() throws InternalTranslationException, InterpreterException {
    // LDRB r5,[r1],r3, LSL #2

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x0045AB27L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000096F0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x0045AB27L, 0xFF0010E8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R5"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "LSL"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x015A532FL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrDPostIndexedLSR() throws InternalTranslationException, InterpreterException {
    // LDRB r6,[r1],r3, LSR #2

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x015A532FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x015A532FL, 0xFF0010E8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R6"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "LSR"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x016B9DAFL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrEPostIndexedASR() throws InternalTranslationException, InterpreterException {
    // LDRB r7,[r1],r3, ASR #2

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x016B9DAFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x016B9DAFL, 0xFF0010E8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R7"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "ASR"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x017CE82FL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrFPostIndexedROR() throws InternalTranslationException, InterpreterException {
    // LDRB r8,[r1],r3, ROR #2

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x017CE82FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x017CE82FL, 0xFF0010E8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R8"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "ROR"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x818E32AFL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrGPostIndexedRRX() throws InternalTranslationException, InterpreterException {
    // LDRB r9,[r1],r3, RRX

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x818E32AFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x818E32AFL, 0xFF0010E8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R9"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "RRX"));
    operandTree2.root.getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000046L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrHOffsetImmediate() throws InternalTranslationException, InterpreterException {
    // LDRB r2,[r1,#1]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x01B0C7B0L, 0xE7FF0010, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(1)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000069L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrIOffsetRegister() throws InternalTranslationException, InterpreterException {
    // LDRB r4,[r1,r3]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000069L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x01F5F1B2, 0xE800E7FF, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R4"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrJOffsetLSL() throws InternalTranslationException, InterpreterException {
    // LDRB r5,[r1, r3 LSL #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x02C56FB8L, 0xE7FF0010, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R5"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSL"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrKOffsetLSR() throws InternalTranslationException, InterpreterException {
    // LDRB r6,[r1, r3 LSR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x01C21230L, 0xE7FF0010, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R6"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrLOffsetASR() throws InternalTranslationException, InterpreterException {
    // LDRB r7,[r1, r3 ASR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x01C21230L, 0xE7FF0010, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R7"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ASR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrMOffsetROR() throws InternalTranslationException, InterpreterException {
    // LDRB r8,[r1, r3 ROR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x81C21230L, 0xE7FF0010, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R8"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ROR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrNOffsetRRX() throws InternalTranslationException, InterpreterException {
    // LDRB r9,[r1, r3 RRX]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x000000E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x81D35CB1L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R9"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "RRX"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(1).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrOPreIndexedImmediate() throws InternalTranslationException,
      InterpreterException {
    // LDRB r2,[r1,#1]!

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000046L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x1B0C7B1L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(1)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7B1L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrPPreIndexedRegister() throws InternalTranslationException,
      InterpreterException {
    // LDRB r4,[r1,r7]!

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7B1L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x000000FFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x01B0C7C1L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R4"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R7"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x01B0C7C1L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrQPreIndexedLSL() throws InternalTranslationException, InterpreterException {
    // LDRB r5,[r1, r3 LSL #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x01B0C7C1L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x2C56FC9L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R5"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSL"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String
        .valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x02C56FC9L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrRPreIndexedLSR() throws InternalTranslationException, InterpreterException {
    // LDRB r6,[r1, r3 LSR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x02C56FC9L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x02D6BA49L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R6"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String
        .valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x02D6BA49L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrSOffsetASR() throws InternalTranslationException, InterpreterException {
    // LDRB r7,[r1, r3 ASR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x02D6BA49L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x02E804C9L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R7"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ASR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String
        .valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x02E804C9L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000010L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrTOffsetROR() throws InternalTranslationException, InterpreterException {
    // LDRB r8,[r1, r3 ROR #2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x02E804C9L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000010L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x82F94F49L, 0x00E7FF00, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R8"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ROR"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String
        .valueOf(2)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x82F94F49L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrUOffsetRRX() throws InternalTranslationException, InterpreterException {
    // LDRB r9,[r1, r3 RRX]

    interpreter.setRegister("R0", BigInteger.valueOf(0x07FFFFDCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x82F94F49L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x00452A02L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x00000000L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x031BE44AL, 0xE800E7FF, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R9"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.OPERATOR, "RRX"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).getChildren().get(0).getChildren()
        .get(1).m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x07FFFFDCL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x031BE44AL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x00452A02L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x00000000L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x000000FFL), interpreter.getVariableValue("R9"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(16, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
