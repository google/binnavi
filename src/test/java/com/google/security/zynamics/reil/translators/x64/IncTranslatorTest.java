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
import com.google.security.zynamics.reil.translators.StandardEnvironmentx64;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.reil.translators.x64.IncTranslator;
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
public class IncTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironmentx64 environment = new StandardEnvironmentx64();

  private final IncTranslator translator = new IncTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testBecomingPositive() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0xFFFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("inc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("OF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testOverflowing() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("inc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(TranslationHelpers.getUnsignedBigIntegerValue(0x8000000000000000L), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("OF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("SF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testOverflowingPreserveCF() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.ONE, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFFFFFFFFFL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("inc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(TranslationHelpers.getUnsignedBigIntegerValue(0x8000000000000000L), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("OF"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("SF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testSimple() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rax", BigInteger.valueOf(123), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("inc", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(124), interpreter.getVariableValue("rax"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("OF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("SF"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
