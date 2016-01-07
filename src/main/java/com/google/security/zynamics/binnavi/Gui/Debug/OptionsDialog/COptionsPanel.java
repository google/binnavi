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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;


/**
 * Options panel for the DebugOptions dialog
 */
public class COptionsPanel extends JPanel {
  private static final long serialVersionUID = -7922435685965952732L;

  public COptionsPanel(final DebuggerOptions options) {
    super(new BorderLayout());

    add(new JLabel("Debugger options overview"), BorderLayout.NORTH);

    final JTable table = new JTable(new CMissingOptionsTableModel(options));

    table.setDefaultRenderer(Object.class, new COptionRenderer());

    add(new JScrollPane(table), BorderLayout.CENTER);
  }
}
