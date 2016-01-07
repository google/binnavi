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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.IGraphSearchFieldListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.tables.CMonospaceRenderer;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Dialog where the search results are shown.
 */
public final class CSearchResultsDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1650099126739368424L;

  /**
   * Search field that backs the search results.
   */
  private final CGraphSearchField m_searchField;

  /**
   * Shows the number of current search results.
   */
  private static JLabel m_resultsLabel = new JLabel("");

  /**
   * Displays the search results.
   */
  private final JTable m_table = new JTable();

  /**
   * Handles changes in the search results.
   */
  private final IGraphSearchFieldListener m_searchListener = new InternalSearchListener();

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param searchField Search field that backs the search results.
   */
  public CSearchResultsDialog(final Window parent, final CGraphSearchField searchField) {
    super(parent, "Search Results");

    m_searchField = searchField;
    setResults(m_searchField.getGraphSearcher().getResults());

    setLayout(new BorderLayout());

    m_searchField.addListener(m_searchListener);

    m_table.setDefaultRenderer(Object.class, new CMonospaceRenderer());
    m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    add(new JScrollPane(m_table));

    final JPanel panel = new JPanel();

    panel.add(m_resultsLabel);

    add(panel, BorderLayout.SOUTH);

    setSize(400, 400);
  }

  /**
   * Fills the search results table model with search results data.
   *
   * @param results The search results data.
   */
  private void setResults(final List<SearchResult> results) {
    final Object[][] data = new Object[results.size()][1];

    int counter = 0;

    for (final SearchResult result : results) {
      if (result.getObject() instanceof NaviNode) {
        final NaviNode node = (NaviNode) result.getObject();
        final ZyLabelContent labelContent = node.getRealizer().getNodeContent();
        final String lineText = labelContent.getLineContent(result.getLine()).getText();
        data[counter++][0] = lineText;
      } else if (result.getObject() instanceof NaviEdge) {
        final NaviEdge edge = (NaviEdge) result.getObject();
        final ZyLabelContent labelContent = edge.getLabelContent();
        final String lineText = labelContent.getLineContent(result.getLine()).getText();
        data[counter++][0] = lineText;
      }
    }

    m_table.setModel(new CResultsTableModel(data, new String[] {"Result"}));
    m_resultsLabel.setText(String.format("%d search results", data.length));
  }

  /**
   * Table model used for the results table.
   */
  private static class CResultsTableModel extends DefaultTableModel {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 7199101380632271213L;

    /**
     * Creates a new table model object.
     *
     * @param data The data of the table.
     * @param columnNames The names of the columns.
     */
    public CResultsTableModel(final Object[][] data, final String[] columnNames) {
      super(data, columnNames);
    }

    @Override
    public boolean isCellEditable(final int row, final int col) {
      return false;
    }
  }

  /**
   * Handles changes in the search results.
   */
  private class InternalSearchListener implements IGraphSearchFieldListener {
    @Override
    public void searched() {
      setResults(m_searchField.getGraphSearcher().getResults());
    }
  }
}
