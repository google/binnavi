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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.google.security.zynamics.reil.translators.StandardEnvironmentx64;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.x64.SarTranslator;
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

@RunWith(JUnit4.class)
public class SarTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());
  private final StandardEnvironmentx64 environment = new StandardEnvironmentx64();
  private final SarTranslator translator = new SarTranslator();
  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testDWordBoundaryShift() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0x7FFFFFFF), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "3"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0xFFFFFFF), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testDWordShiftNegative() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0xFFFFFFFFl), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "3"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(new BigInteger("FFFFFFFF", 16), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testDWordShiftPositive() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(21), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "1"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(10), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  // Test -9/4
  public void testSignedShift() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0xFFFFFFF7l), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "2"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(new BigInteger("FFFFFFFD", 16), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testSingleShift() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0x11), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "1"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(8), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("OF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testWordShift() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0xFFFF), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "3"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    System.out.println("registers: "
        + TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()));
    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x1FFF), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testZeroShift() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ONE, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0x123), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "0"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x123), interpreter.getVariableValue("rax"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("CF"));
    assertFalse(interpreter.isDefined("OF"));
    assertFalse(interpreter.isDefined("SF"));
    assertFalse(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
  
  @Test
  public void testQWordShiftPositive() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(0x1FFFFFFFFFFFFFFFl), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "56"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    //assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(31), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
  
  @Test
  public void testQWordShiftNegative() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("SF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.UNDEFINED);
    interpreter.setRegister("rax",TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFl), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree2.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "8"));
    final IInstruction instruction =
        new MockInstruction("sar", Lists.newArrayList(operandTree1, operandTree2));

    translator.translate(environment, instruction, instructions);
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFl), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("SF"));
    assertFalse(interpreter.isDefined("AF"));
    assertFalse(interpreter.isDefined("OF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("SF"));
    assertTrue(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

}
