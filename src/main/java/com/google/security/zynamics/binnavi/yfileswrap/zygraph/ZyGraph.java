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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader.CViewSettingsGenerator;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.ZyGraph.IZyGraphInternals;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CEdgeDrawingFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.IZyGraphLayoutSettingsListener;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphDisplaySettingsListenerAdapter;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphEdgeSettingsListenerAdapter;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.CEdgeUpdater;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CViewInserter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Implementations.CLayoutFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Implementations.CSettingsFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Synchronizers.CViewGraphSynchronizer;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphConverters;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.gui.zygraph.proximity.MultiEdgeHider;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.wrappers.ViewableGraph;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.types.common.IterationMode;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.AbstractZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyNodeRealizer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyProximityNodeRealizer;

import y.base.Edge;
import y.base.Node;
import y.base.NodeList;
import y.view.Graph2D;

import java.awt.Component;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * The ZyGraph is used to connect views with yfiles graph objects.
 */
public class ZyGraph extends AbstractZyGraph<NaviNode, NaviEdge> {
  /**
   * Raw view that provides the data for the visible graph.
   */
  private INaviView m_rawView;

  /**
   * Defines the layout and behavior of the graph.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Listens on the graph display settings and updates the graph if necessary.
   */
  private final InternalDisplaySettingsListener m_settingsListener =
      new InternalDisplaySettingsListener();

  /**
   * Listens on the graph edge settings and updates the graph if necessary.
   */
  private final InternalEdgeSettingsListener m_edgeSettingsListener =
      new InternalEdgeSettingsListener();

  /**
   * Listens on the graph layout settings and updates the graph if necessary.
   */
  private final InternalLayoutSettingsListener m_layoutSettingsListener =
      new InternalLayoutSettingsListener();

  /**
   * Synchronizes the visible graph with the underlying raw view.
   */
  private final CViewGraphSynchronizer m_synchronizer;

  /**
   * Listeners that are notified about changes in the graph.
   */
  private final ListenerProvider<INaviGraphListener> m_listeners =
      new ListenerProvider<INaviGraphListener>();

  /**
   * Flag used to suppress layouting while going through multiple internal states which should be
   * presented as one state change to the user.
   */
  private boolean m_disableLayoutingInternally = false;

  /**
   * Creates a new graph for a given view.
   *
   * @param view The view that provides the raw data of the graph.
   * @param nodeMap A map that maps between ynodes and ZyNodes of the graph.
   * @param edgeMap A map that maps between yedges and ZyEdges of the graph.
   * @param settings Defines the layout and behavior of the graph.
   * @param g2dview The yFiles view that displays the graph.
   */
  public ZyGraph(final INaviView view, final LinkedHashMap<Node, NaviNode> nodeMap,
      final LinkedHashMap<Edge, NaviEdge> edgeMap, final ZyGraphViewSettings settings,
      final ZyGraph2DView g2dview) {
    super(g2dview, nodeMap, edgeMap, settings);

    m_rawView = Preconditions.checkNotNull(view, "IE00869: Argument view can't be null");
    m_settings = Preconditions.checkNotNull(settings, "IE00870: Settings argument can't be null");

    m_synchronizer = new CViewGraphSynchronizer(this, new GraphInternals(), getMappings());

    hideInvisibleElements();

    CLayoutFunctions.updateBackground(this);

    CEdgeDrawingFunctions.initializeEdgeDrawingMode(this);

    initializeListeners();

    initializeProximityBrowser();

    if ((m_rawView.getType() == ViewType.Native)
        && (getGraph().nodeCount() >= m_settings.getProximitySettings()
        .getProximityBrowsingActivationThreshold())) {
      m_settings.getProximitySettings().setProximityBrowsing(true);
    }

    updateMultipleEdgeState();
  }

  /**
   * When a graph is initially shown, the logically invisible elements have to be removed explicitly
   * from the yFiles graph.
   */
  private void hideInvisibleElements() {
    final Graph2D graph = getGraph();
    for (final INaviEdge edge : m_rawView.getGraph().getEdges()) {
      if (!edge.isVisible()) {
        graph.removeEdge(getYEdge(edge));
      }
    }

    for (final INaviViewNode node : m_rawView.getGraph()) {
      if (!node.isVisible()) {
        graph.removeNode(getYNode(node));
      }
    }
  }

  /**
   * Sets up all necessary listeners to keep the graph up to date.
   */
  private void initializeListeners() {
    m_settings.getDisplaySettings().addListener(m_settingsListener);
    m_settings.getEdgeSettings().addListener(m_edgeSettingsListener);
    m_settings.getLayoutSettings().addListener(m_layoutSettingsListener);
  }

  /**
   * Initializes the proximity browser when the graph is first created.
   */
  private void initializeProximityBrowser() {
    if ((m_rawView.getType() == ViewType.Native)
        && (m_rawView.getNodeCount() >= m_settings.getProximitySettings()
        .getProximityBrowsingActivationThreshold())) {
      initializeProximityBrowserNative();
    } else if ((m_rawView.getType() == ViewType.NonNative)
        && m_settings.getProximitySettings().getProximityBrowsing()) {
      initializeProximityBrowserNonNative();
    }

    if ((m_rawView.getType() == ViewType.Native)
        && (visibleNodeCount() >= m_settings.getLayoutSettings()
        .getAutolayoutDeactivationThreshold())) {
      m_settings.getLayoutSettings().setAutomaticLayouting(false);
    }

    if (m_settings.getLayoutSettings().getAutomaticLayouting()) {
      doLayout();
    }
  }

  /**
   * Initializes the proximity browser for a native view node.
   */
  private void initializeProximityBrowserNative() {
    final List<NaviNode> allNodes = GraphHelpers.getNodes(this);

    if (!allNodes.isEmpty()) {
      // ATTENTION: Do not use allNodes.get(0) because the order of allNodes is not deterministic
      final NaviNode firstNode = getMappings().getNode(getRawView().getGraph().getNodes().get(0));

      allNodes.remove(firstNode);

      showNodes(Lists.newArrayList(firstNode), allNodes);
    }
  }

  /**
   * Initializes the proximity browser for a non-native view node.
   */
  private void initializeProximityBrowserNonNative() {
    // Make sure not to auto-layout non-native views when they are first loaded.
    // m_disableLayoutingInternally disables re-layouting in this class, but to
    // disable re-layouting in the proximity browser, the layouting setting must
    // be changed too.

    m_disableLayoutingInternally = true;

    final boolean previousSetting = m_settings.getLayoutSettings().getAutomaticLayouting();

    m_settings.getLayoutSettings().setAutomaticLayouting(false);

    // Small trick to make sure to show the proximity nodes for this graph.
    // Use a temporary variable to work around OpenJDK build problem. Original code is:
    // showNodes(GraphHelpers.getVisibleNodes(ViewableGraph.wrap(this)), false);
    final Collection<NaviNode> nodes = GraphHelpers.getVisibleNodes(ViewableGraph.wrap(this));
    showNodes(nodes, false);

    m_settings.getLayoutSettings().setAutomaticLayouting(previousSetting);

    m_disableLayoutingInternally = false;

    if (m_rawView.getConfiguration().getId() == -1) {
      // This is a pure performance thing; became necessary after
      // having combined call graphs where all nodes would occupy
      // the same space otherwise, leading to a huge slowdown.

      doLayout();
    }
  }

  /**
   * Removes all listeners the graph object created.
   */
  private void removeListeners() {
    m_settings.getDisplaySettings().removeListener(m_settingsListener);
    m_settings.getEdgeSettings().removeListener(m_edgeSettingsListener);
    m_settings.getLayoutSettings().removeListener(m_layoutSettingsListener);
  }

  /**
   * Updates the visibility of the edges depending on whether multiple edges are hidden or not.
   */
  private void updateMultipleEdgeState() {
    if (m_settings.getEdgeSettings().getDisplayMultipleEdgesAsOne()) {
      MultiEdgeHider.hideMultipleEdgesInternal(this);
    } else {
      MultiEdgeHider.unhideMultipleEdgesInternal(this);
    }
  }

  /**
   * Adds a listener object that is notified about changes in the graph.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final INaviGraphListener listener) {
    super.addListener(listener);

    m_listeners.addListener(listener);

    m_synchronizer.addListener(listener);
  }

  /**
   * Adds a modifier to each node in the graph.
   *
   * @param modifier The modifier to be added.
   */
  public void addNodeModifier(final IZyNodeRealizerListener<NaviNode> modifier) {
    iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        node.addNodeModifier(modifier);

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * This is the preferred way to delete nodes from open graphs.
   *
   * The reason for this is that node deletion throws selection events and proximity browsing
   * re-layouts after each selection event, making the deletion of multiple nodes without pre/post
   * events tediously slow.
   *
   * @param nodes The nodes to delete.
   */
  public void deleteNodes(final List<NaviNode> nodes) {
    // This line is moved up here to forward argument checking to
    // the conversion function.
    final List<INaviViewNode> convertedNodes = GraphConverters.convert(nodes);

    getGraph().firePreEvent();

    getRawView().getContent().deleteNodes(convertedNodes);

    getGraph().firePostEvent();
  }

  @Override
  public void dispose() {
    super.dispose();

    m_synchronizer.dispose();

    removeListeners();

    iterate(new INodeCallback<NaviNode>() {
      @Override
      public IterationMode next(final NaviNode node) {
        final IRealizerUpdater<?> updater = node.getRealizer().getUpdater();

        if (updater != null) {
          updater.dispose();
        }

        return IterationMode.CONTINUE;
      }
    });
  }

  /**
   * Returns the raw view that provides the raw data for the graph.
   *
   * @return The raw view.
   */
  public INaviView getRawView() {
    return m_rawView;
  }

  @Override
  public Set<NaviNode> getSelectedNodes() {
    return m_synchronizer.getSelectedNodes();
  }

  @Override
  public ZyGraphViewSettings getSettings() {
    return m_settings;
  }

  /**
   * Removes a listener that was previously notified about changes in the graph.
   *
   * @param listener The listener to be removed.
   */
  public void removeListener(final INaviGraphListener listener) {
    super.removeListener(listener);

    m_listeners.removeListener(listener);

    m_synchronizer.removeListener(listener);
  }

  /**
   * Saves the graph to the database. The difference between this method and the raw view method
   * with the same name is that this method also stores the graph settings.
   *
   * @throws CouldntSaveDataException Thrown if the graph could not be saved.
   */
  public boolean save() throws CouldntSaveDataException {
    m_rawView.save();

    CSettingsFunctions.saveSettings(m_rawView, getView(), m_settings);
    return true;
  }

  /**
   * Creates a copy of the current native view and transfers the copied native view into the graph
   * object. That means that the graph object changes its underlying raw view when this function is
   * called.
   *
   * @param container The view container where the view is copied to.
   * @param name The new name of the raw view.
   * @param description The new description of the raw view.
   *
   * @return The new raw view.
   *
   * @throws CouldntSaveDataException Thrown if the view could not be saved.
   */
  public INaviView saveAs(final IViewContainer container, final String name,
      final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(container, "IE00871: Container argument can not be null");
    Preconditions.checkNotNull(name, "IE00872: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE00899: Description argument can not be null");

    final INaviView oldView = m_rawView;

    final INaviView newView = container.createView(name, description);

    CViewInserter.insertView(oldView, newView);

    final List<INaviViewNode> oldNodes = oldView.getGraph().getNodes();
    final List<INaviViewNode> newNodes = newView.getGraph().getNodes();

    for (int i = 0; i < oldNodes.size(); i++) {
      final INaviViewNode newNode = newNodes.get(i);
      final NaviNode oldNode = getMappings().getNode(oldNodes.get(i));

      getMappings().setNode(newNode, oldNode);

      oldNode.setRawNode(newNode);

      for (final INaviGraphListener listener : m_listeners) {
        try {
          listener.changedModel(this, oldNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    final List<INaviEdge> oldEdges = oldView.getGraph().getEdges();
    final List<INaviEdge> newEdges = newView.getGraph().getEdges();

    for (int i = 0; i < oldEdges.size(); i++) {
      final INaviEdge newEdge = newEdges.get(i);
      final NaviEdge oldEdge = getMappings().getEdge(oldEdges.get(i));

      assert oldEdge != null;

      getMappings().setEdge(newEdge, oldEdge);

      final ZyEdgeRealizer<NaviEdge> realizer = oldEdge.getRealizer();

      realizer.setUpdater(new CEdgeUpdater(newEdge));

      oldEdge.setRawEdge(newEdge);
    }

    removeListeners();

    newView.save();

    CSettingsFunctions.saveSettings(newView, getView(), m_settings);

    m_rawView = newView;

    initializeListeners();

    m_synchronizer.reset();

    for (final INaviGraphListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because we are calling a listener function.
      try {
        listener.changedView(oldView, newView);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    oldView.close();

    return m_rawView;
  }

  @Override
  public void showNodes(final Collection<NaviNode> toShow, final Collection<NaviNode> toHide) {
    m_synchronizer.setMultiEdgeUpdatingEnabled(false);

    super.showNodes(toShow, toHide);

    m_synchronizer.setMultiEdgeUpdatingEnabled(true);
  }

  /**
   * Returns the number of visible nodes in the graph.
   *
   * @return The number of visible nodes in the graph.
   */
  @SuppressWarnings("unchecked")
  public int visibleNodeCount() {
    return CollectionHelpers.countIf(new NodeList(getGraph().nodes()),
        new ICollectionFilter<Node>() {
      @Override
      public boolean qualifies(final Node item) {
        return !(getGraph().getRealizer(item) instanceof ZyProximityNodeRealizer<?>);
      }
    });
  }

  /**
   * Provides some graph internals for the ViewGraph synchronizer to work with.
   */
  private class GraphInternals implements IZyGraphInternals {
    @Override
    public void notifyNodeDeleted() {
      notifyDeletionListeners();
    }

    @Override
    public void removeEdge(final NaviEdge edge) {
      if (edge.getEdge().getGraph() != null) {
        getGraph().removeEdge(edge.getEdge());
      }

      getMappings().removeEdge(edge);
      NaviNode.unlink(edge.getSource(), edge.getTarget());
    }

    @Override
    public void removeNode(final NaviNode node) {
      ZyGraph.this.removeNode(node);
    }
  }

  /**
   * Listener that keeps track of graph settings and changes the graph when the settings change.
   */
  private class InternalDisplaySettingsListener extends ZyGraphDisplaySettingsListenerAdapter {
    @Override
    public void changedFunctionNodeInformation(final boolean show) {
      for (final NaviNode node : getMappings().getNodes()) {
        if (node.getRawNode() instanceof INaviFunctionNode) {
          (((ZyNodeRealizer<?>) getGraph().getRealizer(node.getNode()))).regenerate();
        }
      }

      updateViews();
    }

    @Override
    public void changedShowMemoryAddresses(final IDebugger debugger, final boolean selected) {
      for (final NaviNode node : getMappings().getNodes()) {
        (((ZyNodeRealizer<?>) getGraph().getRealizer(node.getNode()))).regenerate();
      }

      updateViews();
    }
  }

  /**
   * Listens on the graph edge settings and updates the graph if necessary.
   */
  private class InternalEdgeSettingsListener extends ZyGraphEdgeSettingsListenerAdapter {
    @Override
    public void changedDisplayMultipleEdgesAsOne(final boolean value) {
      updateMultipleEdgeState();
    }

    @Override
    public void changedDrawSelectedBends(final boolean value) {
      iterateEdges(new IEdgeCallback<NaviEdge>() {
        @Override
        public IterationMode nextEdge(final NaviEdge node) {
          node.getRealizer().setDrawBends(value);
          return IterationMode.CONTINUE;
        }
      });

      updateViews();
    }
  }

  /**
   * Listens on the graph layout settings and updates the graph if necessary.
   */
  private class InternalLayoutSettingsListener implements IZyGraphLayoutSettingsListener {
    @Override
    public void changedAutomaticLayouting(final boolean value) {
      if (m_settings.getLayoutSettings().getAutomaticLayouting() && !m_disableLayoutingInternally) {
        doLayout();
      }
    }
  }

  /**
   * Nobody remembers what case 874 was. This was moved from CViewLoader.java since it depends on
   * yFiles, but it is unclear if this is even still needed.
   */
  // TODO(thomasdullien): Figure out what this is for.
  public void workAroundCase874() {
    getView().setCenter(
        CViewSettingsGenerator.createDoubleSetting(getSettings().rawSettings, "view_center_x", 0),
        CViewSettingsGenerator.createDoubleSetting(getSettings().rawSettings, "view_center_y", 0));
    getView().setWorldRect(
        CViewSettingsGenerator.createIntegerSetting(getSettings().rawSettings, "world_rect_x", 0),
        CViewSettingsGenerator.createIntegerSetting(getSettings().rawSettings, "world_rect_y", 0),
        CViewSettingsGenerator.createIntegerSetting(
            getSettings().rawSettings, "world_rect_width", 800),
        CViewSettingsGenerator.createIntegerSetting(
            getSettings().rawSettings, "world_rect_height", 600));
    getView().setZoom(
        CViewSettingsGenerator.createDoubleSetting(getSettings().rawSettings, "zoom", 1));

    updateViews();
  }

  public String getViewName() {
    return getView().getName();
  }

  public Component getViewAsComponent() {
    return getView();
  }

  /**
   * Small helper function to get back the node at a given index.
   *
   * @param index The index of the node.
   *
   * @return The node at the given index.
   */
  public NaviNode getRawNodeFromIndex(int index) {
    return getNode(getRawView().getGraph().getNodes().get(index));
  }

  public void makeRawNodeVisibleAndSelect(int index) {
    INaviViewNode rawNode = getRawView().getGraph().getNodes().get(index);
    NaviNode naviNode = getNode(rawNode);
    if (!rawNode.isVisible()
        && !getSettings().getProximitySettings().getProximityBrowsingFrozen()) {
      rawNode.setVisible(true);
    }

    selectNode(naviNode, !rawNode.isSelected());
  }
}
