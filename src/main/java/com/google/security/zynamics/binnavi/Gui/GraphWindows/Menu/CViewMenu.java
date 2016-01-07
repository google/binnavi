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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu;

import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionChangeViewDescription;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionClone;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionClose;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionGraphExportPNG;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionGraphExportSVG;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionGraphPrint;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSave;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSaveAs;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Contains code for the View menu of the graph window menu bar.
 */
public final class CViewMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6345634239023553814L;

  /**
   * Creates the View menu.
   *
   * @param parent Window for which the menu bar is created.
   * @param graphPanel Panel where the graph is shown.
   * @param graph Graph shown in the graph panel for which the menu bar is created.
   * @param container Context in which the graph was opened.
   * @param actionSave The Save action.
   */
  public CViewMenu(final CGraphWindow parent, final CGraphPanel graphPanel, final ZyGraph graph,
      final IViewContainer container, final CActionSave actionSave) {
    super("View");

    setMnemonic("HK_MENU_VIEW".charAt(0));

    add(actionSave);
    add(CActionProxy.proxy(new CActionSaveAs(parent, graph, container)));
    addSeparator();

    add(CActionProxy.proxy(new CActionClone(parent, graph.getRawView(), container)));
    addSeparator();

    add(CActionProxy.proxy(new CActionChangeViewDescription(parent, graph.getRawView())));
    addSeparator();

    add(CActionProxy.proxy(new CActionGraphPrint(parent, graph)));

    final JMenu exportMenu = new JMenu("Export");

    exportMenu.add(CActionProxy.proxy(new CActionGraphExportPNG(parent, graph)));
    exportMenu.add(CActionProxy.proxy(new CActionGraphExportSVG(parent, graph)));

    add(exportMenu);

    addSeparator();

    add(CActionProxy.proxy(new CActionClose(graphPanel)));
  }
}
