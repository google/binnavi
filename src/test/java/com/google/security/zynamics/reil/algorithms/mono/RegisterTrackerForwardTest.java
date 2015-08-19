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

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class RegisterTrackerForwardTest {
  @Test
  public void testDouble() {
    final InstructionGraphNode fakeNode = new InstructionGraphNode(ReilHelpers.createNop(0));

    final InstructionGraphNode node1 = new InstructionGraphNode(ReilHelpers.createAdd(1,
        OperandSize.DWORD,
        "eax",
        OperandSize.DWORD,
        "5",
        OperandSize.DWORD,
        "ebx"));
    final InstructionGraphNode node2 = new InstructionGraphNode(ReilHelpers.createAdd(2,
        OperandSize.DWORD,
        "ebx",
        OperandSize.DWORD,
        "6",
        OperandSize.DWORD,
        "ecx"));

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(fakeNode, node1, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(fakeNode, node1, edge1);

    final InstructionGraphEdge edge2 =
        new InstructionGraphEdge(node1, node2, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(node1, node2, edge2);

    final List<InstructionGraphNode> nodes = Lists.newArrayList(fakeNode, node1, node2);
    final StateVector<InstructionGraphNode, RegisterSet> init =
        new StateVector<InstructionGraphNode, RegisterSet>();
    init.setState(nodes.get(0), new RegisterSet("eax"));
    init.setState(nodes.get(1), new RegisterSet());
    init.setState(nodes.get(2), new RegisterSet());
  }

  @Test
  public void testEmpty() {
    new InstructionGraph(new ArrayList<InstructionGraphNode>(),
        new ArrayList<InstructionGraphEdge>());
    new StateVector<InstructionGraphNode, RegisterSet>();
  }

  @Test
  public void testOverwritten() {
    // str 5, , eax
    // str eax, , esi

    final InstructionGraphNode fakeNode = new InstructionGraphNode(ReilHelpers.createNop(0));

    final InstructionGraphNode node1 = new InstructionGraphNode(
        ReilHelpers.createStr(1, OperandSize.DWORD, "5", OperandSize.DWORD, "eax"));
    final InstructionGraphNode node2 = new InstructionGraphNode(
        ReilHelpers.createStr(2, OperandSize.DWORD, "eax", OperandSize.DWORD, "esi"));

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(fakeNode, node1, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(fakeNode, node1, edge1);

    final InstructionGraphEdge edge2 =
        new InstructionGraphEdge(node1, node2, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(node1, node2, edge2);

    final List<InstructionGraphNode> nodes = Lists.newArrayList(fakeNode, node1, node2);

    final StateVector<InstructionGraphNode, RegisterSet> init =
        new StateVector<InstructionGraphNode, RegisterSet>();
    init.setState(nodes.get(0), new RegisterSet("eax"));
    init.setState(nodes.get(1), new RegisterSet());
    init.setState(nodes.get(2), new RegisterSet());
  }

  @Test
  public void testSingle() {
    final List<InstructionGraphNode> nodes =
        Lists.newArrayList(new InstructionGraphNode(ReilHelpers.createNop(0)));

    final StateVector<InstructionGraphNode, RegisterSet> init =
        new StateVector<InstructionGraphNode, RegisterSet>();
    init.setState(nodes.get(0), new RegisterSet("eax"));
  }

  @Test
  public void testSingleFinalJump() {
    // str r12, , ctr
    // jcc 1, , ctr

    final ReilInstruction instruction2 =
        ReilHelpers.createStr(0x101, OperandSize.DWORD, "r12", OperandSize.DWORD, "ctr");
    final ReilInstruction instruction3 =
        ReilHelpers.createJcc(0x102, OperandSize.DWORD, "1", OperandSize.DWORD, "ctr");

    final InstructionGraphNode fakeNode = new InstructionGraphNode(ReilHelpers.createNop(0));
    final InstructionGraphNode node2 = new InstructionGraphNode(instruction2);
    final InstructionGraphNode node3 = new InstructionGraphNode(instruction3);

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(fakeNode, node2, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(fakeNode, node2, edge1);

    final InstructionGraphEdge edge2 =
        new InstructionGraphEdge(node2, node3, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(node2, node3, edge2);

    final List<InstructionGraphNode> nodes = Lists.newArrayList(fakeNode, node2, node3);

    final StateVector<InstructionGraphNode, RegisterSet> init =
        new StateVector<InstructionGraphNode, RegisterSet>();
    init.setState(nodes.get(0), new RegisterSet("r12"));
    init.setState(nodes.get(1), new RegisterSet());
    init.setState(nodes.get(2), new RegisterSet());
  }

  @Test
  public void testSplit() {
    // eax ebx
    // \ /
    // add eax, 5, ecx

    final InstructionGraphNode fakeNode1 = new InstructionGraphNode(ReilHelpers.createNop(0));
    final InstructionGraphNode fakeNode2 = new InstructionGraphNode(ReilHelpers.createNop(1));

    final InstructionGraphNode node1 = new InstructionGraphNode(ReilHelpers.createAdd(2,
        OperandSize.DWORD,
        "eax",
        OperandSize.DWORD,
        "5",
        OperandSize.DWORD,
        "ecx"));

    final InstructionGraphEdge edge1 =
        new InstructionGraphEdge(fakeNode1, node1, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(fakeNode1, node1, edge1);

    final InstructionGraphEdge edge2 =
        new InstructionGraphEdge(fakeNode2, node1, EdgeType.JUMP_UNCONDITIONAL);
    InstructionGraphNode.link(fakeNode2, node1, edge2);

    final List<InstructionGraphNode> nodes = Lists.newArrayList(fakeNode1, fakeNode2, node1);

    final StateVector<InstructionGraphNode, RegisterSet> init =
        new StateVector<InstructionGraphNode, RegisterSet>();
    init.setState(nodes.get(0), new RegisterSet("eax"));
    init.setState(nodes.get(1), new RegisterSet("ebx"));
    init.setState(nodes.get(2), new RegisterSet());
  }
}
