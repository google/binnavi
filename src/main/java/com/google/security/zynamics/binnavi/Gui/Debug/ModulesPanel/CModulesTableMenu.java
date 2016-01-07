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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import java.awt.Window;

import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Actions.CGotoModule;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;

/**
 * Context menu of the memory modules table.
 */
public final class CModulesTableMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1327017158482017484L;

  /**
   * Creates a new table menu object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Describes the debugger perspective the table belongs to.
   * @param module The clicked module.
   */
  public CModulesTableMenu(final Window parent, final CDebugPerspectiveModel debugPerspectiveModel,
      final MemoryModule module) {
    Preconditions.checkNotNull(parent, "IE01458: Parent argument can not be null");
    Preconditions.checkNotNull(module, "IE01459: Module argument can not be null");

    add(CActionProxy.proxy(new CGotoModule(parent, debugPerspectiveModel, module)));
  }
}
