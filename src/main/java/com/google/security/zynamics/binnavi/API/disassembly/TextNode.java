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

import java.util.List;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CNaviTextNodeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;



// / Represents nodes that contains simple text.
/**
 * Represents nodes that contain simple text. Examples for this kind of nodes are the comment nodes
 * that can be associated with arbitrary other nodes.
 */
public final class TextNode extends ViewNode {
  /**
   * Wrapped internal text node object.
   */
  private final INaviTextNode m_node;

  /**
   * Listeners that are notified about changes in the text node.
   */
  private final ListenerProvider<ITextNodeListener> m_listeners =
      new ListenerProvider<ITextNodeListener>();

  /**
   * Keeps the API text node object synchronized with the internal text node object.
   */
  private final InternalListener m_internalListener = new InternalListener();

  // / @cond INTERNAL
  /**
   * Creates a new API text node object.
   * 
   * @param view The view the text node belongs to.
   * @param node Wrapped internal text node object.
   * @param tagManager Tag manager used to tag the node.
   */
  // / @endcond
  public TextNode(final View view, final INaviTextNode node, final TagManager tagManager) {
    super(view, node, tagManager);

    m_node = node;

    node.addListener(m_internalListener);
  }

  @Override
  protected String getName() {
    return String.format("Text Node with: '%d' comments.", m_node.getComments().size());
  }

  // ! Adds a text node listener.
  /**
   * Adds a listener object that is notified about changes in the text node.
   * 
   * @param listener The listener that is added to the text node.
   */
  public void addListener(final ITextNodeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Appends a text node comment.
  /**
   * Appends a text node comment.
   * 
   * @param commentText The text for the newly generated comment.
   * 
   * @return The List of {@link IComment} comments currently associated to the text node.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendComment(final String commentText)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_node.appendComment(commentText);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Initialize the text node comments.
  /**
   * Initialize the text node comments.
   * 
   * @param comments The list of comments to associate with the text node.
   */
  public void initializeComment(final List<IComment> comments) {
    m_node.initializeComment(comments);
  }

  // ! Edits a text node comment.
  /**
   * Edit a text node comment.
   * 
   * @param comment The comment which is edited.
   * @param newComment The new text for the comment.
   * 
   * @return The edited comment if edit successful.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   */
  public IComment editComment(final IComment comment, final String newComment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    try {
      return m_node.editComment(comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    }
  }

  // ! Delete a text node comment.
  /**
   * Delete a text node comment.
   * 
   * @param comment The comment to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_node.deleteComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Text shown in the node.
  /**
   * Returns the comments that are displayed in the node.
   * 
   * @return The comments that are displayed in the node.
   */
  public List<IComment> getComments() {
    return m_node.getComments();
  }

  // ! Removes a text node listener.
  /**
   * Removes a listener object from the text node.
   * 
   * @param listener The listener that is removed from the text node.
   */
  public void removeListener(final ITextNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Keeps the API text node object synchronized with the internal text node object.
   */
  private class InternalListener extends CNaviTextNodeListenerAdapter {
    @Override
    public void changedText(final CTextNode node, final String text) {
      for (final ITextNodeListener listener : m_listeners) {
        // ESCA-JAVA0166: Catch Exception because we are calling a listener function.
        try {
          listener.changedText(TextNode.this);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
