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
import com.google.security.zynamics.reil.translators.arm.ARMAdcTranslator;
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
public class ARMAdcTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyARM(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ARMAdcTranslator translator = new ARMAdcTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  final OperandSize dw = OperandSize.DWORD;
  final OperandSize wd = OperandSize.WORD;
  final OperandSize bt = OperandSize.BYTE;

  @Before
  public void setUp() {
  }

  @Test
  public void testSimpleASR() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x53A), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ASR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x996), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleASRS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x539), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ASR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x995), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleASRSregister() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x5C4L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R12", BigInteger.valueOf(0xFL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ASR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R12"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x539L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0xFL), interpreter.getVariableValue("R12"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleImmediate() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(4455), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        String.valueOf(1234)));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xA0C), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleImmediateS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x80000996L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        String.valueOf(1234)));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xA0C), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleLSL() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xDF3), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSL"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x16AC), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleLSLS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xDF2), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSL"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x16AB), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleLSLSregister() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x995L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x4L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSL"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x90C9L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleLSR() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x16AC), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "12"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x53A), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }


  @Test
  public void testSimpleLSRS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x16AC), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "12"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x539), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleLSRSregister() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x90C9L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x4L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "LSR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x5C4L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegister() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xA0C), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xDF3), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterAL() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8085L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCAL", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809BL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8085L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10121L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterCC() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8085L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCCC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809BL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8085L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterCS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10129L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterEQ() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8085L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCEQ", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809BL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8085L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterGE() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCGE", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterGT() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x80A4L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x808EL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCGT", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x80A4L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x808EL), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10133L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterHI() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCHI", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10099L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterHS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCHS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10129L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterLE() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCLE", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }


  @Test
  public void testSimpleRegisterLO() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809BL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8085L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCLO", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809BL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8085L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterLS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCLS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterLT() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCLT", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterMI() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x809FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8089L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCMI", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x809FL), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8089L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterNE() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCNE", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10099L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterPL() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCPL", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10099L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xA0C), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xDF2), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterVC() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCVC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x10099L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRegisterVS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(0x8057L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x8041L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x0L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCVS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x8057L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x8041L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x0L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleROR() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x996), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ROR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "5"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xC800057FL), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRORS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x995), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ROR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "5"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0xC800057EL), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRORSregister() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x539L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R3", BigInteger.valueOf(0x4L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "ROR"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R3"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x900005C4L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.valueOf(0x4L), interpreter.getVariableValue("R3"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRRX() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xC800057FL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ONE, bt, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "RRX"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADC", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x80000996L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("C"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testSimpleRRXS() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("R0", BigInteger.valueOf(1337), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(2233), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0xC800057EL), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.OPERATOR, "RRX"));
    operandTree3.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "R1"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("ADCS", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(1337), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(2233), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x995L), interpreter.getVariableValue("R2"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
