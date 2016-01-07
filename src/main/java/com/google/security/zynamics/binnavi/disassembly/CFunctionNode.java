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
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNodeListener;

import java.awt.Color;
import java.util.List;
import java.util.Set;

/**
 * Represents a function node inside a view.
 */
public final class CFunctionNode extends CNaviViewNode implements INaviFunctionNode {
  /**
   * The function that is represented by the node.
   */
  private final INaviFunction m_function;

  /**
   * SQL provider that writes changes of the node to the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listener that keeps the code node synchronized with changes in the comment manager.
   */
  private final CommentListener m_internalCommentListener = new InternalCommentListener();

  /**
   * Creates a new function node.
   *
   * @param nodeId The ID of the node.
   * @param function The function that is represented by the node.
   * @param x The X coordinate of the node in the view.
   * @param y The Y coordinate of the node in the view.
   * @param width The width of the node in the graph.
   * @param height The height of the node in the graph.
   * @param color The background color of the node.
   * @param selected Selection state of the node.
   * @param visible Visibility state of the node.
   * @param localComments Local comment of the node.
   * @param tags Tags the node is tagged with.
   * @param provider SQL provider that writes changes of the node to the database.
   */
  public CFunctionNode(final int nodeId, final INaviFunction function, final double x,
      final double y, final double width, final double height, final Color color,
      final boolean selected, final boolean visible, final List<IComment> localComments,
      final Set<CTag> tags, final SQLProvider provider) {
    super(nodeId, x, y, width, height, color, Color.BLACK, selected, visible, tags, provider);

    m_function = Preconditions.checkNotNull(function, "IE00086: Function argument can not be null");
    m_provider = Preconditions.checkNotNull(provider, "IE02390: provider argument can not be null");

    CommentManager.get(m_provider).addListener(m_internalCommentListener);
    CommentManager.get(m_provider).initializeFunctionNodeComment(this, localComments);
  }

  @Override
  public List<IComment> appendLocalFunctionComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    return CommentManager.get(m_provider).appendFunctionNodeComment(this, commentText);
  }

  @Override
  public CFunctionNode cloneNode() {
    return new CFunctionNode(1, getFunction(), getX(), getY(), getWidth(), getHeight(), getColor(),
        isSelected(), isVisible(), getLocalFunctionComment(), getTags(), m_provider);
  }

  @Override
  public void deleteLocalFunctionComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteFunctionNodeComment(this, comment);
  }

  @Override
  public IComment editLocalFunctionComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {

    return CommentManager.get(m_provider).editFunctionNodeComment(this, oldComment, commentText);
  }

  @Override
  public IAddress getAddress() {
    return m_function.getAddress();
  }

  @Override
  public List<IComment> getLocalFunctionComment() {
    return CommentManager.get(m_provider).getFunctionNodeComment(this);
  }

  @Override
  public INaviFunction getFunction() {
    return m_function;
  }

  @Override
  public List<IComment> getLocalComment() {
    return getLocalFunctionComment();
  }

  @Override
  public void initializeLocalFunctionComment(final List<IComment> comments) {
    CommentManager.get(m_provider).initializeFunctionNodeComment(this, comments);
  }

  @Override
  public boolean isOwner(final IComment comment) {
    Preconditions.checkNotNull(comment, "IE02534: comment argument can not be null");
    return CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public boolean isStored() {
    return super.isStored();
  }

  @Override
  public void close() {
    super.close();
    CommentManager.get(m_provider).unloadFunctionNodeComment(this, getLocalFunctionComment());
    CommentManager.get(m_provider).removeListener(m_internalCommentListener);
  }

  /**
   * Updates the node when global comments associated with the node change.
   */
  private class InternalCommentListener extends CommentListenerAdapter {

    @Override
    public void appendedFunctionNodeComment(final INaviFunctionNode functionNode,
        final IComment comment) {
      if (CFunctionNode.this.equals(functionNode)) {
        for (final IViewNodeListener listener : getListeners()) {
          if (listener instanceof INaviFunctionNodeListener) {
            try {
              ((INaviFunctionNodeListener) listener).appendedFunctionNodeComment(functionNode,
                  comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void deletedFunctionNodeComment(final INaviFunctionNode functionNode,
        final IComment comment) {
      if (CFunctionNode.this.equals(functionNode)) {
        for (final IViewNodeListener listener : getListeners()) {
          if (listener instanceof INaviFunctionNodeListener) {
            try {
              ((INaviFunctionNodeListener) listener).deletedFunctionNodeComment(functionNode,
                  comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void editedFunctionNodeComment(final INaviFunctionNode functionNode,
        final IComment comment) {
      if (CFunctionNode.this.equals(functionNode)) {
        for (final IViewNodeListener listener : getListeners()) {
          if (listener instanceof INaviFunctionNodeListener) {
            try {
              ((INaviFunctionNodeListener) listener).editedFunctionNodeComment(functionNode,
                  comment);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }

    @Override
    public void initializedFunctionNodeComments(final INaviFunctionNode functionNode,
        final List<IComment> comments) {
      if (CFunctionNode.this.equals(functionNode)) {
        for (final IViewNodeListener listener : getListeners()) {
          if (listener instanceof INaviFunctionNodeListener) {
            try {
              ((INaviFunctionNodeListener) listener).initializedFunctionNodeComment(functionNode,
                  comments);
            } catch (final Exception exception) {
              CUtilityFunctions.logException(exception);
            }
          }
        }
      }
    }
  }
}
