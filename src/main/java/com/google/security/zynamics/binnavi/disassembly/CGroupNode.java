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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;

/**
 * Represents a node that can be used to group other nodes of the graph. Group nodes can be
 * collapsed to hide their member nodes in the graph and they can be expanded again to show their
 * member nodes.
 */
public final class CGroupNode extends CNaviViewNode implements INaviGroupNode {
  /**
   * Listeners that are notified about changes in the group node.
   */
  private final ListenerProvider<INaviGroupNodeListener> m_listeners =
      new ListenerProvider<INaviGroupNodeListener>();

  /**
   * Flag that indicates whether the group node is currently collapsed or not.
   */
  private boolean m_collapsed;

  /**
   * SQL provider for storing the node in the database.
   */
  private final SQLProvider m_provider;

  /**
   * Nodes that are members of the group node.
   */
  private final List<INaviViewNode> m_elements = new ArrayList<INaviViewNode>();

  /**
   * Listener that keeps the code node synchronized with changes in the comment manager.
   */
  private final CommentListener m_internalCommentListener = new InternalCommentListener();

  /**
   * Creates a new group node object.
   * 
   * @param nodeId ID of the node.
   * @param x X-Coordinate of the node in the graph.
   * @param y Y-Coordinate of the node in the graph.
   * @param width The width of the node in the graph.
   * @param height The height of the node in the graph.
   * @param color Background color of the node.
   * @param selected Flag that indicates whether the node is selected or not.
   * @param visible Flag that indicates whether the node is visible or not.
   * @param tags Tags the node is tagged with.
   * @param comment Comment shown in the node .when the group node is in collapsed state.
   * @param collapsed Flag that indicates whether the group node is currently collapsed or not.
   * @param provider SQL provider for storing the node in the database.
   */
  public CGroupNode(final int nodeId, final double x, final double y, final double width,
      final double height, final Color color, final boolean selected, final boolean visible,
      final Set<CTag> tags, final List<IComment> comment, final boolean collapsed,
      final SQLProvider provider) {
    super(nodeId, x, y, width, height, color, color.darker().darker(), selected, visible, tags,
        provider);

    m_collapsed = collapsed;
    m_provider = Preconditions.checkNotNull(provider, "IE02535: provider argument can not be null");

    CommentManager.get(m_provider).initializeGroupNodeComment(this,
        comment == null ? Lists.<IComment>newArrayList() : comment);
    CommentManager.get(m_provider).addListener(m_internalCommentListener);
  }

  @Override
  public void addElement(final INaviViewNode node) {
    Preconditions.checkNotNull(node, "IE00107: Node argument can not be null");
    Preconditions.checkArgument(!m_elements.contains(node),
        "IE00102: Can not add node more than once");
    Preconditions.checkArgument(node.getParentGroup() == null,
        "IE00103: Node already belongs to a group");

    m_elements.add(node);

    node.setParentGroup(this);

    for (final INaviGroupNodeListener listener : m_listeners) {
      try {
        listener.addedElement(this, node);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addGroupListener(final INaviGroupNodeListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException {
    return CommentManager.get(m_provider).appendGroupNodeComment(this, commentText);
  }

  @Override
  public CGroupNode cloneNode() {
    return new CGroupNode(-1, getX(), getY(), getWidth(), getHeight(), getColor(), isSelected(),
        isVisible(), getTags(), getComments(), isCollapsed(), m_provider);
  }

  @Override
  public void deleteComment(final IComment comment) throws CouldntDeleteException {
    CommentManager.get(m_provider).deleteGroupNodeComment(this, comment);
  }

  @Override
  public IComment editComment(final IComment oldComment, final String newComment)
      throws CouldntSaveDataException {
    return CommentManager.get(m_provider).editGroupNodeComment(this, oldComment, newComment);
  }

  @Override
  public List<INaviViewNode> getChildren() {
    final Set<INaviViewNode> children = new HashSet<INaviViewNode>();

    for (final INaviViewNode element : m_elements) {
      final List<INaviViewNode> outsideChildren =
          CollectionHelpers.filter(element.getChildren(), new ICollectionFilter<INaviViewNode>() {
            @Override
            public boolean qualifies(final INaviViewNode node) {
              return node.getParentGroup() != CGroupNode.this;
            }
          });

      children.addAll(outsideChildren);
    }

    return new ArrayList<INaviViewNode>(children);
  }

  @Override
  public List<IComment> getComments() {
    return CommentManager.get(m_provider).getGroupNodeComment(this);
  }

  @Override
  public List<INaviViewNode> getElements() {
    return new ArrayList<INaviViewNode>(m_elements);
  }

  @Override
  public List<INaviEdge> getIncomingEdges() {
    final List<INaviEdge> edges = new ArrayList<INaviEdge>();

    for (final INaviViewNode member : m_elements) {
      final List<INaviEdge> outsideEdges =
          CollectionHelpers.filter(member.getIncomingEdges(), new ICollectionFilter<INaviEdge>() {
            @Override
            public boolean qualifies(final INaviEdge edge) {
              return edge.getSource().getParentGroup() != CGroupNode.this;
            }
          });

      edges.addAll(outsideEdges);
    }

    return edges;
  }

  @Override
  public int getNumberOfElements() {
    return m_elements.size();
  }

  @Override
  public List<INaviEdge> getOutgoingEdges() {
    final List<INaviEdge> edges = new ArrayList<INaviEdge>();

    for (final INaviViewNode member : m_elements) {
      final List<INaviEdge> outsideEdges =
          CollectionHelpers.filter(member.getOutgoingEdges(), new ICollectionFilter<INaviEdge>() {
            @Override
            public boolean qualifies(final INaviEdge edge) {
              return edge.getTarget().getParentGroup() != CGroupNode.this;
            }
          });

      edges.addAll(outsideEdges);
    }

    return edges;
  }

  @Override
  public List<INaviViewNode> getParents() {
    final Set<INaviViewNode> parents = new HashSet<INaviViewNode>();

    for (final INaviViewNode element : m_elements) {
      final List<INaviViewNode> outsideParents =
          CollectionHelpers.filter(element.getParents(), new ICollectionFilter<INaviViewNode>() {
            @Override
            public boolean qualifies(final INaviViewNode node) {
              return node.getParentGroup() != CGroupNode.this;
            }
          });

      parents.addAll(outsideParents);
    }

    return new ArrayList<INaviViewNode>(parents);
  }

  @Override
  public void initializeComment(final List<IComment> comments) {
    CommentManager.get(m_provider).initializeGroupNodeComment(this, comments);
  }

  @Override
  public boolean isCollapsed() {
    return m_collapsed;
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return CUserManager.get(m_provider).isOwner(comment);
  }

  @Override
  public boolean isStored() {
    return super.isStored();
  }

  @Override
  public void removeElement(final INaviViewNode node) {
    Preconditions.checkNotNull(node, "IE00104: Node argument can not be null");
    Preconditions.checkArgument(m_elements.remove(node), "IE00105: Node is not part of this group");

    node.setParentGroup(null);

    for (final INaviGroupNodeListener listener : m_listeners) {
      try {
        listener.removedElement(this, node);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void removeGroupListener(final INaviGroupNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setCollapsed(final boolean collapsed) {
    if (collapsed == m_collapsed) {
      return;
    }

    if (collapsed) {
      for (final INaviViewNode node : m_elements) {
        node.setVisible(true);
      }
    }

    m_collapsed = collapsed;

    for (final INaviGroupNodeListener listener : m_listeners) {
      try {
        listener.changedState(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void close() {
    super.close();
    CommentManager.get(m_provider).unloadGroupNodeComment(this, getComments());
    CommentManager.get(m_provider).removeListener(m_internalCommentListener);
  }

  /**
   * Updates the node when global comments associated with the node change.
   */
  private class InternalCommentListener extends CommentListenerAdapter {

    @Override
    public void appendedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      if (CGroupNode.this.equals(node)) {
        for (final INaviGroupNodeListener listener : m_listeners) {
          try {
            listener.appendedGroupNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void deletedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      if (CGroupNode.this.equals(node)) {
        for (final INaviGroupNodeListener listener : m_listeners) {
          try {
            listener.deletedGroupNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void editedGroupNodeComment(final INaviGroupNode node, final IComment comment) {
      if (CGroupNode.this.equals(node)) {
        for (final INaviGroupNodeListener listener : m_listeners) {
          try {
            listener.editedGroupNodeComment(node, comment);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }

    @Override
    public void initializedGroupNodeComments(final INaviGroupNode node,
        final List<IComment> comments) {
      if (CGroupNode.this.equals(node)) {
        for (final INaviGroupNodeListener listener : m_listeners) {
          try {
            listener.initializedGroupNodeComment(node, comments);
          } catch (final Exception exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      }
    }
  }
}
