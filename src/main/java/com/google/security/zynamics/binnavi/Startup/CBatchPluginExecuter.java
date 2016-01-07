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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.plugins.IBatchPlugin;
import com.google.security.zynamics.binnavi.Plugins.IPluginRegistry;

/**
 * Contains code for executing batch plugins.
 */
public final class CBatchPluginExecuter {
  /**
   * You are not supposed to instantiate this.
   */
  private CBatchPluginExecuter() {
  }

  /**
   * Executes a batch plugin. This can be used to start BinNavi in batch mode and just execute a
   * plugin.
   * 
   * @param pluginName The name of the plugin.
   * @param pluginRegistry The plugin registry that holds all active plugins.
   */
  public static <T> void execute(final String pluginName,
      final IPluginRegistry<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> pluginRegistry) {
    for (final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin : pluginRegistry) {
      if ((plugin instanceof IBatchPlugin) && plugin.getName().equals(pluginName)) {
        // ESCA-JAVA0166:
        try {
          ((IBatchPlugin) plugin).run();
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);

          return;
        }
      }
    }
  }

}
