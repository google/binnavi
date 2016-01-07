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
package com.google.security.zynamics.binnavi.Gui.Debug.History;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CAbstractResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Panel where a history of debug events are shown.
 */
public class CDebuggerHistoryPanel extends CAbstractResultsPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2289940032474667139L;

  /**
   * Text area where the history messages are shown.
   */
  private final JTextPane m_textArea = new JTextPane();

  /**
   * Synchronizes the GUI with the debugger events.
   */
  private final CDebuggerHistorySynchronizer m_synchronizer;

  /**
   * Creates a new panel object.
   *
   * @param model Provides the active debugger.
   */
  public CDebuggerHistoryPanel(final CDebugPerspectiveModel model) {
    super(new BorderLayout());

    m_textArea.setFont(GuiHelper.MONOSPACED_FONT);
    m_textArea.setEditable(false);

    final JPanel optionsPanel = new JPanel(new BorderLayout());

    final JPanel innerOptionsPanel = new JPanel();

    final JCheckBox enabledBox = new JCheckBox("Enabled");
    enabledBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent event) {
        m_synchronizer.setEnabled(enabledBox.isSelected());
      }
    });

    innerOptionsPanel.add(enabledBox);
    optionsPanel.add(innerOptionsPanel, BorderLayout.WEST);

    add(optionsPanel, BorderLayout.NORTH);
    add(new JScrollPane(m_textArea));

    m_synchronizer = new CDebuggerHistorySynchronizer(model, m_textArea);
  }

  @Override
  public void dispose() {
    m_synchronizer.dispose();
  }

  @Override
  public String getTitle() {
    return "History";
  }
}
