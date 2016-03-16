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
package com.google.security.zynamics.reil.translators.ppc;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyPPC;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.ppc.LmwTranslator;
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
public class LmwTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final LmwTranslator translator = new LmwTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {

  }

  @Test
  public void testALL() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r2", BigInteger.valueOf(0x0L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r1", BigInteger.valueOf(996), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setMemory(1000, 0x11111111, 4);
    interpreter.setMemory(1004, 0x22222222, 4);
    interpreter.setMemory(1008, 0x33333333, 4);
    interpreter.setMemory(1012, 0x44444444, 4);
    interpreter.setMemory(1016, 0x55555555, 4);
    interpreter.setMemory(1020, 0x66666666, 4);
    interpreter.setMemory(1024, 0x77777777, 4);
    interpreter.setMemory(1028, 0x88888888, 4);
    interpreter.setMemory(1032, 0x99999999, 4);
    interpreter.setMemory(1036, 0x10101010, 4);
    interpreter.setMemory(1040, 0x11111111, 4);
    interpreter.setMemory(1044, 0x12121212, 4);
    interpreter.setMemory(1048, 0x13131313, 4);
    interpreter.setMemory(1052, 0x14141414, 4);
    interpreter.setMemory(1056, 0x15151515, 4);
    interpreter.setMemory(1060, 0x16161616, 4);
    interpreter.setMemory(1064, 0x17171717, 4);
    interpreter.setMemory(1068, 0x18181818, 4);
    interpreter.setMemory(1072, 0x19191919, 4);
    interpreter.setMemory(1076, 0x20202020, 4);
    interpreter.setMemory(1080, 0x21212121, 4);
    interpreter.setMemory(1084, 0x22222222, 4);
    interpreter.setMemory(1088, 0x23232323, 4);
    interpreter.setMemory(1092, 0x24242424, 4);
    interpreter.setMemory(1096, 0x25252525, 4);
    interpreter.setMemory(1100, 0x26262626, 4);
    interpreter.setMemory(1104, 0x27272727, 4);
    interpreter.setMemory(1108, 0x28282828, 4);
    interpreter.setMemory(1112, 0x29292929, 4);
    interpreter.setMemory(1116, 0xFFFFFFFF, 4);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "byte");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "rtoc"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.MEMDEREF, "["));
    operandTree2.root.m_children.get(0).m_children.add(new MockOperandTreeNode(
        ExpressionType.OPERATOR, "+"));
    operandTree2.root.m_children.get(0).getChildren().get(0).getChildren()
        .add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));
    operandTree2.root.m_children.get(0).getChildren().get(0).getChildren()
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, "4"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("lmw", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(996), interpreter.getVariableValue("%r1"));
    assertEquals(BigInteger.valueOf(0x11111111), interpreter.getVariableValue("%r2"));
    assertEquals(BigInteger.valueOf(0x22222222), interpreter.getVariableValue("%r3"));
    assertEquals(BigInteger.valueOf(0x33333333), interpreter.getVariableValue("%r4"));
    assertEquals(BigInteger.valueOf(0x44444444), interpreter.getVariableValue("%r5"));
    assertEquals(BigInteger.valueOf(0x55555555), interpreter.getVariableValue("%r6"));
    assertEquals(BigInteger.valueOf(0x66666666), interpreter.getVariableValue("%r7"));
    assertEquals(BigInteger.valueOf(0x77777777), interpreter.getVariableValue("%r8"));
    assertEquals(BigInteger.valueOf(0x88888888l), interpreter.getVariableValue("%r9"));
    assertEquals(BigInteger.valueOf(0x99999999l), interpreter.getVariableValue("%r10"));
    assertEquals(BigInteger.valueOf(0x10101010), interpreter.getVariableValue("%r11"));
    assertEquals(BigInteger.valueOf(0x11111111), interpreter.getVariableValue("%r12"));
    assertEquals(BigInteger.valueOf(0x12121212), interpreter.getVariableValue("%r13"));
    assertEquals(BigInteger.valueOf(0x13131313), interpreter.getVariableValue("%r14"));
    assertEquals(BigInteger.valueOf(0x14141414), interpreter.getVariableValue("%r15"));
    assertEquals(BigInteger.valueOf(0x15151515), interpreter.getVariableValue("%r16"));
    assertEquals(BigInteger.valueOf(0x16161616), interpreter.getVariableValue("%r17"));
    assertEquals(BigInteger.valueOf(0x17171717), interpreter.getVariableValue("%r18"));
    assertEquals(BigInteger.valueOf(0x18181818), interpreter.getVariableValue("%r19"));
    assertEquals(BigInteger.valueOf(0x19191919), interpreter.getVariableValue("%r20"));
    assertEquals(BigInteger.valueOf(0x20202020), interpreter.getVariableValue("%r21"));
    assertEquals(BigInteger.valueOf(0x21212121), interpreter.getVariableValue("%r22"));
    assertEquals(BigInteger.valueOf(0x22222222), interpreter.getVariableValue("%r23"));
    assertEquals(BigInteger.valueOf(0x23232323), interpreter.getVariableValue("%r24"));
    assertEquals(BigInteger.valueOf(0x24242424), interpreter.getVariableValue("%r25"));
    assertEquals(BigInteger.valueOf(0x25252525), interpreter.getVariableValue("%r26"));
    assertEquals(BigInteger.valueOf(0x26262626), interpreter.getVariableValue("%r27"));
    assertEquals(BigInteger.valueOf(0x27272727), interpreter.getVariableValue("%r28"));
    assertEquals(BigInteger.valueOf(0x28282828), interpreter.getVariableValue("%r29"));
    assertEquals(BigInteger.valueOf(0x29292929), interpreter.getVariableValue("%r30"));
    assertEquals(BigInteger.valueOf(0xFFFFFFFFl), interpreter.getVariableValue("%r31"));

    assertEquals(BigInteger.valueOf(120L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(32, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
