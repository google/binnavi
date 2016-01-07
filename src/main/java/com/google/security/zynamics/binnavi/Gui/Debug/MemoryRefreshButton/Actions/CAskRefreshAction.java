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
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemRangeDialog.CMemoryRangeDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.Implementations.CMemorySelectionFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Action class that asks the user for a memory section to display.
 */
public final class CAskRefreshAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2776808519717036411L;

  /**
   * Describes the debug GUI perspective where the refresh action takes place.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Memory range dialog associated with this action.
   */
  private final CMemoryRangeDialog m_dialog;

  /**
   * Creates a new refresh action.
   *
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Describes the debug GUI perspective where the refresh action takes
   *        place.
   */
  public CAskRefreshAction(
      final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    Preconditions.checkNotNull(parent, "IE01446: Parent argument can not be null");
    m_debugPerspectiveModel = Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01447: Debug perspective model argument can not be null");

    m_dialog = new CMemoryRangeDialog(parent);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemorySelectionFunctions.askMemoryRange(m_dialog, m_debugPerspectiveModel);
  }
}
