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
package com.google.security.zynamics.binnavi.Gui.Debug.TraceOptionsDialog;

import com.google.security.zynamics.zylib.gui.CDecFormatter;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


/**
 * Dialog where the user can configure debug trace options.
 */
public class CTraceOptionsDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4756716971206634975L;

  /**
   * The user enters the maximum number of hits an echo breakpoint can take here.
   */
  private final JFormattedTextField m_maximumHitsField =
      new JFormattedTextField(new CDecFormatter(4));

  /**
   * True, if the dialog was cancelled.
   */
  private boolean m_wasCancelled = false;

  /**
   * User-selected maximum hit counter value.
   */
  private int m_maximumHits;

  /**
   * Creates a new options dialog.
   *
   * @param parent Parent window of the dialog.
   */
  private CTraceOptionsDialog(final Window parent) {
    super(parent, "Trace Options", ModalityType.DOCUMENT_MODAL);

    setLayout(new BorderLayout());

    m_maximumHitsField.setText("1");

    final JPanel upperPanel = new JPanel(new BorderLayout());

    final JPanel innerPanel = new JPanel(new GridLayout(1, 1));

    innerPanel.add(createPanel("Maximum Hits", m_maximumHitsField));

    upperPanel.add(innerPanel, BorderLayout.NORTH);

    upperPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder("")));
    add(upperPanel);

    final CPanelTwoButtons panel = new CPanelTwoButtons(new InternalListener(), "OK", "Cancel");

    getRootPane().setDefaultButton(panel.getFirstButton());

    add(panel, BorderLayout.SOUTH);

    setSize(400, 200);
  }

  /**
   * Shows a trace options dialog.
   *
   * @param parent Parent window of the dialog.
   *
   * @return The dialog object that was shown.
   */
  public static CTraceOptionsDialog show(final JFrame parent) {
    final CTraceOptionsDialog dlg = new CTraceOptionsDialog(parent);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);

    return dlg;
  }

  /**
   * Creates a standard edit panel of the dialog.
   *
   * @param description Description of the option.
   * @param component The component used to edit the option.
   *
   * @return The created edit panel.
   */
  private Component createPanel(final String description, final JComponent component) {
    final JPanel panel = new JPanel(new GridLayout(1, 2));

    panel.add(new JLabel(description));
    panel.add(component);

    return panel;
  }

  /**
   * Returns the maximum number of echo breakpoint hits option.
   *
   * @return Maximum number of echo breakpoint hits.
   */
  public int getMaximumHits() {
    return m_maximumHits;
  }

  /**
   * Returns whether the dialog was cancelled.
   *
   * @return True, if the dialog was cancelled. False, if it was closed regularly.
   */
  public boolean wasCancelled() {
    return m_wasCancelled;
  }

  /**
   * Keeps track of clicked buttons.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        try {
          m_maximumHits = Integer.valueOf(m_maximumHitsField.getText());

          if (m_maximumHits > 1000) {
            CMessageBox.showInformation(
                CTraceOptionsDialog.this, "Maximum hit count can not be higher than 1000.");
            return;
          }
        } catch (final NumberFormatException exception) {
          CMessageBox.showInformation(CTraceOptionsDialog.this,
              String.format("Invalid maximum hit count string '%s'", m_maximumHitsField.getText()));

          return;
        }
      } else {
        m_wasCancelled = true;
      }

      dispose();
    }
  }
}
