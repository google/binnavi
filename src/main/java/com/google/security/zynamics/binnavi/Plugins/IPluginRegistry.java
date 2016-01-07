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

import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;


/**
 * Each plugin registry manager needs to implement this interface. The interface is parameterized by
 * the type of plugin which is managed by the registry.
 * 
 * @param <T> The interface which all plugins need to implement.
 */
public interface IPluginRegistry<T extends com.google.security.zynamics.binnavi.api2.plugins.IPlugin<?>> extends Iterable<T> {
  /**
   * Adds a new disabled plugin to the plugin registry.
   * 
   * @param plugin The plugin to add.
   * @param reason The reason why the plugin was disabled.
   */
  public void addDisabledPlugin(final T plugin, final DisabledPluginReason reason);

  /**
   * Adds a new plugin to the plugin registry.
   * 
   * @param plugin The plugin to add.
   */
  public void addPlugin(final T plugin);

  /**
   * Returns all disabled plugins.
   * 
   * @return A list of all disabled plugins.
   */
  public List<Pair<T, DisabledPluginReason>> getDisabledPlugins();

  /**
   * Returns all loaded plugins known to the plugin registry.
   * 
   * @return A list of loaded plugins.
   */
  public List<T> getPlugins();

  /**
   * Removes a plugin from the plugin registry.
   * 
   * @param plugin The plugin to remove.
   * @param reason The reason for why the plugin was removed.
   */
  public void removePlugin(final T plugin, final DisabledPluginReason reason);

  /**
   * Unloads all plugins.
   */
  public void unloadAll();
}
