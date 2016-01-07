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
import java.util.Iterator;
import java.util.List;
import java.util.Set;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.CViewNode;

/**
 * Represents nodes in views.
 */
public abstract class CNaviViewNode extends CViewNode<INaviEdge> implements INaviViewNode {
  /**
   * Tags the node is tagged with.
   */
  private final Set<CTag> m_tags;

  /**
   * Synchronizes the node with the database.
   */
  private final SQLProvider m_provider;

  /**
   * Listeners that are notified about changes in the node.
   */
  private final ListenerProvider<INaviViewNodeListener> m_naviViewNodeListeners =
      new ListenerProvider<INaviViewNodeListener>();

  /**
   * Synchronizes the node with relevant tag events.
   */
  private final InternalTagListener m_tagListener = new InternalTagListener();

  /**
   * Parent nodes of the node.
   */
  private final List<INaviViewNode> m_parents = new ArrayList<INaviViewNode>();

  /**
   * Child nodes of the node.
   */
  private final List<INaviViewNode> m_children = new ArrayList<INaviViewNode>();

  /**
   * Parent group of the node.
   */
  private INaviGroupNode m_parentGroup = null;

  /**
   * Creates a new view node.
   * 
   * @param nodeId ID of the node.
   * @param x X-coordinate of the node.
   * @param y Y-coordinate of the node.
   * @param width The width of the node in the graph.
   * @param height The height of the node in the graph.
   * @param color Background color of the node.
   * @param borderColor Border color of the node.
   * @param selected Selection state of the node.
   * @param visible Visibility state of the node.
   * @param tags Tags the node is tagged with.
   * @param provider Synchronizes the node with the database.
   */
  public CNaviViewNode(final int nodeId, final double x, final double y, final double width,
      final double height, final Color color, final Color borderColor, final boolean selected,
      final boolean visible, final Set<CTag> tags, final SQLProvider provider) {
    super(nodeId, x, y, width, height, color, borderColor, selected, visible);

    m_provider =
        Preconditions.checkNotNull(provider, "IE00209: SQL provider can not be can not be null");
    m_tags = new HashSet<CTag>(Preconditions.checkNotNull(tags, "IE00210: Tags can not be null"));

    for (final CTag tag : m_tags) {
      tag.addListener(m_tagListener);
    }
  }

  /**
   * Links two nodes.
   * 
   * @param parent Parent node of the node.
   * @param child Child node of the node.
   */
  public static void link(final INaviViewNode parent, final INaviViewNode child) {
    parent.addChild(child);
    child.addParent(parent);
  }

  @Override
  public void addChild(final INaviViewNode child) {
    m_children.add(child);
  }

  @Override
  public void addListener(final INaviViewNodeListener listener) {
    super.addListener(listener);

    m_naviViewNodeListeners.addListener(listener);
  }

  @Override
  public void addParent(final INaviViewNode parent) {
    m_parents.add(parent);
  }

  @Override
  public abstract CNaviViewNode cloneNode();

  @Override
  public void close() {
    for (final CTag tag : m_tags) {
      tag.removeListener(m_tagListener);
    }
  }

  @Override
  public List<INaviViewNode> getChildren() {
    return new ArrayList<INaviViewNode>(m_children);
  }

  @Override
  public INaviGroupNode getParentGroup() {
    return m_parentGroup;
  }

  @Override
  public List<INaviViewNode> getParents() {
    return new ArrayList<INaviViewNode>(m_parents);
  }

  @Override
  public Set<CTag> getTags() {
    return new HashSet<CTag>(m_tags);
  }

  @Override
  public Iterator<CTag> getTagsIterator() {
    return m_tags.iterator();
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return m_provider.equals(provider);
  }

  /**
   * Determines whether a node was already saved to the database.
   * 
   * @return True, if the node was already saved to the database. False, otherwise.
   */
  public boolean isStored() {
    return getId() != -1;
  }

  @Override
  public boolean isTagged() {
    return !m_tags.isEmpty();
  }

  @Override
  public boolean isTagged(final CTag tag) {
    return m_tags.contains(tag);
  }

  @Override
  public void removeChild(final INaviViewNode node) {
    m_children.remove(node);
  }

  /**
   * Removes a listener object from the view node.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final INaviViewNodeListener listener) {
    super.removeListener(listener);

    m_naviViewNodeListeners.removeListener(listener);
  }

  @Override
  public void removeParent(final INaviViewNode node) {
    m_parents.remove(node);
  }

  @Override
  public void removeTag(final CTag tag) throws CouldntSaveDataException {
    if (isTagged(tag)) {
      if (isStored()) // each new inlined nodes has the id -1 until the view is saved
      {
        m_provider.removeTagFromNode(this, tag.getId());
      }

      m_tags.remove(tag);

      final List<CTag> tagList = Lists.newArrayList(tag);

      for (final INaviViewNodeListener listener : m_naviViewNodeListeners) {
        listener.untaggedNodes(this, tagList);
      }

      tag.removeListener(m_tagListener);
    }
  }

  @Override
  public void setParentGroup(final INaviGroupNode group) {
    if ((group != null) && group.equals(m_parentGroup)) {
      return;
    }

    Preconditions.checkArgument(group != this, "IE00211: Invalid group hierarchy");

    m_parentGroup = group;

    for (final INaviViewNodeListener listener : m_naviViewNodeListeners) {
      try {
        listener.changedParentGroup(this, group);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setVisible(final boolean visible) {
    if (!visible && (m_parentGroup != null) && m_parentGroup.isCollapsed()
        && m_parentGroup.isVisible()) {
      return;
    }

    super.setVisible(visible);
  }

  @Override
  public void tagNode(final CTag tag) throws CouldntSaveDataException {
    if (!isTagged(tag)) {
      if (isStored()) // each new inlined node has the id -1 until the view is saved
      {
        m_provider.saveTagToNode(this, tag.getId());
      }

      m_tags.add(tag);

      for (final INaviViewNodeListener listener : m_naviViewNodeListeners) {
        listener.taggedNode(this, tag);
      }

      tag.addListener(m_tagListener);
    }
  }

  /**
   * Tags a node without writing the tag change to the database.
   * 
   * @param tag The tag to tag the node with.
   */
  @Deprecated
  public void tagNodeSilent(final CTag tag) {
    // ATTENTION: DO NOT USE THIS FUNCTION FOR ANYTHING BUT INITIALIZING

    if (!isTagged(tag)) {
      m_tags.add(tag);

      for (final INaviViewNodeListener listener : m_naviViewNodeListeners) {
        listener.taggedNode(this, tag);
      }

      tag.addListener(m_tagListener);
    }
  }

  /**
   * Synchronizes the node with relevant tag events.
   */
  private class InternalTagListener implements ITagListener {
    @Override
    public void changedDescription(final CTag tag, final String description) {
    }

    @Override
    public void changedName(final CTag tag, final String name) {
    }

    @Override
    public void deletedTag(final CTag tag) {
      m_tags.remove(tag);

      final List<CTag> tagList = Lists.newArrayList(tag);

      for (final INaviViewNodeListener listener : m_naviViewNodeListeners) {
        listener.untaggedNodes(CNaviViewNode.this, tagList);
      }

      tag.removeListener(m_tagListener);
    }
  }
}
