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

import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;

import java.util.List;

import javax.swing.JComponent;

/**
 * This interface can be implemented by plugins that want to extend the popup menu that is shown
 * when the user right-clicks on a module node in the project tree or on a table that is used to
 * display information about modules.
 */
public interface IModuleMenuPlugin extends IPlugin<IPluginInterface> {
  /**
   * Returns a list of components that should be added to the context menu.
   *
   * @param modules The modules for which the context menu is created.
   *
   * @return A list of components that are added to the context menu.
   */
  List<JComponent> extendModuleMenu(List<Module> modules);
}
