/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ICodeContainer;
import com.google.security.zynamics.zylib.types.graphs.IGraphNode;

public class ReilBlock implements ICodeContainer<ReilInstruction>, Iterable<ReilInstruction>,
    IGraphNode<ReilBlock> {
  private final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();
  private final List<ReilEdge> outedges = new ArrayList<ReilEdge>();
  private final List<ReilEdge> inedges = new ArrayList<ReilEdge>();

  public ReilBlock(final Collection<ReilInstruction> instructions) {
    this.instructions.addAll(instructions);
  }

  public static void link(final ReilBlock source, final ReilBlock target, final ReilEdge edge) {
    source.outedges.add(edge);
    target.inedges.add(edge);
  }

  public static void unlink(final ReilBlock source, final ReilBlock target, final ReilEdge edge) {
    source.outedges.remove(edge);
    target.inedges.remove(edge);
  }

  public void addInstruction(final ReilInstruction instruction) {
    instructions.add(instruction);
  }

  @Override
  public IAddress getAddress() {
    return instructions.get(0).getAddress();
  }

  @Override
  public List<? extends ReilBlock> getChildren() {
    final List<ReilBlock> children = new ArrayList<ReilBlock>();
    for (final ReilEdge edge : getOutgoingEdges()) {
      children.add(edge.getTarget());
    }
    return children;
  }

  public List<ReilEdge> getIncomingEdges() {
    return new ArrayList<ReilEdge>(inedges);
  }

  @Override
  public Iterable<ReilInstruction> getInstructions() {
    return instructions;
  }

  @Override
  public ReilInstruction getLastInstruction() {
    return Iterables.getLast(instructions);
  }

  @Override
  public List<ReilEdge> getOutgoingEdges() {
    return new ArrayList<ReilEdge>(outedges);
  }

  @Override
  public List<? extends ReilBlock> getParents() {
    final List<ReilBlock> parents = new ArrayList<ReilBlock>();
    for (final ReilEdge edge : getIncomingEdges()) {
      parents.add(edge.getTarget());
    }
    return parents;
  }

  @Override
  public boolean hasInstruction(final ReilInstruction instruction) {
    return instructions.contains(instruction);
  }

  @Override
  public Iterator<ReilInstruction> iterator() {
    return instructions.iterator();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("REIL Block ");
    builder.append(getAddress().toHexString());
    builder.append("\n");
    for (final ReilInstruction instruction : getInstructions()) {
      builder.append(instruction.toString());
      builder.append("\n");
    }
    return builder.toString();
  }
}
