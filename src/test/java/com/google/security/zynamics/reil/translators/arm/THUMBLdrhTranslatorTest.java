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
import com.google.security.zynamics.reil.translators.arm.THUMBLdrhTranslator;
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
public class THUMBLdrhTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
      new CpuPolicyARM(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final THUMBLdrhTranslator translator = new THUMBLdrhTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  final OperandSize dw = OperandSize.DWORD;
  final OperandSize wd = OperandSize.WORD;
  final OperandSize bt = OperandSize.BYTE;


  @Before
  public void setUp() {
  }

  @Test
  public void testLdrA() throws InternalTranslationException, InterpreterException {
    // LDRH r2,[r1,#1}

    interpreter.setRegister("R0", BigInteger.valueOf(0x00000021L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x00000003L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x00000007L, 0xFF0010E8L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, String.valueOf(4L)));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRH", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x000010E8L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x00000003L), interpreter.getVariableValue("R1"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(8, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testLdrB() throws InternalTranslationException, InterpreterException {
    // LDRH r2,[r1,r2]

    interpreter.setRegister("R0", BigInteger.valueOf(0x000010E8L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R1", BigInteger.valueOf(0x00000003L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("R2", BigInteger.valueOf(0x00000022L), dw, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("C", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("N", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Z", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("V", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("Q", BigInteger.ZERO, bt, ReilRegisterStatus.DEFINED);
    interpreter.setMemory(0x00000025L, 0x10E800E8L, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "R0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "b4");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.getChildren().get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, ","));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R1"));
    operandTree2.root.getChildren().get(0).getChildren().get(0).m_children
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "R2"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("LDRH", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(0x000000E8L), interpreter.getVariableValue("R0"));
    assertEquals(BigInteger.valueOf(0x00000003L), interpreter.getVariableValue("R1"));
    assertEquals(BigInteger.valueOf(0x00000022L), interpreter.getVariableValue("R2"));

    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("C"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("N"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Z"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("V"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("Q"));

    assertEquals(BigInteger.valueOf(4), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(9, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
