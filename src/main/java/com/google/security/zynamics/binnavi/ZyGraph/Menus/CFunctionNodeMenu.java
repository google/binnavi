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

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionToggleBreakpoint;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionToggleBreakpointStatus;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CChangeFunctionNameAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopyLineAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopyNodeAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CFunctionNodeInlineAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.COpenFunctionAction;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Popup menu that is shown when the user right-clicks on a function node of a graph.
 */
public final class CFunctionNodeMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2340597335457814902L;

  /**
   * Creates a new function node menu.
   *
   * @param model Model of the graph that was clicked.
   * @param node The clicked node.
   * @param y The Y-coordinate of the click event.
   */
  public CFunctionNodeMenu(final CGraphModel model, final NaviNode node, final double y) {
    final INaviFunctionNode functionNode = (INaviFunctionNode) node.getRawNode();

    final int line = node.positionToRow(y);

    add(new CChangeFunctionNameAction(model.getParent(), functionNode.getFunction()
        .getModule()
        .getContent()
        .getViewContainer()
        .getView(functionNode.getFunction())));

    CMenuBuilder.addCommentMenu(this, model, node.getRawNode());
    CMenuBuilder.addSelectionMenus(this, model.getGraph(), node);

    if (functionNode.getFunction().getBasicBlockCount() != 0) {
      add(new JMenuItem(CActionProxy.proxy(new COpenFunctionAction(model.getParent(), model
          .getViewContainer(), functionNode.getFunction()))));
      add(new JMenuItem(CActionProxy.proxy(
          new CFunctionNodeInlineAction(model.getParent(), model.getGraph(), functionNode))));
      addSeparator();
    }

    CMenuBuilder.addTaggingMenu(this, model, node);

    final JMenu clipMenu = new JMenu("Clipboard");

    clipMenu.add(CActionProxy.proxy(new CCopyLineAction(node, line)));
    clipMenu.add(CActionProxy.proxy(new CCopyNodeAction(node)));

    add(clipMenu);

    final IDebugger debugger =
        CGraphDebugger.getDebugger(model.getDebuggerProvider(), functionNode);

    if (debugger == null) {
      return;
    }

    addSeparator();

    final JMenu functionMenu = new JMenu("Function" + " " + functionNode.getFunction().getName());

    final UnrelocatedAddress address =
        new UnrelocatedAddress(functionNode.getFunction().getAddress());

    final INaviModule module = functionNode.getFunction().getModule();

    functionMenu.add(CActionProxy.proxy(
        new CActionToggleBreakpoint(debugger.getBreakpointManager(), module, address)));

    final BreakpointAddress relocatedAddress = new BreakpointAddress(module, address);

    if (debugger.getBreakpointManager().hasBreakpoint(BreakpointType.REGULAR, relocatedAddress)) {
      functionMenu.add(CActionProxy.proxy(
          new CActionToggleBreakpointStatus(debugger.getBreakpointManager(), module, address)));
    }

    add(functionMenu);
  }
}
