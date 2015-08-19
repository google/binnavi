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
package com.google.security.zynamics.reil.translators;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolverResult;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterSetLatticeElement;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTracker;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.MockBlockContainer;
import com.google.security.zynamics.zylib.disassembly.MockCodeContainer;
import com.google.security.zynamics.zylib.disassembly.MockCodeEdge;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashSet;

@RunWith(JUnit4.class)
public class ReilTranslatorTest {
  private static final ReilTranslator<MockInstruction> m_translator =
      new ReilTranslator<MockInstruction>();

  private MockInstruction createCall(final long address) {
    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "12345"));

    return new MockInstruction(address, "call", Lists.newArrayList(operandTree3));
  }

  private MockInstruction createMov(final long address, final String lhs, final String rhs) {
    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, lhs));

    final MockOperandTree operandTree4 = new MockOperandTree();
    operandTree4.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree4.root.m_children
        .add(new MockOperandTreeNode(ExpressionType.IMMEDIATE_INTEGER, rhs));

    return new MockInstruction(address, "mov", Lists.newArrayList(operandTree3, operandTree4));
  }

  private MockInstruction createPush(final long address, final String lhs) {
    final MockOperandTree operandTree3 = new MockOperandTree();
    operandTree3.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree3.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, lhs));

    return new MockInstruction(address, "push", Lists.newArrayList(operandTree3));
  }

  @Test
  public void testInlinedFunctionGeneration() throws InternalTranslationException {
    final MockBlockContainer container = new MockBlockContainer();

    final MockCodeContainer block1 = new MockCodeContainer();
    block1.m_instructions.add(createMov(0x1000, "eax", "1"));

    final MockCodeContainer block2 = new MockCodeContainer();
    block2.m_instructions.add(createMov(0x1200, "ebx", "eax"));

    final MockCodeContainer block3 = new MockCodeContainer();
    block3.m_instructions.add(createMov(0x1001, "ecx", "ebx"));

    container.m_blocks.add(block1);
    container.m_blocks.add(block2);
    container.m_blocks.add(block3);

    container.m_edges.add(new MockCodeEdge<MockCodeContainer>(block1, block2,
        EdgeType.ENTER_INLINED_FUNCTION));
    container.m_edges.add(new MockCodeEdge<MockCodeContainer>(block2, block3,
        EdgeType.LEAVE_INLINED_FUNCTION));

    final ReilFunction function = m_translator.translate(new StandardEnvironment(), container);
    System.out.println(function.getGraph().getNodes());
    System.out.println(function.getGraph().getEdges());

    assertEquals(3, function.getGraph().getNodes().size());
    assertEquals(2, function.getGraph().getEdges().size());

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(function, Iterables.getFirst(block1.getInstructions(), null), "eax",
            new RegisterTrackingOptions(true, new HashSet<String>(), true, AnalysisDirection.DOWN));

    System.out.println(result);
  }

  @Test
  public void testInlinedFunctionGeneration2() throws InternalTranslationException {
    final MockBlockContainer container = new MockBlockContainer();

    final MockCodeContainer block1 = new MockCodeContainer();
    block1.m_instructions.add(createPush(0x1000, "eax"));
    block1.m_instructions.add(createMov(0x1001, "edx", "3"));
    block1.m_instructions.add(createCall(0x1002));

    final MockCodeContainer block2 = new MockCodeContainer();
    block2.m_instructions.add(createPush(0x2500, "ebx"));

    final MockCodeContainer block3 = new MockCodeContainer();
    block3.m_instructions.add(createPush(0x1003, "ecx"));

    container.m_blocks.add(block1);
    container.m_blocks.add(block2);
    container.m_blocks.add(block3);

    final MockCodeEdge<MockCodeContainer> edge1 =
        new MockCodeEdge<MockCodeContainer>(block1, block2, EdgeType.ENTER_INLINED_FUNCTION);
    final MockCodeEdge<MockCodeContainer> edge2 =
        new MockCodeEdge<MockCodeContainer>(block2, block3, EdgeType.LEAVE_INLINED_FUNCTION);

    block1.m_outgoingEdges.add(edge1);

    container.m_edges.add(edge1);
    container.m_edges.add(edge2);

    final ReilFunction function = m_translator.translate(new StandardEnvironment(), container);
    System.out.println(function.getGraph().getEdges());

    assertEquals(3, function.getGraph().getNodes().size());
    assertEquals(2, function.getGraph().getEdges().size());

    final MonoReilSolverResult<RegisterSetLatticeElement> result =
        RegisterTracker.track(function, Iterables.get(block1.getInstructions(), 0), "esp",
            new RegisterTrackingOptions(true, new HashSet<String>(), true, AnalysisDirection.DOWN));

    System.out.println(result);
  }

  @Test
  public void testRepStosStos() throws InternalTranslationException {
    final MockCodeContainer container = new MockCodeContainer();

    container.m_instructions.add(new MockInstruction(0x100, "rep stosb", Lists.newArrayList(
        new MockOperandTree(), new MockOperandTree())));
    container.m_instructions.add(new MockInstruction(0x200, "stosb",
        new ArrayList<MockOperandTree>()));

    final ReilGraph g = m_translator.translate(new StandardEnvironment(), container);

    System.out.println(g);

    assertEquals(9, g.nodeCount());
    assertEquals(11, g.edgeCount());
  }

  @Test
  public void testSimple() throws InternalTranslationException {
    final ReilGraph g =
        m_translator.translate(new StandardEnvironment(), new MockInstruction("nop",
            new ArrayList<MockOperandTree>()));

    assertEquals(1, g.nodeCount());
    assertEquals(0, g.edgeCount());
  }

  @Test
  public void testStos() throws InternalTranslationException {
    final ReilGraph g =
        m_translator.translate(new StandardEnvironment(), new MockInstruction("stosb",
            new ArrayList<MockOperandTree>()));

    System.out.println(g.getNodes().get(0).getInstructions());
    System.out.println(g.getNodes().get(1).getInstructions());
    System.out.println(g.getNodes().get(2).getInstructions());
    System.out.println(g.getNodes().get(3).getInstructions());
    System.out.println(g.getEdges());

    assertEquals(4, g.nodeCount());
    assertEquals(4, g.edgeCount());
  }
}
