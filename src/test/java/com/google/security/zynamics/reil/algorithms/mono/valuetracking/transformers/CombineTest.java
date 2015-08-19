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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.IStateVector;
import com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTracker;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CombineTest {
  @Test
  public void testIndependentBaseRegisters() {
    final ReilInstruction instruction1 =
        ReilHelpers.createAdd(100, OperandSize.DWORD, "esi", OperandSize.DWORD, "4",
            OperandSize.DWORD, "eax");
    final ReilInstruction instruction2 =
        ReilHelpers.createAdd(101, OperandSize.DWORD, "edi", OperandSize.DWORD, "8",
            OperandSize.DWORD, "eax");
    final ReilInstruction instruction3 =
        ReilHelpers.createStr(102, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx");

    final ReilBlock block1 = new ReilBlock(Lists.newArrayList(instruction1));
    final ReilBlock block2 = new ReilBlock(Lists.newArrayList(instruction2));
    final ReilBlock block3 = new ReilBlock(Lists.newArrayList(instruction3));

    final ReilEdge edge1 = new ReilEdge(block1, block3, EdgeType.JUMP_UNCONDITIONAL);
    final ReilEdge edge2 = new ReilEdge(block2, block3, EdgeType.JUMP_UNCONDITIONAL);

    ReilBlock.link(block1, block3, edge1);
    ReilBlock.link(block2, block3, edge2);

    final ReilFunction function =
        new ReilFunction("Fark", new ReilGraph(Lists.newArrayList(block1, block2, block3),
            Lists.newArrayList(edge1, edge2)));

    System.out.println(function.getGraph());

    final IStateVector<InstructionGraphNode, ValueTrackerElement> result =
        ValueTracker.track(function);

    System.out.println(result);
  }

  @Test
  public void testSimple() {
    final ReilInstruction instruction1 =
        ReilHelpers.createStr(100, OperandSize.DWORD, "0", OperandSize.DWORD, "eax");
    final ReilInstruction instruction2 =
        ReilHelpers.createJcc(101, OperandSize.DWORD, "eax", OperandSize.DWORD, "104");
    final ReilInstruction instruction3 =
        ReilHelpers.createAdd(102, OperandSize.DWORD, "eax", OperandSize.DWORD, "4",
            OperandSize.DWORD, "ebx");
    final ReilInstruction instruction4 =
        ReilHelpers.createJcc(103, OperandSize.DWORD, "1", OperandSize.DWORD, "104");
    final ReilInstruction instruction5 =
        ReilHelpers.createAdd(104, OperandSize.DWORD, "eax", OperandSize.DWORD, "8",
            OperandSize.DWORD, "ebx");
    final ReilInstruction instruction6 =
        ReilHelpers.createStr(105, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx");

    final ReilBlock block1 = new ReilBlock(Lists.newArrayList(instruction1, instruction2));
    final ReilBlock block2 = new ReilBlock(Lists.newArrayList(instruction3, instruction4));
    final ReilBlock block3 = new ReilBlock(Lists.newArrayList(instruction5));
    final ReilBlock block4 = new ReilBlock(Lists.newArrayList(instruction6));

    final ReilEdge edge1 = new ReilEdge(block1, block2, EdgeType.JUMP_UNCONDITIONAL);
    final ReilEdge edge2 = new ReilEdge(block1, block3, EdgeType.JUMP_UNCONDITIONAL);
    final ReilEdge edge3 = new ReilEdge(block2, block4, EdgeType.JUMP_UNCONDITIONAL);
    final ReilEdge edge4 = new ReilEdge(block3, block4, EdgeType.JUMP_UNCONDITIONAL);

    ReilBlock.link(block1, block2, edge1);
    ReilBlock.link(block1, block3, edge2);
    ReilBlock.link(block2, block4, edge3);
    ReilBlock.link(block3, block4, edge4);

    final ReilFunction function =
        new ReilFunction("Fark", new ReilGraph(Lists.newArrayList(block1, block2, block3, block4),
            Lists.newArrayList(edge1, edge2, edge3, edge4)));

    System.out.println(function.getGraph());

    final IStateVector<InstructionGraphNode, ValueTrackerElement> result =
        ValueTracker.track(function);

    System.out.println(result);
  }
}
