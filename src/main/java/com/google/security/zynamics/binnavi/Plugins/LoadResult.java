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
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;

/**
 * Used to store the result of a plugin loading operation.
 */
public final class LoadResult<T> {
  /**
   * Plugins that were loaded successfully.
   */
  private final List<Pair<IPlugin<T>, PluginStatus>> loadedPlugins;

  /**
   * Plugins that threw an exception during initialization.
   */
  private final List<Pair<String, Throwable>> failedPlugins;

  /**
   * Creates a new load result object.
   *
   * @param loadedPlugins Plugins that were loaded successfully.
   * @param failedPlugins Plugins that threw an exception during initialization.
   */
  public LoadResult(final List<Pair<IPlugin<T>, PluginStatus>> loadedPlugins,
      final List<Pair<String, Throwable>> failedPlugins) {
    this.loadedPlugins = Lists.newArrayList(Preconditions.checkNotNull(loadedPlugins,
        "IE01657: Loaded plugins argument can not be null"));
    this.failedPlugins = Lists.newArrayList(Preconditions.checkNotNull(failedPlugins,
        "IE01659: Failed plugins argument can not be null"));
  }

  /**
   * Returns the plugins that threw an exception during initialization.
   *
   * @return The plugins that threw an exception during initialization.
   */
  public List<Pair<String, Throwable>> getFailedPlugins() {
    return Lists.newArrayList(failedPlugins);
  }

  /**
   * Returns the plugins that were loaded successfully.
   *
   * @return The plugins that were loaded successfully.
   */
  public List<Pair<IPlugin<T>, PluginStatus>> getLoadedPlugins() {
    return Lists.newArrayList(loadedPlugins);
  }
}
