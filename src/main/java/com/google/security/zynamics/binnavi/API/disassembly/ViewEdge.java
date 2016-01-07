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
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.graphs.DefaultEdge;



// / Represents a single edge in a view.
/**
 * Class used to represent edges in graph views.
 */
public final class ViewEdge extends DefaultEdge<ViewNode> implements ApiObject<INaviEdge> {
  /**
   * Wrapped internal view edge object.
   */
  private final INaviEdge m_edge;

  /**
   * Listeners that are notified about changes in the edge.
   */
  private final ListenerProvider<IViewEdgeListener> m_listeners =
      new ListenerProvider<IViewEdgeListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API edge object.
   * 
   * @param edge Wrapped internal edge object.
   * @param source Source node of the edge.
   * @param target Target node of the edge.
   */
  // / @endcond
  public ViewEdge(final INaviEdge edge, final ViewNode source, final ViewNode target) {
    super(source, target);

    m_edge = Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
  }

  // / @cond INTERNAL
  /**
   * Returns the listeners attached to this object. This function was introduced for performance
   * reasons to cut down on the number of listeners attached to edges. API Edge listener
   * notification is now handled elsewhere.
   * 
   * @return The listeners attached to this object.
   */
  // / @endcond
  protected ListenerProvider<IViewEdgeListener> getListeners() {
    return m_listeners;
  }

  @Override
  public INaviEdge getNative() {
    return m_edge;
  }

  // ! Adds a view edge listener.
  /**
   * Adds an object that is notified about changes in the view edge.
   * 
   * @param listener The listener object that is notified about changes in the view edge.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the view
   *         edge.
   */
  public void addListener(final IViewEdgeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Returns the color of the edge.
  /**
   * Returns the color of the edge.
   * 
   * @return The color of the edge.
   */
  public Color getColor() {
    return m_edge.getColor();
  }

  // ESCA-JAVA0059: This method stays for the JavaDoc comment
  // ! Source block of the edge.
  /**
   * Returns the source block of the edge.
   * 
   * @return The source block of the edge.
   */
  @Override
  public ViewNode getSource() {
    return super.getSource();
  }

  // ESCA-JAVA0059: This method stays for the JavaDoc comment
  // ! Target block of the edge.
  /**
   * Returns the target block of the edge.
   * 
   * @return The target block of the edge.
   */
  @Override
  public ViewNode getTarget() {
    return super.getTarget();
  }

  // ! Type of the edge.
  /**
   * Returns the type of the edge.
   * 
   * @return The type of the edge.
   */
  public EdgeType getType() {
    return EdgeType.convert(m_edge.getType());
  }

  // ! Visibility state of the edge.
  /**
   * Determines whether the edge is visible or not.
   * 
   * @return True, if the edge is visible. False, if not.
   */
  public boolean isVisible() {
    return m_edge.isVisible();
  }

  // ! Removes a view edge listener.
  /**
   * Removes a listener object from the view edge.
   * 
   * @param listener The listener object to remove from the view edge.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the view edge.
   */
  public void removeListener(final IViewEdgeListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the color of the edge.
  /**
   * Changes the color of the edge.
   * 
   * @param color The new color of the edge.
   */
  public void setColor(final Color color) {
    m_edge.setColor(color);
  }

  // ! Changes the visibility state of the edge.
  /**
   * Used to show or hide the edge.
   * 
   * @param value True to show the edge; false to hide it.
   */
  public void setVisible(final boolean value) {
    m_edge.setVisible(value);
  }

  // ! Printable representation of the edge.
  /**
   * Returns the string representation of the edge.
   * 
   * @return The string representation of the edge.
   */
  @Override
  public String toString() {
    return String.format("View Edge [%s -> %s]", super.getSource().getName(), super.getTarget()
        .getName());
  }

  // ! Delete a local edge comment.
  /**
   * Delete a local edge comment.
   * 
   * @param comment The local comment to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteLocalComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_edge.deleteLocalComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Append a local edge comment.
  /**
   * Append a local edge comment.
   * 
   * @param comment The string for the comment to append.
   * @return The List of local comments currently associated to the edge after the append has been
   *         successful.
   * 
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendLocalComment(final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_edge.appendLocalComment(comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Initialize the local edge comments.
  /**
   * Initialize the local edge comments.
   * 
   * @param comments The list of local comments to associate to the edge.
   */
  public void initializeLocalComment(final List<IComment> comments) {
    m_edge.initializeLocalComment(comments);
  }

  // ! edit a local edge comment.
  /**
   * Edit a local edge comment.
   * 
   * @param comment The comment to edit
   * @param newComment The new comment text.
   * 
   * @return The edited comment.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   */
  public IComment editLocalComment(final IComment comment, final String newComment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    try {
      return m_edge.editLocalComment(comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    }
  }

  // ! Delete a local edge comment.
  /**
   * Delete a local edge comment.
   * 
   * @param comment The local comment to delete.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException
   */
  public void deleteGlobalComment(final IComment comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException {
    try {
      m_edge.deleteGlobalComment(comment);
    } catch (final CouldntDeleteException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntDeleteException(
          exception);
    }
  }

  // ! Append a local edge comment.
  /**
   * Append a local edge comment.
   * 
   * @param comment The string for the comment to append.
   * @return The List of local comments currently associated to the edge after the append has been
   *         successful.
   * 
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendGlobalComment(final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_edge.appendGlobalComment(comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Initialize the local edge comments.
  /**
   * Initialize the local edge comments.
   * 
   * @param comments The list of local comments to associate to the edge.
   */
  public void initializeGlobalComment(final List<IComment> comments) {
    m_edge.initializeGlobalComment(comments);
  }

  // ! edit a local edge comment.
  /**
   * Edit a local edge comment.
   * 
   * @param comment The comment to edit
   * @param newComment The new comment text.
   * 
   * @return The edited comment.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   */
  public IComment editGlobalComment(final IComment comment, final String newComment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException {
    try {
      return m_edge.editGlobalComment(comment, newComment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    }
  }
}
