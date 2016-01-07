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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action that can be used to follow a value in the target process memory.
 */
public final class CFollowDumpAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3952276688640973986L;

  /**
   * Debug GUI where that contains the memory viewer.
   */
  private final CDebugPerspectiveModel m_perspectiveModel;

  /**
   * The offset to go to.
   */
  private final IAddress m_offset;

  /**
   * Creates a new action object.
   *
   * @param perspectiveModel Debug GUI where that contains the memory viewer.
   * @param offset The offset to go to.
   */
  public CFollowDumpAction(final CDebugPerspectiveModel perspectiveModel, final IAddress offset) {
    super("Follow DWORD in Dump");

    Preconditions.checkNotNull(
        perspectiveModel, "IE01416: Perspective model argument can not be null");

    Preconditions.checkNotNull(offset, "IE01417: Offset argument can not be null");

    m_perspectiveModel = perspectiveModel;
    m_offset = offset;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.gotoOffset(m_perspectiveModel, m_offset, true);
  }
}
