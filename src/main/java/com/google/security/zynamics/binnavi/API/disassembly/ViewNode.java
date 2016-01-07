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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.general.ListenerProvider;

// / Represents a single node in a view.
/**
 * Represents a single node in a view.
 */
public abstract class ViewNode implements ApiObject<INaviViewNode>, IGraphNode<ViewNode> {

  /**
   * The view the node belongs to.
   */
  private final View m_view;

  /**
   * The internal node object which acts as the backend of the API node object.
   */
  private final INaviViewNode m_node;

  /**
   * Tag manager used to tag this node.
   */
  private final TagManager m_tagManager;

  /**
   * Listeners that are notified about changes in the API node object.
   */
  private final ListenerProvider<IViewNodeListener> m_listeners =
      new ListenerProvider<IViewNodeListener>();

  // / @cond INTERNAL
  /**
   * Creates a new view node object.
   *
   * @param view The view the node belongs to.
   * @param node The internal node object which acts as the backend of the API node object.
   * @param manager Tag manager used to tag this node.
   */
  // / @endcond
  protected ViewNode(final View view, final INaviViewNode node, final TagManager manager) {
    m_view = Preconditions.checkNotNull(view, "Error: View argument can not be null");
    m_node = Preconditions.checkNotNull(node, "Error: Node argument can't be null");
    m_tagManager = Preconditions.checkNotNull(manager, "Error: Manager argument can not be null");
  }

  /**
   * Converts a list of internal tag objects to API tag objects.
   *
   * @param tags The list of internal tag objects.
   *
   * @return The list of converted API tag objects.
   */
  private List<Tag> getTags(final Collection<CTag> tags) {
    final List<Tag> apiTags = new ArrayList<Tag>();

    for (final CTag tag : tags) {
      apiTags.add(m_tagManager.getTag(tag));
    }

    return apiTags;
  }

  // / @cond INTERNAL
  /**
   * Converts a list of internal view nodes to API view nodes.
   *
   * @param nodes The internal view nodes to convert.
   *
   * @return The converted API view nodes.
   */
  // / @endcond
  protected List<ViewNode> convert(final List<? extends INaviViewNode> nodes) {
    final List<ViewNode> out = new ArrayList<ViewNode>();

    for (final INaviViewNode node : nodes) {
      out.add(ObjectFinders.getObject(node, getView().getGraph().getNodes()));
    }

    return out;
  }

  // / @cond INTERNAL
  /**
   * Returns the listeners attached to this object. This function was introduced for performance
   * reasons to cut down on the number of listeners attached to view nodes. API view node listener
   * notification is now handled elsewhere.
   *
   * @return The listeners attached to this object.
   */
  // / @endcond
  protected ListenerProvider<IViewNodeListener> getListeners() {
    return m_listeners;
  }

  // / @cond INTERNAL
  /**
   * Returns the name of the node. This is pretty much a string representation of the node that is
   * used to build string representations of edges.
   *
   * @return The name of the node.
   */
  // / @endcond
  protected abstract String getName();

  // / @cond INTERNAL
  /**
   * Returns the view the node belongs to.
   *
   * @return The view the node belongs to.
   */
  // / @endcond
  protected View getView() {
    return m_view;
  }

  @Override
  public INaviViewNode getNative() {
    return m_node;
  }

  // ! Adds a view node listener.
  /**
   * Adds a listener object that is notified about changes in the view node.
   *
   * @param listener The listener that is added to the view node.
   */
  public void addListener(final IViewNodeListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Tags the node.
  /**
   * Tags a node with a given tag.
   *
   * @param tag The tag that is added to the node.
   *
   * @throws CouldntSaveDataException Thrown if the node could not be tagged.
   */
  public void addTag(final Tag tag) throws CouldntSaveDataException {
    Preconditions.checkNotNull(tag, "Error: Tag argument can't be null");

    try {
      m_node.tagNode(tag.getNative().getObject());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! The border color of the node.
  /**
   * Returns the current border color of the node.
   *
   * @return The current border color of the node.
   */
  public Color getBorderColor() {
    return m_node.getBorderColor();
  }

  // ! The child nodes of the node.
  /**
   * Returns the nodes of the view that are reachable by following the outgoing edges of the view
   * node.
   *
   * @return The reachable children of the view node.
   */
  @Override
  public List<ViewNode> getChildren() {
    return convert(m_node.getChildren());
  }

  // ! The color of the node.
  /**
   * Returns the current background color of the node.
   *
   * @return The current background color of the node.
   */
  public Color getColor() {
    return m_node.getColor();
  }

  // ! Returns the incoming edges of the view node.
  /**
   * Returns the incoming edges of the view node.
   *
   * @return The incoming edges of the view node.
   */
  public List<ViewEdge> getIncomingEdges() {
    final List<ViewEdge> edges = new ArrayList<ViewEdge>();

    for (final ViewEdge edge : m_view.getGraph().getEdges()) {
      if (edge.getTarget() == this) {
        edges.add(edge);
      }
    }

    return edges;
  }

  // ! Returns the outgoing edges of the view node.
  /**
   * Returns the ougoing edges of the view node.
   *
   * @return The outgoing edges of the view node.
   */
  public List<ViewEdge> getOutgoingEdges() {
    final List<ViewEdge> edges = new ArrayList<ViewEdge>();

    for (final ViewEdge edge : m_view.getGraph().getEdges()) {
      if (edge.getSource() == this) {
        edges.add(edge);
      }
    }

    return edges;
  }

  // ! Parent group of the node.
  /**
   * Returns the group node in which the node is contained. If the node does not belong to any
   * group, the return value of this method is null.
   *
   * @return Parent group node of the node or null.
   */
  public GroupNode getParentGroup() {
    final INaviGroupNode parentGroup = m_node.getParentGroup();

    return parentGroup == null ? null : (GroupNode) ObjectFinders.getObject(
        m_node.getParentGroup(), m_view.getGraph().getNodes());
  }

  // ! The parent nodes of the node.
  /**
   * Returns the nodes of the view that are reachable by following the incoming edges of the view
   * node.
   *
   * @return The reachable parents of the view node.
   */
  @Override
  public List<ViewNode> getParents() {
    return convert(m_node.getParents());
  }

  // ! Returns tags of the node.
  /**
   * Returns the tags that are currently associated with the node.
   *
   * @return A list of tags.
   */
  public List<Tag> getTags() {
    return getTags(m_node.getTags());
  }

  // ! X-position of the node.
  /**
   * Returns the current X position of the node in the view.
   *
   * @return The current X position of the node in the view.
   */
  public double getX() {
    return m_node.getX();
  }

  // ! Y-position of the node.
  /**
   * Returns the current Y position of the node in the view.
   *
   * @return The current Y position of the node in the view.
   */
  public double getY() {
    return m_node.getY();
  }

  // ! Selection state of the node.
  /**
   * Determines whether the node is selected or not.
   *
   * @return True, if the node is selected. False, otherwise.
   */
  public boolean isSelected() {
    return m_node.isSelected();
  }

  // ! Checks if the node is tagged with a given tag.
  /**
   * Determines whether the node is tagged with a given tag.
   *
   * @param tag The tag that is checked.
   *
   * @return True, if the node is tagged with the tag. False, otherwise.
   *
   * @throws IllegalArgumentException Thrown if the tag argument is null.
   */
  public boolean isTagged(final Tag tag) {
    Preconditions.checkNotNull(tag, "Error: Tag argument can not be null");

    return m_node.isTagged(tag.getNative().getObject());
  }

  // ! Visibility state of the node.
  /**
   * Determines whether the node is visible or not.
   *
   * @return True, if the node is visible. False, otherwise.
   */
  public boolean isVisible() {
    return m_node.isVisible();
  }

  // ! Removes a view node listener.
  /**
   * Removes a listener object from the view node.
   *
   * @param listener The listener object to remove from the node.
   */
  public void removeListener(final IViewNodeListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Removes a tag from the node.
  /**
   * Removes a tag from the node.
   *
   * @param tag The tag to remove from the node.
   *
   * @throws CouldntSaveDataException Thrown if the tag could not be removed from the node.
   */
  public void removeTag(final Tag tag) throws CouldntSaveDataException {
    Preconditions.checkNotNull(tag, "Error: Tag argument can't be null");

    try {
      m_node.removeTag(tag.getNative().getObject());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the border color of the node.
  /**
   * Changes the border color of the node.
   *
   * @param color The new border color of the node.
   */
  public void setBorderColor(final Color color) {
    m_node.setBorderColor(color);
  }

  // ! Changes the background color of the node.
  /**
   * Changes the background color of the node.
   *
   * @param color The new background color of the node.
   */
  public void setColor(final Color color) {
    m_node.setColor(color);
  }

  // ! Changes the selection state of the node.
  /**
   * Selects or deselects the node.
   *
   * @param selection True to select the node, false to deselect it.
   */
  public void setSelected(final boolean selection) {
    m_node.setSelected(selection);
  }

  // ! Changes the X-position of the node.
  /**
   * Changes the X position of the node.
   *
   * @param newX The new X position of the node.
   */
  public void setX(final double newX) {
    m_node.setX(newX);
  }

  // ! Changes the Y-position of the node.
  /**
   * Changes the Y position of the node.
   *
   * @param newY The new Y position of the node.
   */
  public void setY(final double newY) {
    m_node.setY(newY);
  }

  @Override
  public String toString() {
    return getName();
  }
}
