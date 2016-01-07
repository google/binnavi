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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Updaters.GroupNodes;

import y.view.Graph2D;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.IZyNodeRealizer;

/**
 * Updates the group node on relevant tag changes.
 */
public final class CTagUpdater implements ITagListener {
  /**
   * The graph the node belongs to.
   */
  private final Graph2D m_graph;

  /**
   * This is the realizer that is notified if the underlying data changes. Note that this field can
   * be null.
   */
  private IZyNodeRealizer m_realizer;

  /**
   * Creates a new updater object.
   *
   * @param graph Graph to update on changes.
   */
  public CTagUpdater(final Graph2D graph) {
    m_graph = graph;
  }

  /**
   * Regenerates the content of the node and updates the graph view.
   */
  private void rebuildNode() {
    m_realizer.regenerate();

    m_graph.updateViews();
  }

  @Override
  public void changedDescription(final CTag tag, final String description) {
    tag.removeListener(this);

    rebuildNode();
  }

  @Override
  public void changedName(final CTag tag, final String name) {
    tag.removeListener(this);

    rebuildNode();
  }

  @Override
  public void deletedTag(final CTag tag) {
    tag.removeListener(this);

    rebuildNode();
  }

  /**
   * Sets the realizer of the node to update.
   *
   * @param realizer The realizer of the node to update.
   */
  public void setRealizer(final IZyNodeRealizer realizer) {
    Preconditions.checkNotNull(realizer, "IE00997: Realizer argument can't be null");

    m_realizer = realizer;
  }
}
