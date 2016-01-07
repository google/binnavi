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
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.Iterator;
import java.util.List;

/**
 * Keeps track of all registered plugins and can be used to register new plugins.
 */
public final class PluginRegistry implements Iterable<IPlugin<IPluginInterface>>,
    ApiObject<com.google.security.zynamics.binnavi.Plugins.PluginRegistry> {
  /**
   * The wrapped internal plugin registry.
   */
  private final com.google.security.zynamics.binnavi.Plugins.PluginRegistry m_registry;

  /**
   * Creates a new API plugin registry object.
   *
   * @param registry The wrapped internal registry object.
   */
  public PluginRegistry(
      final com.google.security.zynamics.binnavi.Plugins.PluginRegistry registry) {
    m_registry = registry;
  }

  @Override
  public com.google.security.zynamics.binnavi.Plugins.PluginRegistry getNative() {
    return m_registry;
  }

  /***
   * Add a new plugin to the list of registered plugins.
   *
   * @param plugin The plugin to add.
   */
  public void addPlugin(final IPlugin<IPluginInterface> plugin) {
    Preconditions.checkNotNull(plugin, "Error: Plugin argument can not be null");
    m_registry.addPlugin(plugin);
  }

  /**
   * Returns a list of all known plugins.
   *
   * @return All known plugins.
   */
  public List<IPlugin<IPluginInterface>> getPlugins() {
    return m_registry.getPlugins();
  }

  @Override
  public Iterator<IPlugin<IPluginInterface>> iterator() {
    return m_registry.iterator();
  }
}
