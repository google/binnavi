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
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Panel where the results of a search operation are shown.
 */
class CSearchOutputPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5751745214399060832L;

  /**
   * Hex view where the module data is shown.
   */
  private final JHexView m_hexView;

  /**
   * Table where the search results are shown.
   */
  private final JTable m_table;

  /**
   * Creates a new panel object.
   * 
   * @param hexView Hex view where the module data is shown.
   */
  public CSearchOutputPanel(final JHexView hexView) {
    super(new BorderLayout());

    m_hexView = hexView;

    m_table = new JTable(new CSearchResultModel());
    m_table.getSelectionModel().addListSelectionListener(new InternalSelectionListener());

    add(new JScrollPane(m_table));
  }

  /**
   * Returns the table model that displays the search results.
   * 
   * @return The table model of the search results table.
   */
  public CSearchResultModel getTableModel() {
    return (CSearchResultModel) m_table.getModel();
  }

  /**
   * Listener that highlights the search results in the hex view when a user selects a search
   * results in the results table.
   */
  private class InternalSelectionListener implements ListSelectionListener {
    @Override
    public void valueChanged(final ListSelectionEvent event) {
      if (event.getValueIsAdjusting()) {
        return;
      }

      final int selectedIndex = m_table.getSelectedRow();

      if (selectedIndex == -1) {
        return;
      }

      final CSearchResult result = getTableModel().getResult(selectedIndex);

      m_hexView.uncolorizeAll();

      m_hexView.colorize(5, result.m_offset, result.m_length, Color.BLACK, Color.YELLOW);

      m_hexView.gotoOffset(result.m_offset);
    }
  }
}
