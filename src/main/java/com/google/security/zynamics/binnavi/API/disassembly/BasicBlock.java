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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.API.reil.ReilGraph;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.REIL.InstructionFinders;
import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;
import com.google.security.zynamics.binnavi.disassembly.IBlockNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;

// / Represents basic blocks of flowgraphs.
/**
 * Basic blocks are one of the two elements that can be found in {@link FlowGraph} objects.
 * 
 * The structure of basic blocks is immutable. If you want to modify basic blocks it is necessary to
 * convert the function that contains the basic blocks into a {@link View} first and to modify the
 * corresponding code nodes in the view.
 */
public final class BasicBlock implements Iterable<Instruction>, IGraphNode<BasicBlock>,
    ApiObject<IBlockNode> {
  /**
   * Wrapped internal basic block object.
   */
  private final IBlockNode m_block;

  /**
   * Parent function of the basic block.
   */
  private final Function m_parentFunction;

  /**
   * Instructions in the basic block.
   */
  private final List<Instruction> m_instructions = new ArrayList<Instruction>();

  /**
   * Child blocks of the basic block.
   */
  private final List<BasicBlock> m_children = new ArrayList<BasicBlock>();

  /**
   * Parent blocks of the basic block.
   */
  private final List<BasicBlock> m_parents = new ArrayList<BasicBlock>();

  /**
   * REIL translator used to create the REIL graph of the basic block.
   */
  private final ReilTranslator<INaviInstruction> m_translator =
      new ReilTranslator<INaviInstruction>();

  /**
   * REIL graph of the basic block.
   */
  private ReilGraph m_reilGraph = null;

  // / @cond INTERNAL
  /**
   * Creates a new API basic block object.
   * 
   * @param block Wrapped internal basic block object.
   * @param parentFunction Parent function of the basic block.
   */
  // / @endcond
  public BasicBlock(final IBlockNode block, final Function parentFunction) {
    m_block = Preconditions.checkNotNull(block, "Error: Block argument can't be null");
    m_parentFunction =
        Preconditions.checkNotNull(parentFunction, "Error: Parent function argument can't be null");

    for (final INaviInstruction instruction : m_block.getBlock().getInstructions()) {
      m_instructions.add(new Instruction(instruction));
    }

    if (m_instructions.isEmpty()) {
      throw new IllegalArgumentException("Error: Block without instructions can not exist.");
    }
  }

  // / @cond INTERNAL
  /**
   * Links a parent node with a child node.
   * 
   * @param parent The parent node that has the child node added as a child.
   * @param child The child node that has the parent node added as a parent.
   */
  // / @endcond
  static void link(final BasicBlock parent, final BasicBlock child) {
    parent.m_children.add(child);
    child.m_parents.add(parent);
  }

  @Override
  public IBlockNode getNative() {
    return m_block;
  }

  // ! Address of the basic block.
  /**
   * Returns the address of the basic block. This address equals the address of the first
   * instruction in the basic block.
   * 
   * @return The address of the basic block.
   */
  public Address getAddress() {
    return new Address(m_block.getBlock().getAddress().toBigInteger());
  }

  // ! Children of the basic block.
  /**
   * Returns the children of the basic block. The children of a basic block are exactly the basic
   * block where control flow can continue after the last instruction of the basic block was
   * executed.
   * 
   * @return A list of basic blocks that are child blocks of the basic block.
   */
  @Override
  public List<BasicBlock> getChildren() {
    return new ArrayList<BasicBlock>(m_children);
  }

  // ! Comment of the basic block.
  /**
   * Returns the comment of the basic block. This comment is displayed as the global comment of code
   * nodes in views.
   * 
   * @return The comment of the basic block.
   */
  public String getComment() {
    return m_block.getBlock().getGlobalComment();
  }

  // ! Instructions inside the basic block.
  /**
   * Returns a list of instructions inside the basic block.
   * 
   * @return The instructions of the basic block.
   */
  public List<Instruction> getInstructions() {
    return new ArrayList<Instruction>(m_instructions);
  }

  // ! Function the basic block belongs to.
  /**
   * Returns the parent function of the basic block.
   * 
   * @return The parent function of the basic block.
   */
  public Function getParentFunction() {
    return m_parentFunction;
  }

  // ! Parent blocks of the basic block.
  /**
   * Returns the parents of the basic block. The parents of the basic block are exactly those basic
   * blocks that can be executed right before the first instruction of the basic block is executed.
   * 
   * @return A list of basic blocks that are parent blocks of the basic block.
   */
  @Override
  public List<BasicBlock> getParents() {
    return new ArrayList<BasicBlock>(m_parents);
  }

  // ! REIL code of the basic block.
  /**
   * Converts the basic block to REIL code.
   * 
   * Using this function over manual translation via ReilTranslator has the advantage that REIL
   * translation results are automatically cached. Subsequent uses of this function requires no
   * additional re-translation of the basic block provided that nothing relevant (like added/removed
   * code nodes) changed.
   * 
   * @return The REIL representation of the instruction.
   * 
   * @throws InternalTranslationException Thrown if the REIL translation failed.
   */
  public ReilGraph getReilCode() throws InternalTranslationException {
    if (m_reilGraph == null) {
      try {
        m_reilGraph =
            ReilGraphConverter.createReilGraph(m_translator.translate(new StandardEnvironment(),
                m_block));
      } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
        throw new InternalTranslationException(e, InstructionFinders.findInstruction(this,
            e.getInstruction()));
      }
    }

    return m_reilGraph;
  }

  // ! Iterates over the instructions in the basic block.
  /**
   * Can be used to iterate over all instructions in the basic block.
   * 
   * @return An iterator to iterate over the instructions of the basic block.
   */
  @Override
  public Iterator<Instruction> iterator() {
    return m_instructions.iterator();
  }

  // ! Printable representation of the basic block.
  /**
   * Returns a string representation of the basic block.
   * 
   * @return A string representation of the basic block.
   */
  @Override
  public String toString() {
    final StringBuilder blockString = new StringBuilder();

    for (final Instruction instruction : m_instructions) {
      blockString.append(instruction.toString());
      blockString.append('\n');
    }

    return blockString.toString();
  }
}
