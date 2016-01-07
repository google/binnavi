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
package com.google.security.zynamics.binnavi.ZyGraph.Menus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CGroupAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionCreateCommentNode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionEditComments;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionSelectNodePredecessors;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionSelectNodeSuccessors;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionSelectSameFunctionType;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionSelectSameParentFunction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CTagNodeAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CTagSelectedNodesAction;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Helper class that supports the creation of the context menus that are shown in the graph.
 */
public final class CMenuBuilder {
  /**
   * You are not supposed to instantiate this class.
   */
  private CMenuBuilder() {
  }

  /**
   * Adds menus related to comments to a given node context menu.
   *
   * @param menu The node context menu that is extended.
   * @param node The clicked node.
   */
  public static void addCommentMenu(
      final JPopupMenu menu, final CGraphModel model, final INaviViewNode node) {
    Preconditions.checkNotNull(menu, "IE02140: Menu argument can not be null");
    Preconditions.checkNotNull(node, "IE02143: Node argument can not be null");

    menu.add(CActionProxy.proxy(new CActionEditComments(model, node)));
    menu.add(CActionProxy.proxy(
        new CActionCreateCommentNode(model.getParent(), model.getGraph().getRawView(), node)));

    menu.addSeparator();
  }

  /**
   * Adds menus related to node selection to a given node context menu.
   *
   * @param menu The node context menu to extend.
   * @param graph The graph the clicked node belongs to.
   * @param node The clicked node.
   */
  public static void addSelectionMenus(
      final JPopupMenu menu, final ZyGraph graph, final NaviNode node) {
    Preconditions.checkNotNull(menu, "IE02144: Menu argument can not be null");
    Preconditions.checkNotNull(graph, "IE02145: Graph argument can not be null");
    Preconditions.checkNotNull(node, "IE02146: Node argument can not be null");

    final JMenu selectionMenu = new JMenu("Selection");

    selectionMenu.add(CActionProxy.proxy(new CActionSelectNodePredecessors(graph, node)));
    selectionMenu.add(CActionProxy.proxy(new CActionSelectNodeSuccessors(graph, node)));

    if (graph.getSelectedNodes().size() > 0) {
      selectionMenu.add(CActionProxy.proxy(new CGroupAction(graph)));
    }

    if (node.getRawNode() instanceof INaviCodeNode) {
      try {
        final INaviFunction parentFunction =
            ((INaviCodeNode) node.getRawNode()).getParentFunction();

        selectionMenu.add(CActionProxy.proxy(
            new CActionSelectSameParentFunction(graph, parentFunction)));
      } catch (final MaybeNullException exception) {
        // Obviously we can not select nodes of the same parent function if there
        // is no parent function.
      }
    } else if (node.getRawNode() instanceof INaviFunctionNode) {
      final INaviFunction function = ((INaviFunctionNode) node.getRawNode()).getFunction();

      selectionMenu.add(CActionProxy.proxy(
          new CActionSelectSameFunctionType(graph, function.getType())));
    }

    menu.add(selectionMenu);

    menu.addSeparator();
  }

  /**
   * Adds menus related to node tagging to a given node context menu.
   *
   * @param menu The node context menu to extend.
   * @param model The model of the graph the clicked node belongs to.
   * @param node The clicked node.
   */
  public static void addTaggingMenu(
      final JPopupMenu menu, final CGraphModel model, final NaviNode node) {
    Preconditions.checkNotNull(menu, "IE02147: Menu argument can not be null");
    Preconditions.checkNotNull(model, "IE02148: Model argument can not be null");
    Preconditions.checkNotNull(node, "IE02149: Node argument can not be null");

    final JMenuItem tagNodeItem = new JMenuItem(CActionProxy.proxy(
        new CTagNodeAction(model.getParent(), model.getGraphPanel().getTagsTree(), node)));
    final JMenuItem tagSelectedNodesItem =
        new JMenuItem(CActionProxy.proxy(new CTagSelectedNodesAction(
            model.getParent(), model.getGraphPanel().getTagsTree(), model.getGraph())));

    final CTagsTree tree = model.getGraphPanel().getTagsTree();
    tagNodeItem.setEnabled(tree.getSelectionPath() != null);
    tagSelectedNodesItem.setEnabled(
        (tree.getSelectionPath() != null) && !model.getGraph().getSelectedNodes().isEmpty());

    menu.add(tagNodeItem);
    menu.add(tagSelectedNodesItem);

    menu.addSeparator();
  }
}
