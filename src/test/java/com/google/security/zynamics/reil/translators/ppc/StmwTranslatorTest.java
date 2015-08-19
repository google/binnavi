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
import com.google.security.zynamics.reil.translators.ppc.StmwTranslator;
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
public class StmwTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final StmwTranslator translator = new StmwTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter.setRegister("%r2", BigInteger.valueOf(0x11111111L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r3", BigInteger.valueOf(0x22222222L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r4", BigInteger.valueOf(0x33333333L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r5", BigInteger.valueOf(0x44444444L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r6", BigInteger.valueOf(0x55555555L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r7", BigInteger.valueOf(0x66666666L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r8", BigInteger.valueOf(0x77777777L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r9", BigInteger.valueOf(0x88888888L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r10", BigInteger.valueOf(0x99999999L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r11", BigInteger.valueOf(0x10101010L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r12", BigInteger.valueOf(0x11111111L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r13", BigInteger.valueOf(0x12121212L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r14", BigInteger.valueOf(0x13131313L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r15", BigInteger.valueOf(0x14141414L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r16", BigInteger.valueOf(0x15151515L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r17", BigInteger.valueOf(0x16161616L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r18", BigInteger.valueOf(0x17171717L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r19", BigInteger.valueOf(0x18181818L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r20", BigInteger.valueOf(0x19191919L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r21", BigInteger.valueOf(0x20202020L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r22", BigInteger.valueOf(0x21212121L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r23", BigInteger.valueOf(0x22222222L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r24", BigInteger.valueOf(0x23232323L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r25", BigInteger.valueOf(0x24242424L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r26", BigInteger.valueOf(0x25252525L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r27", BigInteger.valueOf(0x26262626L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r28", BigInteger.valueOf(0x27272727L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r29", BigInteger.valueOf(0x28282828L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r30", BigInteger.valueOf(0x29292929L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r31", BigInteger.valueOf(0xFFFFFFFFL), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
  }

  @Test
  public void testALL() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r1", BigInteger.valueOf(996), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "byte");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r2"));

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

    final IInstruction instruction = new MockInstruction("stmw", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100L));

    assertEquals(BigInteger.valueOf(996), interpreter.getVariableValue("%r1"));

    assertEquals(BigInteger.valueOf(0x11111111),
        BigInteger.valueOf(interpreter.readMemoryDword(1000)));
    assertEquals(BigInteger.valueOf(0x22222222),
        BigInteger.valueOf(interpreter.readMemoryDword(1004)));
    assertEquals(BigInteger.valueOf(0x33333333),
        BigInteger.valueOf(interpreter.readMemoryDword(1008)));
    assertEquals(BigInteger.valueOf(0x44444444),
        BigInteger.valueOf(interpreter.readMemoryDword(1012)));
    assertEquals(BigInteger.valueOf(0x55555555),
        BigInteger.valueOf(interpreter.readMemoryDword(1016)));
    assertEquals(BigInteger.valueOf(0x66666666),
        BigInteger.valueOf(interpreter.readMemoryDword(1020)));
    assertEquals(BigInteger.valueOf(0x77777777),
        BigInteger.valueOf(interpreter.readMemoryDword(1024)));
    assertEquals(BigInteger.valueOf(0x88888888),
        BigInteger.valueOf(interpreter.readMemoryDword(1028)));
    assertEquals(BigInteger.valueOf(0x99999999),
        BigInteger.valueOf(interpreter.readMemoryDword(1032)));
    assertEquals(BigInteger.valueOf(0x10101010),
        BigInteger.valueOf(interpreter.readMemoryDword(1036)));
    assertEquals(BigInteger.valueOf(0x11111111),
        BigInteger.valueOf(interpreter.readMemoryDword(1040)));
    assertEquals(BigInteger.valueOf(0x12121212),
        BigInteger.valueOf(interpreter.readMemoryDword(1044)));
    assertEquals(BigInteger.valueOf(0x13131313),
        BigInteger.valueOf(interpreter.readMemoryDword(1048)));
    assertEquals(BigInteger.valueOf(0x14141414),
        BigInteger.valueOf(interpreter.readMemoryDword(1052)));
    assertEquals(BigInteger.valueOf(0x15151515),
        BigInteger.valueOf(interpreter.readMemoryDword(1056)));
    assertEquals(BigInteger.valueOf(0x16161616),
        BigInteger.valueOf(interpreter.readMemoryDword(1060)));
    assertEquals(BigInteger.valueOf(0x17171717),
        BigInteger.valueOf(interpreter.readMemoryDword(1064)));
    assertEquals(BigInteger.valueOf(0x18181818),
        BigInteger.valueOf(interpreter.readMemoryDword(1068)));
    assertEquals(BigInteger.valueOf(0x19191919),
        BigInteger.valueOf(interpreter.readMemoryDword(1072)));
    assertEquals(BigInteger.valueOf(0x20202020),
        BigInteger.valueOf(interpreter.readMemoryDword(1076)));
    assertEquals(BigInteger.valueOf(0x21212121),
        BigInteger.valueOf(interpreter.readMemoryDword(1080)));
    assertEquals(BigInteger.valueOf(0x22222222),
        BigInteger.valueOf(interpreter.readMemoryDword(1084)));
    assertEquals(BigInteger.valueOf(0x23232323),
        BigInteger.valueOf(interpreter.readMemoryDword(1088)));
    assertEquals(BigInteger.valueOf(0x24242424),
        BigInteger.valueOf(interpreter.readMemoryDword(1092)));
    assertEquals(BigInteger.valueOf(0x25252525),
        BigInteger.valueOf(interpreter.readMemoryDword(1096)));
    assertEquals(BigInteger.valueOf(0x26262626),
        BigInteger.valueOf(interpreter.readMemoryDword(1100)));
    assertEquals(BigInteger.valueOf(0x27272727),
        BigInteger.valueOf(interpreter.readMemoryDword(1104)));
    assertEquals(BigInteger.valueOf(0x28282828),
        BigInteger.valueOf(interpreter.readMemoryDword(1108)));
    assertEquals(BigInteger.valueOf(0x29292929),
        BigInteger.valueOf(interpreter.readMemoryDword(1112)));
    assertEquals(BigInteger.valueOf(0xFFFFFFFF),
        BigInteger.valueOf(interpreter.readMemoryDword(1116)));

    assertEquals(BigInteger.valueOf(120L), BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(32, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
