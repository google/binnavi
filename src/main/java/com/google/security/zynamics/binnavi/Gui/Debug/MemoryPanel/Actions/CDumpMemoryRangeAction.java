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
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataProvider;

/**
 * Action class that can be used dump the current memory range.
 */
public final class CDumpMemoryRangeAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5265270539377308983L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger that reads the data from the target process.
   */
  private final IDebugger m_debugger;

  /**
   * Data provider that collects the data to dump.
   */
  private final IDataProvider m_dataProvider;

  /**
   * Beginning of the memory dump.
   */
  private final IAddress m_start;

  /**
   * Number of bytes to dump.
   */
  private final int m_size;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that reads the data from the target process.
   * @param dataProvider Data provider that collects the data to dump.
   * @param start Beginning of the memory dump.
   * @param size Number of bytes to dump.
   */
  public CDumpMemoryRangeAction(final JFrame parent, final IDebugger debugger,
      final IDataProvider dataProvider, final IAddress start, final int size) {
    super("Dump whole section");

    m_parent = Preconditions.checkNotNull(parent, "IE01412: Parent argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE01413: Debugger argument can not be null");
    m_dataProvider =
        Preconditions.checkNotNull(dataProvider, "IE01414: Data provider argument can not be null");
    m_start = Preconditions.checkNotNull(start, "IE01415: Start argument can not be null");

    m_size = size;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CMemoryFunctions.dumpMemoryRange(m_parent, m_debugger, m_dataProvider, m_start, m_size);
  }
}
