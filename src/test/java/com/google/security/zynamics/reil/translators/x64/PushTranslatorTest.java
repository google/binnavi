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
import com.google.security.zynamics.reil.translators.x64.PushTranslator;
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
public class PushTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyX64(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final PushTranslator translator = new PushTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter.setRegister("rsp", BigInteger.valueOf(0x2000), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
  }

  @Test
  public void testPushRsp() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rsp", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rsp"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(8L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(BigInteger.valueOf(0x12345678l), interpreter.getMemory().load(0x12345670, 8));
    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testPushL08() throws InternalTranslationException, InterpreterException {
    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "byte");
    operandTree.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "12"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);    
    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));
    
    // Even if you do a "push byte 0x08", this will put the dword 0x00000008
    // on the stack and decrement ESP by 4.
    assertEquals(BigInteger.valueOf(0x1FFC), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(12, interpreter.readMemoryDword(0x1FFC));
    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testPushL16() throws InternalTranslationException, InterpreterException {
    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "1234"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x1FFE), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(2L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(1234, interpreter.readMemoryWord(0x1FFE));
    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testPushL32() throws InternalTranslationException, InterpreterException {
    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER,
        "12345678"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x1FFC), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(12345678, interpreter.readMemoryDword(0x1FFC));
    assertEquals(2, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testPushR16() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "word");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "ax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x1FFE), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(2L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(0x5678, interpreter.readMemoryWord(0x1FFE));
    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testPushR32() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", BigInteger.valueOf(0x12345678), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "eax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x1FFC), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(4L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(0x12345678, interpreter.readMemoryDword(0x1FFC));
    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
  @Test
  public void testPushR64() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("rax", BigInteger.valueOf(0x123456789abcdefL), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree = new MockOperandTree();
    operandTree.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "qword");
    operandTree.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rax"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree);

    final IInstruction instruction = new MockInstruction("push", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x1FF8), interpreter.getVariableValue("rsp"));

    assertEquals(BigInteger.valueOf(8L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals( BigInteger.valueOf(0x123456789abcdefL), interpreter.getMemory().load(0x1FF8, 8));
    assertEquals(3, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
