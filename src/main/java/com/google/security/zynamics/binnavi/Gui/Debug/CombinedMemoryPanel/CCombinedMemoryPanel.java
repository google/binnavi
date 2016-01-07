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
package com.google.security.zynamics.binnavi.Gui.Debug.CombinedMemoryPanel;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.CMemoryRefreshButtonPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryRefreshButton.IRefreshRangeProvider;
import com.google.security.zynamics.binnavi.Gui.Debug.MemorySelectionPanel.CMemorySelectionPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.CStackView;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelListenerAdapter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IDebugPerspectiveModelListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessHelpers;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import com.jidesoft.swing.JideSplitPane;

/**
 * This panel contains a memory selection panel and a stack panel. This is the default panel shown
 * when the Debug GUI perspective is activated in a graph window.
 *
 *  In this panel the user can view the memory of the current debug target and the stack of the
 * currently selected thread.
 */
public final class CCombinedMemoryPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 668134668181649929L;

  /**
   * Panel where the memory of the current debug target is shown.
   */
  private final CMemorySelectionPanel m_memorySelectionPanel;

  /**
   * Panel where the stack of the currently selected thread is shown.
   */
  private final CStackView m_stackPanel;

  /**
   * Describes the active debugger GUI options.
   */
  private final CDebugPerspectiveModel m_debugPerspectiveModel;

  /**
   * Makes sure that the memory panel comes visible on changing memory addresses.
   */
  private final IDebugPerspectiveModelListener m_internalListener =
      new InternalDebugPerspectiveModelListener();

  /**
   * Creates a new combined memory panel object.
   *
   * @param parent Parent window of the panel.
   * @param debugPerspectiveModel Describes the active debugger GUI options.
   */
  public CCombinedMemoryPanel(
      final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01361: Parent argument can not be null");
    m_debugPerspectiveModel = Preconditions.checkNotNull(
        debugPerspectiveModel, "IE01362: Debug perspective model argument can not be null");

    m_debugPerspectiveModel.addListener(m_internalListener);

    final JideSplitPane pane = new JideSplitPane(JideSplitPane.HORIZONTAL_SPLIT) {
      private static final long serialVersionUID = -1326165812499630343L;

      // ESCA-JAVA0025: Workaround for Case 1168
      @Override
      public void updateUI() {
        // Workaround for Case 1168: The mere presence of a JIDE component
        // screws up the look and feel.
      }
    };

    pane.setDividerSize(3); // ATTENTION: Part of the Case 1168 workaround
    pane.setProportionalLayout(true);

    final CMemoryRefreshButtonPanel refreshPanel = new CMemoryRefreshButtonPanel(
        parent, debugPerspectiveModel, new InternalRangeProvider(),
        new InternalStackRangeProvider());

    m_memorySelectionPanel = new CMemorySelectionPanel(parent, debugPerspectiveModel, refreshPanel);

    // Create the GUI
    pane.addPane(m_memorySelectionPanel);

    m_stackPanel = new CStackView(debugPerspectiveModel);
    pane.addPane(m_stackPanel);

    add(pane);
  }

  @Override
  public void dispose() {
    m_memorySelectionPanel.dispose();
    m_stackPanel.dispose();
  }

  @Override
  public String getTitle() {
    return "Memory";
  }

  /**
   * Makes sure that the memory panel comes visible on changing memory addresses.
   */
  private class InternalDebugPerspectiveModelListener
      extends CDebugPerspectiveModelListenerAdapter {
    @Override
    public void changedActiveAddress(final IAddress address, final boolean focusMemoryWindow) {
      if (address != null) {
        notifyShow(getComponent());
      }
    }
  }

  /**
   * Provides the memory range to refresh to the refresh button.
   */
  private class InternalRangeProvider implements IRefreshRangeProvider {
    @Override
    public IAddress getAddress() {
      return new CAddress(m_memorySelectionPanel.getMemoryPanel()
          .getHexView().getHexView().getFirstVisibleOffset());
    }

    @Override
    public int getSize() {
      return m_memorySelectionPanel.getMemoryPanel().getHexView().getHexView().getVisibleBytes();
    }
  }

  /**
   * Provides the stack memory range to refresh to the refresh button.
   */
  private class InternalStackRangeProvider implements IRefreshRangeProvider {
    @Override
    public IAddress getAddress() {
      final long startAddress = m_stackPanel.getStackProvider().getStartAddress();

      return startAddress == -1 ? null : new CAddress(startAddress);
    }

    @Override
    public int getSize() {
      final IDebugger activeDebugger = m_debugPerspectiveModel.getCurrentSelectedDebugger();

      if (activeDebugger == null) {
        return 0;
      }

      final MemoryMap memoryMap = activeDebugger.getProcessManager().getMemoryMap();

      if (memoryMap == null) {
        return 0;
      }

      final MemorySection section = ProcessHelpers.getSectionWith(memoryMap, getAddress());

      return section == null ? 0 : section.getSize();
    }
  }
}
