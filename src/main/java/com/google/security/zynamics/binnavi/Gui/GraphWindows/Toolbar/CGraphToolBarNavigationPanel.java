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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Toolbar;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto.CGotoAddressField;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto.CGotoAddressHelp;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CSearchFieldHelp;
import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Encapsulates the Goto and Search panel shown in graph window toolbars.
 */
public final class CGraphToolBarNavigationPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8530561676529180206L;

  /**
   * Goto field that can be used to jump to addresses.
   */
  private final CGotoAddressField m_gotoField;

  /**
   * Search panel of the graph window.
   */
  private final CToolbarSearchPanel m_searchPanel;

  /**
   * Creates a new panel object.
   *
   * @param graph The graph shown in the graph window.
   * @param modules The list of modules present in the current graph.
   * @param parent The parent JFrame.
   */
  public CGraphToolBarNavigationPanel(
      final ZyGraph graph, final List<INaviModule> modules, final JFrame parent) {
    super(new BorderLayout());

    m_gotoField = new CGotoAddressField(graph, modules, parent);
    m_searchPanel = new CToolbarSearchPanel(graph);

    final JPanel pBorderPanel = new JPanel(new BorderLayout());
    pBorderPanel.setBorder(new EmptyBorder(new Insets(1, 1, 1, 1)));

    final JPanel pFields = new JPanel(new BorderLayout());
    pFields.setBorder(new TitledBorder(""));

    final JPanel pSearchField = new JPanel(new BorderLayout());

    pSearchField.add(
        new CHelpLabel("   " + "Search" + " ", new CSearchFieldHelp()), BorderLayout.WEST);

    pSearchField.add(m_searchPanel, BorderLayout.CENTER);

    pSearchField.setPreferredSize(new Dimension(200, 20));
    pSearchField.setMinimumSize(new Dimension(200, 20));

    pFields.add(pSearchField, BorderLayout.CENTER);

    final JPanel pGotoField = new JPanel(new BorderLayout());
    pGotoField.add(
        new CHelpLabel("  " + "Address" + " ", new CGotoAddressHelp()), BorderLayout.WEST);

    pGotoField.add(m_gotoField, BorderLayout.CENTER);

    pGotoField.setPreferredSize(new Dimension(200, 20));
    pGotoField.setMinimumSize(new Dimension(200, 20));

    pFields.add(pGotoField, BorderLayout.WEST);

    pBorderPanel.add(pFields, BorderLayout.CENTER);

    pBorderPanel.setPreferredSize(new Dimension(400, 20));
    pBorderPanel.setMinimumSize(new Dimension(400, 20));

    add(pBorderPanel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_searchPanel.dispose();
  }

  /**
   * Returns the address field that can be used to jump to addresses in graphs.
   *
   * @return The address field.
   */
  public CGotoAddressField getGotoAddressField() {
    return m_gotoField;
  }

  /**
   * Returns the search panel that can be used to search for texts in graph.
   *
   * @return The search panel.
   */
  public CGraphSearchPanel getSearchPanel() {
    return m_searchPanel.getSearchPanel();
  }
}
