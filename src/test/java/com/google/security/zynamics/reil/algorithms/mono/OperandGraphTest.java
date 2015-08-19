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
package com.google.security.zynamics.reil.algorithms.mono;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.OperandGraph;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(JUnit4.class)
public class OperandGraphTest {
  @Test
  public void testEmpty() {
    final ReilGraph rg = new ReilGraph(new ArrayList<ReilBlock>(), new ArrayList<ReilEdge>());

    final OperandGraph g = OperandGraph.create(rg);

    assertEquals(0, g.nodeCount());
    assertEquals(0, g.edgeCount());
  }

  @Test
  public void testOneNode() {
    final Collection<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    instructions.add(ReilHelpers.createAdd(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "123",
        OperandSize.QWORD, "t0"));
    instructions.add(ReilHelpers.createAnd(1, OperandSize.QWORD, "t0", OperandSize.DWORD,
        String.valueOf(0xFFFFFFFF), OperandSize.DWORD, "t1"));

    final ReilBlock block1 = new ReilBlock(instructions);

    final List<ReilBlock> blocks = Lists.<ReilBlock>newArrayList(block1);

    final ReilGraph rg = new ReilGraph(blocks, new ArrayList<ReilEdge>());

    final OperandGraph g = OperandGraph.create(rg);

    assertEquals(6, g.nodeCount());
    assertEquals(5, g.edgeCount());
  }

  @Test
  public void testTwoNodes() {
    final Collection<ReilInstruction> instructions1 = new ArrayList<ReilInstruction>();
    instructions1.add(ReilHelpers.createAdd(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "123",
        OperandSize.QWORD, "t0"));

    final Collection<ReilInstruction> instructions2 = new ArrayList<ReilInstruction>();
    instructions2.add(ReilHelpers.createAnd(1, OperandSize.QWORD, "t0", OperandSize.DWORD,
        String.valueOf(0xFFFFFFFF), OperandSize.DWORD, "t1"));

    final ReilBlock block1 = new ReilBlock(instructions1);
    final ReilBlock block2 = new ReilBlock(instructions2);

    final ReilEdge edge1 = new ReilEdge(block1, block2, EdgeType.JUMP_CONDITIONAL_FALSE);

    ReilBlock.link(block1, block2, edge1);

    final List<ReilBlock> blocks = Lists.newArrayList(block1, block2);
    final List<ReilEdge> edges = Lists.newArrayList(edge1);

    final ReilGraph rg = new ReilGraph(blocks, edges);

    final OperandGraph g = OperandGraph.create(rg);

    assertEquals(6, g.nodeCount());
    assertEquals(5, g.edgeCount());
  }
}
