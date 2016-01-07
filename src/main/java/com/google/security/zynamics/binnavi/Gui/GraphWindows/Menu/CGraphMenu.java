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

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionDeleteInvisibleNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionDeleteSelectedNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionDeleteUnselectedNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionGraphSettings;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionInsertView;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShowDataflow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShowProximityBrowsingSettingsDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShowReil;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Actions.CActionInlineAll;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * Graph menu of the graph window menu bar.
 */
public final class CGraphMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4694180868761680353L;

  /**
   * Creates the Graph submenu.
   *
   * @param parent Window for which the menu bar is created.
   * @param graph Graph shown in the graph panel for which the menu bar is created.
   * @param container Context in which the graph was opened.
   * @param autoLayoutMenu Menu to enable and disable automated layouting.
   * @param proximityBrowsingMenu Menu to enable and disable proximity browsing.
   */
  public CGraphMenu(final CGraphWindow parent, final ZyGraph graph, final IViewContainer container,
      final JCheckBoxMenuItem proximityBrowsingMenu, final JCheckBoxMenuItem autoLayoutMenu) {
    super("Graph");

    final ZyGraphViewSettings settings = graph.getSettings();

    setMnemonic("HK_MENU_GRAPH".charAt(0));

    autoLayoutMenu.setSelected(settings.getLayoutSettings().getAutomaticLayouting());
    add(autoLayoutMenu);

    proximityBrowsingMenu.setSelected(settings.getProximitySettings().getProximityBrowsing());
    add(proximityBrowsingMenu);

    addSeparator();

    add(CActionProxy.proxy(new CActionGraphSettings(parent, graph)));
    add(CActionProxy.proxy(
        new CActionShowProximityBrowsingSettingsDialog(parent, graph.getSettings())));

    addSeparator();

    add(CActionProxy.proxy(new CActionInsertView(parent, graph, container)));

    addSeparator();

    add(CActionProxy.proxy(new CActionDeleteSelectedNodes(graph, false)));
    add(CActionProxy.proxy(new CActionDeleteUnselectedNodes(graph)));
    add(CActionProxy.proxy(new CActionDeleteInvisibleNodes(graph)));

    addSeparator();

    final JMenu transformMenu = new JMenu("Transform");

    transformMenu.add(CActionProxy.proxy(new CActionInlineAll(parent, container, graph)));

    if ((container instanceof CModuleContainer) || (container instanceof CProjectContainer)) {
      transformMenu.addSeparator();
      transformMenu.add(CActionProxy.proxy(new CActionShowReil(
          parent, container, container.getModules().get(0), graph.getRawView())));
    }

    transformMenu.add(CActionProxy.proxy(
        new CActionShowDataflow(parent, container, graph.getRawView())));

    add(transformMenu);
  }
}
