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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

// / Represents a node that represents a function in a view.
/**
 * Represents a function node in a view.
 */
public final class FunctionNode extends ViewNode {
  /**
   * Wrapped internal function node object.
   */
  private final INaviFunctionNode m_node;

  /**
   * Function represented by the function node.
   */
  private final Function m_function;

  /**
   * Listeners that are notified about changes in the function node.
   */
  private final ListenerProvider<IFunctionNodeListener> m_listeners =
      new ListenerProvider<IFunctionNodeListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API function node object.
   *
   * @param view View the function node belongs to.
   * @param node Wrapped internal function node object.
   * @param function Function represented by the function node.
   * @param tagManager Tag manager used to tag the function node.
   */
  // / @endcond
  public FunctionNode(final View view, final INaviFunctionNode node, final Function function,
      final TagManager tagManager) {
    super(view, node, tagManager);

    m_function = Preconditions.checkNotNull(function, "Error: Function argument can not be null");

    m_node = node;
  }

  // / @cond INTERNAL
  /**
   * Returns the function listeners attached to this object. This function was introduced for
   * performance reasons to cut down on the number of listeners attached to function nodes. API
   * function node listener notification is now handled elsewhere.
   *
   * @return The function listeners attached to this object.
   */
  // / @endcond
  protected ListenerProvider<IFunctionNodeListener> getFunctionListeners() {
    return m_listeners;
  }

  @Override
  protected String getName() {
    return toString();
  }

  // ! Adds a function node listener.
  /**
   * Adds an object that is notified about changes in the function node.
   *
   * @param listener The listener object that is notified about changes in the function node.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         function node.
   */
  public void addListener(final IFunctionNodeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Appends a comment to a function node.
  /**
   * Appends a new comment to the node.
   *
   * @param comment The appended node comment.
   *
   * @throws CouldntSaveDataException Thrown if the comment could not be written to the database.
   * @throws CouldntLoadDataException Thrown if the comment could not be loaded after storing it.
   */
  public List<IComment> appendComment(final String comment) throws CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {

    final List<IComment> comments = new ArrayList<IComment>();

    try {
      comments.addAll(m_node.appendLocalFunctionComment(comment));
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }

    for (final IFunctionNodeListener listener : m_listeners) {
      try {
        listener.appendedComment(this, Iterables.getLast(comments));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return comments;
  }

  // ! Deletes a comment from the comments of a function node.
  /**
   * Deletes a comment from the comments of a function node.
   *
   * @param comment The comment which is deleted from the function node.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    try {
      m_node.deleteLocalFunctionComment(comment);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException exception) {
      throw new CouldntDeleteException(exception);
    }

    for (final IFunctionNodeListener listener : m_listeners) {
      try {
        listener.deletedComment(this, comment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  // ! Edits a comment of a function node.
  /**
   * Edits a comment of a function node.
   *
   * @param comment The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public IComment editComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException {

    IComment editedComment = null;

    try {
      editedComment = m_node.editLocalFunctionComment(comment, commentText);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException exception) {
      throw new CouldntSaveDataException(exception);
    }

    for (final IFunctionNodeListener listener : m_listeners) {
      try {
        listener.editedComment(this, editedComment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return editedComment;
  }

  // ! Comment of the function node.
  /**
   * Returns the comment associated with the node.
   *
   * @return The comment associated with the node.
   */
  public List<?> getComment() {
    return m_node.getLocalFunctionComment();
  }

  // ! Function represented by this node.
  /**
   * Returns the function represented by this node.
   *
   * @return The function represented by this node.
   */
  public Function getFunction() {
    return m_function;
  }

  // ! Initializes comments of a function node
  /**
   * Initializes the comments of a function node
   *
   * @param comments The comments of the function node.
   */
  public void initializeComment(final ArrayList<IComment> comments) {
    m_node.initializeLocalFunctionComment(comments);

    for (final IFunctionNodeListener listener : m_listeners) {
      try {
        listener.initializedComment(this, comments);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener object from the function node.
   *
   * @param listener The listener object to remove from the function node.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the function
   *         node.
   */
  public void removeListener(final IFunctionNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Printable representation of the function node.
  /**
   * Returns the string representation of the function node.
   *
   * @return The string representation of the function node.
   */
  @Override
  public String toString() {
    return String.format("Function Node ['%s']", m_node.getFunction().getName());
  }
}
