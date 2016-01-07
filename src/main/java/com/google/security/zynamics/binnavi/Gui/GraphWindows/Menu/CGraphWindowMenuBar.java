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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.IGraphMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache.CCriteriumCache;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindowMenuBarSynchronizer;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IViewSwitcher;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionAutomaticLayouting;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionProximityBrowsing;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSave;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;

/**
 * Main menu bar of the graph window. The main menu of a graph window is different for each graph
 * panel.
 */
public final class CGraphWindowMenuBar extends JMenuBar {
  /**
   * Contains the information about the displayed graph the menu refers to.
   */
  private final CGraphModel m_model;

  /**
   * Used to save the graph.
   */
  private final CActionSave m_actionSave;

  /**
   * Synchronizes the menu bar with the selected graph settings.
   */
  private final CGraphWindowMenuBarSynchronizer m_synchronizer;

  /**
   * The selection menu.
   */
  private final CSelectionMenu m_selectionMenu;

  /**
   * The plugins menu.
   */
  private final CPluginsMenu m_pluginsMenu;

  /**
   * Creates a new menu bar for a given graph.
   *
   * @param model Contains the information about the displayed graph the menu refers to.
   * @param viewSwitcher Toggles between the different graph panel perspectives.
   */
  public CGraphWindowMenuBar(final CGraphModel model, final IViewSwitcher viewSwitcher) {
    Preconditions.checkNotNull(model, "IE01626: Model argument can not be null");

    m_model = model;

    m_actionSave = new CActionSave(model.getParent(), model.getGraph());
    final JCheckBoxMenuItem autoLayoutMenu =
        new JCheckBoxMenuItem(new CActionAutomaticLayouting(model.getGraph()));
    final JCheckBoxMenuItem proximityBrowsingMenu = new JCheckBoxMenuItem(CActionProxy.proxy(
        new CActionProximityBrowsing(model.getParent(), model.getGraph())));

    add(new CViewMenu(model.getParent(), model.getGraphPanel(), model.getGraph(), model
        .getViewContainer(), m_actionSave));
    add(new CGraphMenu(model.getParent(), model.getGraph(), model.getViewContainer(),
        proximityBrowsingMenu, autoLayoutMenu));
    add(m_selectionMenu = new CSelectionMenu(model));
    add(new CSearchMenu(model.getGraph()));
    add(m_pluginsMenu = new CPluginsMenu(model));
    add(new CWindowsMenu(model.getParent(), viewSwitcher));

    m_synchronizer = new CGraphWindowMenuBarSynchronizer(
        model.getGraph().getSettings(), proximityBrowsingMenu, autoLayoutMenu);

    updateGui();
  }

  /**
   * Frees allocated resources.
   */
  public void dipose() {
    m_synchronizer.dispose();
    m_selectionMenu.dispose();

    final List<IGraphMenuPlugin> plugins = Lists.newArrayList();
    for (final IPlugin<IPluginInterface> plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IGraphMenuPlugin) {
        plugins.add((IGraphMenuPlugin) plugin);
      }
    }

    for (final IGraphMenuPlugin plugin : plugins) {
      try {
        plugin.closed(m_model.getGraphFrame());
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        final String innerMessage = "E00106: " + "Plugin caused an unexpected exception";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The plugin %s caused an unexpected exception.", plugin.getName()),
            new String[] {"The plugin contains a bug."}, new String[] {
                "The plugin probably behaves erroneously from this point on but it remains active"});

        NaviErrorDialog.show(m_model.getParent(), innerMessage, innerDescription, exception);
      }
    }

    m_pluginsMenu.dispose();
  }

  /**
   * Returns the criterium cache of the menu.
   *
   * @return The criterium cache of the menu.
   */
  public CCriteriumCache getCriteriumCache() {
    return m_selectionMenu.getCriteriumCache();
  }

  /**
   * Asks the menu bar to update itself.
   */
  public void updateGui() {
    m_actionSave.setEnabled(m_model.getGraph().getRawView().getType() == ViewType.NonNative);
  }
}
