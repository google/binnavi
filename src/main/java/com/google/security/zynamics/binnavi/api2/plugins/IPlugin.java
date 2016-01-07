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
package com.google.security.zynamics.binnavi.api2.plugins;

/**
 * Every plugin needs to implement this interface.
 * 
 * @param <T> The type of the plugin interface the plugin wants to use to interface with BinNavi.
 */
public interface IPlugin<T> {
  // ! The description of the plugin.
  /**
   * Returns a description of the plugin. This description is used in the plugin configuration
   * dialog to let the user know what the plugin does.
   * 
   * @return The description of the plugin.
   */
  String getDescription();

  // ! The GUID of the plugin.
  /**
   * Returns a unique GUID that identifies the plugin. Plugins with duplicate GUIDs will not be
   * loaded.
   * 
   * @return The GUID that identifies the plugin.
   */
  long getGuid();

  // ! The name of the plugin.
  /**
   * Returns the name of the plugin. This name is used in the plugin configuration dialog to let the
   * user select what plugin to configure. Note that plugins that return null from this function
   * will not be loaded.
   * 
   * @return The name of the plugin.
   */
  String getName();

  // ! Initializes the plugin.
  /**
   * The plugin manager calls this function for each plugin right after loading a plugin for the
   * first time. Note that this function should be used to initialize the plugin. Do not use the
   * constructor of the plugin to initialize plugin. Further note that all plugins must have a
   * standard constructor that does not take any arguments. Only this constructor is used to load a
   * plugin.
   * 
   * @param pluginInterface The plugin interface that gives a plugin access to BinNavi.
   */
  void init(T pluginInterface);

  // ! Unloads the plugin.
  /**
   * This function is called when the plugin is unloaded. Plugins that actively changed the
   * appearance of BinNavi (for example by adding menus or showing dialogs) must remove all changes
   * in this function.
   */
  void unload();
}
