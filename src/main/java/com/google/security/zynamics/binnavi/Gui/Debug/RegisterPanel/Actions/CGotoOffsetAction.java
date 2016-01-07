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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action class that can be used to go to an offset in memory.
 */
public final class CGotoOffsetAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3893672164697576085L;

  /**
   * The Debug GUI perspective where the memory is shown.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * The offset to go to.
   */
  private final IAddress m_offset;

  /**
   * Creates a new Goto Offset action object.
   *
   * @param offset The offset to go to.
   * @param debugPerspectiveModel The Debug GUI perspective where the memory is shown.
   */
  public CGotoOffsetAction(
      final CDebugPerspectiveModel debugPerspectiveModel, final IAddress offset) {
    super(String.format("Goto offset %s", offset.toHexString()));

    Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01106: Debug perspective model argument can not be null");

    m_debugPerspectiveModel = debugPerspectiveModel;
    m_offset = offset;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.gotoOffset(m_debugPerspectiveModel, m_offset, true);
  }
}
