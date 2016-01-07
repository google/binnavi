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
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.PluginConfigItem;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Plugin registry that manages all loaded and not-loaded plugins.
 */
public final class PluginRegistry implements IPluginRegistry<IPlugin<IPluginInterface>> {
  /**
   * List of loaded plugins.
   */
  private final List<IPlugin<IPluginInterface>> plugins = Lists.newArrayList();

  /**
   * List of plugins disabled for any reason.
   */
  private final List<Pair<IPlugin<IPluginInterface>, DisabledPluginReason>> disabledPlugins =
      Lists.newArrayList();

  /**
   * Verifies the validity of the plugin object to add to the registry. If the plugin is not as
   * expected, throw an exception.
   * 
   * @param plugin The plugin to verify.
   */
  private void verifyPluginToAdd(final IPlugin<IPluginInterface> plugin) {
    Preconditions.checkNotNull(plugin, "IE00835: Plugin can't be null");

    if ((plugin.getName() == null) || plugin.getName().equals("")) {
      throw new IllegalArgumentException("IE00836: Invalid plugin name");
    }

    if (plugin.getGuid() == 0) {
      throw new IllegalArgumentException("IE00837: Invalid plugin GUID");
    }

    for (final IPlugin<IPluginInterface> oldPlugin : plugins) {
      // IMPORTANT: Do not use contains to compare plugins. If you do,
      // then plugins must override equals.
      if (oldPlugin == plugin) {
        throw new IllegalArgumentException("IE00838: Can not add plugin more than once");
      }

      // ESCA-JAVA0286:
      if (oldPlugin.getGuid() == plugin.getGuid()) {
        throw new IllegalArgumentException("IE00839: Plugin with GUID " + plugin.getGuid()
            + " already exists");
      }
    }
  }

  @Override
  public void addDisabledPlugin(final IPlugin<IPluginInterface> plugin,
      final DisabledPluginReason reason) {
    Preconditions.checkNotNull(plugin, "IE00833: Plugin can't be null");
    Preconditions.checkNotNull(reason, "IE00834: Reason can't be null");

    disabledPlugins.add(new Pair<IPlugin<IPluginInterface>, DisabledPluginReason>(plugin, reason));
  }

  @Override
  public void addPlugin(final IPlugin<IPluginInterface> plugin) {
    verifyPluginToAdd(plugin);

    for (final PluginConfigItem pluginType :
        ConfigManager.instance().getGeneralSettings().getPlugins()) {
      // If the configuration file says this plugin should be disabled, do
      // not load the plugin.
      if ((pluginType.getGUID() == plugin.getGuid()) && !pluginType.isLoad()) {
        addDisabledPlugin(plugin, DisabledPluginReason.NotLoaded);
        return;
      }
    }

    plugins.add(plugin);
  }

  @Override
  public List<Pair<IPlugin<IPluginInterface>, DisabledPluginReason>> getDisabledPlugins() {
    return new ArrayList<Pair<IPlugin<IPluginInterface>, DisabledPluginReason>>(disabledPlugins);
  }

  @Override
  public List<IPlugin<IPluginInterface>> getPlugins() {
    return new ArrayList<IPlugin<IPluginInterface>>(plugins);
  }

  @Override
  public Iterator<IPlugin<IPluginInterface>> iterator() {
    return plugins.iterator();
  }

  @Override
  public void removePlugin(final IPlugin<IPluginInterface> plugin,
      final DisabledPluginReason reason) {
    Preconditions.checkNotNull(plugin, "IE00840: Plugin can't be null");
    Preconditions.checkNotNull(reason, "IE00841: Reason can't be null");

    if (!plugins.remove(plugin)) {
      throw new IllegalArgumentException("IE00842: Plugin is not managed by the registry");
    }

    disabledPlugins.add(new Pair<IPlugin<IPluginInterface>, DisabledPluginReason>(plugin, reason));
  }

  @Override
  public void unloadAll() {
    // TODO: Creating a copy of the plugins list is not sufficient for synchronization.
    for (final IPlugin<IPluginInterface> plugin : Lists.newArrayList(plugins)) {
      plugin.unload();
      removePlugin(plugin, DisabledPluginReason.Unload);
    }
    disabledPlugins.clear();
  }
}
