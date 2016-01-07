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

import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchPanel;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.IZyGraphSearchSettingsListener;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Search panel of the graph window toolbar.
 */
public final class CToolbarSearchPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3951783806021010278L;

  /**
   * Search field used to search through the graph.
   */
  private final CGraphSearchPanel m_searchPanel;

  /**
   * Listener that updates the search panel on changes to the search settings.
   */
  private final InternalSearchListener m_internalSearchListener = new InternalSearchListener();

  /**
   * Settings object synchronized with the tool bar elements.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Creates a new search panel object.
   *
   * @param graph Graph in the graph panel.
   */
  public CToolbarSearchPanel(final ZyGraph graph) {
    super(new BorderLayout());

    m_searchPanel = new CGraphSearchPanel(graph);
    m_settings = graph.getSettings();

    m_settings.getSearchSettings().addListener(m_internalSearchListener);

    add(m_searchPanel);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_searchPanel.dispose();
    m_settings.getSearchSettings().removeListener(m_internalSearchListener);
  }

  /**
   * Returns the search panel that can be used to search for texts in graph.
   *
   * @return The search panel.
   */
  public CGraphSearchPanel getSearchPanel() {
    return m_searchPanel;
  }

  /**
   * Listener that updates the search panel on changes to the search settings.
   */
  private class InternalSearchListener implements IZyGraphSearchSettingsListener {
    @Override
    public void changedSearchCaseSensitive(final boolean value) {
      m_searchPanel.getSearchField().getGraphSearcher().getSettings().setCaseSensitive(value);
    }

    @Override
    public void changedSearchRegEx(final boolean value) {
      m_searchPanel.getSearchField().getGraphSearcher().getSettings().setRegEx(value);
    }

    @Override
    public void changedSearchSelectionNodesOnly(final boolean value) {
      m_searchPanel.getSearchField().getGraphSearcher().getSettings().setOnlySelected(value);
    }

    @Override
    public void changedSearchVisibleNodesOnly(final boolean value) {
      m_searchPanel.getSearchField().getGraphSearcher().getSettings().setOnlyVisible(value);
    }
  }
}
