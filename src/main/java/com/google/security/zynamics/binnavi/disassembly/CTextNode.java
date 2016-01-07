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

import java.awt.Color;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Represents a node that can display an arbitrary comment text.
 */
public final class CTextNode extends CNaviViewNode implements INaviTextNode {

  /**
   * Synchronizes the text node with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listeners that are notified about changes in the text node.
   */
  private final ListenerProvider<INaviTextNodeListener> m_listeners =
      new ListenerProvider<INaviTextNodeListener>();


  /**
   * Listener that keeps the code node synchronized with changes in the comment manager.
   */
  private final CommentListener m_internalCommentListener = new InternalCommentListener();

  /**
   * Creates a new view node.
   * 
   * @param nodeId ID of the node.
   * @param x X-coordinate of the node.
   * @param y Y-coordinate of the node.
   * @param width The width of the node in the graph.
   * @param height The height of the node in the graph.
   * @param color Background color of the node.
   * @param selected Selection state of the node.
   * @param visible Visibility state of the node.
   * @param tags Tags the node is tagged with.
   * @param comments The comments shown in the node.
   * @param provider Synchronizes the node with the database.
   */
  public CTextNode(final int nodeId, final double x, final double y, final double width,
      final double height, final Color color, final boolean selected, final boolean visible,
      final Set<CTag> tags, final List<IComment> comments, final SQLProvider provider) {
    super(nodeId, x, y, width, height, color, color.darker().darker(), selected, visible, tags,
        provider);

    m_provider = Preconditions.checkNotNull(provider, "IE02393: provider argument can not be null");
    CommentManager.get(m_provider).initializeTextNodeComment(this, comments);
    CommentManager.get(m_provider).addListener(m_internalCommentListener);
  }

  @Override
  public void addListener(final INaviTextNodeListener listener) {
    super.addListener(listener);
    m_listeners.addListener(listener);
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    return CommentManager.get(m_provider).appendTextNodeComment(this, commentText);
  }

  @Override
  public CTextNode cloneNode() {
    return new CTextNode(-1, getX(), getY(), getWidth(), getHeight(), getColor(), isSelected(),
        isVisible(), getTags(), getComments(), m_provider);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteTextNodeComment(this, comment);
  }

  @Override
  public IComment editComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editTextNodeComment(this, oldComment, commentText);
  }

  @Override
  public List<IComment> getComments() {
    return CommentManager.get(m_provider).getTextNodeComment(this);
  }

  @Override
  public void initializeComment(final List<IComment> comments) {
    CommentManager.get(m_provider).initializeTextNodeComment(this, comments);
  }

  @Override
  public boolean isOwner(final IComment comment) {
    Preconditions.checkNotNull(comment, "IE02612: comment argument can not be null");
    return CUserManager.get(m_provider).getCurrentActiveUser().equals(comment.getUser());
  }

  @Override
  public void removeListener(final INaviTextNodeListener listener) {
    super.removeListener(listener);
    m_listeners.removeListener(listener);
  }

  @Override
  public void close() {
    super.close();
    CommentManager.get(m_provider).unloadTextNodeComment(this, getComments());
    CommentManager.get(m_provider).removeListener(m_internalCommentListener);
  }

  /**
   * Updates the node when global comments associated with the node change.
   */
  private class InternalCommentListener extends CommentListenerAdapter {

    @Override
    public void appendedTextNodeComment(final INaviTextNode node, final IComment comment) {
      if (CTextNode.this.equals(node)) {
        for (final INaviTextNodeListener listener : m_listeners) {
          try {
            listener.appendedTextNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedTextNodeComment(final INaviTextNode node, final IComment comment) {
      if (CTextNode.this.equals(node)) {
        for (final INaviTextNodeListener listener : m_listeners) {
          try {
            listener.deletedTextNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedTextNodeComment(final INaviTextNode node, final IComment comment) {
      if (CTextNode.this.equals(node)) {
        for (final INaviTextNodeListener listener : m_listeners) {
          try {
            listener.editedTextNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedTextNodeComments(final INaviTextNode node, final List<IComment> comments) {
      if (CTextNode.this.equals(node)) {
        for (final INaviTextNodeListener listener : m_listeners) {
          try {
            listener.initializedTextNodeComment(node, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
