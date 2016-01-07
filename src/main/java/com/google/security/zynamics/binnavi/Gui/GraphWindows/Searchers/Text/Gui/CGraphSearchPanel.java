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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CSearchResultsDialog;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;



/**
 * Panel that shows the graph search field.
 */
public final class CGraphSearchPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2588322530971947772L;

  /**
   * Dialog where the search results are shown.
   */
  private CSearchResultsDialog m_dialog;

  /**
   * Search field that can be used to search for texts in the graph.
   */
  private final CGraphSearchField m_searchField;

  /**
   * Creates a new search panel.
   *
   * @param graph Graph searched by this panel.
   */
  public CGraphSearchPanel(final ZyGraph graph) {
    super(new BorderLayout());

    m_searchField = new CGraphSearchField(graph);

    add(m_searchField);

    final JButton button = new JButton(CActionProxy.proxy(new CShowResultsAction()));

    add(button, BorderLayout.EAST);
  }

  public void dispose() {
    m_searchField.dispose();
  }

  /**
   * Returns the search field shown in this panel.
   *
   * @return The search field shown in this panel.
   */
  public CGraphSearchField getSearchField() {
    return m_searchField;
  }

  /**
   * Shows or hides the results dialog.
   *
   * @param visible True, to show the dialog. False, to hide it.
   */
  public void showResultsDialog(final boolean visible) {
    if (m_dialog != null) {
      m_dialog.setVisible(visible);
    }
  }

  /**
   * Action for showing the search results dialog.
   */
  private class CShowResultsAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5761584425849212530L;

    /**
     * Creates a new action object.
     */
    public CShowResultsAction() {
      super("Results");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (m_dialog == null) {
        final Window parent = SwingUtilities.getWindowAncestor(CGraphSearchPanel.this);

        m_dialog = new CSearchResultsDialog(parent, m_searchField);

        GuiHelper.centerChildToParent(parent, m_dialog, true);
      }

      m_dialog.setVisible(true);
    }
  }
}
