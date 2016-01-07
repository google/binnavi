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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;
import com.google.security.zynamics.zylib.gui.JHexPanel.SimpleDataProvider;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;

import java.awt.BorderLayout;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JScrollPane;



/**
 * Displays recorded memory data from trace events.
 */
public final class CTraceMemoryDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -400766004659293742L;

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param data The hex data to display.
   */
  private CTraceMemoryDialog(final Window parent, final byte[] data) {
    super(parent, "Trace Memory Data");

    setSize(500, 300);
    setResizable(false);

    setLayout(new BorderLayout());

    final JHexView hexView = new JHexView();

    hexView.setData(new SimpleDataProvider(data) {
      @Override
      public boolean isEditable() {
        return false;
      }
    });

    hexView.setEnabled(true);
    hexView.setDefinitionStatus(DefinitionStatus.DEFINED);

    add(new JScrollPane(hexView));

    new CDialogEscaper(this);

    CIconInitializer.initializeWindowIcons(this);
  }

  /**
   * Shows a dialog that displays recorded memory data from a trace event.
   *
   * @param parent Parent window of the dialog.
   * @param data The hex data to display.
   */
  public static void show(final Window parent, final byte[] data) {
    final CTraceMemoryDialog dialog = new CTraceMemoryDialog(parent, data);

    GuiHelper.centerChildToParent(parent, dialog, true);

    dialog.setVisible(true);
  }
}
