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
import com.google.security.zynamics.reil.translators.arm.ARMLdmTranslator;
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
public class ARMLdmTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyARM(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ARMLdmTranslator translator = new ARMLdmTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  final OperandSize dw = OperandSize.DWORD;
  final OperandSize wd = OperandSize.WORD;
  final OperandSize bt = OperandSize.BYTE;


  @Before
  public void setUp() {
  }

  @Test
  public void testLdmDA() throws InternalTranslationException, InterpreterException {
    // DA == FA for Ldm

    interpreter.setRegister("R0", BigInteger.valueOf(0x809CL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x80CCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x14L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x2L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R10", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R11", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x8080L, 0x2L, 4);
    interpreter.setMemory(0x8084L, 0x3L, 4);
    interpreter.setMemory(0x8088L, 0x4L, 4);
    interpreter.setMemory(0x808CL, 0x5L, 4);
    interpreter.setMemory(0x8090L, 0x6L, 4);
    interpreter.setMemory(0x8094L, 0x7L, 4);
    interpreter.setMemory(0x8098L, 0x8L, 4);
    interpreter.setMemory(0x809CL, 0x1L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.EXPRESSION_LIST, "{"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R4"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R5"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R6"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R7"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R8"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R9"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R10"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R11"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDMDA", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809CL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x80CCL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x14L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x5L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x6L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("R9"));
    assertEquals(BigInteger.valueOf(0x8L), interpreter.getVariableValue("R10"));
    assertEquals(BigInteger.valueOf(0x1L), interpreter.getVariableValue("R11"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(8 * 4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(18, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdmDB() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809CL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x80CCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x14L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x2L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R10", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R11", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x807CL, 0x1L, 4);
    interpreter.setMemory(0x8080L, 0x2L, 4);
    interpreter.setMemory(0x8084L, 0x3L, 4);
    interpreter.setMemory(0x8088L, 0x4L, 4);
    interpreter.setMemory(0x808CL, 0x5L, 4);
    interpreter.setMemory(0x8090L, 0x6L, 4);
    interpreter.setMemory(0x8094L, 0x7L, 4);
    interpreter.setMemory(0x8098L, 0x8L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.EXPRESSION_LIST, "{"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R4"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R5"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R6"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R7"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R8"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R9"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R10"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R11"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDMDB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809CL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x80CCL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x14L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x1L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x5L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x6L), interpreter.getVariableValue("R9"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("R10"));
    assertEquals(BigInteger.valueOf(0x8L), interpreter.getVariableValue("R11"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(8 * 4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(18, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdmFD() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809CL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x80CCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x14L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x2L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R10", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R11", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x809CL, 0x1L, 4);
    interpreter.setMemory(0x80A0L, 0x2L, 4);
    interpreter.setMemory(0x80A4L, 0x3L, 4);
    interpreter.setMemory(0x80A8L, 0x4L, 4);
    interpreter.setMemory(0x80ACL, 0x5L, 4);
    interpreter.setMemory(0x80B0L, 0x6L, 4);
    interpreter.setMemory(0x80B4L, 0x7L, 4);
    interpreter.setMemory(0x80B8L, 0x8L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.EXPRESSION_LIST, "{"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R4"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R5"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R6"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R7"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R8"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R9"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R10"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R11"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    // FD is IA
    final IInstruction instruction = new MockInstruction("LDMFD", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809CL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x80CCL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x14L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x1L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x5L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x6L), interpreter.getVariableValue("R9"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("R10"));
    assertEquals(BigInteger.valueOf(0x8L), interpreter.getVariableValue("R11"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(8 * 4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(18, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }


  @Test
  public void testLdmIA() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8060L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x80B0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x14L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x2L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R10", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R11", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x8060L, 0x1L, 4);
    interpreter.setMemory(0x8064L, 0x2L, 4);
    interpreter.setMemory(0x8068L, 0x3L, 4);
    interpreter.setMemory(0x806CL, 0x4L, 4);
    interpreter.setMemory(0x8070L, 0x5L, 4);
    interpreter.setMemory(0x8074L, 0x6L, 4);
    interpreter.setMemory(0x8078L, 0x7L, 4);
    interpreter.setMemory(0x807CL, 0x8L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "!"));
    operandTree1.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.EXPRESSION_LIST, "{"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R4"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R5"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R6"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R7"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R8"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R9"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R10"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R11"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDMIA", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8080L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x80B0L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x14L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x1L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x5L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x6L), interpreter.getVariableValue("R9"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("R10"));
    assertEquals(BigInteger.valueOf(0x8L), interpreter.getVariableValue("R11"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(8 * 4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(18, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdmIB() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809CL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x80CCL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x14L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x2L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R4", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R5", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R6", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R7", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R8", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R9", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R10", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R11", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x80A0L, 0x2L, 4);
    interpreter.setMemory(0x80A4L, 0x3L, 4);
    interpreter.setMemory(0x80A8L, 0x4L, 4);
    interpreter.setMemory(0x80ACL, 0x5L, 4);
    interpreter.setMemory(0x80B0L, 0x6L, 4);
    interpreter.setMemory(0x80B4L, 0x7L, 4);
    interpreter.setMemory(0x80B8L, 0x8L, 4);
    interpreter.setMemory(0x80BCL, 0x1L, 4);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.EXPRESSION_LIST, "{"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R4"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R5"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R6"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R7"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R8"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R9"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R10"));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R11"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDMIB", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809CL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x80CCL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x14L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.valueOf(0x2L), interpreter.getVariableValue("R4"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("R5"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R6"));
    assertEquals(BigInteger.valueOf(0x5L), interpreter.getVariableValue("R7"));
    assertEquals(BigInteger.valueOf(0x6L), interpreter.getVariableValue("R8"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("R9"));
    assertEquals(BigInteger.valueOf(0x8L), interpreter.getVariableValue("R10"));
    assertEquals(BigInteger.valueOf(0x1L), interpreter.getVariableValue("R11"));

    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(8 * 4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(18, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
