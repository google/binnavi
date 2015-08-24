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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.IMainWindowMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.CActionExit;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionAbout;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionContextHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionInitialCallgraphSettings;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionInitialFlowgraphSettings;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionOpenLogConsole;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionOpenScriptingDialog;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionReportBug;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionShowSettingsDialog;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CAddDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CCheckForUpdatesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CPluginManagementAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CPluginsReloadAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CStartTutorialAction;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

/**
 * Base class of all menu builder classes that are used to build the window menus and popup menus of
 * all the nodes in the project tree.
 */
public abstract class CAbstractMenuBuilder {
  /**
   * Project tree that is updated on certain events.
   */
  private final JTree m_projectTree;

  /**
   * Popup menu created by this menu builder.
   */
  private JPopupMenu m_popupMenu;

  /**
   * Menu bar created by this menu builder.
   */
  private JMenuBar m_mainMenu;

  /**
   * Creates a new menu builder.
   * 
   * @param projectTree Project tree that is updated on certain events.
   */
  protected CAbstractMenuBuilder(final JTree projectTree) {
    m_projectTree =
        Preconditions.checkNotNull(projectTree, "IE01938: Project tree argument can not be null");
  }

  /**
   * Creates the About menu.
   * 
   * @return The created menu.
   */
  private JMenu createAboutMenu() {
    final JMenu menu = new JMenu("Help");
    final JFrame parent = getParent();

    menu.setMnemonic('H');

    menu.add(new JMenuItem(new CActionHelp(parent)));
    menu.add(new JMenuItem(new CActionContextHelp(parent)));
    menu.addSeparator();

    menu.add(new JMenuItem(new CStartTutorialAction(parent)));
    menu.addSeparator();

    menu.add(new JMenuItem(new CActionReportBug(parent)));
    menu.add(new JMenuItem(new CCheckForUpdatesAction(parent)));
    menu.add(new JMenuItem(new CActionAbout(parent)));

    return menu;
  }

  /**
   * Creates the BinNavi menu.
   * 
   * @return The created menu.
   */
  private JMenu createBinNaviMenu() {
    // TODO: Rename to "File" for consistency with other applications?
    final JMenu menu = new JMenu("BinNavi");

    menu.setMnemonic('B');

    menu.add(CActionProxy.proxy(new CAddDatabaseAction(m_projectTree)));
    menu.addSeparator();
    menu.add(CActionProxy.proxy(new CActionExit(getParent())));

    return menu;
  }

  /**
   * Creates all menus.
   */
  private void createMenus() {
    // TODO: Old menus must be disposed before creating new ones

    m_mainMenu = new JMenuBar();
    m_mainMenu.add(createBinNaviMenu());

    final JMenu menu = getMenu();

    if (menu != null) {
      createMenu(menu);

      m_mainMenu.add(menu);
    }

    m_mainMenu.add(createPluginsMenu());
    m_mainMenu.add(createSettingsMenu());
    m_mainMenu.add(createAboutMenu());

    m_popupMenu = new JPopupMenu();

    createMenu(m_popupMenu);
  }

  /**
   * Creates the plugin menu.
   * 
   * @return The created menu.
   */
  private JMenu createPluginsMenu() {
    final List<IMainWindowMenuPlugin> plugins = new ArrayList<IMainWindowMenuPlugin>();

    for (@SuppressWarnings("rawtypes")
    final IPlugin plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof IMainWindowMenuPlugin) {
        plugins.add((IMainWindowMenuPlugin) plugin);
      }
    }

    final JMenu menu = new JMenu("Plugins");
    menu.setMnemonic('U');

    menu.add(CActionProxy.proxy(new CActionOpenScriptingDialog(getParent())));
    menu.add(CActionProxy.proxy(new CActionOpenLogConsole()));
    menu.addSeparator();
    menu.add(CActionProxy.proxy(new CPluginManagementAction(getParent())));
    menu.add(CActionProxy.proxy(new CPluginsReloadAction()));
    menu.addSeparator();

    for (final IMainWindowMenuPlugin plugin : plugins) {
      // ESCA-JAVA0166: Catch Exception because we are calling a plugin function.
      try {
        final List<JMenuItem> menus = plugin.extendPluginMenu();

        for (final JMenuItem m : menus) {
          menu.add(m);
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        final String innerMessage = "E00092: " + "Plugin caused an unexpected exception";
        final String innerDescription =
            CUtilityFunctions
                .createDescription(
                    String.format("The plugin %s caused an unexpected exception.", plugin.getName()),
                    new String[] {"The plugin contains a bug."},
                    new String[] {"The plugin probably behaves erroneously from this point on but it remains active"});

        NaviErrorDialog.show(getParent(), innerMessage, innerDescription, exception);
      }
    }

    return menu;
  }

  /**
   * Creates the settings menu.
   * 
   * @return The created menu.
   */
  private JMenu createSettingsMenu() {
    final JMenu menu = new JMenu("Settings");
    menu.setMnemonic('S');

    menu.add(new CActionShowSettingsDialog(getParent()));
    menu.add(new JSeparator());
    menu.add(new CActionInitialCallgraphSettings(getParent()));
    menu.add(new CActionInitialFlowgraphSettings(getParent()));

    return menu;
  }

  /**
   * Adds submenus to a given component.
   * 
   * @param menu The main menu to which submenus are added.
   */
  protected abstract void createMenu(JComponent menu);

  /**
   * Frees allocated resources.
   */
  protected void dispose() {
  }

  /**
   * Returns the main menu built by the menu builder. This function does not add any submenus to the
   * generated menu. The {@link #createMenu} function is responsible for that.
   * 
   * @return The main menu built by the menu builder.
   */
  protected abstract JMenu getMenu();

  /**
   * Returns the parent window of the menu.
   * 
   * @return The parent window.
   */
  protected JFrame getParent() {
    return (JFrame) SwingUtilities.getWindowAncestor(m_projectTree);
  }

  /**
   * Returns the project tree of the main window.
   * 
   * @return The project tree of the main window.
   */
  protected JTree getProjectTree() {
    return m_projectTree;
  }

  /**
   * Returns the menu bar created by this menu builder.
   * 
   * @return The menu bar created by this menu builder.
   */
  public JMenuBar getMainMenu() {
    createMenus();

    return m_mainMenu;
  }

  /**
   * Returns the context menu created by this menu builder.
   * 
   * @return The context menu created by this menu builder.
   */
  public JPopupMenu getPopupMenu() {
    createMenus();

    return m_popupMenu;
  }
}
