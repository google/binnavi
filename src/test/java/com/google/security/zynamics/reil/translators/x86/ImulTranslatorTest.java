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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
import com.google.security.zynamics.reil.translators.x86.ImulTranslator;
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
public class ImulTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX86(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final ImulTranslator translator = new ImulTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testSimple() throws InternalTranslationException, InterpreterException {
    // Define some flags to check whether they are cleared
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("PF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("edx", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("eax", BigInteger.valueOf(1000), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ecx", BigInteger.valueOf(200), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ecx"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1);

    final IInstruction instruction = new MockInstruction("imul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(6, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(200000), interpreter.getVariableValue("eax"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("edx"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("OF"));
    assertFalse(interpreter.isDefined("PF"));
    assertFalse(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testThreeOperands() throws InternalTranslationException, InterpreterException {
    // Define some flags to check whether they are cleared
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("eax", BigInteger.valueOf(10), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree3.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree1, operandTree2, operandTree3);

    final IInstruction instruction = new MockInstruction("imul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(20), interpreter.getVariableValue("eax"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("OF"));
    assertFalse(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testWordDwordOperands() throws InternalTranslationException, InterpreterException {
    // imul cx, [edx + 12]

    // Define some flags to check whether they are cleared
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("ecx", BigInteger.valueOf(0xFFFF0020L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("edx", BigInteger.ZERO, OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    interpreter.setMemory(12, 0x1234, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "cx"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.REGISTER, "edx"));
    operandTree2.root.m_children.get(0).m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.IMMEDIATE_INTEGER, "12"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("imul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(5, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(0xFFFF4680L), interpreter.getVariableValue("ecx"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("OF"));
    assertFalse(interpreter.isDefined("SF"));
    assertFalse(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testWordOperands() throws InternalTranslationException, InterpreterException {
    // Define some flags to check whether they are cleared
    interpreter.setRegister("AF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("OF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ZF", BigInteger.ZERO, OperandSize.DWORD, ReilRegisterStatus.DEFINED);

    interpreter.setRegister("eax", BigInteger.valueOf(10), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("imul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());

    assertEquals(BigInteger.valueOf(100), interpreter.getVariableValue("eax"));
    assertFalse(interpreter.isDefined("AF"));
    assertTrue(interpreter.isDefined("CF"));
    assertTrue(interpreter.isDefined("OF"));
    assertFalse(interpreter.isDefined("SF"));
    assertFalse(interpreter.isDefined("ZF"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
