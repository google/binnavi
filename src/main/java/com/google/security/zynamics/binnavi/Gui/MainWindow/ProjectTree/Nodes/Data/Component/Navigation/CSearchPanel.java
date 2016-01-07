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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation;

import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


/**
 * Panel where the user can search through the binary data of a module.
 */
public final class CSearchPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1702552070875854106L;

  /**
   * Creates a new search panel object.
   * 
   * @param hexView Hex view where the binary module data is shown.
   */
  public CSearchPanel(final JHexView hexView) {
    super(new BorderLayout());

    setBorder(new TitledBorder("Search"));

    final CSearchOutputPanel outputPanel = new CSearchOutputPanel(hexView);

    add(new CSearchInputPanel(hexView, outputPanel.getTableModel()), BorderLayout.NORTH);

    add(outputPanel);
  }
}
