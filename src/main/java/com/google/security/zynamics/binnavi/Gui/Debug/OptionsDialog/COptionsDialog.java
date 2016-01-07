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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * This dialog can be used to display the available debugger options of a debugger.
 */
public final class COptionsDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8410779795049480922L;

  private final CExceptionSettingsPanel m_exceptionSettingsPanel;

  private final CDebuggerEventSettingsPanel m_debuggerEventSettingsPanel;

  private final DebuggerOptions m_options;

  /**
   * Creates a new debugger options dialog.
   *
   * @param parent Parent window of the dialog.
   * @param options Debugger options to display in the dialog.
   */
  public COptionsDialog(final JFrame parent, final DebuggerOptions options,
      final DebuggerEventSettings eventSettings) {
    super(parent, "Available Debugger Options", true);

    Preconditions.checkNotNull(parent, "IE01466: Parent argument can not be null");
    Preconditions.checkNotNull(options, "IE01467: Options argument can not be null");

    m_options = DebuggerOptions.newInstance(options);

    setLayout(new BorderLayout());

    m_exceptionSettingsPanel = new CExceptionSettingsPanel(m_options);

    m_debuggerEventSettingsPanel = new CDebuggerEventSettingsPanel(eventSettings);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(new COptionsPanel(m_options), "Debug Options");
    tabbedPane.add(m_exceptionSettingsPanel, "Exception Options");
    tabbedPane.add(m_debuggerEventSettingsPanel, "Debugger Event Settings");

    add(tabbedPane);

    final JPanel buttonPanel = new JPanel(new BorderLayout());

    final JButton okButton = new JButton(new AbstractAction("OK") {
      private static final long serialVersionUID = -2448050114927136382L;

      @Override
      public void actionPerformed(final ActionEvent arg0) {
        setVisible(false);
      }
    });

    okButton.setPreferredSize(new Dimension(100, 25));
    buttonPanel.add(okButton, BorderLayout.EAST);
    add(buttonPanel, BorderLayout.SOUTH);

    new CDialogEscaper(this);

    setSize(600, 400);

    setLocationRelativeTo(null);
  }

  /**
   * Get the debugger event settings.
   *
   * @return An instance of the CDebuggerEventSettings class.
   */
  public DebuggerEventSettings getDebuggerEventSettings() {
    return m_debuggerEventSettingsPanel.getSettings();
  }

  /**
   * Get the changed debugger options.
   *
   * @return An instance of the CDebugger class.
   */
  public DebuggerOptions getDebuggerOptions() {
    m_options.setExceptions(m_exceptionSettingsPanel.getExceptionSettings());

    return m_options;
  }
}
