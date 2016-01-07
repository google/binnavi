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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;



import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

/**
 * This dialog allows the user to configure the global settings like language settings, colors, IDA
 * Pro location, and so on.
 */
public final class CSettingsDialog extends JDialog {

  /**
   * The tabbed pane of the dialog that contains all the specific controls.
   */
  private final JTabbedPane m_pane = new JTabbedPane();

  /**
   * Creates a new settings dialog.
   * 
   * @param parent The parent frame of the dialog.
   */
  public CSettingsDialog(final Window parent) {
    super(parent, "Settings", ModalityType.APPLICATION_MODAL);

    setLayout(new BorderLayout(5, 5));

    new CDialogEscaper(this);

    // Make sure that all components are derived from CAbstractSettingsPanel
    m_pane.add("General", new CGeneralSettingsPanel());
    m_pane.add("Colors", new CColorSettingsPanel());

    add(m_pane);

    add(new CPanelTwoButtons(new InternalListener(), "OK", "Cancel"), BorderLayout.SOUTH);

    setSize(600, 400);
  }

  /**
   * Action listener for the buttons of the dialog.
   */
  private class InternalListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      // When the OK was pressed, the individual controls
      // are told to save the data entered by the user.

      if (event.getActionCommand().equals("OK")) {
        boolean requireRestart = false;

        for (final Component component : m_pane.getComponents()) {
          final CAbstractSettingsPanel panel = (CAbstractSettingsPanel) component;

          requireRestart |= panel.save();
        }

        if (requireRestart) {
          CMessageBox.showInformation(CSettingsDialog.this,
              "Please note that the selected language only becomes active after "
                  + "BinNavi is started the next time.");
        }
      }

      dispose();
    }
  }
}
