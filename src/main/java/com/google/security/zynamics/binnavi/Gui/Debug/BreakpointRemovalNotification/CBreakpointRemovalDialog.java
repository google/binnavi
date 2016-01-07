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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointRemovalNotification;

import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Dialog that shows what breakpoints were not removed successfully.
 */
public final class CBreakpointRemovalDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2869489487984715797L;

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param failedAddresses Addresses of the breakpoints that could not be removed.
   */
  private CBreakpointRemovalDialog(
      final JFrame parent, final Iterable<Pair<RelocatedAddress, Integer>> failedAddresses) {
    super(parent, Constants.DEFAULT_WINDOW_TITLE, true);

    new CDialogEscaper(this);
    CIconInitializer.initializeWindowIcons(this);

    setLayout(new BorderLayout());

    final JPanel upperPanel = new JPanel();

    upperPanel.add(
        new JLabel("The following breakpoints could not be removed from the target process:"));

    add(upperPanel, BorderLayout.NORTH);

    final StringBuilder stringBuilder = new StringBuilder();

    for (final Pair<RelocatedAddress, Integer> pair : failedAddresses) {
      stringBuilder.append(pair.first().getAddress().toHexString());
      stringBuilder.append('\n');
    }

    final JTextArea textArea = new JTextArea(stringBuilder.toString());
    textArea.setEditable(false);
    textArea.setFont(GuiHelper.MONOSPACED_FONT);

    add(new JScrollPane(textArea));

    final JPanel lowerPanel = new JPanel(new BorderLayout());

    final JButton button = new JButton(new InternalButtonAction());

    lowerPanel.add(button, BorderLayout.EAST);

    add(lowerPanel, BorderLayout.SOUTH);

    setSize(500, 300);
  }

  /**
   * Shows a new breakpoint removal dialog.
   *
   * @param parent Parent window of the dialog.
   * @param failedAddresses Addresses of the breakpoints that could not be removed.
   */
  public static void show(
      final JFrame parent, final Iterable<Pair<RelocatedAddress, Integer>> failedAddresses) {
    final CBreakpointRemovalDialog dialog = new CBreakpointRemovalDialog(parent, failedAddresses);

    GuiHelper.centerChildToParent(parent, dialog, true);

    dialog.setVisible(true);
  }

  /**
   * Listens on clicks on the OK button.
   */
  private final class InternalButtonAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 3334899675534000200L;

    /**
     * Creates a new button listener.
     */
    public InternalButtonAction() {
      super("OK");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      dispose();
    }
  }
}
