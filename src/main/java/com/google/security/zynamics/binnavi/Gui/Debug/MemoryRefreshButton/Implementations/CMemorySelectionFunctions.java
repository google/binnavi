/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Implementations;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemRangeDialog.CMemoryRangeDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;

/**
 * Contains the implementations of the memory selection actions.
 */
public final class CMemorySelectionFunctions {
  /**
   * Do not create this class.
   */
  private CMemorySelectionFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Asks the user for a memory range and displays it afterwards.
   *
   * @param dlg Dialog where the user can select a range.
   * @param debugPerspectiveModel Describes the debug GUI perspective where the refresh action takes
   *        place.
   */
  public static void askMemoryRange(
      final CMemoryRangeDialog dlg, final CDebugPerspectiveModel debugPerspectiveModel) {
    final IDebugger debugger = debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger == null) {
      return;
    }

    dlg.setVisible(true);

    final IAddress start = dlg.getStart();
    final IAddress numberOfBytes = dlg.getBytes();

    if (start != null && numberOfBytes != null) {
      debugPerspectiveModel.setActiveMemoryAddress(start, true);

      final ProcessManager pmanager = debugger.getProcessManager();

      pmanager.setMemoryMap(new MemoryMap(new FilledList<MemorySection>()));
      pmanager.getMemory().clear();

      final ArrayList<MemorySection> sections = new ArrayList<MemorySection>();

      sections.add(new MemorySection(start, new CAddress(start.toBigInteger().add(
          numberOfBytes.toBigInteger()).subtract(BigInteger.ONE))));

      final MemoryMap map = new MemoryMap(sections);

      pmanager.setMemoryMap(map);
    }
  }

  /**
   * Refreshes the currently visible memory.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that is used to request memory.
   * @param address Start address of the memory part to refresh.
   * @param size Number of bytes to reload.
   */
  public static void refreshMemory(
      final JFrame parent, final IDebugger debugger, final IAddress address, final int size) {
    Preconditions.checkNotNull(parent, "IE01451: Parent argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01452: Debugger argument can not be null");

    if (!debugger.isConnected()) {
      // Fail silently. Apparently some other event closed the
      // debug connection.
      return;
    }

    try {
      debugger.getMemoryMap();
      debugger.readMemory(address, size);
    } catch (final DebugExceptionWrapper exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00080: " + "Could not refresh memory";
      final String innerDescription = CUtilityFunctions.createDescription(
          "The memory list and the memory data could not be refreshed.",
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The memory data was not refreshed."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    }
  }
}
