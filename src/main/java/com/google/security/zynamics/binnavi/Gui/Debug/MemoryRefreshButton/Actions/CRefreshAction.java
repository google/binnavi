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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.IRefreshRangeProvider;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Implementations.CMemorySelectionFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action class that can be used to refresh the current memory section.
 */
public final class CRefreshAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3258591696396189580L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Describes the debug GUI perspective where the refresh action takes place.
   */
  private final IFrontEndDebuggerProvider m_debugPerspectiveModel;

  /**
   * Provides information about the range to refresh.
   */
  private final IRefreshRangeProvider m_rangeProvider;

  /**
   * Provides information about the stack memory to refresh.
   */
  private final IRefreshRangeProvider m_stackRangeProvider;

  /**
   * Creates a new refresh memory action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Describes the debug GUI perspective where the refresh action takes
   *        place.
   * @param rangeProvider Provides information about the range to refresh.
   * @param stackRangeProvider Provides information about the stack to refresh.
   */
  public CRefreshAction(final JFrame parent, final IFrontEndDebuggerProvider debugPerspectiveModel,
      final IRefreshRangeProvider rangeProvider, final IRefreshRangeProvider stackRangeProvider) {
    Preconditions.checkNotNull(parent, "IE01448: Parent argument can not be null");

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01449: Debug perspective model argument can not be null");

    m_parent = parent;
    m_debugPerspectiveModel = debugPerspectiveModel;
    m_rangeProvider = rangeProvider;
    m_stackRangeProvider = stackRangeProvider;

    putValue(Action.SMALL_ICON, new ImageIcon(CMain.class.getResource("data/memoryupdate_up.jpg")));
    putValue(Action.SHORT_DESCRIPTION, "Refresh Memory");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

    if (debugger != null) {
      CMemorySelectionFunctions.refreshMemory(
          m_parent, debugger, m_rangeProvider.getAddress(), m_rangeProvider.getSize());

      final IAddress stackAddress = m_stackRangeProvider.getAddress();
      final int size = m_stackRangeProvider.getSize();

      if (stackAddress != null && size != 0) {
        CMemorySelectionFunctions.refreshMemory(m_parent, debugger, stackAddress, size);
      }
    }
  }
}
