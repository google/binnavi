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
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Plugins.IPluginRegistry;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.config.ConfigManager;

import java.awt.Window;
import java.io.File;
import java.util.List;

/**
 * Loads all plugins from the plugins directory.
 */
public final class CPluginLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private CPluginLoader() {}

  /**
   * Loads all plugins.
   *
   * @param parent Parent window used for dialogs.
   * @param pluginInterface Plugin interface passed to the plugins.
   * @param registry Plugin registry where the loaded plugins are added.
   * @param startPath BinNavi main directory.
   * @param configFile BinNavi configurations file.
   * @param <T> The plugins interface which is used by plugins to interface with
   *        BinNavi.
   */
  public static <T> void loadPlugins(final Window parent, final T pluginInterface,
      final IPluginRegistry<IPlugin<T>> registry, final String startPath,
      final ConfigManager configFile) {
    Preconditions.checkNotNull(pluginInterface,
        "IE02089: Plugin interface argument can not be null");
    Preconditions.checkNotNull(registry, "IE02090: Registry argument can not be null");
    Preconditions.checkNotNull(startPath, "IE02091: Start path argument can not be null");
    Preconditions.checkNotNull(configFile, "IE02092: Config file argument can not be null");

    final List<String> pluginPaths = Lists.newArrayList(startPath + File.separator + "plugins",
        startPath + File.separator + "userplugins");
    for (final String pluginPath : pluginPaths) {
      NaviLogger.info(String.format("Loading plugins from %s", pluginPath));
    }
    final PluginLoaderThread<T> thread = new PluginLoaderThread<T>(parent, startPath, pluginPaths,
        pluginInterface, registry, configFile);
    CProgressDialog.show(parent, "Loading plugins ...", thread);

    if (thread.getException() != null) {
      CUtilityFunctions.logException(thread.getException());

      final String message =
          "E00104: " + "An unexpected problem occurred while the plugins were loaded";
      final String description = CUtilityFunctions.createDescription(
          "It is unclear what caused this problem. Please check the stack trace for more "
          + "information. If the stack trace does not help you fix this problem please contact "
          + "the BinNavi support.", new String[] {}, new String[] {});
      NaviErrorDialog.show(null, message, description, thread.getException());
    }
  }
}
