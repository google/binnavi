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

import java.util.List;

import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode.CCodeNodeMenu;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;

/**
 * This class creates the popup menus that are shown when the user right-clicks on graph elements.
 */
public final class ZyGraphPopupMenus {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyGraphPopupMenus() {
  }

  /**
   * Creates the popup menu that is displayed when the user hits a node.
   *
   * @param model The model of the graph that was clicked.
   * @param node The node in question.
   * @param clickedObject The clicked object.
   * @param y The y-coordinate of the click relative to the clicked node.
   * @param contentSelectionActive Flag that indicates whether content selection is active.
   * @param extensions List of registered menu extensions.
   *
   * @return The popup menu to be displayed.
   */
  private static JPopupMenu getNodePopup(final CGraphModel model,
      final NaviNode node,
      final Object clickedObject,
      final double y,
      final boolean contentSelectionActive,
      final List<ICodeNodeExtension> extensions) {
    Preconditions.checkNotNull(node, "IE00976: Node argument can't be null");

    // sp: What's going on here is some of the worst casting code I've
    // ever written. Tell me if you find a better solution.

    if (contentSelectionActive) {
      return new CContentSelectionMenu(model, node);
    } else {
      final IViewNode<?> rawNode = node.getRawNode();

      if (rawNode instanceof CFunctionNode) {
        return new CFunctionNodeMenu(model, node, y);
      } else if (rawNode instanceof CCodeNode) {
        return new CCodeNodeMenu(model, node, clickedObject, y, extensions);
      } else if (rawNode instanceof CTextNode) {
        return new CTextNodeMenu(
            model.getParent(), model.getGraph().getRawView(), (CTextNode) rawNode);
      } else if (rawNode instanceof CGroupNode) {
        return new CGroupNodeMenu(
            model.getParent(), model.getGraph().getRawView(), (CGroupNode) rawNode);
      } else {
        throw new IllegalStateException("IE00977: Unknown node type");
      }
    }
  }

  /**
   * Creates a popup menu that is displayed in the graph when the user right-clicks somewhere in the
   * graph. The exact menu created by this function depends on what graph element was hit by the
   * click and the current state of the graph.
   *
   * @param model The model of the graph that was clicked.
   * @param node The node in question.
   * @param y The y-coordinate of the click relative to the clicked node.
   * @param clickedObject The clicked object.
   * @param contentSelectionActive Flag that indicates whether content selection is active.
   * @param extensions List of registered menu extensions.
   *
   * @return The menu to be displayed or null if not menu should be displayed.
   */
  public static JPopupMenu getPopupMenu(final CGraphModel model,
      final NaviNode node,
      final Object clickedObject,
      final double y,
      final boolean contentSelectionActive,
      final List<ICodeNodeExtension> extensions) {
    return getNodePopup(model, node, clickedObject, y, contentSelectionActive, extensions);
  }
}
