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
package com.google.security.zynamics.binnavi.API.reil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.IGraphNode;

// / Basic block of REIL code
/**
 * Represents a single basic block of REIL code.
 */
public final class ReilBlock implements IGraphNode<ReilBlock>, Iterable<ReilInstruction> {
  /**
   * Instructions of the block.
   */
  private final List<ReilInstruction> m_instructions;

  /**
   * Parent blocks of the block.
   */
  private final List<ReilBlock> m_parents = new ArrayList<ReilBlock>();

  /**
   * Children of the block.
   */
  private final List<ReilBlock> m_children = new ArrayList<ReilBlock>();

  // / @cond INTERNAL
  /**
   * Creates a new API REIL block object.
   * 
   * @param block The wrapped internal REIL block object.
   */
  // / @endcond
  public ReilBlock(final com.google.security.zynamics.reil.ReilBlock block) {
    m_instructions = createInstructions(block);
  }

  /**
   * Creates a new REIL block.
   * 
   * @param instructions List of instructions that belong to the block.
   */
  public ReilBlock(final List<ReilInstruction> instructions) {
    Preconditions.checkNotNull(instructions, "Error: Instructions argument can not be null");

    for (final ReilInstruction instruction : instructions) {
      Preconditions.checkNotNull(instruction, "Error: Instructions list contains a null-element");
    }

    m_instructions = new ArrayList<ReilInstruction>(instructions);
  }

  /**
   * Converts the instructions of an internal REIL block into API instruction objects.
   * 
   * @param block Source block.
   * 
   * @return The converted REIL instructions.
   */
  private static List<ReilInstruction> createInstructions(
      final com.google.security.zynamics.reil.ReilBlock block) {
    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    for (final com.google.security.zynamics.reil.ReilInstruction reilInstruction : block
        .getInstructions()) {
      instructions.add(new ReilInstruction(reilInstruction));
    }

    return instructions;
  }

  // / @cond INTERNAL
  /**
   * Links two REIL blocks.
   * 
   * @param parent Parent block to link.
   * @param child Child block to link.
   */
  // / @endcond
  protected static void link(final ReilBlock parent, final ReilBlock child) {
    parent.m_children.add(child);
    child.m_parents.add(parent);
  }

  // ! Start address of the REIL block.
  /**
   * Returns the start address of the REIL block.
   * 
   * @return The start address of the REIL block.
   */
  public Address getAddress() {
    return m_instructions.get(0).getAddress();
  }

  // ! The children of the REIL block.
  /**
   * Returns a list of REIL blocks which are the immediate child blocks of this REIL block.
   * 
   * @return The child blocks of the REIL block.
   */
  @Override
  public List<ReilBlock> getChildren() {
    return new ArrayList<ReilBlock>(m_children);
  }

  // ! Instructions inside the REIL block.
  /**
   * Returns a list of instructions inside the REIL block.
   * 
   * @return The instructions of the basic block.
   */
  public List<ReilInstruction> getInstructions() {
    return new ArrayList<ReilInstruction>(m_instructions);
  }

  // ! The parents of the REIL block.
  /**
   * Returns a list of REIL blocks which are the immediate parent blocks of this REIL block.
   * 
   * @return The parent blocks of the REIL block.
   */
  @Override
  public List<ReilBlock> getParents() {
    return new ArrayList<ReilBlock>(m_parents);
  }

  // ! Iterates over all instructions in the REIL block.
  @Override
  public Iterator<ReilInstruction> iterator() {
    return m_instructions.iterator();
  }

  // ! Printable representation of the REIL block.
  /**
   * Returns the string representation of the REIL block.
   * 
   * @return The string representation of the REIL block.
   */
  @Override
  public String toString() {
    final StringBuilder strinBuilder = new StringBuilder();

    for (final ReilInstruction instruction : m_instructions) {
      strinBuilder.append(instruction.toString());
      strinBuilder.append('\n');
    }

    return strinBuilder.toString();
  }
}
