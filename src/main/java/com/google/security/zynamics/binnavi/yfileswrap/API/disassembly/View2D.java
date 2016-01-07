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
package com.google.security.zynamics.binnavi.yfileswrap.API.disassembly;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.IView2DListener;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewContainer;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import y.view.EdgeLabel;

import java.awt.event.MouseEvent;
import java.util.List;

// / Represents a single drawable view.
/**
 * The graphical representation of a view. View2D objects represent the graphs shown in graph
 * windows.
 */
public final class View2D {

  /**
   * Wrapped internal database object.
   */
  private final IDatabase m_database;

  /**
   * Wrapped internal view container object.
   */
  private final IViewContainer m_container;

  /**
   * The wrapped internal graph object.
   */
  private final ZyGraph m_graph;

  /**
   * View shown in the graph.
   */
  private View m_view;

  /**
   * For performance reasons, the node mapping between internal nodes and API nodes is cached.
   */
  private final BiMap<INaviViewNode, ViewNode> m_nodeMap = HashBiMap.create();

  /**
   * Keeps the API graph object synchronized with the internal graph object.
   */
  private final InternalGraphListener m_listener = new InternalGraphListener();

  /**
   * Global plugin interface object.
   */
  private final IPluginInterface m_pluginInterface;

  /**
   * Listeners that are notified about changed in the View2D object.
   */
  private final ListenerProvider<IView2DListener> m_listeners =
      new ListenerProvider<IView2DListener>();

  /**
   * API view container object the view belongs to.
   */
  private final ViewContainer m_apiContainer;

  // / @cond INTERNAL
  /**
   * Creates a new API View2D object.
   *
   * @param database Wrapped internal database object.
   * @param container Wrapped internal view container object.
   * @param graph The wrapped internal graph object.
   * @param pluginInterface Global plugin interface object.
   */
  // / @endcond
  public View2D(final IDatabase database, final IViewContainer container, final ZyGraph graph,
      final IPluginInterface pluginInterface) {
    Preconditions.checkNotNull(container, "Error: container argument can not be null");
    Preconditions.checkNotNull(graph, "Error: graph argument can not be null");
    Preconditions.checkNotNull(pluginInterface, "Error: pluginInterface argument can not be null");

    if (!container.isLoaded()) {
      throw new IllegalStateException("View container is not loaded");
    }

    m_database = database;
    m_container = container;
    m_graph = graph;
    m_view =
        findView(database, graph.getRawView(), pluginInterface.getDatabaseManager().getDatabases());
    m_apiContainer =
        findContainer(database, container, pluginInterface.getDatabaseManager().getDatabases());

    Preconditions.checkNotNull(m_apiContainer, "Error: Can not determine view container");

    m_pluginInterface = pluginInterface;

    for (final ViewNode node : m_view.getGraph().getNodes()) {
      m_nodeMap.put(node.getNative(), node);
    }

    m_graph.addListener(m_listener);
  }

  /**
   * Searches for the API view container that wraps a given internal view container.
   *
   * @param database Database to search for.
   * @param container Internal view container to search for.
   * @param databases Databases to search through.
   * @return The API view container that wraps the given internal view container.
   */
  private static ViewContainer findContainer(
      final IDatabase database, final IViewContainer container, final List<Database> databases) {
    final Database apiDatabase = ObjectFinders.getObject(database, databases);

    if (container.getAddressSpaces() == null) {
      // Module
      return ObjectFinders.getObject(container.getNative(), apiDatabase.getModules());
    } else {
      // Project
      return ObjectFinders.getObject(container.getNative(), apiDatabase.getProjects());
    }
  }

  /**
   * Searches for the API view that wraps a given internal view.
   *
   * @param database Database to search for.
   * @param internalView Internal view to search for.
   * @param databases Databases to search through.
   *
   * @return The API view that wraps the given internal view.
   */
  private static View findView(
      final IDatabase database, final INaviView internalView, final List<Database> databases) {
    final Database apiDatabase = ObjectFinders.getObject(database, databases);

    View view = null;

    for (final Module m : apiDatabase.getModules()) {
      if (!m.isLoaded()) {
        continue;
      }

      view = ObjectFinders.getObject(internalView, m.getViews());

      if (view != null) {
        return view;
      }
    }

    for (final Project project : apiDatabase.getProjects()) {
      if (!project.isLoaded()) {
        continue;
      }

      view = ObjectFinders.getObject(internalView, project.getViews());

      if (view != null) {
        return view;
      }
    }

    throw new IllegalStateException("Error: Unknown view");
  }

  // ! Adds a View2D listener.
  /**
   * Adds an object that is notified about changes in the view.
   *
   * @param listener The listener object that is notified about changes in the view.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the view.
   */
  public void addListener(final IView2DListener listener) {
    m_listeners.addListener(listener);
  }

  // ! Layouts the graph using a circular layout.
  /**
   * Layouts the graph using a circular layout.
   */
  public void doCircularLayout() {
    m_graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);

    m_graph.doLayout();
  }

  // ! Layouts the graph using a hierarchical layout.
  /**
   * Layouts the graph using a hierarchical layout.
   */
  public void doHierarchicalLayout() {
    m_graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.HIERARCHIC);

    m_graph.doLayout();
  }

  // ! Layouts the graph using an orthogonal layout.
  /**
   * Layouts the graph using an orthogonal layout.
   */
  public void doOrthogonalLayout() {
    m_graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.ORTHOGONAL);

    m_graph.doLayout();
  }

  /**
   * Returns the container in whose context the view is opened.
   *
   * @return The container in whose context the view is opened.
   */
  public ViewContainer getContainer() {
    return m_apiContainer;
  }

  // / @cond INTERNAL
  /**
   * Returns the API node object of a raw node object.
   *
   * @param node The raw node object.
   *
   * @return The API node object.
   */
  // / @endcond
  public ViewNode getNode(final INaviViewNode node) {
    return m_nodeMap.get(node);
  }

  // ! View that contains the raw data for the View2D.
  /**
   * Returns the view that provides the underlying data used to build the visible graph.
   *
   * @return The view used to build the graph.
   */
  public View getView() {
    return m_view;
  }

  // ! Removes a View2D listener.
  /**
   * Removes a listener object from the view.
   *
   * @param listener The listener object to remove from the view.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the view.
   */
  public void removeListener(final IView2DListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Saves the view to the database.
  /**
   * Saves the view to the database. Note that only non-native views can be saved. For native views
   * please see saveAs.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be saved to the database.
   */
  public void save() throws CouldntSaveDataException {
    try {
      m_graph.save();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Saves a copy of the view to the database.
  /**
   * Saves a copy of the view to the database.
   *
   * @param name The name of the copied view.
   * @param description The description of the copied view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be saved to the database.
   */
  public void saveAs(final String name, final String description) throws CouldntSaveDataException {
    try {
      m_graph.saveAs(m_container, name, description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Toggles proximity browsing.
  /**
   * Toggles between enabled and disabled proximity browsing.
   *
   * @param active True, if proximity browsing should be enabled. False, otherwise.
   */
  public void setProximityBrowsing(final boolean active) {
    m_graph.getSettings().getProximitySettings().setProximityBrowsing(active);
  }

  // ! Printable representation of the view.
  /**
   * Returns the string representation of the view.
   *
   * @return The string representation of the view.
   */
  @Override
  public String toString() {
    return String.format("View2D '%s'", m_view.getName());
  }

  // ! Repaints the view.
  /**
   * Repaints the graph. This is useful after the graph was modified.
   */
  public void updateUI() {
    m_graph.updateViews();
  }

  // ! Zooms graph to screen.
  /**
   * Zooms the view in such a way that the whole graph is visible.
   */
  public void zoomToScreen() {
    ZoomFunctions.zoomToScreen(m_graph);
  }

  /**
   * Keeps the API graph object synchronized with the internal graph object.
   */
  private class InternalGraphListener implements INaviGraphListener {
    @Override
    public void addedNode(final ZyGraph graph, final NaviNode zyNode) {
    }

    @Override
    public void changedModel(final ZyGraph zyGraph, final NaviNode node) {
    }

    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      final View oldPluginView = m_view;

      m_view = findView(
          m_database, m_graph.getRawView(), m_pluginInterface.getDatabaseManager().getDatabases());

      for (final IView2DListener listener : m_listeners) {
        try {
          listener.changedView2D(View2D.this, oldPluginView, m_view);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void edgeClicked(
        final NaviEdge node, final MouseEvent event, final double x, final double y) {
    }

    @Override
    public void edgeLabelEntered(final EdgeLabel label, final MouseEvent event) {
    }

    @Override
    public void edgeLabelExited(final EdgeLabel label) {
    }

    @Override
    public void nodeClicked(
        final NaviNode node, final MouseEvent event, final double x, final double y) {
    }

    @Override
    public void nodeEntered(final NaviNode node, final MouseEvent event) {
    }

    @Override
    public void nodeHovered(final NaviNode node, final double x, final double y) {
    }

    @Override
    public void nodeLeft(final NaviNode node) {
    }

    @Override
    public void proximityBrowserNodeClicked(final ZyProximityNode<?> proximityNode,
        final MouseEvent event, final double x, final double y) {
    }

    @Override
    public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
    }

    @Override
    public void untaggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
    }
  }
}
