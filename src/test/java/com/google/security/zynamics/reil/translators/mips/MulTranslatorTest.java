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
package com.google.security.zynamics.reil.translators.mips;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyMips;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.mips.MulTranslator;
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
public class MulTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyMips(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final MulTranslator translator = new MulTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Test
  public void testMsubu1PositivePositive() throws InternalTranslationException,
      InterpreterException {
    interpreter.setRegister("$v0", BigInteger.valueOf(0xDEADBEEFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v1", BigInteger.valueOf(0x7L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v2", BigInteger.valueOf(0x3L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree0 = new MockOperandTree();
    operandTree0.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree0.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v0"));

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v1"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree0, operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("mul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    // check correct outcome

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x15L), interpreter.getVariableValue("$v0"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("$v1"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("$v2"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testMsubu2PositiveNegative() throws InternalTranslationException,
      InterpreterException {
    interpreter.setRegister("$v0", BigInteger.valueOf(0xDEADBEEFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v1", BigInteger.valueOf(0x7L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v2", BigInteger.valueOf(0xFFFFFFFDL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree0 = new MockOperandTree();
    operandTree0.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree0.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v0"));

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v1"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree0, operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("mul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    // check correct outcome

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0xFFFFFFEBL), interpreter.getVariableValue("$v0"));
    assertEquals(BigInteger.valueOf(0x7L), interpreter.getVariableValue("$v1"));
    assertEquals(BigInteger.valueOf(0xFFFFFFFDL), interpreter.getVariableValue("$v2"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testMsubu3NegativePositive() throws InternalTranslationException,
      InterpreterException {
    interpreter.setRegister("$v0", BigInteger.valueOf(0xDEADBEEFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v1", BigInteger.valueOf(0xFFFFFFF9L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v2", BigInteger.valueOf(0x3L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree0 = new MockOperandTree();
    operandTree0.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree0.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v0"));

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v1"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree0, operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("mul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    // check correct outcome

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0xFFFFFFEBL), interpreter.getVariableValue("$v0"));
    assertEquals(BigInteger.valueOf(0xFFFFFFF9L), interpreter.getVariableValue("$v1"));
    assertEquals(BigInteger.valueOf(0x3L), interpreter.getVariableValue("$v2"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }

  @Test
  public void testMsubu4NegativeNegative() throws InternalTranslationException,
      InterpreterException {
    interpreter.setRegister("$v0", BigInteger.valueOf(0xDEADBEEFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v1", BigInteger.valueOf(0xFFFFFFF9L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("$v2", BigInteger.valueOf(0xFFFFFFFDL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);


    final MockOperandTree operandTree0 = new MockOperandTree();
    operandTree0.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree0.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v0"));

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v1"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "$v2"));

    final List<MockOperandTree> operands =
        Lists.newArrayList(operandTree0, operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("mul", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    // check correct outcome

    assertEquals(4, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
    assertEquals(BigInteger.valueOf(0x15L), interpreter.getVariableValue("$v0"));
    assertEquals(BigInteger.valueOf(0xFFFFFFF9L), interpreter.getVariableValue("$v1"));
    assertEquals(BigInteger.valueOf(0xFFFFFFFDL), interpreter.getVariableValue("$v2"));
    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
  }
}
