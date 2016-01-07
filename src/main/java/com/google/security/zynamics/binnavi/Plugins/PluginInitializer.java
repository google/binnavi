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
package com.google.security.zynamics.binnavi.Plugins;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.PluginConfigItem;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Initializes the loaded plugin with the global plugin interface.
 */
public final class PluginInitializer {
  /**
   * You are not supposed to instantiate this class.
   */
  private PluginInitializer() {}

  /**
   * Finds a plugin in the configuration file by GUID.
   *
   * @param validatedPlugins The plugin load order to look through.
   * @param guid The GUID of the plugin to look for.
   * @param <T> The interface the given plugins implement.
   * @return The plugin or null if the plugin could not be found.
   */
  private static <T> IPlugin<T> findPlugin(
      final List<Pair<IPlugin<T>, PluginStatus>> validatedPlugins, final long guid) {
    for (final Pair<IPlugin<T>, PluginStatus> pt : validatedPlugins) {
      if (pt.first().getGuid() == guid) {
        return pt.first();
      }
    }
    return null;
  }

  /**
   * Loads the plugins that are configured in the configuration file.
   *
   * @param pluginInterface The plugin interface object that's given to all
   *        plugins.
   * @param registry Plugin registry that contains the plugins.
   * @param xmlPluginOrder The plugin load order as defined in the configuration
   *        file.
   * @param validatedPlugins The plugins recognized in the plugins directory.
   * @param initializedPlugins If a plugin initialization is successful, the
   *        plugin is added to this list.
   * @param processedPlugins After this function, the GUIDs of all configured
   *        plugins are stored in this list.
   * @param <T> The interface which is used by plugins to interface with
   *        BinNavi.
   */
  private static <T> void initializeKnownPlugins(final T pluginInterface,
      final IPluginRegistry<IPlugin<T>> registry, final List<PluginConfigItem> xmlPluginOrder,
      final List<Pair<IPlugin<T>, PluginStatus>> validatedPlugins,
      final List<Pair<IPlugin<T>, Exception>> initializedPlugins,
      final Set<Long> processedPlugins) {
    for (final PluginConfigItem pt : xmlPluginOrder) {
      processedPlugins.add(pt.getGUID());

      final IPlugin<T> plugin = findPlugin(validatedPlugins, pt.getGUID());

      if (plugin != null) {
        if (pt.isLoad()) {
          initializePlugin(plugin, pluginInterface, registry, initializedPlugins);
        } else {
          registry.addDisabledPlugin(plugin, DisabledPluginReason.NotLoaded);
        }
      }
    }
  }

  /**
   * Loads a single plugin.
   *
   * @param plugin The plugin to load.
   * @param pluginInterface The plugin interface object that's given to all
   *        plugins.
   * @param registry Plugin registry that contains the plugins.
   * @param initializedPlugins If a plugin is properly loaded, it is added to
   *        this list.
   * @param <T> The interface which is used by plugins to interface with
   *        BinNavi.
   */
  private static <T> void initializePlugin(final IPlugin<T> plugin, final T pluginInterface,
      final IPluginRegistry<IPlugin<T>> registry,
      final List<Pair<IPlugin<T>, Exception>> initializedPlugins) {
    // We load a plugin either if the plugin is not configured in the
    // configuration file or if the configuration file says the plugin
    // should be loaded.

    registry.addPlugin(plugin);
    try {
      plugin.init(pluginInterface);
    } catch (final Exception exception) {
      registry.removePlugin(plugin, DisabledPluginReason.ThrewOnInit);
      initializedPlugins.add(new Pair<IPlugin<T>, Exception>(plugin, exception));
    }
  }

  /**
   * Loads the plugins that are not configured in the configuration file.
   *
   * @param pluginInterface The plugin interface object that's given to all
   *        plugins.
   * @param registry Plugin registry that contains the plugins.
   * @param validatedPlugins The plugins recognized in the plugins directory.
   * @param initializedPlugins If a plugin initialization is successful, the
   *        plugin is added to this list.
   * @param processedPlugins After this function, the GUIDs of all configured
   *        plugins are stored in this list.
   * @param <T> The plugin interface which is used by plugins to interface with
   *        BinNavi.
   */
  private static <T> void initializeUnknownPlugins(final IPluginRegistry<IPlugin<T>> registry,
      final T pluginInterface, final List<Pair<IPlugin<T>, PluginStatus>> validatedPlugins,
      final List<Pair<IPlugin<T>, Exception>> initializedPlugins,
      final Set<Long> processedPlugins) {
    // Don't remove the getPlugins() => clone()
    for (final Pair<IPlugin<T>, PluginStatus> pluginPair : validatedPlugins) {
      if ((pluginPair.second() == PluginStatus.Valid)
          && !processedPlugins.contains(pluginPair.first().getGuid())) {
        initializePlugin(pluginPair.first(), pluginInterface, registry, initializedPlugins);
      }
    }
  }

  /**
   * Initializes all valid plugins with the proper plugin interface object.
   *
   * @param pluginInterface The plugin interface object that's given to all
   *        plugins.
   * @param registry Plugin registry that contains the plugins.
   * @param list Plugins which were loaded successfully.
   * @param configFile BinNavi configuration file.
   * @param <T> The plugin interface which is used by plugins to interface with
   *        BinNavi.
   * @return List of plugins and the exception they threw during initialization.
   */
  public static <T> List<Pair<IPlugin<T>, Exception>> initializePlugins(final T pluginInterface,
      final IPluginRegistry<IPlugin<T>> registry, final List<Pair<IPlugin<T>, PluginStatus>> list,
      final ConfigManager configFile) {
    Preconditions.checkNotNull(pluginInterface, "IE00828: Plugin interface can't be null");
    Preconditions.checkNotNull(registry, "IE00829: Plugin registry can't be null");
    Preconditions.checkNotNull(list, "IE00830: Plugins can't be null");
    Preconditions.checkNotNull(configFile, "IE00831: Config File can't be null");

    // Plugins must be initialized in the correct order. This order is specified
    // by the user in the XML configuration file. The initialization order
    // decides
    // the GUI layout of BinNavi among other things.

    final ArrayList<Pair<IPlugin<T>, Exception>> initializedPlugins =
        new ArrayList<Pair<IPlugin<T>, Exception>>();
    final List<PluginConfigItem> pluginOrder = configFile.getGeneralSettings().getPlugins();
    final Set<Long> processedPlugins = new HashSet<Long>();
    initializeKnownPlugins(pluginInterface, registry, pluginOrder, list, initializedPlugins,
        processedPlugins);
    initializeUnknownPlugins(registry, pluginInterface, list, initializedPlugins, processedPlugins);
    return initializedPlugins;
  }
}
