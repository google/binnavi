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
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.CMemoryViewer;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;

/**
 * Action class that shows the Goto dialog and moves the caret in the memory viewer to the selected
 * offset.
 */
public final class CGotoAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6712952484091111537L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Hex view to focus after the Goto operation.
   */
  private final CMemoryViewer m_view;

  /**
   * Process manager that checks whether the given offset is in the target memory.
   */
  private final CDebugPerspectiveModel m_debugger;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param view Hex view to focus after the Goto operation.
   * @param debugger Process manager that checks whether the given offset is in the target memory.
   */
  public CGotoAction(
      final JFrame parent, final CMemoryViewer view, final CDebugPerspectiveModel debugger) {
    super("Goto Address");

    Preconditions.checkNotNull(parent, "IE01418: Parent argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01419: Process manager argument can not be null");

    m_parent = parent;
    m_view = view;
    m_debugger = debugger;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.gotoOffset(m_parent, m_view, m_debugger);
  }
}
