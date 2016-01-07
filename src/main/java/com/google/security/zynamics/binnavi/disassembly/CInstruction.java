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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.zylib.ZyTree.IZyTreeNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single instruction in a disassembled program.
 */
public final class CInstruction implements INaviInstruction {
  /**
   * Address of the instruction. Guaranteed to be non-null.
   */
  private final IAddress m_address;

  /***
   * Mnemonic of the instruction. Guaranteed to be non-null.
   */
  private final String m_mnemonic;

  /**
   * Operands of the instruction. Guaranteed to be non-null but can be empty because not all
   * instructions have operands.
   */
  private final List<COperandTree> m_operands;

  /**
   * Module the instruction belongs to.
   */
  private final INaviModule m_module;

  /**
   * Binary data of the instruction.
   */
  private final byte[] m_data;

  /**
   * Architecture identification string of the instruction.
   */
  private final String m_architecture;

  /**
   * SQL provider that is used to update this instruction in the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listeners that are notified when something in the instruction changes.
   */
  private final ListenerProvider<IInstructionListener> m_listeners =
      new ListenerProvider<IInstructionListener>();

  /**
   * Used to synchronize the global comment of this instruction.
   */
  private final CommentListener m_internalCommentListener = new InternalCommentListener();

  private boolean m_saved;

  /**
   * Creates a new instruction object.
   * 
   * @param module The module the instruction belongs to.
   * @param address The address of the instruction.
   * @param mnemonic The mnemonic of the instruction.
   * @param operands The operands of the instruction.
   * @param data Binary data of the instruction.
   * @param architecture Architecture identification string of the instruction.
   * @param provider The SQL provider that is used to synchronize the instruction with the database.
   */
  public CInstruction(final boolean saved, final INaviModule module, final IAddress address,
      final String mnemonic, final List<COperandTree> operands, final byte[] data,
      final String architecture, final SQLProvider provider) {
    m_module = Preconditions.checkNotNull(module, "IE00126: Module argument can not be null");
    m_address = Preconditions.checkNotNull(address, "IE00127: Address argument can not be null");
    m_mnemonic = Preconditions.checkNotNull(mnemonic, "IE00128: Mnemonic argument can not be null");
    Preconditions.checkArgument(!mnemonic.isEmpty(), "IE00129: Mnemonic argument can not be empty");
    Preconditions.checkNotNull(operands, "IE00130: Operands argument can not be null");
    verifyOperands(operands);
    Preconditions.checkNotNull(data, "IE02195: Data argument can not be null");
    m_architecture =
        Preconditions.checkNotNull(architecture, "IE02196: Architecture argument can not be null");
    Preconditions.checkArgument(!architecture.isEmpty(),
        "IE02197: Architecture argument can not be empty");
    m_provider =
        Preconditions.checkNotNull(provider, "IE00132: SQL provider argument can not be null");

    m_saved = saved;
    m_operands = new ArrayList<COperandTree>(operands);
    m_data = data.clone();

    CommentManager.get(m_provider).addListener(m_internalCommentListener);

    for (final COperandTree operand : operands) {
      operand.setNaviInstruction(this);
    }
  }

  /**
   * Clones the given operand list.
   * 
   * @param operands The operand list to clone.
   * 
   * @return The cloned operand list.
   */
  private static List<COperandTree> clone(final List<COperandTree> operands) {
    final List<COperandTree> clonedOperands = new FilledList<COperandTree>();

    for (final COperandTree operand : operands) {
      clonedOperands.add(operand.cloneTree());
    }

    return clonedOperands;
  }

  /**
   * Verifies a single node of an operand tree.
   * 
   * @param node The node to verify.
   */
  private static void verifyNode(final IZyTreeNode node) {
    Preconditions.checkNotNull(node, "IE00133: Operand tree node with value null detected");

    for (final IZyTreeNode child : node.getChildren()) {
      verifyNode(child);
    }
  }

  /**
   * Verifies the validity of the operand trees.
   * 
   * @param operands The operands of the instruction that must be verified.
   */
  private static void verifyOperands(final List<COperandTree> operands) {
    for (final IOperandTree operandTree : operands) {
      verifyNode(operandTree.getRootNode());
    }
  }

  @Override
  public void addListener(final IInstructionListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(commentText, "IE02536: comment argument can not be null");

    return CommentManager.get(m_provider).appendGlobalInstructionComment(this, commentText);
  }

  @Override
  public CInstruction cloneInstruction() {
    final CInstruction instruction =
        new CInstruction(m_saved, m_module, m_address, m_mnemonic, clone(m_operands), m_data,
            m_architecture, m_provider);

    CommentManager.get(m_provider).initializeGlobalInstructionComment(instruction,
        getGlobalComment());

    return instruction;
  }

  @Override
  public void close() {
    for (final COperandTree operand : m_operands) {
      operand.close();
    }

    CommentManager.get(m_provider).unloadGlobalInstructionComment(this, getGlobalComment());
    CommentManager.get(m_provider).removeListener(m_internalCommentListener);
  }

  @Override
  public void deleteGlobalComment(final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(comment, "IE02537: comment argument can not be null");

    CommentManager.get(m_provider).deleteGlobalInstructionComment(this, comment);
  }

  @Override
  public IComment editGlobalComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(comment, "IE02538: comment argument can not be null");

    return CommentManager.get(m_provider).editGlobalInstructionComment(this, comment, commentText);
  }

  @Override
  public IAddress getAddress() {
    return m_address;
  }

  @Override
  public String getArchitecture() {
    return m_architecture;
  }

  @Override
  public byte[] getData() {
    return m_data.clone();
  }

  @Override
  public List<IComment> getGlobalComment() {
    return CommentManager.get(m_provider).getGlobalInstructionComment(this);
  }

  @Override
  public String getInstructionString() {
    final StringBuffer stringBuffer = new StringBuffer();

    stringBuffer.append(m_mnemonic);
    stringBuffer.append(' ');

    for (int i = 0; i < m_operands.size(); ++i) {
      stringBuffer.append(m_operands.get(i).toString());

      if (i != (m_operands.size() - 1)) {
        stringBuffer.append(", ");
      }
    }

    return stringBuffer.toString();
  }

  @Override
  public long getLength() {
    return m_data.length;
  }

  @Override
  public String getMnemonic() {
    return m_mnemonic;
  }

  @Override
  public Integer getMnemonicCode() {
    return m_mnemonic.hashCode();
  }

  @Override
  public INaviModule getModule() {
    return m_module;
  }

  @Override
  public int getOperandPosition(final INaviOperandTree operand) {
    return m_operands.indexOf(operand);
  }

  @Override
  public List<COperandTree> getOperands() {
    return new ArrayList<COperandTree>(m_operands);
  }

  @Override
  public void initializeGlobalComment(final ArrayList<IComment> comment) {
    Preconditions.checkNotNull(comment, "IE00134: Instruction comment can not be null");
    CommentManager.get(m_provider).initializeGlobalInstructionComment(this, comment);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(m_provider);
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public boolean isStored() {
    return m_saved;
  }

  @Override
  public void removeListener(final IInstructionListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setSaved(final boolean saved) {
    m_saved = saved;
  }

  @Override
  public String toString() {
    final StringBuffer stringBuffer = new StringBuffer(m_address.toHexString());

    stringBuffer.append(' ');
    stringBuffer.append(m_mnemonic);
    stringBuffer.append(' ');

    for (int i = 0; i < m_operands.size(); ++i) {
      stringBuffer.append(m_operands.get(i).toString());

      if (i != (m_operands.size() - 1)) {
        stringBuffer.append(", ");
      }
    }

    return stringBuffer.toString();
  }

  /**
   * Used to synchronize the global comment of this instruction.
   */
  private class InternalCommentListener extends CommentListenerAdapter {
    @Override
    public void appendedGlobalInstructionComment(final INaviInstruction instruction,
        final IComment comment) {
      if (instruction.getAddress().equals(getAddress())) {
        for (final IInstructionListener listener : m_listeners) {
          try {
            listener.appendedComment(instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedGlobalInstructionComment(final INaviInstruction instruction,
        final IComment comment) {
      if (instruction.getAddress().equals(getAddress())) {
        for (final IInstructionListener listener : m_listeners) {
          try {
            listener.deletedComment(instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedGlobalInstructionComment(final INaviInstruction instruction,
        final IComment comment) {
      if (instruction.getAddress().equals(getAddress())) {
        for (final IInstructionListener listener : m_listeners) {
          try {
            listener.editedComment(instruction, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedGlobalInstructionComments(final INaviInstruction instruction,
        final List<IComment> comments) {
      if (instruction.getAddress().equals(getAddress())) {
        for (final IInstructionListener listener : m_listeners) {
          try {
            listener.initializedComment(instruction, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
