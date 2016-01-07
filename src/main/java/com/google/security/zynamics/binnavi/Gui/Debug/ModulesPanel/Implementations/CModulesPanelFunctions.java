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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.Implementations;

import java.awt.Window;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;

/**
 * This class contains the implementations for the actions that are available in the modules panel
 * of the debugger. This panel is the one where information about the modules of a process is
 * listed.
 */
public final class CModulesPanelFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CModulesPanelFunctions() {
  }

  /**
   * Displays the beginning of a module in the memory viewer.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Debug perspective model that provides the active debugger.
   * @param module The module to show in the memory viewer.
   */
  public static void gotoModule(final Window parent,
      final CDebugPerspectiveModel debugPerspectiveModel, final MemoryModule module) {
    Preconditions.checkNotNull(parent, "IE01462: Parent argument can not be null");
    Preconditions.checkNotNull(debugPerspectiveModel,
        "IE01463: Debug perspective model argument can not be null");
    Preconditions.checkNotNull(module, "IE01464: Module argument can not be null");

    final IDebugger debugger = debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger == null) {
      return;
    }

    final MemoryMap memoryMap = debugger.getProcessManager().getMemoryMap();

    final MemorySection section =
        ProcessHelpers.getSectionWith(memoryMap, module.getBaseAddress().getAddress());

    if (section == null) {
      final String message =
          String.format("E00046: " + "Could not display module '%s' in the memory view",
              module.getName());
      final String description =
          String.format("The module '%s' could not be displayed in the memory view because "
              + "the offset %s is not currently known to BinNavi. "
              + "Try refreshing the memory map to fix this issue.", module.getName(),
              module.getBaseAddress());

      NaviErrorDialog.show(parent, message, description);
    } else {
      CMemoryFunctions
          .gotoOffset(debugPerspectiveModel, module.getBaseAddress().getAddress(), true);
    }
  }
}
