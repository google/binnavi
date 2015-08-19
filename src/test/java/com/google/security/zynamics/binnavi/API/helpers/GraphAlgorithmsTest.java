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
package com.google.security.zynamics.binnavi.API.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.helpers.GraphAlgorithms;
import com.google.security.zynamics.binnavi.API.helpers.MalformedGraphException;
import com.google.security.zynamics.binnavi.API.helpers.Tree;
import com.google.security.zynamics.binnavi.API.helpers.TreeNode;
import com.google.security.zynamics.binnavi.API.reil.OperandSize;
import com.google.security.zynamics.binnavi.API.reil.ReilInstruction;
import com.google.security.zynamics.binnavi.API.reil.ReilOperand;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraph;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphEdge;
import com.google.security.zynamics.binnavi.API.reil.mono.InstructionGraphNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public final class GraphAlgorithmsTest {
  @Test
  public void testDominatorTree() throws MalformedGraphException {
    final InstructionGraphNode node1 =
        new InstructionGraphNode(new ReilInstruction(new Address(0x100), "nop", new ReilOperand(
            OperandSize.OPERAND_SIZE_EMPTY, ""),
            new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, ""), new ReilOperand(
                OperandSize.OPERAND_SIZE_EMPTY, "")));
    final InstructionGraphNode node2 =
        new InstructionGraphNode(new ReilInstruction(new Address(0x100), "nop", new ReilOperand(
            OperandSize.OPERAND_SIZE_EMPTY, ""),
            new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, ""), new ReilOperand(
                OperandSize.OPERAND_SIZE_EMPTY, "")));
    final InstructionGraphNode node3 =
        new InstructionGraphNode(new ReilInstruction(new Address(0x100), "nop", new ReilOperand(
            OperandSize.OPERAND_SIZE_EMPTY, ""),
            new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, ""), new ReilOperand(
                OperandSize.OPERAND_SIZE_EMPTY, "")));
    final InstructionGraphNode node4 =
        new InstructionGraphNode(new ReilInstruction(new Address(0x100), "nop", new ReilOperand(
            OperandSize.OPERAND_SIZE_EMPTY, ""),
            new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, ""), new ReilOperand(
                OperandSize.OPERAND_SIZE_EMPTY, "")));
    final InstructionGraphNode node5 =
        new InstructionGraphNode(new ReilInstruction(new Address(0x100), "nop", new ReilOperand(
            OperandSize.OPERAND_SIZE_EMPTY, ""),
            new ReilOperand(OperandSize.OPERAND_SIZE_EMPTY, ""), new ReilOperand(
                OperandSize.OPERAND_SIZE_EMPTY, "")));

    final List<InstructionGraphNode> nodes = Lists.newArrayList(node1, node2, node3, node4, node5);

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(node1, node2, EdgeType.JumpUnconditional);
    final InstructionGraphEdge edge2 =
        new InstructionGraphEdge(node1, node3, EdgeType.JumpUnconditional);
    final InstructionGraphEdge edge3 =
        new InstructionGraphEdge(node2, node4, EdgeType.JumpUnconditional);
    final InstructionGraphEdge edge4 =
        new InstructionGraphEdge(node3, node4, EdgeType.JumpUnconditional);
    final InstructionGraphEdge edge5 =
        new InstructionGraphEdge(node4, node5, EdgeType.JumpUnconditional);

    InstructionGraphNode.link(node1, node2, edge1);
    InstructionGraphNode.link(node1, node3, edge2);
    InstructionGraphNode.link(node2, node4, edge3);
    InstructionGraphNode.link(node3, node4, edge4);
    InstructionGraphNode.link(node4, node5, edge5);

    final List<InstructionGraphEdge> edges = Lists.newArrayList(edge1, edge2, edge3, edge4, edge5);

    final InstructionGraph graph = new InstructionGraph(nodes, edges);

    final Tree<InstructionGraphNode> tree = GraphAlgorithms.getDominatorTree(graph, node1, null);

    assertEquals(node1, tree.getRootNode().getObject());
    assertEquals(3, tree.getRootNode().getChildren().size());

    final TreeNode<InstructionGraphNode> firstChild = tree.getRootNode().getChildren().get(0);
    final TreeNode<InstructionGraphNode> secondChild = tree.getRootNode().getChildren().get(1);
    final TreeNode<InstructionGraphNode> thirdChild = tree.getRootNode().getChildren().get(2);

    // All children are different
    assertTrue(firstChild.getObject() != secondChild.getObject());
    assertTrue(firstChild.getObject() != thirdChild.getObject());
    assertTrue(secondChild.getObject() != thirdChild.getObject());

    assertTrue((firstChild.getObject() == node4) || (firstChild.getChildren().size() == 0));
    assertTrue((firstChild.getObject() == node2)
        || (firstChild.getObject() == node3)
        || ((firstChild.getChildren().size() == 1) && (firstChild.getChildren().get(0).getObject() == node5)));

    assertTrue((secondChild.getObject() == node4) || (secondChild.getChildren().size() == 0));
    assertTrue((secondChild.getObject() == node2)
        || (secondChild.getObject() == node3)
        || ((secondChild.getChildren().size() == 1) && (secondChild.getChildren().get(0)
            .getObject() == node5)));

    assertTrue((thirdChild.getObject() == node4) || (thirdChild.getChildren().size() == 0));
    assertTrue((thirdChild.getObject() == node2)
        || (thirdChild.getObject() == node3)
        || ((thirdChild.getChildren().size() == 1) && (thirdChild.getChildren().get(0).getObject() == node5)));
  }
}
