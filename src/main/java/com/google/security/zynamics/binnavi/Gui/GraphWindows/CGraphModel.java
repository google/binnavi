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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.gui.GraphFrame;
import com.google.security.zynamics.binnavi.API.gui.GraphWindow;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo.CSelectionHistory;
import com.google.security.zynamics.binnavi.ZyGraph.CDefaultProximityUpdater;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Encapsulates all important aspects of a single displayed graph.
 */
public final class CGraphModel implements IGraphModel {
  /**
   * Maximum number of Undo operations available for this graph.
   */
  private static final int MAX_UNDO_LEVEL = 30;

  /**
   * Parent window where the graph is shown.
   */
  private final CGraphWindow m_parent;

  /**
   * Database where the graph is stored.
   */
  private final IDatabase m_database;

  /**
   * Context in which the graph is shown.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Graph which is displayed in the graph panel.
   */
  private final ZyGraph m_graph;

  /**
   * API object that represents the displayed graph.
   */
  private final View2D m_view2d;

  /**
   * API object that represents the frame in which the graph is shown.
   */
  private final GraphFrame m_graphFrame;

  /**
   * Panel where the graph is shown.
   */
  private CGraphPanel m_graphPanel;

  /**
   * Selection history object associated with this graph.
   */
  private final CSelectionHistory m_history;

  /**
   * Used to update the graph during proximity browsing.
   */
  private final CDefaultProximityUpdater m_proximityUpdater;

  /**
   * Creates a new graph model object.
   *
   * @param parent Parent window where the graph is shown.
   * @param database Database where the graph is stored.
   * @param viewContainer Context in which the graph is shown.
   * @param graph Graph which is displayed in the graph panel.
   */
  public CGraphModel(final CGraphWindow parent, final IDatabase database,
      final IViewContainer viewContainer, final ZyGraph graph) {
    m_parent = Preconditions.checkNotNull(parent, "IE01610: Parent argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE01611: Database argument can not be null");
    m_viewContainer = Preconditions.checkNotNull(
        viewContainer, "IE01612: View container argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01613: Graph argument can not be null");

    m_history = new CSelectionHistory(m_graph, MAX_UNDO_LEVEL);
    m_view2d = new View2D(database, viewContainer, graph, PluginInterface.instance());
    m_graphFrame =
        new GraphFrame(new GraphWindow(parent), m_view2d, viewContainer.getDebuggerProvider());
    m_proximityUpdater = new CDefaultProximityUpdater(parent, m_graph);
    m_graph.addListener(m_proximityUpdater);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_graph.dispose();
    m_proximityUpdater.dispose();
  }

  /**
   * Returns the API object that represents the database where the graph is stored.
   *
   * @return The API database object.
   */
  public Database getApiDatabase() {
    return ObjectFinders.getObject(
        m_database, PluginInterface.instance().getDatabaseManager().getDatabases());
  }

  /**
   * Returns the database where the graph is stored.
   *
   * @return The database where the graph is stored.
   */
  @Override
  public IDatabase getDatabase() {
    return m_database;
  }

  /**
   * Returns the debugger provider that contains the debuggers to be used to debug the graph.
   *
   * @return The debugger provider associated with the graph.
   */
  @Override
  public BackEndDebuggerProvider getDebuggerProvider() {
    return m_viewContainer.getDebuggerProvider();
  }

  /**
   * Returns the graph that is displayed.
   *
   * @return The graph that is displayed.
   */
  @Override
  public ZyGraph getGraph() {
    return m_graph;
  }

  /**
   * Returns the API object that represents the frame where the graph is shown.
   *
   * @return The API frame where the graph is shown.
   */
  public GraphFrame getGraphFrame() {
    return m_graphFrame;
  }

  /**
   * Returns the panel where the graph is shown.
   *
   * @return The panel where the graph is shown.
   */
  public CGraphPanel getGraphPanel() {
    return m_graphPanel;
  }

  /**
   * Returns the parent window of the graph.
   *
   * @return The parent window of the graph.
   */
  @Override
  public CGraphWindow getParent() {
    return m_parent;
  }

  /**
   * Returns the selection history object for the given graph.
   *
   * @return The selection history object.
   */
  public CSelectionHistory getSelectionHistory() {
    return m_history;
  }

  /**
   * Returns the API object that represents the displayed graph.
   *
   * @return The API graph object.
   */
  public View2D getView2D() {
    return m_view2d;
  }

  /**
   * Returns the view container in which the graph is shown.
   *
   * @return The view container in which the graph is shown.
   */
  @Override
  public IViewContainer getViewContainer() {
    return m_viewContainer;
  }

  /**
   * Sets the graph panel where the graph is displayed.
   *
   * @param graphPanel The graph panel where the graph is displayed.
   */
  public void setPanel(final CGraphPanel graphPanel) {
    Preconditions.checkArgument(m_graphPanel == null, "IE01615: Graph panel can not be set twice");
    m_graphPanel =
        Preconditions.checkNotNull(graphPanel, "IE01614: Graph panel argument can not be null");
  }
}
