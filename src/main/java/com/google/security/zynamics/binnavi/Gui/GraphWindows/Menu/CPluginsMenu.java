/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.IGraphMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.IPluginInterfaceListener;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionGraphScripting;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionOpenLogConsole;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;


/**
 * Plugins menu of the graph window menu bar.
 */
public final class CPluginsMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1418451269354925733L;

  /**
   * Updates the Plugins menu when plugins are loaded.
   */
  private final IPluginInterfaceListener m_listener = new InternalPluginInterfaceListener();

  /**
   * Contains the information about the displayed graph the menu refers to.
   */
  private final CGraphModel m_model;

  /**
   * Creates the Plugins menu.
   *
   * @param model
   */
  public CPluginsMenu(final CGraphModel model) {
    super("Plugins");

    m_model = model;

    setMnemonic("HK_MENU_PLUGINS".charAt(0));

    fillPluginsMenu();

    PluginInterface.instance().addListener(m_listener);
  }

  /**
   * Asks a plugin to extend the Plugins menu.
   *
   * @param plugin The plugin asked to extend the menu.
   */
  private void extendPluginMenu(final IGraphMenuPlugin plugin) {
    // ESCA-JAVA0166: Calling a plugin function.
    try {
      final List<JComponent> menuItems = plugin.extendPluginMenu(m_model.getGraphFrame());

      if (menuItems != null) {
        for (final JComponent menuItem : menuItems) {
          add(menuItem);
        }
      }
    } catch (final Exception exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00085: " + "Plugin caused an unexpected exception";
      final String innerDescription = CUtilityFunctions.createDescription(
          String.format("The plugin %s caused an unexpected exception.", plugin.getName()),
          new String[] {"The plugin contains a bug."}, new String[] {
              "The plugin probably behaves erroneously from this point on but it remains active"});

      NaviErrorDialog.show(m_model.getParent(), innerMessage, innerDescription, exception);
    }
  }

  /**
   * Creates the menu items of the Plugins menu.
   */
  private void fillPluginsMenu() {
    final List<IGraphMenuPlugin> plugins = new ArrayList<IGraphMenuPlugin>();

    for (
        @SuppressWarnings("rawtypes")
    final IPlugin plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IGraphMenuPlugin) {
        plugins.add((IGraphMenuPlugin) plugin);
      }
    }

    add(CActionProxy.proxy(new CActionGraphScripting(m_model.getGraphPanel())));
    add(CActionProxy.proxy(new CActionOpenLogConsole()));

    addSeparator();

    for (final IGraphMenuPlugin plugin : plugins) {
      extendPluginMenu(plugin);
    }
  }

  /**
   * Removes all menu items from the plugins menu and fills the menu again.
   */
  private void resetPluginsMenu() {
    removeAll();

    fillPluginsMenu();
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    PluginInterface.instance().removeListener(m_listener);
  }

  /**
   * Updates the Plugins menu when plugins are loaded.
   */
  private class InternalPluginInterfaceListener implements IPluginInterfaceListener {
    @Override
    public void loadedPlugins() {
      resetPluginsMenu();
    }
  }
}
