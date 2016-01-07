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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CGotoAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CSearchAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.gui.JHexPanel.IHexPanelListener;

/**
 * This panel class contains the scrollable memory panel.
 */
public final class CMemoryPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1010425056754096954L;

  /**
   * Component where the memory is shown.
   */
  private final CMemoryViewer m_hexView;

  /**
   * Label that shows the current offset.
   */
  private final JLabel m_offsetLabel = new JLabel();

  /**
   * Action that is used to go to an address in memory.
   */
  private final Action m_gotoAction;

  /**
   * Action that is used to search for some value in memory.
   */
  private final Action m_searchAction;

  /**
   * Updates the current offset label.
   */
  private final IHexPanelListener m_hexPanelListener = new IHexPanelListener() {
    @Override
    public void selectionChanged(final long start, final long length) {
      updateOffsetLabel();
    }
  };

  /**
   * Creates a new memory panel object.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that provides the displayed memory data.
   */
  public CMemoryPanel(final JFrame parent, final CDebugPerspectiveModel debugger) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01399: Parent argument can not be null");
    Preconditions.checkNotNull(debugger, "IE01400: Debugger argument can not be null");

    m_hexView = new CMemoryViewer(parent, debugger);

    m_searchAction = CActionProxy.proxy(new CSearchAction(parent, debugger, m_hexView));
    m_gotoAction = CActionProxy.proxy(new CGotoAction(parent, m_hexView, debugger));

    add(m_hexView);
    add(m_offsetLabel, BorderLayout.SOUTH);

    updateOffsetLabel();

    m_hexView.getHexView().addHexListener(m_hexPanelListener);

    initHotkeys();
  }

  /**
   * Initialize some hotkeys for easy access to functions of the debug panel.
   */
  private void initHotkeys() {
    final InputMap imap = m_hexView.getInputMap();
    final InputMap windowImap = m_hexView.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

    windowImap.put(HotKeys.GOTO_HK.getKeyStroke(), "GOTO");
    m_hexView.getActionMap().put("GOTO", m_gotoAction);

    imap.put(HotKeys.SEARCH_HK.getKeyStroke(), "SEARCH");
    m_hexView.getActionMap().put("SEARCH", m_searchAction);
  }

  /**
   * Updates the current offset label.
   */
  private void updateOffsetLabel() {
    m_offsetLabel.setText(String.format("%s: %08X", "Current Offset", m_hexView.getHexView()
        .getCurrentOffset()));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_hexView.getHexView().removeHexListener(m_hexPanelListener);

    m_hexView.dispose();
  }

  /**
   * Returns the hex view component where the memory is shown.
   * 
   * @return The hex view component.
   */
  public CMemoryViewer getHexView() {
    return m_hexView;
  }

  /**
   * Updates the GUI depending on the state of the debugger.
   * 
   * @param connected Flag that indicates whether the debugger is connected.
   * @param suspended Flag that indicates whether the debugger is suspended.
   */
  public void updateGui(final boolean connected, final boolean suspended) {
    m_gotoAction.setEnabled(connected && suspended);
  }
}
