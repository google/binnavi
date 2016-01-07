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
package com.google.security.zynamics.binnavi.API.plugins;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.DatabaseManager;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewContainer;
import com.google.security.zynamics.binnavi.API.gui.GraphWindow;
import com.google.security.zynamics.binnavi.API.gui.MainWindow;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphWindowListener;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.Gui.WindowManager.IWindowManagerListener;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Startup.CPluginLoader;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.standardplugins.PluginLoader;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.Collection;
import java.util.List;

/**
 * The PluginInterface class is the class that plugins and scripts use to interact with
 * com.google.security.zynamics.binnavi.
 *
 *  Instances of this class are passed to all plugins and scripts that want to interact with
 * com.google.security.zynamics.binnavi.
 */
public final class PluginInterface implements IPluginInterface {
  /**
   * The only valid instance of the PluginInterface class.
   */
  private static PluginInterface instance;

  /**
   * BinNavi start path.
   */
  private final String startPath;

  /**
   * The database manager that keeps track of the known databases.
   */
  private final DatabaseManager manager;

  /**
   * Manages the loaded plugins.
   */
  private final PluginRegistry registry;

  /**
   * Main window object.
   */
  private MainWindow mainWindow;

  /**
   * Listeners that are notified about changes in the plugin interface.
   */
  private final ListenerProvider<IPluginInterfaceListener> listeners =
      new ListenerProvider<IPluginInterfaceListener>();

  /**
   * Creates a new plugin interface object.
   *
   * @param startPath BinNavi start path.
   * @param manager The database manager that keeps track of the known databases.
   * @param registry Manages the loaded plugins.
   */
  private PluginInterface(final String startPath, final IDatabaseManager manager,
      final com.google.security.zynamics.binnavi.Plugins.PluginRegistry registry) {
    Preconditions.checkNotNull(manager, "Database manager can't be null");

    this.startPath = startPath;
    this.manager = new DatabaseManager(manager);
    this.registry = new PluginRegistry(registry);
  }

  /**
   * Returns the only valid instance of the PluginInterface class.
   *
   * @return The only valid plugin interface.
   */
  public static PluginInterface instance() {
    Preconditions.checkNotNull(instance, "Error: Plugin interface was not yet instantiated");

    return instance;
  }

  /**
   * Initializes the only valid instance of the plugin interface class.
   *
   * @param startPath BinNavi start path.
   * @param manager The database manager that keeps track of the known databases.
   * @param registry Manages the loaded plugins.
   *
   * @return The only valid plugin interface.
   */
  public static PluginInterface instance(final String startPath, final IDatabaseManager manager,
      final com.google.security.zynamics.binnavi.Plugins.PluginRegistry registry) {
    if (instance != null) {
      throw new IllegalStateException(
          "Error: Plugin interface can not be instantiated more than once");
    }

    instance = new PluginInterface(startPath, manager, registry);

    return instance;
  }

  /**
   * Shows a view in a given graph window.
   *
   * @param graphWindow The window in which the view is shown. This argument can be null.
   * @param view The view to show.
   * @param container Context in which the view is opened.
   *
   * @return Describes the open view.
   */
  private View2D show(final CGraphWindow graphWindow, final View view,
      final IViewContainer container) {
    for (final CGraphWindow window : CWindowManager.instance()) {
      for (final IGraphPanel panel : window) {
        if (panel.getModel().getGraph().getRawView() == view.getNative()) {
          return panel.getModel().getView2D();
        }
      }
    }

    final WindowWaiter waiter = new WindowWaiter(view.getNative());

    CWindowManager.instance().addListener(waiter);

    final List<CGraphWindow> openBefore = CWindowManager.instance().getOpenWindows();

    for (final CGraphWindow window : openBefore) {
      window.addListener(waiter);
    }

    CViewOpener.showView(mainWindow.getFrame(), container, view.getNative(), graphWindow);

    while (waiter.getModel() == null) {
      try {
        Thread.sleep(1000);
      } catch (final InterruptedException exception) {
        CUtilityFunctions.logException(exception);
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    }

    final CGraphModel model = waiter.getModel();

    CWindowManager.instance().removeListener(waiter);

    for (final CGraphWindow window : openBefore) {
      window.removeListener(waiter);
    }

    return model.getView2D();
  }

  /**
   * Adds a listener object that is notified about changes in the plugin interface.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IPluginInterfaceListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Returns the database manager that contains all databases known to
   * com.google.security.zynamics.binnavi.
   *
   * @return The database manager.
   */
  @Override
  public DatabaseManager getDatabaseManager() {
    return manager;
  }

  /**
   * Returns the main window object. This object represents the BinNavi window that contains the
   * project tree.
   *
   * @return The BinNavi main window.
   */
  @Override
  public MainWindow getMainWindow() {
    return mainWindow;
  }

  /**
   * Returns the plugin registry.
   *
   * @return The plugin registry.
   */
  public PluginRegistry getPluginRegistry() {
    return registry;
  }

  /**
   * Returns the path to the BinNavi JAR file.
   *
   * @return The path to the BinNavi JAR file.
   */
  public String getProgramPath() {
    return startPath;
  }

  /**
   * Reloads all plugins.
   */
  public void reloadPlugins() {
    registry.getNative().unloadAll();

    NaviLogger.info("Loading built-in plugins...");
    final Collection<IPlugin<IPluginInterface>> plugins = new PluginLoader().getPlugins();
    for (final IPlugin<IPluginInterface> plugin : plugins) {
      registry.addPlugin(plugin);
    }

    CPluginLoader.loadPlugins(mainWindow.getFrame(), this, registry.getNative(), startPath,
        ConfigManager.instance());

    for (final IPluginInterfaceListener listener : listeners) {
      try {
        listener.loadedPlugins();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener object from the plugin interface.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IPluginInterfaceListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Initializes the main window object.
   *
   * @param mainWindow The main window object.
   */
  public void setMainWindow(final MainWindow mainWindow) {
    if (this.mainWindow != null) {
      throw new IllegalStateException("Error: Main window was already set");
    }

    this.mainWindow = mainWindow;
  }

  /**
   * Shows a view in the last window.
   *
   * @param view The view to show.
   *
   * @return The view2d object that is shown.
   */
  public View2D showInLastWindow(final View view) {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    final ViewContainer container = view.getContainer();

    if (container instanceof Module) {
      final Module module = (Module) container;

      return show(CWindowManager.instance().getLastWindow(), view,
          new CModuleContainer(container.getDatabase().getNative(), module.getNative()));
    } else {
      final Project project = (Project) container;

      return show(CWindowManager.instance().getLastWindow(), view,
          new CProjectContainer(container.getDatabase().getNative(), project.getNative()));
    }
  }

  /**
   * Shows a view in a new window.
   *
   * @param view The view to show.
   *
   * @return The view2d object that is shown.
   */
  public View2D showInNewWindow(final View view) {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    final ViewContainer container = view.getContainer();

    if (container instanceof Module) {
      final Module module = (Module) container;

      return show(null, view,
          new CModuleContainer(container.getDatabase().getNative(), module.getNative()));
    } else {
      final Project project = (Project) container;

      return show(null, view,
          new CProjectContainer(container.getDatabase().getNative(), project.getNative()));
    }
  }

  /**
   * Shows a view in a given window.
   *
   * @param window The window where the view is shown.
   * @param view The view to show.
   *
   * @return The view2d object that is shown.
   */
  public View2D showInWindow(final GraphWindow window, final View view) {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    final ViewContainer container = view.getContainer();

    if (container instanceof Module) {
      final Module module = (Module) container;

      return show(window.getNative(), view,
          new CModuleContainer(container.getDatabase().getNative(), module.getNative()));
    } else {
      final Project project = (Project) container;

      return show(window.getNative(), view,
          new CProjectContainer(container.getDatabase().getNative(), project.getNative()));
    }
  }

  /**
   * Returns the string representation of the plugin interface.
   *
   * @return The string representation of the plugin interface.
   */
  @Override
  public String toString() {
    return "BinNavi Plugin Interface";
  }

  /**
   * Helper class for waiting until a window opens.
   */
  private class WindowWaiter implements IWindowManagerListener, IGraphWindowListener {
    /**
     * The view to wait for.
     */
    private final INaviView m_view;

    /**
     * The model of the open view.
     */
    private CGraphModel m_model;

    /**
     * Creates a new waiter object.
     *
     * @param view The view to wait for.
     */
    public WindowWaiter(final INaviView view) {
      m_view = view;
    }

    /**
     * Checks whether a given panel shows the view the waiter is waiting for.
     *
     * @param panel The panel to check.
     */
    private void processPanel(final IGraphPanel panel) {
      if (panel.getModel().getGraph().getRawView() == m_view) {
        m_model = panel.getModel();
      }
    }

    /**
     * Returns the model for the view to wait for.
     *
     * @return The model for the view to wait for.
     */
    public CGraphModel getModel() {
      return m_model;
    }

    @Override
    public void graphPanelClosed() {
      // This should not be passed to the API
    }

    @Override
    public void graphPanelOpened(final CGraphPanel graphPanel) {
      processPanel(graphPanel);
    }

    @Override
    public void windowClosed(final CWindowManager windowManager,
        final IGraphContainerWindow window) {
      // This should not be passed to the API
    }

    @Override
    public void windowOpened(final CWindowManager windowManager,
        final IGraphContainerWindow window) {
      for (final IGraphPanel panel : window) {
        processPanel(panel);
      }
    }
  }
}
