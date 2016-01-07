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
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.reil.InternalTranslationException;
import com.google.security.zynamics.binnavi.API.reil.ReilGraph;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.ListenerProvider;


// / Represents a single disassembled instruction.
/**
 * An Instruction object represents a single instruction of a disassembled module.
 */
public final class Instruction implements ApiObject<INaviInstruction> {
  /**
   * REIL translator used to translate the instruction to REIL code.
   */
  private static final ReilTranslator<INaviInstruction> m_translator =
      new ReilTranslator<INaviInstruction>();

  /**
   * Wrapped internal instruction object.
   */
  private final INaviInstruction m_instruction;

  /**
   * Operands of the instruction.
   */
  private final List<Operand> m_operands = new ArrayList<Operand>();

  /**
   * REIL representation of the instruction.
   */
  private ReilGraph m_reilGraph = null;

  /**
   * Keeps the API instruction object synchronized with the internal instruction object.
   */
  private final InternalInstructionListener m_listener = new InternalInstructionListener();

  /**
   * Listeners that are notified about changes in the instruction.
   */
  private final ListenerProvider<IInstructionListener> m_listeners =
      new ListenerProvider<IInstructionListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API instruction object.
   * 
   * @param instruction The wrapped internal instruction object.
   */
  // / @endcond
  public Instruction(final INaviInstruction instruction) {
    m_instruction =
        Preconditions.checkNotNull(instruction, "Error: Instruction argument can't be null");

    for (final COperandTree operand : m_instruction.getOperands()) {
      m_operands.add(new Operand(operand));
    }

    instruction.addListener(m_listener);
  }

  /**
   * Converts API operands to internal operands.
   * 
   * @param module The module the operands belong to.
   * @param operands The API operands to convert.
   * 
   * @return The converted internal operands.
   */
  private static List<COperandTree> convert(final INaviModule module, final List<Operand> operands) {
    final List<COperandTree> convertedOperands = new ArrayList<COperandTree>();

    for (final Operand operand : operands) {
      convertedOperands.add(module.createOperand(convert(module, operand.getRootNode(), null)));
    }

    return convertedOperands;
  }

  /**
   * Converts an API operand tree node to an internal operand tree node.
   * 
   * @param module The module the operand tree node belong to.
   * @param operandExpression The API operand tree node to convert.
   * @param parent The parent node of the created tree node.
   * 
   * @return The converted internal operand tree node.
   */
  private static COperandTreeNode convert(final INaviModule module,
      final OperandExpression operandExpression, final COperandTreeNode parent) {
    final COperandTreeNode convertedNode =
        module.createOperandExpression(operandExpression.getNative().getValue(), operandExpression
            .getType().getNative());

    if (parent != null) {
      COperandTreeNode.link(parent, convertedNode);
    }

    for (final OperandExpression child : operandExpression.getChildren()) {
      convert(module, child, convertedNode);
    }

    return convertedNode;
  }

  // ! Creates a new instruction.
  /**
   * Creates a new instruction which is part of this module. The new instruction is immediately
   * stored in the database.
   * 
   * @param module The module the instruction belongs to.
   * @param address The address of the instruction.
   * @param mnemonic The mnemonic of the instruction.
   * @param operands The operands that belong to the instruction.
   * @param data The binary data of the instruction.
   * @param architecture The architecture of the instruction.
   * 
   * @return The created instruction object.
   */
  public static Instruction create(final Module module, final Address address,
      final String mnemonic, final List<Operand> operands, final byte[] data,
      final String architecture) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    Preconditions.checkNotNull(mnemonic, "Error: Mnemonic argument can not be null");

    for (final Operand operand : operands) {
      Preconditions.checkNotNull(operand, "Error: Operands list contains a null-argument");
    }

    Preconditions.checkNotNull(data, "Error: Data argument can not be null");

    return new Instruction(module.getNative().createInstruction(new CAddress(address.toLong()), mnemonic,
        convert(module.getNative(), operands), data, architecture));
  }

  @Override
  public INaviInstruction getNative() {
    return m_instruction;
  }

  // ! Adds an instruction listener.
  /**
   * Adds an object that is notified about changes in the instruction.
   * 
   * @param listener The listener object that is notified about changes in the instruction.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         instruction.
   */
  public void addListener(final IInstructionListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Appends a comment to the list of global instruction comments.
  /**
   * Appends a comment to the list of global instruction comments.
   * 
   * @param commentText The comment which is appended to the list of global instruction comments.
   * 
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {

    List<IComment> comments = Lists.newArrayList();

    try {
      comments = m_instruction.appendGlobalComment(commentText);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }

    for (final IInstructionListener listener : m_listeners) {
      listener.appendedComment(this, Iterables.getLast(comments));
    }

    return comments;
  }

  // ! Deletes a comment from the list of global instruction comments.
  /**
   * Deletes a comment from the list of global instruction comments.
   * 
   * @param comment The comment which will be deleted.
   */
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    try {
      m_instruction.deleteGlobalComment(comment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    }

    for (final IInstructionListener listener : m_listeners) {
      listener.deletedComment(this, comment);
    }
  }

  // ! Edits a global instruction comment.
  /**
   * Edits a global instruction comment.
   * 
   * @param comment The comment which is edited.
   * 
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public IComment editComment(final IComment comment, final String newComment)
      throws CouldntSaveDataException {
    IComment editedComment = null;
    try {
      editedComment = m_instruction.editGlobalComment(comment, newComment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }

    for (final IInstructionListener listener : m_listeners) {
      listener.editedComment(this, comment);
    }
    return editedComment;
  }

  // ! Address of the instruction.
  /**
   * Returns the address of the instruction.
   * 
   * @return The address of the instruction.
   */
  public Address getAddress() {
    return new Address(m_instruction.getAddress().toBigInteger());
  }

  // ! Returns the architecture of the instruction.
  /**
   * Returns the architecture of the instruction.
   * 
   * @return The architecture of the instruction.
   */
  public String getArchitecture() {
    return m_instruction.getArchitecture();
  }

  // ! Comment of the instruction.
  /**
   * Returns the comment of the instruction. This comment is the string that is displayed in views
   * as the global instruction comment of the instruction.
   * 
   * @return The comment of the instruction.
   */
  public List<IComment> getComment() {
    return m_instruction.getGlobalComment();
  }

  // ! Binary data of the instruction.
  /**
   * Returns the original bytes of the instruction.
   * 
   * @return The original bytes of the instruction.
   */
  public byte[] getData() {
    return m_instruction.getData();
  }

  // ! Mnemonic of the instruction.
  /**
   * Returns the mnemonic of the instruction.
   * 
   * @return The mnemonic of the instruction.
   */
  public String getMnemonic() {
    return m_instruction.getMnemonic();
  }

  // ! Operands of the instruction.
  /**
   * Returns the operands of the instruction.
   * 
   * @return The operands of the instruction.
   */
  public List<Operand> getOperands() {
    return new ArrayList<Operand>(m_operands);
  }

  // ! REIL code of the instruction.
  /**
   * Converts the instruction to REIL code.
   * 
   * Using this function over manual translation via ReilTranslator has the advantage that REIL
   * translation results are automatically cached. Subsequent uses of this function requires no
   * additional re-translation of the instruction provided that nothing relevant (like added/removed
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
                m_instruction));
      } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
        throw new InternalTranslationException(e, this);
      }
    }

    return m_reilGraph;
  }

  // ! Initializes the instruction comment.
  /**
   * Initializes the comment of an instruction.
   * 
   * @param comment The new comment of the instruction.
   * 
   * @throws IllegalArgumentException Thrown if the comment argument is null.
   */
  public void initializeComment(final ArrayList<IComment> comment) {
    m_instruction.initializeGlobalComment(comment);

    for (final IInstructionListener listener : m_listeners) {
      listener.initializedComment(this, comment);
    }
  }

  // ! Removes an instruction listener.
  /**
   * Removes a listener object from the instruction.
   * 
   * @param listener The listener object to remove from the instruction.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the
   *         instruction.
   */
  public void removeListener(final IInstructionListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the instruction.
  /**
   * Returns the string representation of the instruction.
   * 
   * @return The string representation of the instruction.
   */
  @Override
  public String toString() {
    final StringBuffer instructionString =
        new StringBuffer(String.format("%s  %s ", getAddress().toHexString(), getMnemonic()));

    boolean addComma = false;

    for (final Operand operand : getOperands()) {
      if (addComma) {
        instructionString.append(", ");
      }

      addComma = true;

      instructionString.append(operand.toString());
    }

    return instructionString.toString();
  }

  /**
   * Keeps the API instruction object synchronized with the internal instruction object.
   */
  private class InternalInstructionListener implements
      com.google.security.zynamics.binnavi.disassembly.IInstructionListener {
    @Override
    public void appendedComment(final IInstruction instruction, final IComment comment) {
      for (final IInstructionListener listener : m_listeners) {
        try {
          listener.appendedComment(Instruction.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedComment(final IInstruction instruction, final IComment comment) {
      for (final IInstructionListener listener : m_listeners) {
        try {
          listener.deletedComment(Instruction.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedComment(final IInstruction instruction, final IComment comment) {
      for (final IInstructionListener listener : m_listeners) {
        try {
          listener.editedComment(Instruction.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedComment(final IInstruction instruction, final List<IComment> comment) {
      for (final IInstructionListener listener : m_listeners) {
        try {
          listener.initializedComment(Instruction.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
