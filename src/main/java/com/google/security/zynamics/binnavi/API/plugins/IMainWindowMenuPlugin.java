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

import java.util.List;

import javax.swing.JMenuItem;

// / Used to extend the main menu of the main window.
/**
 * This interface can be implemented by all plugins that want to extend the main menu of main
 * windows.
 */
public interface IMainWindowMenuPlugin extends com.google.security.zynamics.binnavi.api2.plugins.IPlugin<PluginInterface> {
  // ! Extends the main window main menu.
  /**
   * Returns the components that should be added to the main menu of the main window.
   *
   *  If you do not want to extend the plugin menu in any way, please make sure to return an empty
   * list. Returning null is invalid.
   *
   * @return A list of components that are added to the plugin menu.
   */
  List<JMenuItem> extendPluginMenu();
}
