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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.SearchResult;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Model.GraphSearcher;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraphHelpers;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;

import java.awt.Color;
import java.awt.Window;
import java.util.regex.PatternSyntaxException;

import javax.swing.ComboBoxEditor;



/**
 * Contains code for executing a search in a graph window.
 */
public final class CSearchExecuter {
  /**
   * Background color used when a search failed.
   */
  private static final Color BACKGROUND_COLOR_FAIL = new Color(255, 128, 128);

  /**
   * Background color used when a search succeeded.
   */
  private static final Color BACKGROUND_COLOR_SUCCESS = Color.WHITE;

  /**
   * You are not supposed to instantiate this class.
   */
  private CSearchExecuter() {
  }

  /**
   * Cycles through an existing search operation.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph to search through.
   * @param searcher Provides search results to cycle through.
   * @param cycleBackwards True, to cycle backwards through the results. False, to cycle in forward
   *        order.
   * @param zoomToResult True, to zoom to a result. False, to move without zooming.
   */
  private static void cycleExistingSearch(final Window parent, final ZyGraph graph,
      final GraphSearcher searcher, final boolean cycleBackwards, final boolean zoomToResult) {
    // If the searcher didn't change since last time, we only skip to
    // the previous (CTRL+ENTER) or next (ENTER) search result. (SHIFT zooms to target)
    if (cycleBackwards) {
      searcher.getCursor().previous();

      if (searcher.getCursor().isBeforeFirst()) {
        CMessageBox.showInformation(
            parent, "All search results were displayed. Going back to the last one");
      }
    } else {
      searcher.getCursor().next();

      if (searcher.getCursor().isAfterLast()) {
        CMessageBox.showInformation(
            parent, "All search results were displayed. Going back to the first one");
      }
    }


    final SearchResult result = searcher.getCursor().current();

    if (result == null) {
      return;
    }

    if (result.getObject() instanceof NaviNode) {
      ZyGraphHelpers.centerNode(graph, (NaviNode) result.getObject(), zoomToResult);
    } else if (result.getObject() instanceof NaviEdge) {
      ZyGraphHelpers.centerEdgeLabel(graph, (NaviEdge) result.getObject(), zoomToResult);
    }
  }

  /**
   * Cycles through an existing search operation.
   *
   * @param parent Parent window used for dialogs.
   * @param editor Combobox editor whose color is changed depending on the search result.
   * @param graph Graph to search through.
   * @param searcher Executes the search over the graph.
   * @param searchString The string to search for.
   * @param zoomToResult True, to zoom to a result. False, to move without zooming.
   */
  private static void startNewSearch(final Window parent,
      final ComboBoxEditor editor,
      final ZyGraph graph,
      final GraphSearcher searcher,
      final String searchString,
      final boolean zoomToResult) {
    try {
      // Search for all occurrences
      searcher.search(GraphHelpers.getNodes(graph), GraphHelpers.getEdges(graph), searchString);

      if (searcher.getResults().isEmpty()) {
        editor.getEditorComponent().setBackground(BACKGROUND_COLOR_FAIL);
      } else {
        editor.getEditorComponent().setBackground(BACKGROUND_COLOR_SUCCESS);
      }

      // Immediately display all search occurrences
      for (final SearchResult result : searcher.getResults()) {
        if (result.getObject() instanceof NaviNode) {
          final NaviNode node = (NaviNode) result.getObject();
          node.setBackgroundColor(
              result.getLine(), result.getPosition(), result.getLength(), Color.YELLOW);
        } else if (result.getObject() instanceof NaviEdge) {
          final NaviEdge edge = (NaviEdge) result.getObject();
          edge.getLabelContent().getLineContent(result.getLine())
              .setBackgroundColor(result.getPosition(), result.getLength(), Color.YELLOW);
        }
      }

      final SearchResult result = searcher.getCursor().current();

      if (result != null) {
        if (result.getObject() instanceof NaviNode) {
          ZyGraphHelpers.centerNode(graph, (NaviNode) result.getObject(), zoomToResult);
        } else if (result.getObject() instanceof NaviEdge) {
          ZyGraphHelpers.centerEdgeLabel(graph, (NaviEdge) result.getObject(), zoomToResult);
        }
      }

      graph.updateGraphViews();
    } catch (final PatternSyntaxException exception) {
      // Do not bother to log this
      CMessageBox.showInformation(
          parent, String.format("Invalid Regular Expression '%s'", searchString));
    }
  }

  /**
   * Executes a search operation.
   *
   * @param parent Parent window used for dialogs.
   * @param editor Combobox editor whose color is changed depending on the search result.
   * @param graph Graph to search through.
   * @param searcher Executes the search over the graph.
   * @param searchString The string to search for.
   * @param cycleBackwards True, to cycle backwards through the results. False, to cycle in forward
   *        order.
   * @param zoomToResult True, to zoom to a result. False, to move without zooming.
   */
  public static void search(final Window parent,
      final ComboBoxEditor editor,
      final ZyGraph graph,
      final GraphSearcher searcher,
      final String searchString,
      final boolean cycleBackwards,
      final boolean zoomToResult) {
    // If something in the searcher changed, we have to recalculate
    // the search results.
    if (searcher.hasChanged() || !searchString.equals(searcher.getLastSearchString())) {
      CSearchExecuter.startNewSearch(parent, editor, graph, searcher, searchString, zoomToResult);
    } else if (!searcher.getResults().isEmpty()) // Don't bother cycling through an empty results
                                                 // list
    {
      CSearchExecuter.cycleExistingSearch(parent, graph, searcher, cycleBackwards, zoomToResult);
    }
  }
}
