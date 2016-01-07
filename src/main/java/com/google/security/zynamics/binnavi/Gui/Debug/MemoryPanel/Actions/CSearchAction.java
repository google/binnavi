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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.CMemoryViewer;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;

/**
 * Action class that is used to offer memory search functionality.
 */
public final class CSearchAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1886770750862510207L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * Debugger that performs the Search operation.
   */
  private final IFrontEndDebuggerProvider m_debugger;

  /**
   * Memory viewer where the results of the search is shown.
   */
  private final CMemoryViewer m_memoryView;

  /**
   * Creates a new search action
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that performs the Search operation.
   * @param memoryView Memory viewer where the results of the search is shown.
   */
  public CSearchAction(final Window parent, final IFrontEndDebuggerProvider debugger,
      final CMemoryViewer memoryView) {
    super("Search memory");

    Preconditions.checkNotNull(parent, "IE01424: Parent argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01425: Debugger argument can not be null");

    Preconditions.checkNotNull(memoryView, "IE01426: Memory view argument can not be null");

    m_parent = parent;
    m_memoryView = memoryView;
    m_debugger = debugger;

    putValue(Action.SHORT_DESCRIPTION, "Search memory");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final IDebugger debugger = m_debugger.getCurrentSelectedDebugger();

    if (debugger != null) {
      CMemoryFunctions.searchMemory(m_parent, debugger, m_memoryView);
    }
  }
}
