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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNodeListener;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// / Represents a view node that can be used to group other view nodes.
/**
 * A group node can be used to group a subset of the nodes of a view. Using group nodes it is
 * possible to put all nodes of a graph that share a common feature into a special kind of subgraph
 * inside the view.
 */
public final class GroupNode extends ViewNode {
  /**
   * Wrapped internal group node object.
   */
  private final INaviGroupNode m_node;

  /**
   * Elements in the group node.
   */
  private final List<ViewNode> m_elements = new ArrayList<ViewNode>();

  /**
   * Keeps the API group node object synchronized with the internal group node object.
   */
  private final InternalListener m_internalListener = new InternalListener();

  /**
   * Listeners that are notified about changes in the group node.
   */
  private final ListenerProvider<IGroupNodeListener> m_listeners =
      new ListenerProvider<IGroupNodeListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API group node object.
   * 
   * @param view View the group node belongs to.
   * @param node Wrapped internal group node object.
   * @param manager Tag manager used to tag the group node.
   */
  // / @endcond
  public GroupNode(final View view, final INaviGroupNode node, final TagManager manager) {
    super(view, node, manager);

    m_node = node;

    m_node.addGroupListener(m_internalListener);
  }

  /**
   * Adds an element to the group node.
   * 
   * @param node The node to add.
   * 
   * @return The API node added to the group node.
   */
  private ViewNode addElement(final INaviViewNode node) {
    final List<ViewNode> nodes = getView().getGraph().getNodes();

    for (final ViewNode viewNode : nodes) {
      if (viewNode.getNative() == node) {
        m_elements.add(viewNode);

        return viewNode;
      }
    }

    return null;
  }

  /**
   * Removes an element from the group node.
   * 
   * @param element The element to remove.
   * 
   * @return The removed API element.
   */
  private ViewNode removeElement(final INaviViewNode element) {
    for (final ViewNode viewNode : m_elements) {
      if (viewNode.getNative() == element) {
        m_elements.remove(viewNode);

        return viewNode;
      }
    }

    return null;
  }

  @Override
  protected String getName() {
    return String.format("Group Node [%d elements]", m_node.getNumberOfElements());
  }

  @Override
  public INaviGroupNode getNative() {
    return m_node;
  }

  // ! Adds a group node listener.
  /**
   * Adds an object that is notified about changes in the group node.
   * 
   * @param listener The listener object that is notified about changes in the group node.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the group
   *         node.
   */
  public void addListener(final IGroupNodeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Adds a node to the group node.
  /**
   * Adds a node to the group node.
   * 
   * @param node The node to add to the group node.
   */
  public void addNode(final ViewNode node) {
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");

    if (m_node.getElements().contains(node.getNative())) {
      if (!m_elements.contains(node)) {
        m_elements.add(node);
      }
    } else {
      m_node.addElement(node.getNative());
    }
  }

  // ! Append a group node comment.
  /**
   * Append a group node comment.
   * 
   * @param comment the comment to be appended.
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException
   * @throws com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException
   */
  public List<IComment> appendComment(final String comment)
      throws com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException,
      com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException {
    try {
      return m_node.appendComment(comment);
    } catch (final CouldntSaveDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException(
          exception);
    } catch (final CouldntLoadDataException exception) {
      throw new com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException(
          exception);
    }
  }

  // ! Delete a group node comment.
  /**
   * Delete a group node comment.
   * 
   * @param comment the {@link IComment} comment to be deleted.
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

  // ! Edit a group node comment.
  /**
   * Edit a group node comment.
   * 
   * @param comment The {@link IComment} which is edited.
   * @param newComment The new text for the comment.
   * 
   * @return The edited comment if successful null otherwise.
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

  // ! Text shown in collapsed state.
  /**
   * Returns the text that is displayed when the group node is collapsed.
   * 
   * @return The text that is displayed when the group node is collapsed.
   */
  public List<IComment> getComment() {
    return m_node.getComments();
  }

  // ! Nodes inside the group.
  /**
   * Returns all elements inside the group node.
   * 
   * @return A list of group node members.
   */
  public List<ViewNode> getElements() {
    return new ArrayList<ViewNode>(m_elements);
  }

  // ! Checks if the group node is collapsed.
  /**
   * Returns a flag that indicates whether the group node is collapsed or open.
   * 
   * @return True, to signal that the group node is collapsed. False, to signal that it is open.
   */
  public boolean isCollapsed() {
    return m_node.isCollapsed();
  }

  // ! Removes a group node listener.
  /**
   * Removes a listener object from the group node.
   * 
   * @param listener The listener object to remove from the group node.
   * 
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the group
   *         node.
   */
  public void removeListener(final IGroupNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Removes an element from the group node.
  /**
   * Removes an element from the group node.
   * 
   * @param element The element to be removed from the group node.
   */
  public void removeNode(final ViewNode element) {
    Preconditions.checkNotNull(element, "Error: Element argument can not be null");

    m_node.removeElement(element.getNative());
  }

  // ! Collapses or uncollapses the group node.
  /**
   * Collapses or uncollapses the group node.
   * 
   * @param collapsed True, to collapse the group node. False, to uncollapse it.
   */
  public void setCollapsed(final boolean collapsed) {
    m_node.setCollapsed(collapsed);
  }

  // ! Printable representation of the group node.
  /**
   * Returns a string representation of the group node.
   * 
   * @return A string representation of the group node.
   */
  @Override
  public String toString() {
    return getName();
  }

  /**
   * Keeps the API group node object synchronized with the internal group node object.
   */
  private class InternalListener implements INaviGroupNodeListener {
    @Override
    public void addedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
      final ViewNode apiNode = addElement(node);

      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.addedNode(GroupNode.this, apiNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void appendedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.appendedComment(GroupNode.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedState(final INaviGroupNode node) {
      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.changedState(GroupNode.this, isCollapsed());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.deletedComment(GroupNode.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.editedComment(GroupNode.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedGroupNodeComment(final INaviGroupNode node, final List<IComment> comment) {
      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.initializedComment(GroupNode.this, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void removedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
      final ViewNode apiNode = removeElement(node);

      m_elements.remove(apiNode);

      for (final IGroupNodeListener listener : m_listeners) {
        try {
          listener.removedNode(GroupNode.this, apiNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
