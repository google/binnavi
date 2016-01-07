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

import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Plugins.PluginRegistry;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;

// ! Allows one to write BinNavi plugins that run independently of
// com.google.security.zynamics.binnavi.
/**
 * This class can be used to use the BinNavi API independently from BinNavi. That means you can
 * access BinNavi objects from stand-alone scripts without having to run BinNavi simultaneously.
 *
 *  Please note that API access through this object does not provide all aspects of the BinNavi API.
 * The following limitations exist:
 *
 * - PluginInterface::getProgramPath returns the empty string
 */
public final class StandAlone {
  /**
   * The plugin interface object.
   */
  private static PluginInterface pluginInterface;

  /**
   * You are not supposed to instantiate this class.
   */
  private StandAlone() {}

  // ! Returns the BinNavi plugin interface.
  /**
   * Returns the plugin interface that can be used from stand-alone scripts.
   *
   * @return The plugin interface that can be used from stand-alone scripts.
   */
  public synchronized static PluginInterface getPluginInterface() {
    try {
      ConfigManager.instance().read();
    } catch (final FileReadException exception) {
      NaviLogger.severe("Error: could not read config file.");
    }
    final CDatabaseManager manager = CDatabaseManager.instance();
    final PluginRegistry registry = new PluginRegistry();
    if (pluginInterface == null) {
      pluginInterface = PluginInterface.instance("" /* start path */, manager, registry);
    }
    return pluginInterface;
  }
}
