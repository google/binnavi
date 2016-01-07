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
package com.google.security.zynamics.binnavi.Gui.plugins;

import com.google.security.zynamics.zylib.gui.DefaultWrapper;

/**
 * Wrapper class to display IPlugin objects in GUI controls.
 * 
 * @param <T> The plugin interface which is used by plugins to interface with com.google.security.zynamics.binnavi.
 */
public final class PluginItem<T> extends DefaultWrapper<com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T>> {
  /**
   * Creates a new plugin wrapper.
   * 
   * @param plugin The plugin to wrap.
   */
  public PluginItem(final com.google.security.zynamics.binnavi.api2.plugins.IPlugin<T> plugin) {
    super(plugin);
  }

  @Override
  public String toString() {
    return getObject().getName();
  }
}
