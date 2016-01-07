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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CSearchExecuter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CSearchInputField;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.IGraphSearchFieldListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model.GraphSearcher;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraphHelpers;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.comboboxes.memorybox.JMemoryBox;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * Search field class that provides a text field that can be used to search through graphs.
 */
public final class CGraphSearchField extends JMemoryBox {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7655847738244119546L;

  /**
   * Graph searched through by this field.
   */
  private final ZyGraph m_graph;

  /**
   * The underlying graph searching model.
   */
  private final GraphSearcher m_searcher;

  /**
   * Input component of the input field.
   */
  private final JTextField m_textField = new CSearchInputField();

  /**
   * Listeners notified about completed searches.
   */
  private final ListenerProvider<IGraphSearchFieldListener> m_listenerProvider =
      new ListenerProvider<IGraphSearchFieldListener>();

  /**
   * Creates a new search field.
   *
   * @param graph Graph searched through by this field.
   */
  public CGraphSearchField(final ZyGraph graph) {
    super(20);

    Preconditions.checkNotNull(graph, "IE01812: Target view can't be null");

    m_graph = graph;

    m_searcher = new GraphSearcher();

    setEditor(new BasicComboBoxEditor() {
      @Override
      protected JTextField createEditorComponent() {
        return m_textField;
      }
    });

    registerHotkeys();
  }

  /**
   * Returns the search string entered by the user.
   *
   * @return The search string entered by the user.
   */
  private String getText() {
    return ((JTextField) getEditor().getEditorComponent()).getText();
  }

  /**
   * Notifies registered listeners about completed searches.
   */
  private void notifyListeners() {
    for (final IGraphSearchFieldListener listener : m_listenerProvider) {
      listener.searched();
    }
  }

  /**
   * Registers all hotkeys processed by the graph search field.
   */
  private void registerHotkeys() {
    final ActionMap actionMap = ((JTextField) getEditor().getEditorComponent()).getActionMap();
    final InputMap imap = ((JTextField) getEditor().getEditorComponent()).getInputMap();
    setActionMap(actionMap);
    setInputMap(JComponent.WHEN_FOCUSED, imap);

    imap.put(HotKeys.GRAPH_SEARCH_NEXT_KEY.getKeyStroke(), "NEXT");
    imap.put(HotKeys.GRAPH_SEARCH_NEXT_ZOOM_KEY.getKeyStroke(), "NEXT_ZOOM");
    imap.put(HotKeys.GRAPH_SEARCH_PREVIOUS_KEY.getKeyStroke(), "PREVIOUS");
    imap.put(HotKeys.GRAPH_SEARCH_PREVIOUS_ZOOM_KEY.getKeyStroke(), "PREVIOUS_ZOOM");

    actionMap.put("NEXT", CActionProxy.proxy(new CActionHotKey("NEXT")));
    actionMap.put("NEXT_ZOOM", CActionProxy.proxy(new CActionHotKey("NEXT_ZOOM")));
    actionMap.put("PREVIOUS", CActionProxy.proxy(new CActionHotKey("PREVIOUS")));
    actionMap.put("PREVIOUS_ZOOM", CActionProxy.proxy(new CActionHotKey("PREVIOUS_ZOOM")));
  }

  /**
   * Searches for a search string.
   *
   * @param searchString The string to search for.
   * @param cycleBackwards True, to cycle backwards through the search results. False, to cycle
   *        forwards.
   * @param zoomToResult True, to zoom to the result. False, to center it.
   */
  private void searchFor(final String searchString, final boolean cycleBackwards,
      final boolean zoomToResult) {
    if (!"".equals(searchString)) {
      // Remove the listener because the add function shuffles the items
      // in the combobox which leads to unwanted events.
      add(searchString);
    }

    CSearchExecuter.search(SwingUtilities.getWindowAncestor(this), getEditor(), m_graph,
        m_searcher, searchString, cycleBackwards, zoomToResult);

    notifyListeners();
  }

  /**
   * Adds a listener that is notified about completed searches.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IGraphSearchFieldListener listener) {
    m_listenerProvider.addListener(listener);
  }

  /**
   * Centers the next search result.
   *
   * @param cycleBackwards True, to cycle backwards through the search results. False, to cycle
   *        forwards.
   * @param zoomToResult True, to zoom to the result. False, to center it.
   */
  public void centerNextSearchHit(final boolean cycleBackwards, final boolean zoomToResult) {
    final String text = getText();

    // No text entered => Clear earlier results
    if ("".equals(text)) {
      m_searcher.clearResults();

      m_graph.updateGraphViews();

      notifyListeners();

      return;
    }

    searchFor(text, cycleBackwards, zoomToResult);
  }

  public void dispose() {
    m_searcher.dispose();
  }

  /**
   * Returns the underlying search model.
   *
   * @return The underlying search model.
   */
  public GraphSearcher getGraphSearcher() {
    return m_searcher;
  }

  /**
   * Jumps to a search result of the given index.
   *
   * @param index The index of the search result to jump to.
   */
  public void jumpTo(final int index) {
    m_searcher.getCursor().jumpTo(index);

    final SearchResult result = m_searcher.getCursor().current();

    if (result == null) {
      return;
    }

    if (result.getObject() instanceof NaviNode) {
      ZyGraphHelpers.centerNode(m_graph, (NaviNode) result.getObject(), false);
    } else if (result.getObject() instanceof NaviEdge) {
      ZyGraphHelpers.centerEdgeLabel(m_graph, (NaviEdge) result.getObject(), false);
    }
  }

  /**
   * Removes a listener object that was previously notified about completed searches.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IGraphSearchFieldListener listener) {
    m_listenerProvider.removeListener(listener);
  }

  /**
   * Searches for a given search string.
   *
   * @param searchString The string to search for.
   */
  public void searchFor(final String searchString) {
    setSelectedItem(searchString);

    m_searcher.clearResults();

    searchFor(searchString, false, false);
  }

  /**
   * Action class for search hotkey handling.
   */
  private final class CActionHotKey extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -1618510363590582162L;

    /**
     * Action identifier string.
     */
    private final String m_action;

    /**
     * Creates a new action object.
     *
     * @param action Action identifier string.
     */
    private CActionHotKey(final String action) {
      super(action);

      m_action = action;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      if ("NEXT".equals(m_action)) {
        centerNextSearchHit(false, false);
      } else if ("NEXT_ZOOM".equals(m_action)) {
        centerNextSearchHit(false, true);
      } else if ("PREVIOUS".equals(m_action)) {
        centerNextSearchHit(true, false);
      } else if ("PREVIOUS_ZOOM".equals(m_action)) {
        centerNextSearchHit(true, true);
      }
    }
  }
}
