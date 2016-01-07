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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CSelectNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CSelectSubtreeNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CSelectVisibleNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CSelectVisibleSubtreeNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CUnselectNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CUnselectSubtreeNodesAction;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.JPopupMenu;
import javax.swing.JSeparator;



/**
 * Builds the context menu of tag tree container nodes.
 */
public final class CTaggedNodesContainerNodeMenuBuilder {
  /**
   * The created popup menu.
   */
  private final JPopupMenu m_popupMenu;

  /**
   * Creates a new builder object.
   *
   * @param graph The graph whose nodes are tagged.
   * @param tag The clicked tag.
   */
  public CTaggedNodesContainerNodeMenuBuilder(final ZyGraph graph, final ITreeNode<CTag> tag) {
    m_popupMenu = new JPopupMenu();

    m_popupMenu.add(CActionProxy.proxy(new CSelectNodesAction(graph, tag.getObject())));
    m_popupMenu.add(CActionProxy.proxy(new CSelectVisibleNodesAction(graph, tag.getObject())));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CSelectSubtreeNodesAction(graph, tag)));
    m_popupMenu.add(CActionProxy.proxy(new CSelectVisibleSubtreeNodesAction(graph, tag)));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CUnselectNodesAction(graph, tag.getObject())));
    m_popupMenu.add(CActionProxy.proxy(new CUnselectSubtreeNodesAction(graph, tag)));
  }

  /**
   * Returns the created popup menu.
   *
   * @return The created popup menu.
   */
  public JPopupMenu getPopupMenu() {
    return m_popupMenu;
  }
}
