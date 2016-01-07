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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CInlineFunctionAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CUnInlineAction;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



/**
 * Contains code for the inlining part of a code node menu.
 */
public final class CInliningMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2909506440205019819L;

  /**
   * Adds the menu for inlining and uninlining subfunctions.
   *
   * @param model The graph model that provides information about the graph.
   * @param node The node the menu is created for.
   * @param functions List of subfunctions called from the node.
   * @param allowUninlining True, if uninlining should be enabled.
   */
  public CInliningMenu(final CGraphModel model, final NaviNode node,
      final List<Pair<INaviInstruction, INaviFunction>> functions, final boolean allowUninlining) {
    super("Inline Subfunction");

    final List<INaviEdge> outgoingEdges = node.getRawNode().getOutgoingEdges();

    if (allowUninlining) {
      add(CActionProxy.proxy(new CUnInlineAction(
          model.getParent(), model.getGraph(), (INaviCodeNode) node.getRawNode())));
    }

    boolean alreadyInlined = false;

    if (outgoingEdges.size() == 1) {
      alreadyInlined =
          outgoingEdges.get(outgoingEdges.size() - 1).getType() == EdgeType.ENTER_INLINED_FUNCTION;
    }

    int functionCounter = 0;

    for (final Pair<INaviInstruction, INaviFunction> p : functions) {
      if (functionCounter == functions.size() - 1 && alreadyInlined) {
        break;
      }

      if (functionCounter == 0 && allowUninlining) {
        addSeparator();
      }

      add(new JMenuItem(CActionProxy.proxy(new CInlineFunctionAction(model, (INaviCodeNode) node
          .getRawNode(), p.first(), p.second()))));

      functionCounter++;
    }

    setEnabled(getMenuComponentCount() != 0);
  }
}
