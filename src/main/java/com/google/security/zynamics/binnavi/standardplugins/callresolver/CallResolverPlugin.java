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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import com.google.security.zynamics.binnavi.API.disassembly.AddressSpace;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.plugins.IAddressSpaceMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.IModuleMenuPlugin;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * This is the plugin class that gets picked up by com.google.security.zynamics.binnavi. The plugin
 * extends the context menus of modules and address spaces in the project tree of the main window.
 */
public final class CallResolverPlugin implements IModuleMenuPlugin, IAddressSpaceMenuPlugin {
  @Override
  public List<JComponent> extendAddressSpaceMenu(final List<AddressSpace> spaces) {
    final List<JComponent> menus = new ArrayList<JComponent>();

    if (spaces.size() == 1) {
      menus.add(new JMenuItem(new ResolveCallsAction(
          PluginInterface.instance().getMainWindow().getFrame(),
          new AddressSpaceCallResolverTarget(spaces.get(0)))));
    }

    return menus;
  }

  @Override
  public List<JComponent> extendModuleMenu(final List<Module> modules) {
    final List<JComponent> menus = new ArrayList<JComponent>();

    if (modules.size() == 1) {
      menus.add(new JMenuItem(new ResolveCallsAction(
          PluginInterface.instance().getMainWindow().getFrame(),
          new ModuleCallResolverTarget(modules.get(0)))));
    }

    return menus;
  }

  @Override
  public String getDescription() {
    return "This plugin can be used to resolve indirect function calls that depend on one more "
        + "or variable values that are not known to the disassembly.";
  }

  @Override
  public long getGuid() {
    return 423547690534790L;
  }

  @Override
  public String getName() {
    return "Call Resolver";
  }

  @Override
  public void init(final IPluginInterface pluginInterface) {}

  @Override
  public void unload() {}
}
