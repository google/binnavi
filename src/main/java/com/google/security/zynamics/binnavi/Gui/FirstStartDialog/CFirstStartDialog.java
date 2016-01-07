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
package com.google.security.zynamics.binnavi.Gui.FirstStartDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.panels.ImagePanel;



/**
 * Dialog that shows information about BinNavi when BinNavi is started for the first time.
 */
public final class CFirstStartDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7963704880348566862L;

  /**
   * Creates a new dialog object.
   * 
   * @param parent Parent window of the dialog.
   */
  private CFirstStartDialog(final Window parent) {
    super(parent, "Welcome to BinNavi", ModalityType.APPLICATION_MODAL);

    setLayout(new BorderLayout());

    setSize(450, 417);

    setResizable(false);

    GuiHelper.centerChildToParent(parent, this, true);

    final JPanel centerPanel = new JPanel(new BorderLayout());

    final ImagePanel panel =
        new ImagePanel(new ImageIcon(CMain.class.getResource("data/startup_logo.png")).getImage());

    centerPanel.add(panel, BorderLayout.WEST);

    final JTextArea area =
        new JTextArea("Welcome to BinNavi" + "\n\n"
            + "This is the first time you are using BinNavi on this computer. "
            + "If you have never used BinNavi before, you should familiarize "
            + "yourself with the basic concepts of BinNavi." + "\n\n"
            + "To learn about these concepts you can either read the manual or use "
            + "one of the interactive tutorials you can find in the Help menu of the "
            + "BinNavi main window." + "\n\n"
            + "If you just want to get started, you should configure the database "
            + "you can find on the left side of the main window. Once you have "
            + "successfully established a connection to the database you want to use, "
            + "you can start to import disassembly data from an external data source like IDA Pro.");

    area.setBorder(new EmptyBorder(5, 5, 5, 5));
    area.setWrapStyleWord(true);
    area.setLineWrap(true);

    area.setEditable(false);

    final JPanel innerPanel = new JPanel(new BorderLayout());

    innerPanel.setBorder(new LineBorder(Color.BLACK, 1));
    innerPanel.add(area);

    centerPanel.add(innerPanel);

    add(centerPanel);

    final JPanel bottomPanel = new JPanel(new BorderLayout());

    bottomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JButton closeButton = new JButton(new CloseAction());

    bottomPanel.add(closeButton, BorderLayout.EAST);

    add(bottomPanel, BorderLayout.SOUTH);

    getRootPane().setDefaultButton(closeButton);

    new CDialogEscaper(this);
  }

  /**
   * Shows the dialog.
   * 
   * @param parent Parent window of the dialog.
   */
  public static void show(final Window parent) {
    final CFirstStartDialog dialog = new CFirstStartDialog(parent);

    dialog.setVisible(true);
  }

  /**
   * Action used for closing the dialog.
   */
  private class CloseAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 6161754174528511682L;

    /**
     * Creates a new action object.
     */
    public CloseAction() {
      super("Start");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      dispose();
    }
  }
}
