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
package com.google.security.zynamics.binnavi.Gui.HotkeyDialog;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.OKButtonPanel;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * The dialog that contains all available hotkeys.
 */
public final class CGraphWindowHotkeysDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3986160386779881484L;

  /**
   * Creates a new hotkeys dialog.
   *
   * @param parent The parent window of the hotkeys dialog.
   */
  public CGraphWindowHotkeysDialog(final JFrame parent) {
    super(parent, "Available Hotkeys", ModalityType.MODELESS);
    setLayout(new BorderLayout());
    add(new JScrollPane(new CGraphWindowHotkeyTable()));
    final OKButtonPanel okButtonPanel = new OKButtonPanel(this);
    add(okButtonPanel, BorderLayout.SOUTH);
    new CDialogEscaper(this);
    setSize(700, 600);
  }
}
