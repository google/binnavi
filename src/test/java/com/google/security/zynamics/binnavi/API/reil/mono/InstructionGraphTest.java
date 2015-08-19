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
package com.google.security.zynamics.binnavi.API.reil.mono;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;
import com.google.security.zynamics.binnavi.API.reil.ReilOperand;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraph;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphEdge;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;


@RunWith(JUnit4.class)
public final class InstructionGraphTest {
  @Test
  public void testRemoveEdge() {
    final List<InstructionGraphNode> nodes = new ArrayList<InstructionGraphNode>();
    final List<InstructionGraphEdge> edges = new ArrayList<InstructionGraphEdge>();

    final InstructionGraphNode node1 =
        new InstructionGraphNode(new ReilInstruction(new Address(0), "nop",
            ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND));
    final InstructionGraphNode node2 =
        new InstructionGraphNode(new ReilInstruction(new Address(1), "nop",
            ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND, ReilOperand.EMPTY_OPERAND));

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(node1, node2, EdgeType.JumpUnconditional);

    InstructionGraphNode.link(node1, node2, edge1);

    nodes.add(node1);
    nodes.add(node2);

    edges.add(edge1);

    final InstructionGraph graph = new InstructionGraph(nodes, edges);

    assertEquals(graph.edgeCount(), 1);

    assertEquals(1, node2.getParents().size());
    assertEquals(1, node1.getChildren().size());

    graph.removeEdge(edge1);

    InstructionGraphNode.unlink(node1, node2, edge1);

    assertEquals(0, node2.getParents().size());
    assertEquals(0, node1.getChildren().size());
  }
}
