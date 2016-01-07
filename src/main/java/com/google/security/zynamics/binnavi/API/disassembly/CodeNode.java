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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.API.reil.ReilGraph;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.REIL.InstructionFinders;
import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;
import com.google.security.zynamics.binnavi.disassembly.CNaviCodeNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.general.ListenerProvider;


// / Represents a view node that shows instructions.
/**
 * A code node is a node that can be part of view graphs. Each code node represents a basic block.
 */
public final class CodeNode extends ViewNode {
  /**
   * Wrapped internal code node object.
   */
  private final INaviCodeNode m_node;

  /**
   * Instructions of the code node.
   */
  private final List<Instruction> m_instructions = new ArrayList<Instruction>();

  /**
   * Listeners that are notified about changes in the code node.
   */
  private final ListenerProvider<ICodeNodeListener> m_listeners =
      new ListenerProvider<ICodeNodeListener>();

  /**
   * Keeps the API code node synchronized with the internal code node.
   */
  private final InternalListener m_internalListener = new InternalListener();

  /**
   * REIL translator used to translate the code node to REIL.
   */
  private final ReilTranslator<INaviInstruction> m_translator =
      new ReilTranslator<INaviInstruction>();

  /**
   * REIL graph of the code node.
   */
  private ReilGraph m_reilGraph;

  // / @cond INTERNAL
  /**
   * Creates a new API code node object.
   * 
   * @param view View the code node belongs to.
   * @param node The wrapped internal code node object.
   * @param tagManager Tag manager used to tag the node.
   */
  // / @endcond
  public CodeNode(final View view, final INaviCodeNode node, final TagManager tagManager) {
    super(view, node, tagManager);

    m_node = node;

    for (final INaviInstruction instruction : m_node.getInstructions()) {
      m_instructions.add(new Instruction(instruction));
    }

    node.addListener(m_internalListener);
  }

  @Override
  protected String getName() {
    return String.format("Code Node %s", m_node.getAddress().toHexString());
  }

  @Override
  public INaviCodeNode getNative() {
    return m_node;
  }

  // ! Adds an instruction to the code node.
  /**
   * Adds an instruction to the code node. The instruction is appended at the end of the code node.
   * 
   * Please note that not the instruction object you pass to the function is added to the code node.
   * Rather, a copy of the passed instruction object is made and added to the code node. This
   * guarantees that each instruction object appears only once in a view.
   * 
   * @param instruction The instruction to add to the code node.
   * 
   * @return The instruction object that was really added to the code node.
   */
  public Instruction addInstruction(final Instruction instruction) {
    Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null");

    // TODO (timkornau): this is not checked if this does what it should
    m_node.addInstruction(instruction.getNative(), null);

    return instruction;

    // return ObjectFinders.getObject(instruction, m_instructions);
  }

  // ! Adds a code node listener.
  /**
   * Adds a listener object that is notified about changes in the code node.
   * 
   * @param listener The listener that is added to the code node.
   */
  public void addListener(final ICodeNodeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Start address of the code node.
  /**
   * Returns the address of the code node. The address of a code node equals the address of the
   * first instruction of the code node.
   * 
   * @return The address of the code node.
   */
  public Address getAddress() {
    return new Address(m_node.getAddress().toBigInteger());
  }

  // ! Local comments of the code node.
  /**
   * Returns the local comment associated with the code node.
   * 
   * @return The local comment associated with the code node.
   */
  public List<IComment> getLocalComments() {
    return m_node.getComments().getLocalCodeNodeComment();
  }

  // ! Global comments of the code node.
  /**
   * Returns the global comment associated with the code node.
   * 
   * @return The global comment associated with the code node.
   */
  public List<IComment> getGlobalComments() {
    return m_node.getComments().getGlobalCodeNodeComment();
  }

  // ! Initializes the local code node comments.
  /**
   * Initializes the local code node comments.
   * 
   * @param comments The List of {@link IComment} to associate to the code node.
   */
  public void initializeLocalComment(final List<IComment> comments) {
    m_node.getComments().initializeLocalCodeNodeComment(comments);
  }

  // ! Initializes the global code node comments.
  /**
   * Initializes the global code node comments.
   * 
   * @param comments The list of {@link IComment} to associate to the code node.
   */
  public void initializeGlobalComment(final List<IComment> comments) {
    m_node.getComments().initializeGlobalCodeNodeComment(comments);
  }

  // ! Initialize the local instruction comments.
  /**
   * Initialize the local instruction comments.
   * 
   * @param instruction The instruction to associate the comments with.
   * @param comments The List of {@link IComment} to associate to the {@link Instruction}.
   */
  public void initializeLocalInstructionComment(final Instruction instruction,
      final List<IComment> comments) {
    m_node.getComments().initializeLocalInstructionComment(instruction.getNative(), comments);
  }

  // ! Append a global code node comment.
  /**
   * Append a global code node comment.
   * 
   * @param comment The comment string for the new comment to append.
   * 
   * @return The list of currently associated global comments of the code node after the append
   *         operation on success null on failure.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendLocalComment(final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {

    List<IComment> currentComments = new ArrayList<IComment>();

    try {
      currentComments = m_node.getComments().appendLocalCodeNodeComment(comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }

    return currentComments;
  }

  // ! Edit a local code node comment.
  /**
   * Edit a local code node comment.
   * 
   * @param comment The comment which is edited.
   * @param newComment The new comment string to replace the old comment string.
   * 
   * @return The edited comment.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   */
  public IComment editLocalComment(final IComment comment, final String newComment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    try {
      return m_node.getComments().editLocalCodeNodeComment(comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    }
  }

  // ! Delete a local code node comment.
  /**
   * Delete a local code node comment.
   * 
   * @param comment The {@link IComment} to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteLocalComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_node.getComments().deleteLocalCodeNodeComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Append a global code node comment.
  /**
   * Append a global code node comment.
   * 
   * @param comment The {@link String} comment to append.
   * @return The List of global comments currently associated to the code node if append operation
   *         was successful null otherwise.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendGlobalComment(final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_node.getComments().appendGlobalCodeNodeComment(comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Edit a global code node comment.
  /**
   * Edit a global code node comment.
   * 
   * @param comment The {@link IComment} to be edited.
   * @param newComment The {@link String} to replace the comment text.
   * @return The edited {@link IComment}.
   */
  public IComment editGlobalComment(final IComment comment, final String newComment) {
    try {
      m_node.getComments().editGlobalCodeNodeComment(comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
    }
    return null;
  }

  // ! Delete a global code node comment.
  /**
   * Delete a global code node comment.
   * 
   * @param comment The {@link IComment} to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteGlobalComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_node.getComments().deleteGlobalCodeNodeComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Edit a local instruction comment.
  /**
   * Edit a local instruction comment.
   * 
   * @param instruction The {@link Instruction} to which the comment is associated.
   * @param comment The {@link IComment} which is edited.
   * @param newComment The {@link String} to edit the comment with.
   * 
   * @return The edited {@link IComment} if successful null otherwise.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   */
  public IComment editLocalInstructionComment(final Instruction instruction,
      final IComment comment, final String newComment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    try {
      return m_node.getComments().editLocalInstructionComment(instruction.getNative(), comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    }
  }

  // ! Delete a local instruction comment.
  /**
   * Delete a local instruction comment.
   * 
   * @param instruction The instruction the comment is currently associated to.
   * @param comment The comment to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteLocalInstructionComment(final Instruction instruction, final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_node.getComments().deleteLocalInstructionComment(instruction.getNative(), comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Append a local instruction comment.
  /**
   * Append a local instruction comment.
   * 
   * @param instruction The instruction to which the comment will be associated.
   * @param comment The text for the comment to append.
   * 
   * @return The list of local comments currently associated to the instruction after the append
   *         operation was successful.
   * 
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendLocalInstructionComment(final Instruction instruction,
      final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_node.getComments().appendLocalInstructionComment(instruction.getNative(), comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Get the instructions of the code node.
  /**
   * @return The instructions of the code node.
   */
  public List<Instruction> getInstructions() {
    return new ArrayList<Instruction>(m_instructions);
  }

  // ! REIL code of the code node.
  /**
   * Converts the code node to REIL code.
   * 
   * Using this function over manual translation via ReilTranslator has the advantage that REIL
   * translation results are automatically cached. Subsequent uses of this function requires no
   * additional re-translation of the code node provided that nothing relevant (like added/removed
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
                m_node));
      } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
        throw new InternalTranslationException(e, InstructionFinders.findInstruction(this,
            e.getInstruction()));
      }
    }

    return m_reilGraph;
  }

  // ! Removes an instruction from the code node.
  /**
   * Removes an instruction from the code node.
   * 
   * @param instruction The instruction to remove from the code node.
   */
  public void removeInstruction(final Instruction instruction) {
    Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null");

    m_node.removeInstruction(instruction.getNative());
  }

  // ! Removes a code node listener.
  /**
   * Removes a listener object from the code node.
   * 
   * @param listener The listener that is removed from the code node.
   */
  public void removeListener(final ICodeNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the background color of an instruction.
  /**
   * Changes the background color of an instruction in the code node.
   * 
   * @param instruction The instruction whose color is changed.
   * @param level Identifies the drawing level. Lower levels are drawn at a higher priority than
   *        higher levels. Levels below 1000 are reserved for BinNavi and can not be used.
   * @param color The color used to highlight the instruction. If this argument is null,
   *        highlighting is cleared.
   */
  public void setInstructionColor(final Instruction instruction, final int level, final Color color) {
    Preconditions.checkNotNull(instruction, "Instruction argument can not be null");

    if (level < 10000) {
      throw new IllegalArgumentException("Drawing levels below 10000 are reserved for BinNavi");
    }

    m_node.setInstructionColor(instruction.getNative(), level, color);
  }

  // ! Printable representation of the code node.
  /**
   * Returns a string representation of the code node.
   * 
   * @return A string representation of the code node.
   */
  @Override
  public String toString() {
    final StringBuffer nodeString = new StringBuffer();

    for (final Instruction instruction : m_instructions) {
      nodeString.append(instruction.toString());
      nodeString.append('\n');
    }

    return nodeString.toString();
  }

  /**
   * Keeps the API code node synchronized with the internal code node.
   */
  private class InternalListener extends CNaviCodeNodeListenerAdapter {
    @Override
    public void addedInstruction(final INaviCodeNode codeNode, final INaviInstruction instruction) {
      final Instruction addedInstruction = new Instruction(instruction);

      m_instructions.add(addedInstruction);

      for (final ICodeNodeListener listener : m_listeners) {
        try {
          listener.addedInstruction(CodeNode.this, addedInstruction);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedInstruction(final INaviCodeNode codeNode, final INaviInstruction instruction) {
      final Instruction removedInstruction = ObjectFinders.getObject(instruction, m_instructions);

      m_instructions.remove(removedInstruction);

      for (final ICodeNodeListener listener : m_listeners) {
        try {
          listener.removedInstruction(CodeNode.this, removedInstruction);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
