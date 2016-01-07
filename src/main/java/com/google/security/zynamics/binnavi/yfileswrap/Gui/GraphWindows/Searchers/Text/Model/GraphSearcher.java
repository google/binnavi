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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CGraphSearchResultsCursor;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CGraphSearchSettings;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CNodeSearcher;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CResultFilter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CSearchResultComparator;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.IGraphSearchSettingsListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



/**
 * Searcher class that can be used to search through graphs.
 *
 * TODO: Separate the management of the search results from the iteration over the search results.
 */
public final class GraphSearcher {
  /**
   * Graph searcher settings.
   */
  private final CGraphSearchSettings m_settings = new CGraphSearchSettings();

  /**
   * Result cursor for iterating over the search results.
   */
  private final CGraphSearchResultsCursor m_cursor = new CGraphSearchResultsCursor();

  /**
   * List of current search results.
   */
  private final List<SearchResult> m_results = new ArrayList<SearchResult>();

  /**
   * Flag that indicates whether search options changed in a way that makes a new search necessary.
   */
  private boolean m_changed = true;

  /**
   * Stores the last search string.
   */
  private String m_lastSearchString;

  /**
   * Updates the graph searcher on changes to graph settings.
   */
  private final IGraphSearchSettingsListener m_internalSettingsListener =
      new InternalSettingsListener();

  /**
   * Creates a new graph searcher object.
   */
  public GraphSearcher() {
    m_settings.addListener(m_internalSettingsListener);
  }

  /**
   * Searches through the given nodes and edges.
   *
   * @param nodes The nodes to search through.
   * @param edges The edges to search through.
   * @param searchString The string to search for.
   */
  private void searchAll(
      final List<NaviNode> nodes, final List<NaviEdge> edges, final String searchString) {
    for (final NaviNode node : nodes) {
      searchNode(node, searchString);
    }

    for (final NaviEdge edge : edges) {
      searchEdge(edge, searchString);
    }
  }

  /**
   * Searches through an edge.
   *
   * @param edge The edge to search through.
   * @param searchString The string to search for.
   */
  private void searchEdge(final NaviEdge edge, final String searchString) {
    m_results.addAll(CEdgeSearcher.search(
        edge, searchString, m_settings.isRegEx(), m_settings.isCaseSensitive()));

    if (!m_results.isEmpty()) {
      m_cursor.reset();
    }
  }

  /**
   * Searches through a node.
   *
   * @param node The node to search through.
   * @param searchString The string to search for.
   */
  private void searchNode(final NaviNode node, final String searchString) {
    m_results.addAll(CNodeSearcher.search(
        node, searchString, m_settings.isRegEx(), m_settings.isCaseSensitive()));

    if (!m_results.isEmpty()) {
      m_cursor.reset();
    }
  }

  /**
   * Clears the results list.
   */
  public void clearResults() {
    for (final SearchResult result : m_results) {
      if (result.getObject() instanceof NaviNode) {
        ((NaviNode) result.getObject()).setBackgroundColor(
            result.getLine(), result.getPosition(), result.getLength(), null);
      } else if (result.getObject() instanceof NaviEdge) {
        final NaviEdge edge = (NaviEdge) result.getObject();

        final ZyLabelContent content = edge.getLabelContent();
        content.getLineContent(result.getLine()).setBackgroundColor(
            result.getPosition(), result.getLength(), null);
      }
    }

    m_results.clear();
    m_cursor.clear();

    m_changed = false;
  }

  public void dispose() {
    m_settings.removeListener(m_internalSettingsListener);
  }

  /**
   * Returns the cursor to iterate over the search results.
   *
   * @return The cursor to iterate over the search results.
   */
  public CGraphSearchResultsCursor getCursor() {
    return m_cursor;
  }

  /**
   * Returns the last used search string.
   *
   * @return The last used search string.
   */
  public String getLastSearchString() {
    return m_lastSearchString;
  }

  /**
   * Returns a list of all search results discovered in the last search.
   *
   * @return A list of search results.
   */
  public List<SearchResult> getResults() {
    return new ArrayList<SearchResult>(m_results);
  }

  /**
   * Returns the active graph settings.
   *
   * @return The active graph settings.
   */
  public CGraphSearchSettings getSettings() {
    return m_settings;
  }

  /**
   * Returns a flag that indicates that search options changed since the last search.
   *
   * @return True, if search options changed. False, otherwise.
   */
  public boolean hasChanged() {
    return m_changed;
  }

  /**
   * Searches through a list of nodes and edges while considering the active search settings.
   *
   * @param nodes The nodes to search through.
   * @param edges The edges to search through.
   * @param searchString The string to search for.
   */
  public void search(
      final List<NaviNode> nodes, final List<NaviEdge> edges, final String searchString) {
    m_lastSearchString = searchString;

    clearResults();

    final List<NaviNode> filteredNodes = CResultFilter.filteredNodes(
        nodes, m_settings.isOnlySelected(), m_settings.isOnlyVisible());
    final List<NaviEdge> filteredEdges = CResultFilter.filteredEdges(
        edges, m_settings.isOnlySelected(), m_settings.isOnlyVisible());

    searchAll(filteredNodes, filteredEdges, searchString);

    Collections.sort(m_results, new CSearchResultComparator());

    m_cursor.setResults(m_results);
  }

  /**
   * Updates the graph searcher on changes to graph settings.
   */
  private class InternalSettingsListener implements IGraphSearchSettingsListener {
    @Override
    public void changedCaseSensitive() {
      m_changed = true;
    }

    @Override
    public void changedOnlySelected() {
      m_changed = true;
    }

    @Override
    public void changedOnlyVisible() {
      m_changed = true;
    }

    @Override
    public void changedRegEx() {
      m_changed = true;
    }
  }
}
