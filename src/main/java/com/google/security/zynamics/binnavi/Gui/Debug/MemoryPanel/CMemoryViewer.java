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

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

// ESCA-JAVA0136:
/**
 * The main purpose of this class is to encapsulate the hex component with its data provider.
 */
public final class CMemoryViewer extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1768975106918698292L;

  /**
   * The memory viewer component is used to display the memory of the target process
   */
  private final JHexView m_hexView = new JHexView();

  /**
   * Provides the data to be displayed.
   */
  private final CMemoryProvider m_dataProvider = new CMemoryProvider();

  /**
   * Keeps the shown memory synchronized with the requested memory sections.
   */
  private final CMemoryViewerSynchronizer m_synchronizer;

  /**
   * Creates a new memory viewer object.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugPerspectiveModel Debugger that provides the displayed memory data.
   */
  public CMemoryViewer(final JFrame parent, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(new BorderLayout());

    Preconditions.checkNotNull(parent, "IE01404: Parent argument can not be null");
    Preconditions.checkNotNull(debugPerspectiveModel, "IE01405: Debugger argument can not be null");

    // Set up the hex view component that is used to display the memory
    // of the debugged process.
    m_hexView.setData(m_dataProvider);
    m_hexView.setMenuCreator(new CMemoryMenu(parent, debugPerspectiveModel, this));

    m_hexView.setVisible(true);

    add(m_hexView);

    m_synchronizer =
        new CMemoryViewerSynchronizer(m_hexView, m_dataProvider, debugPerspectiveModel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
    m_hexView.dispose();
  }

  /**
   * Returns the hex view object.
   * 
   * @return The hex view object.
   */
  public JHexView getHexView() {
    return m_hexView;
  }

  @Override
  public boolean isEnabled() {
    return m_hexView.isEnabled();
  }

  @Override
  public boolean requestFocusInWindow() {
    return m_hexView.requestFocusInWindow();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    m_hexView.setEnabled(enabled);
  }
}
