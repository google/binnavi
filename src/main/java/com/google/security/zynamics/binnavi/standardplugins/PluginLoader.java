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
package com.google.security.zynamics.binnavi.standardplugins;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.plugins.IPluginServer;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.standardplugins.callresolver.CallResolverPlugin;
import com.google.security.zynamics.binnavi.standardplugins.coverage.VisualCoveragePlugin;
import com.google.security.zynamics.binnavi.standardplugins.criterium.LoopSelectionCriteriumPlugin;
import com.google.security.zynamics.binnavi.standardplugins.pathfinder.PathfinderPlugin;

import java.util.Collection;
import java.util.List;

/**
 * Loads all standard plugins for BinNavi. Be aware that some plugins should not be shipped in an
 * release prior to writing proper documentation for them.
 */
public final class PluginLoader implements IPluginServer<IPluginInterface> {
  @Override
  public Collection<IPlugin<IPluginInterface>> getPlugins() {
    final List<IPlugin<IPluginInterface>> plugins = Lists.newArrayList();
    plugins.add(new PathfinderPlugin());
    plugins.add(new CallResolverPlugin());
    plugins.add(new LoopSelectionCriteriumPlugin());
    plugins.add(new VisualCoveragePlugin());
    // Plugins below this line are not supposed to be shipped.
    return plugins;
  }
}
