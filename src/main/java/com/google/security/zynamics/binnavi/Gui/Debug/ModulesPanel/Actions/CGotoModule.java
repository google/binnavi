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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Actions;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Implementations.CModulesPanelFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;


/**
 * Action class to go to the beginning of a memory module in the memory viewer.
 */
public final class CGotoModule extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6564446716008091093L;

  /**
   * Parent used for dialogs.
   */
  private final Window m_parent;

  /**
   * Describes the debugger perspective where the module is shown.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Memory module to be shown.
   */
  private final MemoryModule m_module;

  /**
   * Creates a new action object.
   *
   * @param parent Parent used for dialogs.
   * @param debugPerspectiveModel Describes the debugger perspective where the module is shown.
   * @param module Memory module to be shown.
   */
  public CGotoModule(final Window parent, final CDebugPerspectiveModel debugPerspectiveModel,
      final MemoryModule module) {
    super(String.format("Display module '%s'", module.getName()));

    m_parent = parent;
    m_debugPerspectiveModel = debugPerspectiveModel;
    m_module = module;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CModulesPanelFunctions.gotoModule(m_parent, m_debugPerspectiveModel, m_module);
  }
}
