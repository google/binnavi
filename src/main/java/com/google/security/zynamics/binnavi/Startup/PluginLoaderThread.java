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
package com.google.security.zynamics.binnavi.Startup;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.Scripting.ScriptLoader;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Plugins.DisabledPluginReason;
import com.google.security.zynamics.binnavi.Plugins.IPluginRegistry;
import com.google.security.zynamics.binnavi.Plugins.JarLoader;
import com.google.security.zynamics.binnavi.Plugins.LoadResult;
import com.google.security.zynamics.binnavi.Plugins.PluginInitializer;
import com.google.security.zynamics.binnavi.Plugins.PluginLoader;
import com.google.security.zynamics.binnavi.Plugins.PluginStatus;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CStandardHelperThread;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.awt.Window;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Thread that is used to load plugins in the background while a progress dialog is active.
 *
 * @param <T> The plugin interface type of the plugins which should be loaded.
 */
public final class PluginLoaderThread<T> extends CStandardHelperThread {
  /**
   * Parent window used for dialogs.
   */
  private final Window parent;

  /**
   * Main path of the BinNavi installation.
   */
  private final String rootPath;

  /**
   * Paths to the plugin directories.
   */
  private final List<String> pluginPaths;

  /**
   * Plugin interface object.
   */
  private final T pluginInterface;

  /**
   * Plugin registry the plugins are added to.
   */
  private final IPluginRegistry<IPlugin<T>> registry;

  /**
   * Configuration file that contains the BinNavi settings.
   */
  private final ConfigManager configFile;

  /**
   * Creates a new plugin loader thread.
   *
   * @param parent Parent window used for dialogs.
   * @param rootPath Main path of the BinNavi installation.
   * @param pluginPaths Paths to the plugin directory.
   * @param pluginInterface Plugin interface object.
   * @param registry Plugin registry the plugins are added to.
   * @param configFile Configuration file that contains the BinNavi settings.
   */
  // ESCA-JAVA0138:
  public PluginLoaderThread(final Window parent,
      final String rootPath,
      final List<String> pluginPaths,
      final T pluginInterface,
      final IPluginRegistry<IPlugin<T>> registry,
      final ConfigManager configFile) {
    this.rootPath =
        Preconditions.checkNotNull(rootPath, "IE02094: Root path argument can not be null");
    this.pluginPaths =
        Preconditions.checkNotNull(pluginPaths, "IE02095: Plugin paths argument can not be null");
    this.pluginInterface = Preconditions.checkNotNull(pluginInterface,
        "IE02096: Plugin interface argument can not be null");
    this.registry =
        Preconditions.checkNotNull(registry, "IE02097: Registry argument can not be null");
    this.configFile =
        Preconditions.checkNotNull(configFile, "IE02098: Config file argument can not be null");
    this.parent = parent; // May be null
  }

  /**
   * Finds the plugin with a given GUID from a list of plugins.
   *
   * @param plugins The plugins to search through.
   * @param guid The GUID to search for.
   *
   * @return The plugin with the given GUID.
   */
  private static <T> IPlugin<T> getPluginWithGuid(
      final Iterable<Pair<IPlugin<T>, PluginStatus>> plugins, final long guid) {
    for (final Pair<IPlugin<T>, PluginStatus> plugin : plugins) {
      if (plugin.first().getGuid() == guid) {
        return plugin.first();
      }
    }
    return null;
  }

  /**
   * Shows an error dialog depending on a plugin status.
   *
   * @param plugins The loaded plugins.
   * @param plugin The plugin that does not have a valid status.
   * @param pluginStatus The non-valid plugin status.
   */
  private void showError(final LoadResult<T> plugins, final IPlugin<T> plugin,
      final PluginStatus pluginStatus) {
    String message = "";
    String description = "";

    switch (pluginStatus) {
      case DuplicateGuid:
        message = "E00009: " + "Detected a GUID collision between two plugins";
        description = CUtilityFunctions.createDescription(String.format(
            "BinNavi could not load the plugins '%s' and '%s' because they "
            + "share the same GUID.", plugin.getName(), getPluginWithGuid(
            plugins.getLoadedPlugins(), plugin.getGuid()).getName()), new String[] {
            "Plugin GUIDs have to be unique but two plugins share the same "
            + "GUID."}, new String[] {
            "Neither of the two plugins is loaded until the plugin authors "
            + "resolve their GUID collision."});
        break;
      case InvalidGuid:
        message = "E00010: " + "Detected a plugin with an invalid GUID";
        description = CUtilityFunctions.createDescription(String.format(
            "The plugin '%s' has the invalid GUID %d.", plugin.getName(),
            plugin.getGuid()), new String[] {
            "Certain values are not valid for plugin GUIDs. The specified "
            + "plugin uses such an invalid GUID."}, new String[] {
            "The plugin is not loaded until the plugin author changes "
            + "the GUID of this plugin."});
        break;
      case InvalidName:
        message = "E00011: " + "Detected a plugin with an invalid name";
        description = CUtilityFunctions.createDescription(String.format(
            "The plugin with GUID %d has the invalid name '%s'.", plugin.getGuid(),
            plugin.getName()), new String[] {
            "Certain values are not valid for plugin names. The specified "
            + "plugin uses such an invalid name."}, new String[] {
            "The plugin is not loaded until the plugin author changes the "
            + "name of this plugin."});
        break;
      case InvalidNameGuid:
        message = "E00103: " + "Detected a plugin with an invalid name and an invalid GUID";
        description = CUtilityFunctions.createDescription(String.format(
            "The plugin with GUID %d has the invalid name '%s'.", plugin.getGuid(),
            plugin.getName()), new String[] {
            "Certain values are not valid for plugin names and plugin GUIDs. "
            + "The specified plugin uses such an invalid name and an invalid GUID."}, new String[] {
            "The plugin is not loaded until the plugin author changes the "
            + "name and the GUID of this plugin."});
        break;
      default:
        throw new IllegalStateException("IE00001: Encountered an invalid plugin status");
    }

    NaviErrorDialog.show(parent, message, description);
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    setDescription("Loading JAR files");

    final Set<File> jarFiles = JarLoader.collectJars(rootPath);

    JarLoader.loadJars(jarFiles, this);

    final Set<File> scriptFiles = ScriptLoader.collectScripts(rootPath);

    ScriptLoader.init(scriptFiles, pluginInterface, this);

    final List<IPlugin<T>> scriptPlugins = registry.getPlugins();


    for (final String pluginPath : pluginPaths) {
      final Set<File> pluginFiles = PluginLoader.collectPluginFiles(pluginPath);
      final LoadResult<T> plugins = PluginLoader.loadPlugins(pluginPath, pluginFiles, this);

      for (final Pair<String, Throwable> failedPlugin : plugins.getFailedPlugins()) {
        CUtilityFunctions.logException(failedPlugin.second());

        final String message = "E00007: " + "Loading a plugin failed";
        final String description = CUtilityFunctions.createDescription(String.format(
            "BinNavi could not load the plugin file file '%s' because the plugin "
            + "caused an exception.", failedPlugin.first()), new String[] {
            "The plugin file contains a bug that caused the exception"}, new String[] {
            "The plugin file was not loaded and the functionality of "
            + "the script will not be available in BinNavi"});

        NaviErrorDialog.show(parent, message, description, failedPlugin.second());
      }

      for (final Pair<IPlugin<T>, PluginStatus> pair : plugins.getLoadedPlugins()) {
        final IPlugin<T> plugin = pair.first();
        final PluginStatus status = pair.second();

        if (status != PluginStatus.Valid) {
          showError(plugins, plugin, status);
        }
      }

      PluginInitializer.initializePlugins(pluginInterface, registry, plugins.getLoadedPlugins(),
          configFile);

      for (final IPlugin<T> plugin : scriptPlugins) {
        try {
          plugin.init(pluginInterface);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);

          registry.removePlugin(plugin, DisabledPluginReason.ThrewOnInit);
        }
      }

      for (final Pair<IPlugin<T>, DisabledPluginReason> disabledPair :
          registry.getDisabledPlugins()) {
        final IPlugin<T> plugin = disabledPair.first();
        final DisabledPluginReason reason = disabledPair.second();

        if (reason == DisabledPluginReason.ThrewOnInit) {
          final String message = "E00102: " + "Error during plugin initialization";
          final String description = CUtilityFunctions.createDescription(String.format(
              "BinNavi could not initialize the plugin '%s' because the plugin "
              + "caused an exception.", plugin.getName()),
              new String[] {"A bug in the plugin caused an exception."}, new String[] {
                  "The plugin could not be initialized properly and was removed "
                  + "from the list of loaded plugins."});

          NaviErrorDialog.show(parent, message, description);
        }
      }
    }
  }
}
