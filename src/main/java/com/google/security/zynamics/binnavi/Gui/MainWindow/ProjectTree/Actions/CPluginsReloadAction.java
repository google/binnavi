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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.HotKeys;


/**
 * Action class for reloading plugins.
 */
public final class CPluginsReloadAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6032646212938176324L;

  /**
   * Creates a new action object.
   */
  public CPluginsReloadAction() {
    super("Reload Plugins");
    putValue(ACCELERATOR_KEY, HotKeys.RELOAD_PLUGINS_ACCELERATOR_KEY.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    PluginInterface.instance().reloadPlugins();
  }
}
