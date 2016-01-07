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
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a code node inside a view.
 */
public final class CCodeNode extends CNaviViewNode implements INaviCodeNode {
  /**
   * Used for speed optimization.
   */
  private final List<INaviInstruction> codeNodeInstructions = new ArrayList<INaviInstruction>();

  /**
   * The parent function of the code node.
   */
  private final INaviFunction m_parentFunction;

  /**
   * SQL provider that is used to write changes in the nodes to the database.
   */
  private final SQLProvider m_provider;

  /**
   * Encapsulates all comments associated with the code node.
   */
  private final CCodeNodeComments m_comments;

  /**
   * Listeners that are notified about changes in the code node.
   */
  private final ListenerProvider<INaviCodeNodeListener> m_listeners =
      new ListenerProvider<INaviCodeNodeListener>();

  // ESCA-JAVA0138:
  /**
   * Creates a new code node object.
   * 
   * @param nodeId The ID of the node.
   * @param x The X position of the node in the graph.
   * @param y The Y position of the node in the graph.
   * @param width The width of the node in the graph.
   * @param height The height of the node in the graph.
   * @param color The background color of the node.
   * @param selected The selection state of the node.
   * @param visible The visibility state of the node.
   * @param localComment The local comment of the node.
   * @param parentFunction The parent function of the node.
   * @param borderColor Border color of the node.
   * @param tags Tags the node is tagged with.
   * @param provider SQL provider that is used to write changes in the nodes to the database.
   */
  public CCodeNode(final int nodeId, final double x, final double y, final double width,
      final double height, final Color color, final Color borderColor, final boolean selected,
      final boolean visible, final List<IComment> localComment, final INaviFunction parentFunction,
      final Set<CTag> tags, final SQLProvider provider) {
    super(nodeId, x, y, width, height, color, borderColor, selected, visible, tags, provider);

    m_parentFunction = parentFunction;
    m_provider = provider;
    m_comments =
        new CCodeNodeComments(this, m_parentFunction, localComment, m_listeners, m_provider);
  }

  @Override
  public void addInstruction(final INaviInstruction instruction, final List<IComment> localComment) {
    Preconditions.checkNotNull(instruction, "IE00056: Instruction argument can not be null");

    codeNodeInstructions.add(instruction);

    if (localComment != null) {
      m_comments.initializeLocalInstructionComment(instruction, localComment);
    }

    for (final INaviCodeNodeListener listener : m_listeners) {
      try {
        listener.addedInstruction(this, instruction);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final INaviCodeNodeListener listener) {
    super.addListener(listener);
    m_listeners.addListener(listener);
  }

  @Override
  public CCodeNode cloneNode() {
    final CCodeNode codeNode =
        new CCodeNode(-1, getX(), getY(), getWidth(), getHeight(), getColor(), getBorderColor(),
            isSelected(), isVisible(), m_comments.getLocalCodeNodeComment(), m_parentFunction,
            getTags(), m_provider);

    for (final INaviInstruction instruction : codeNodeInstructions) {
      codeNode.addInstruction(instruction.cloneInstruction(),
          m_comments.getLocalInstructionComment(instruction));
    }

    return codeNode;
  }

  @Override
  public void close() {
    super.close();
    for (final INaviInstruction instruction : codeNodeInstructions) {
      instruction.close();
      CommentManager.get(m_provider).unloadLocalnstructionComment(this, instruction,
          m_comments.getLocalInstructionComment(instruction));
    }
    m_comments.dispose();
  }

  @Override
  public IAddress getAddress() {
    return codeNodeInstructions.get(0).getAddress();
  }

  @Override
  public CCodeNodeComments getComments() {
    return m_comments;
  }

  @Override
  public Iterable<INaviInstruction> getInstructions() {
    return codeNodeInstructions;
  }

  @Override
  public INaviInstruction getLastInstruction() {
    return codeNodeInstructions.isEmpty() ? null : codeNodeInstructions.get(codeNodeInstructions
        .size() - 1);
  }

  @Override
  public INaviFunction getParentFunction() throws MaybeNullException {
    if (m_parentFunction == null) {
      throw new MaybeNullException();
    }

    return m_parentFunction;
  }

  @Override
  public boolean hasInstruction(final INaviInstruction instruction) {
    return codeNodeInstructions.contains(instruction);
  }

  @Override
  public int instructionCount() {
    return codeNodeInstructions.size();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public void removeInstruction(final INaviInstruction instruction) {
    Preconditions.checkNotNull(instruction, "IE00062: Instruction argument can not be null");
    Preconditions.checkArgument(codeNodeInstructions.contains(instruction),
        "IE00063: Instruction is not part of this node");

    codeNodeInstructions.remove(instruction);

    for (final INaviCodeNodeListener listener : m_listeners) {
      try {
        listener.removedInstruction(this, instruction);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeListener(final INaviCodeNodeListener listener) {
    super.removeListener(listener);
    m_listeners.removeListener(listener);
  }

  @Override
  public void setInstructionColor(final INaviInstruction instruction, final int level,
      final Color color) {
    Preconditions.checkNotNull(instruction, "IE01264: Instruction argument can not be null");
    Preconditions.checkArgument(codeNodeInstructions.contains(instruction),
        "IE01276: Instruction does not belong to the code node");

    for (final INaviCodeNodeListener listener : m_listeners) {
      try {
        listener.changedInstructionColor(this, instruction, level, color);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder description = new StringBuilder("Code Node " + getId() + "\n");

    for (final IInstruction instruction : codeNodeInstructions) {
      description.append("  ");
      description.append(instruction.toString());
      description.append('\n');
    }

    return description.toString();
  }
}
