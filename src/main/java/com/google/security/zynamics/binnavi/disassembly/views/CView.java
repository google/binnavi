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
package com.google.security.zynamics.binnavi.disassembly.views;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.cache.EdgeCache;
import com.google.security.zynamics.binnavi.Database.cache.NodeCache;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CLoopHighlighter;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CFunctionNodeColorizer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.EdgeInitializer;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the raw data of a view. This includes things like the name of the view or its
 * creation date as well as the nodes and edges of the view.
 */
public final class CView implements INaviView, ICallgraphView, IFlowgraphView {
  /**
   * SQL provider that is used to load the view.
   */
  private final SQLProvider m_provider;

  /**
   * List of listeners that are notified about changes in the view.
   */
  private final ListenerProvider<INaviViewListener> m_listeners =
      new ListenerProvider<INaviViewListener>();

  /**
   * The number of basic blocks in the view. This variable is only used until the view is loaded.
   */
  private int m_bbcount;

  /**
   * The number of edges in the view. This variable is only used until the view is loaded.
   */
  private int m_edgecount;

  /**
   * Reports view loading events to listeners.
   */
  private final CViewLoaderReporter m_loadReporter = new CViewLoaderReporter(m_listeners);

  /**
   * Contains configuration information of the view.
   */
  private final CViewConfiguration m_configuration;

  /**
   * Contains the content of loaded views.
   */
  private CViewContent m_content = null;

  /**
   * Keeps track of the previously known good graph type. This is necessary to reset the graph type
   * when the view is closed.
   */
  private GraphType m_lastGraphType;

  /**
   * Keeps track of what tags are used to tag nodes.
   */
  private final Set<CTag> m_nodeTags;

  private CView(final int viewId,
      final String name,
      final String description,
      final ViewType type,
      final Date creationDate,
      final Date modificationDate,
      final IDirectedGraph<INaviViewNode, INaviEdge> iDirectedGraph,
      final Set<CTag> tags,
      final boolean isStared,
      final SQLProvider provider) {
    Preconditions.checkArgument((viewId > 0) || (viewId == -1),
        "IE01099: View IDs must be positive");
    Preconditions.checkNotNull(name, "IE00271: Name can not be null");
    Preconditions.checkNotNull(description, "IE01100: Description can not be null");
    Preconditions.checkNotNull(type, "IE01101: View type can not be null");
    Preconditions.checkNotNull(creationDate, "IE00471: Creation date can not be null");
    Preconditions.checkNotNull(modificationDate, "IE00472: Modification date can not be null");
    Preconditions.checkNotNull(iDirectedGraph, "IE02234: Graph argument can not be null");
    Preconditions.checkNotNull(tags, "IE00279: Tags argument can not be null");

    for (final CTag tag : tags) {
      Preconditions.checkNotNull(tag, "IE00280: Tag list contains a null-tag");
      Preconditions.checkArgument(tag.getType() == TagType.VIEW_TAG,
          "IE00767: Tag list contains a tag with an invalid type");
      Preconditions.checkArgument(tag.inSameDatabase(provider),
          "IE00282: Tag list contains a tag that is stored in a different database than the view");
    }

    m_provider = Preconditions.checkNotNull(provider, "IE00283: SQL provider date can not be null");
    m_configuration = new CViewConfiguration(this,
        m_listeners,
        provider,
        viewId,
        description,
        name,
        type,
        creationDate,
        modificationDate,
        tags,
        isStared);

    m_bbcount = iDirectedGraph.nodeCount();
    m_edgecount = iDirectedGraph.edgeCount();

    // It is important that those two lines stay at the end.
    m_content = new CViewContent(this, m_listeners, m_provider,
        (MutableDirectedGraph<INaviViewNode, INaviEdge>) iDirectedGraph);
    m_nodeTags = new HashSet<CTag>();

    m_lastGraphType = m_content.getGraphType();

    ViewManager.get(m_provider).putView(this);
  }

  private CView(final int viewId,
      final String name,
      final String description,
      final ViewType type,
      final GraphType graphType,
      final Date creationDate,
      final Date modificationDate,
      final int bbcount,
      final int edgecount,
      final Set<CTag> tags,
      final Set<CTag> nodeTags,
      final boolean isStared,
      final SQLProvider provider) {
    Preconditions.checkArgument((viewId > 0) || (viewId == -1),
        "IE00270: View IDs must be positive");
    Preconditions.checkNotNull(name, "IE00138: Name can not be null");
    Preconditions.checkNotNull(type, "IE00273: View type can not be null");
    Preconditions.checkNotNull(graphType, "IE00274: Graph type can not be null");
    Preconditions.checkNotNull(creationDate, "IE00275: Creation date can not be null");
    Preconditions.checkNotNull(modificationDate, "IE00276: Modification date can not be null");
    Preconditions.checkArgument(bbcount >= 0,
        "IE00277: Basic block count argument can not be null");
    Preconditions.checkArgument(edgecount >= 0, "IE00278: Edge count argument can not be null");
    Preconditions.checkNotNull(tags, "IE01098: Tags argument can not be null");

    for (final CTag tag : tags) {
      Preconditions.checkNotNull(tag, "IE01097: Tag list contains a null-tag");
      Preconditions.checkArgument(tag.getType() == TagType.VIEW_TAG,
          "IE00281: Tag list contains a tag with an invalid type");
      Preconditions.checkArgument(tag.inSameDatabase(provider),
          "IE00285: Tag list contains a tag that is stored in a different database than the view");
    }

    m_provider = Preconditions.checkNotNull(provider, "IE00470: SQL provider date can not be null");
    m_configuration = new CViewConfiguration(this,
        m_listeners,
        provider,
        viewId,
        description,
        name,
        type,
        creationDate,
        modificationDate,
        tags,
        isStared);

    m_bbcount = bbcount;
    m_edgecount = edgecount;
    m_nodeTags = new HashSet<CTag>(nodeTags);

    m_lastGraphType = graphType;
    ViewManager.get(m_provider).putView(this);
  }

  public static CView createUnsavedProjectView(final INaviProject project, final INaviView view,
      final String name, final String description, final SQLProvider provider) {
    final CView newView = new CView(-1,
        name,
        description,
        ViewType.NonNative,
        Calendar.getInstance().getTime(),
        Calendar.getInstance().getTime(),
        view.getGraph(),
        new HashSet<CTag>(),
        false,
        provider);
    newView.getConfiguration().setProject(project);
    return newView;
  }

  public CView(final int viewId,
      final INaviModule module,
      final String name,
      final String description,
      final ViewType type,
      final Date creationDate,
      final Date modificationDate,
      final MutableDirectedGraph<INaviViewNode, INaviEdge> graph,
      final Set<CTag> tags,
      final boolean isStared,
      final SQLProvider provider) {
    this(viewId,
        name,
        description,
        type,
        creationDate,
        modificationDate,
        graph,
        tags,
        isStared,
        provider);

    Preconditions.checkNotNull(module, "IE01271: Module argument can not be null");

    m_configuration.setModule(module);
  }

  public CView(final int viewId,
      final INaviModule module,
      final String name,
      final String description,
      final ViewType type,
      final GraphType graphType,
      final Date creationDate,
      final Date modificationDate,
      final int bbcount,
      final int edgecount,
      final Set<CTag> tags,
      final Set<CTag> nodeTags,
      final boolean isStared,
      final SQLProvider provider) {
    this(viewId,
        name,
        description,
        type,
        graphType,
        creationDate,
        modificationDate,
        bbcount,
        edgecount,
        tags,
        nodeTags,
        isStared,
        provider);

    Preconditions.checkNotNull(module, "IE01279: Module argument can not be null");

    m_configuration.setModule(module);
  }

  public CView(final int viewId,
      final INaviProject project,
      final String name,
      final String description,
      final ViewType type,
      final Date creationDate,
      final Date modificationDate,
      final MutableDirectedGraph<INaviViewNode, INaviEdge> graph,
      final Set<CTag> tags,
      final boolean isStared,
      final SQLProvider provider) {
    this(viewId,
        name,
        description,
        type,
        creationDate,
        modificationDate,
        graph,
        tags,
        isStared,
        provider);

    Preconditions.checkNotNull(project, "IE01280: Project argument can not be null");

    m_configuration.setProject(project);
  }

  public CView(final int viewId,
      final INaviProject project,
      final String name,
      final String description,
      final ViewType type,
      final GraphType graphType,
      final Date creationDate,
      final Date modificationDate,
      final int bbcount,
      final int edgecount,
      final Set<CTag> tags,
      final Set<CTag> nodeTags,
      final boolean isStared,
      final SQLProvider provider) {
    this(viewId,
        name,
        description,
        type,
        graphType,
        creationDate,
        modificationDate,
        bbcount,
        edgecount,
        tags,
        nodeTags,
        isStared,
        provider);

    Preconditions.checkNotNull(project, "IE01281: Project argument can not be null");

    m_configuration.setProject(project);
  }

  @Override
  public void addListener(final INaviViewListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE00284: View is not loaded");
    }

    for (final INaviViewListener listener : m_listeners) {
      try {
        if (!listener.closingView(this)) {
          return false;
        }
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph = m_content.getGraph();

    for (final INaviViewNode node : oldGraph) {
      node.close();
    }

    for (final INaviEdge edge : oldGraph.getEdges()) {
      edge.dispose();
    }

    m_nodeTags.clear();
    m_nodeTags.addAll(m_content.getNodeTags());

    m_content = null;

    for (final INaviViewListener listener : m_listeners) {
      try {
        listener.closedView(this, oldGraph);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return true;
  }

  @Override
  public List<? extends ICodeEdge<?>> getBasicBlockEdges() {
    return m_content.getBasicBlockEdges();
  }

  @Override
  public List<CCodeNode> getBasicBlocks() {
    return m_content.getBasicBlocks();
  }

  @Override
  public CViewConfiguration getConfiguration() {
    return m_configuration;
  }

  @Override
  public IViewContent getContent() {
    Preconditions.checkNotNull(m_content, "IE00465: View is not loaded");

    return m_content;
  }

  @Override
  public List<INaviView> getDerivedViews() throws CouldntLoadDataException {
    return m_provider.getDerivedViews(this);
  }

  @Override
  public int getEdgeCount() {
    return isLoaded() ? m_content.getEdgeCount() : m_edgecount;
  }

  @Override
  public MutableDirectedGraph<INaviViewNode, INaviEdge> getGraph() {
    if (!isLoaded()) {
      throw new IllegalStateException("IE02199: View is not loaded");
    }
    return m_content.getGraph();
  }

  @Override
  public GraphType getGraphType() {
    return isLoaded() ? m_content.getGraphType() : m_lastGraphType;
  }

  @Override
  public int getLoadState() {
    return m_loadReporter.getStep();
  }

  @Override
  public String getName() {
    return m_configuration.getName();
  }

  @Override
  public int getNodeCount() {
    return isLoaded() ? m_content.getNodeCount() : m_bbcount;
  }

  @Override
  public Set<CTag> getNodeTags() {
    return isLoaded() ? m_content.getNodeTags() : new HashSet<CTag>(m_nodeTags);
  }

  @Override
  public ViewType getType() {
    return m_configuration.getType();
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00313: Object argument can not be null");
    return object.inSameDatabase(m_provider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return m_provider.equals(provider);
  }

  @Override
  public boolean isLoaded() {
    return m_content != null;
  }

  @Override
  public boolean isStared() {
    return m_configuration.isStared();
  }

  @Override
  public void load() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    synchronized (m_loadReporter) {
      if (isLoaded()) {
        throw new IllegalStateException("IE01110: View is already loaded");
      }

      m_loadReporter.start();

      if (!m_loadReporter.report(ViewLoadEvents.Started)) {
        throw new LoadCancelledException();
      }

      try {
        final MutableDirectedGraph<INaviViewNode, INaviEdge> graph =
            m_configuration.isStored() ? m_provider.loadView(this)
                : new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
                    new ArrayList<INaviEdge>());
        m_content = new CViewContent(this, m_listeners, m_provider, graph);
        if (!m_loadReporter.report(ViewLoadEvents.Finished)) {
          throw new LoadCancelledException();
        }

        // In the case of a native view the stored color in a basic block is black per default.
        // To change the color of the view to the configured default we need to walk each of the
        // nodes and change the colors according to the configuration. This is not necessary in the
        // case of a non-native view as the color has been saved in the database.
        if (this.getType().equals(ViewType.Native)) {
          colorNodes();
        }

        // In the case of a flow graph we want to color loops in the graph.
        if (getContent().getGraphType().equals(GraphType.FLOWGRAPH)) {
          colorLoops();
        }

        colorEdges();

        // Set the modification state to false to not dirty flag a just loaded view.
        ((CViewContent) getContent()).setModified(false);

        for (final INaviViewListener listener : m_listeners) {
          try {
            listener.loadedView(this);
          } catch (final IllegalArgumentException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
      } finally {
        m_loadReporter.stop();
      }
    }
  }


  /**
   * Colors the loops in the view. This can fail if the view has no function it belongs to or if the
   * graph is malformed.
   */
  private void colorLoops() {
    try {
      for (final INaviViewNode currentNode : getGraph().getNodes()) {
        if (currentNode.getParents().isEmpty()) {
          CLoopHighlighter.colorLoops(getGraph(), currentNode);
          break;
        }
      }
    } catch (final MalformedGraphException exception) {
      NaviLogger.warning("Error: Graph is malformed, can not color loops");
    }
  }

  /**
   * Colors the {@link INaviEdge edges} according to their {@link EdgeType type}.
   */
  private void colorEdges() {
    for (final INaviEdge edge : getGraph().getEdges()) {
      EdgeInitializer.adjustColor(edge);
    }
  }

  /**
   * Colors the {@link INaviCodeNode nodes} of the graph with the default colors configured by the
   * user.
   */
  private void colorNodes() {
    final Color blockColor = ConfigManager.instance().getColorSettings().getBasicBlocksColor();

    for (final INaviViewNode node : getGraph().getNodes()) {
      if (node instanceof INaviCodeNode) {
        node.setColor(blockColor);
      } else if (node instanceof INaviFunctionNode) {
        node.setColor(CFunctionNodeColorizer.getFunctionColor(
            ((INaviFunctionNode) node).getFunction().getType()));
      }
    }
  }


  @Override
  public Map<String, String> loadSettings() throws CouldntLoadDataException {
    return m_configuration.isStored() ? m_provider.loadSettings(this)
        : new HashMap<String, String>();
  }

  @Override
  public void removeListener(final INaviViewListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void save() throws CouldntSaveDataException {
    Preconditions.checkArgument(getType() == ViewType.NonNative,
        "IE00314: Native views can not be saved");
    Preconditions.checkState(isLoaded(), "IE00315: View must be loaded before it can be saved");

    if (m_configuration.isStored()) {
      m_provider.save(this);
    } else {
      CView newView;
      final INaviModule naviModule = m_configuration.getModule();
      if (naviModule == null) {
        newView = m_provider.createView(m_configuration.getProject(), this, getName(),
            m_configuration.getDescription());
      } else {
        newView =
            m_provider.createView(naviModule, this, getName(), m_configuration.getDescription());
      }
      m_configuration.setId(newView.getConfiguration().getId());
    }

    final IDirectedGraph<INaviViewNode, INaviEdge> graph = m_content.getGraph();

    m_bbcount = graph.nodeCount();
    m_edgecount = graph.edgeCount();

    for (final INaviViewListener listener : m_listeners) {
      listener.savedView(this);
    }

    m_configuration.updateModificationDate();
    m_content.save();
    m_lastGraphType = m_content.getGraphType();

    // Update caches.
    NodeCache.get(m_provider).addNodes(graph.getNodes());
    EdgeCache.get(m_provider).addEdges(graph.getEdges());
  }

  @Override
  public void saveSettings(final Map<String, String> settings) throws CouldntSaveDataException {
    if (!m_configuration.isStored()) {
      throw new IllegalStateException("IE01111: Settings can not be saved before view is saved");
    }

    m_provider.saveSettings(this, settings);
  }

  @Override
  public boolean wasModified() {
    return !m_configuration.isStored() || ((m_content != null) && m_content.wasModified());
  }
}
