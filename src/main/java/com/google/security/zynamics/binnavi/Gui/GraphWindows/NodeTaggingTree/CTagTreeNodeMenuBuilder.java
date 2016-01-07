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
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CAddTagToSelectedNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CAppendTagAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CDeleteTagAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CDeleteTagSubtreeAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CEditTagAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CInsertTagAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CRemoveTagFromAllNodesAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CRemoveTagFromSelectedNodesAction;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;



/**
 * Builds the context menu of tag tree nodes.
 */
public final class CTagTreeNodeMenuBuilder {
  /**
   * The created popup menu.
   */
  private final JPopupMenu m_popupMenu;

  /**
   * Creates a new builder object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph The graph whose nodes are tagged.
   * @param tagManager Provides tagging information.
   * @param tag The clicked tag.
   */
  public CTagTreeNodeMenuBuilder(final JFrame parent, final ZyGraph graph,
      final ITagManager tagManager, final ITreeNode<CTag> tag) {
    m_popupMenu = new JPopupMenu();
    m_popupMenu.add(CActionProxy.proxy(new CAddTagToSelectedNodesAction(parent, graph, tag)));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CRemoveTagFromSelectedNodesAction(parent, graph, tag)));
    m_popupMenu.add(CActionProxy.proxy(
        new CRemoveTagFromAllNodesAction(parent, graph, tag.getObject())));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CAppendTagAction(parent, tagManager, tag)));
    m_popupMenu.add(CActionProxy.proxy(new CInsertTagAction(parent, tagManager, tag)));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CEditTagAction(parent, tag.getObject())));
    m_popupMenu.add(new JSeparator());
    m_popupMenu.add(CActionProxy.proxy(new CDeleteTagAction(parent, tagManager, tag)));
    m_popupMenu.add(CActionProxy.proxy(new CDeleteTagSubtreeAction(parent, tagManager, tag)));
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
