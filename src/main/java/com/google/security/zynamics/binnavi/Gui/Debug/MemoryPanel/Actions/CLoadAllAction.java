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
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations.CMemoryFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Action class that can be used to load a whole memory section.
 */
public final class CLoadAllAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2977317053984570234L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that loads the memory sections.
   */
  private final IDebugger m_debugger;

  /**
   * The offset where the loading begins.
   */
  private final IAddress m_offset;

  /**
   * The number of bytes to load.
   */
  private final int m_size;

  /**
   * Creates a new load all action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that loads the memory sections.
   * @param offset The offset where the loading begins.
   * @param size The number of bytes to load.
   */
  public CLoadAllAction(
      final JFrame parent, final IDebugger debugger, final IAddress offset, final int size) {
    super("Load whole section");

    Preconditions.checkNotNull(parent, "IE01421: Parent argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01422: Debugger argument can not be null");

    Preconditions.checkNotNull(offset, "IE01423: Offset argument can't be null");

    m_parent = parent;
    m_debugger = debugger;
    m_offset = offset;
    m_size = size;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.loadAll(m_parent, m_debugger, m_offset, m_size);
  }
}
