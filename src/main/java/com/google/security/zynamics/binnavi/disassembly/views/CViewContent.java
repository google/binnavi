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
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviEdgeListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNodeListener;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNodeListener;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNodeListener;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNodeListener;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CFunctionNodeColorizer;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ICodeContainer;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Describes the content of loaded views.
 */
public final class CViewContent implements IViewContent {
  /**
   * Default color of text nodes.
   */
  private static final Color TEXTNODE_COLOR = new Color(255, 255, 210);

  /**
   * The view whose content is described.
   */
  private final INaviView view;

  /**
   * Listeners that are notified about changes in the content.
   */
  private final ListenerProvider<INaviViewListener> listeners;

  /**
   * Synchronizes the view content with the database.
   */
  private final SQLProvider provider;

  /**
   * Graph of the view.
   */
  private final MutableDirectedGraph<INaviViewNode, INaviEdge> graph;

  /**
   * Says whether the view is Call graph, a Flow graph or a mixed graph.
   */
  private GraphType m_graphType = GraphType.MIXED_GRAPH;

  /**
   * Cached REIL code of the view.
   */
  private ReilFunction m_reilFunction = null;

  /**
   * Translates the view to REIL code.
   */
  private final ReilTranslator<INaviInstruction> m_translator =
      new ReilTranslator<INaviInstruction>();

  /**
   * Updates the view on relevant changes in its nodes.
   */
  private final InternalNodeListener m_internalNodeListener = new InternalNodeListener();

  /**
   * Updates the view on relevant changes in its edges.
   */
  private final InternalEdgeListener m_internalEdgeListener = new InternalEdgeListener();

  /**
   * Flag that signals whether the view was modified since the last save operation.
   */
  private boolean m_wasModified = false;

  /**
   * Creates a new view content object.
   *
   * @param view The view whose content is described.
   * @param listeners Listeners that are notified about changes in the content.
   * @param provider Synchronizes the view content with the database.
   * @param graph Graph of the view.
   */
  public CViewContent(final INaviView view, final ListenerProvider<INaviViewListener> listeners,
      final SQLProvider provider, final MutableDirectedGraph<INaviViewNode, INaviEdge> graph) {
    this.view = Preconditions.checkNotNull(view, "IE02613: view argument can not be null");
    this.listeners =
        Preconditions.checkNotNull(listeners, "IE02614: listeners argument can not be null");
    this.provider =
        Preconditions.checkNotNull(provider, "IE02615: provider argument can not be null");

    this.graph = Preconditions.checkNotNull(graph, "IE02616: graph argument can not be null");

    for (final INaviViewNode node : this.graph.getNodes()) {
      node.addListener(m_internalNodeListener);
    }

    for (final INaviEdge edge : this.graph.getEdges()) {
      edge.addListener(m_internalEdgeListener);
    }

    updateGraphType();
  }

  /**
   * Deletes a group node from the view.
   *
   * @param groupNode The group node to delete.
   */
  private static void deleteGroupNode(final INaviGroupNode groupNode) {
    final List<INaviViewNode> elements = groupNode.getElements();

    groupNode.setCollapsed(false);

    for (final INaviViewNode node : elements) {
      groupNode.removeElement(node);

      if (groupNode.getParentGroup() != null) {
        groupNode.getParentGroup().addElement(node);
      }
    }
  }

  /**
   * Filter all group nodes from a collection of view nodes.
   *
   * @param nodes The view nodes to search through.
   *
   * @return All group nodes of the given view nodes.
   */
  private static Collection<INaviViewNode> filterGroupNodes(final Collection<INaviViewNode> nodes) {
    return Collections2.filter(nodes, new Predicate<INaviViewNode>() {
      @Override
      public boolean apply(final INaviViewNode node) {
        return !(node instanceof INaviGroupNode);
      }
    });
  }

  /**
   * Adds a node to the view and sets up all necessary listeners.
   *
   * @param node The node to add.
   */
  private void addNode(final INaviViewNode node) {
    graph.addNode(node);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.addedNode(view, node);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Returns all incoming and outgoing edges of a node from the view.
   *
   * @param node The view whose edges are removed.
   */
  private void removeEdges(final INaviViewNode node) {
    if (node instanceof INaviGroupNode) {
      // Group nodes can not have real edges attached to them
      return;
    }

    final Set<INaviEdge> toDelete = new HashSet<INaviEdge>();

    for (final INaviEdge incomingEdge : node.getIncomingEdges()) {
      incomingEdge.getSource().removeChild(node);
      incomingEdge.getSource().removeOutgoingEdge(incomingEdge);

      toDelete.add(incomingEdge);
    }

    for (final INaviEdge outgoingEdge : node.getOutgoingEdges()) {
      outgoingEdge.getTarget().removeParent(node);
      outgoingEdge.getTarget().removeIncomingEdge(outgoingEdge);

      toDelete.add(outgoingEdge);
    }

    for (final INaviEdge edge : toDelete) {
      deleteEdge(edge);
    }
  }

  /**
   * Updates the graph type of the view.
   *
   * @param graphType The new graph type of the view.
   */
  private void setGraphType(final GraphType graphType) {
    if (m_graphType.equals(graphType)) {
      return;
    }

    final GraphType oldType = m_graphType;
    m_graphType = graphType;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedGraphType(view, m_graphType, oldType);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the modification state of the view.
   *
   * @param value The new modification state.
   */
  public void setModified(final boolean value) {
    if (m_wasModified == value) {
      return;
    }

    m_wasModified = value;

    for (final INaviViewListener listener : listeners) {
      try {
        listener.changedModificationState(view, value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Calculates the new graph type of the view from the current state of the view graph.
   */
  private void updateGraphType() {
    boolean hasCodeNode = false;
    boolean hasFunctionNode = false;

    for (final INaviViewNode node : graph.getNodes()) {
      if (node instanceof INaviFunctionNode) {
        hasFunctionNode = true;

        if (hasCodeNode) {
          setGraphType(GraphType.MIXED_GRAPH);

          return;
        }
      } else if (node instanceof INaviCodeNode) {
        hasCodeNode = true;

        if (hasFunctionNode) {
          setGraphType(GraphType.MIXED_GRAPH);

          return;
        }
      }
    }

    if (hasCodeNode) {
      setGraphType(GraphType.FLOWGRAPH);
    } else if (hasFunctionNode) {
      setGraphType(GraphType.CALLGRAPH);
    } else {
      setGraphType(GraphType.MIXED_GRAPH);
    }
  }

  /**
   * Updates the graph type after a new node was added to the graph.
   *
   * @param node The added node.
   */
  private void updateGraphType(final IViewNode<?> node) {
    if (m_graphType == GraphType.MIXED_GRAPH) {
      if (getNodeCount() == 1) {
        if (node instanceof INaviCodeNode) {
          setGraphType(GraphType.FLOWGRAPH);
        } else if (node instanceof INaviFunctionNode) {
          setGraphType(GraphType.CALLGRAPH);
        }
      }
    } else if (m_graphType == GraphType.CALLGRAPH) {
      if (node instanceof INaviCodeNode) {
        setGraphType(GraphType.MIXED_GRAPH);
      }
    } else {
      if (node instanceof INaviFunctionNode) {
        setGraphType(GraphType.MIXED_GRAPH);
      }
    }
  }

  @Override
  public CCodeNode createCodeNode(final INaviFunction parentFunction,
      final List<? extends INaviInstruction> instructions) {
    Preconditions.checkNotNull(instructions, "IE00286: Instructions argument can not be null");

    if ((parentFunction != null) && !parentFunction.inSameDatabase(provider)) {
      throw new IllegalArgumentException(
          "IE00287: Parent function and view are not in the same database");
    }

    for (final INaviInstruction instruction : instructions) {
      Preconditions.checkNotNull(instruction,
          "IE00288: Instruction list contains a null-instruction");
      Preconditions.checkArgument(instruction.inSameDatabase(provider),
          "IE00289: Instruction and view are not in the same database");
    }

    final CCodeNode codeNode = new CCodeNode(-1,
        0,
        0,
        0,
        0,
        Color.WHITE,
        Color.BLACK,
        false,
        true,
        null,
        parentFunction,
        new HashSet<CTag>(),
        provider);

    for (final INaviInstruction instruction : instructions) {
      codeNode.addInstruction(instruction, null);
    }

    addNode(codeNode);
    updateGraphType(codeNode);
    codeNode.addListener(m_internalNodeListener);
    m_reilFunction = null;

    return codeNode;
  }

  @Override
  public CNaviViewEdge createEdge(final INaviViewNode source, final INaviViewNode target,
      final EdgeType edgeType) {
    Preconditions.checkNotNull(source, "IE00290: Source argument can not be null");
    Preconditions.checkNotNull(target, "IE00291: Target argument can not be null");
    Preconditions.checkNotNull(edgeType, "IE00292: Edge type argument can not be null");
    Preconditions.checkArgument(source.inSameDatabase(provider),
        "IE00187: Source node is not in the same database");
    Preconditions.checkArgument(target.inSameDatabase(provider),
        "IE00189: Target node is not in the same database");

    final CNaviViewEdge edge = new CNaviViewEdge(-1,
        source,
        target,
        edgeType,
        0,
        0,
        0,
        0,
        Color.BLACK,
        false,
        true,
        null,
        new ArrayList<CBend>(),
        provider);

    source.addOutgoingEdge(edge);
    target.addIncomingEdge(edge);

    edge.addListener(m_internalEdgeListener);
    graph.addEdge(edge);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.addedEdge(view, edge);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return edge;
  }

  @Override
  public CFunctionNode createFunctionNode(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE00294: Function argument can not be null");
    Preconditions.checkArgument(function.inSameDatabase(provider),
        "IE00295: Function and view are not in the same database");

    final CFunctionNode functionNode = new CFunctionNode(-1,
        function,
        0,
        0,
        0,
        0,
        CFunctionNodeColorizer.getFunctionColor(function.getType()),
        false,
        true,
        null,
        new HashSet<CTag>(),
        provider);

    addNode(functionNode);
    updateGraphType(functionNode);
    functionNode.addListener(m_internalNodeListener);
    return functionNode;
  }

  @Override
  public CGroupNode createGroupNode(final Collection<INaviViewNode> nodes) {
    Preconditions.checkNotNull(nodes, "IE00297: Nodes argument can not be null");
    Preconditions.checkArgument(!nodes.isEmpty(), "IE00298: Nodes list can not be empty");

    final CGroupNode groupNode = new CGroupNode(-1,
        0,
        0,
        0,
        0,
        TEXTNODE_COLOR,
        false,
        true,
        new HashSet<CTag>(),
        null,
        false,
        provider);

    for (final INaviViewNode node : nodes) {
      Preconditions.checkNotNull(node, "IE00299: Nodes list contains a null-argument");

      groupNode.addElement(node);
    }

    addNode(groupNode);

    // Do not bother to update the graph type because group nodes have no effect on graph types.

    groupNode.addListener(m_internalNodeListener);
    groupNode.addGroupListener(m_internalNodeListener);

    return groupNode;
  }

  @Override
  public CTextNode createTextNode(final List<IComment> comments) {

    final CTextNode textNode = new CTextNode(-1,
        0,
        0,
        0,
        0,
        TEXTNODE_COLOR,
        false,
        true,
        new HashSet<CTag>(),
        comments,
        provider);

    addNode(textNode);

    // Do not bother to update the graph type because text nodes have no effect on graph types.

    textNode.addListener(m_internalNodeListener);

    return textNode;
  }

  @Override
  public void deleteEdge(final INaviEdge edge) {
    Preconditions.checkNotNull(edge, "IE00300: Edge argument can not be null");

    graph.removeEdge(edge);
    edge.removeListener(m_internalEdgeListener);

    edge.getSource().removeChild(edge.getTarget());
    edge.getTarget().removeParent(edge.getSource());

    edge.getSource().removeOutgoingEdge(edge);
    edge.getTarget().removeIncomingEdge(edge);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.deletedEdge(view, edge);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void deleteNode(final INaviViewNode node) {
    Preconditions.checkNotNull(node, "IE00302: Node argument can not be null");

    if (!graph.getNodes().contains(node)) {
      throw new IllegalArgumentException(
          String.format("IE00304: Node '%s' is not part of this view", node.toString()));
    }

    if ((node instanceof INaviGroupNode) && (((INaviGroupNode) node).getNumberOfElements() != 0)) {
      deleteGroupNode((INaviGroupNode) node);
      return;
    }

    removeEdges(node);

    graph.removeNode(node);
    node.removeListener(m_internalNodeListener);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.deletedNode(view, node);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateGraphType();
  }

  @Override
  public void deleteNodes(final Collection<INaviViewNode> nodes) {
    Preconditions.checkNotNull(nodes, "IE00305: Nodes argument can not be null");

    // Make defensive copy in order to evade the concurrent modification exception
    final Collection<INaviViewNode> nodesCopy = new ArrayList<INaviViewNode>(nodes);

    final List<INaviViewNode> gnodes = graph.getNodes();

    for (final INaviViewNode naviViewNode : nodesCopy) {
      Preconditions.checkNotNull(naviViewNode, "IE00307: Node list contains a null-node");
      Preconditions.checkArgument(gnodes.contains(naviViewNode),
          "IE00308: Node list contains at least one node that is not part of this view");
    }

    final List<INaviGroupNode> parentsToDelete = new ArrayList<INaviGroupNode>();

    for (final INaviViewNode node : nodesCopy) {
      if (node instanceof INaviGroupNode) {
        if (((INaviGroupNode) node).getNumberOfElements() != 0) {
          deleteGroupNode((INaviGroupNode) node);
        }

        continue;
      }

      removeEdges(node);

      graph.removeNode(node);
      node.removeListener(m_internalNodeListener);

      final INaviGroupNode parent = node.getParentGroup();

      if (parent != null) {
        parent.removeElement(node);

        if (parent.getParentGroup() != null) {
          parent.getParentGroup().addElement(node);
        }

        if (parent.getNumberOfElements() == 0) {
          parentsToDelete.add(parent);
        }
      }
    }

    // We remove group nodes from the list of nodes that are put into the listener notification
    // because deletedNode notifications were already sent for group nodes when they lost their last
    // member.
    final Collection<INaviViewNode> filteredNodes = filterGroupNodes(nodesCopy);

    for (final INaviViewListener listener : listeners) {
      try {
        listener.deletedNodes(view, filteredNodes);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    updateGraphType();
  }

  @Override
  public List<? extends ICodeEdge<?>> getBasicBlockEdges() {
    final ArrayList<ICodeEdge<?>> nodes = new ArrayList<ICodeEdge<?>>();

    for (final INaviEdge node : graph.getEdges()) {
      if ((node.getSource() instanceof ICodeContainer)
          && (node.getTarget() instanceof ICodeContainer)) {
        nodes.add(node);
      }
    }

    return nodes;
  }

  @Override
  public List<CCodeNode> getBasicBlocks() {
    final ArrayList<CCodeNode> nodes = new ArrayList<CCodeNode>();

    for (final INaviViewNode node : graph.getNodes()) {
      if (node instanceof CCodeNode) {
        nodes.add((CCodeNode) node);
      }
    }

    return nodes;
  }

  @Override
  public int getEdgeCount() {
    return graph.getEdges().size();
  }

  @Override
  public MutableDirectedGraph<INaviViewNode, INaviEdge> getGraph() {
    return graph;
  }

  @Override
  public GraphType getGraphType() {
    return m_graphType;
  }

  @Override
  public int getNodeCount() {
    return graph.getNodes().size();
  }

  /**
   * Returns the node tags used to tag nodes of this view.
   *
   * @return The node tags used.
   */
  public Set<CTag> getNodeTags() {
    final HashSet<CTag> tags = new HashSet<CTag>();

    for (final INaviViewNode node : graph) {
      tags.addAll(node.getTags());
    }

    return tags;
  }

  @Override
  public ReilFunction getReilCode() throws InternalTranslationException {
    if (m_reilFunction == null) {
      final StandardEnvironment env = new StandardEnvironment();
      m_reilFunction = m_translator.translate(env, view);
    }
    return m_reilFunction;
  }

  /**
   * Saves the view.
   */
  public void save() {
    setModified(false);
  }

  /**
   * Returns whether the view was modified or not.
   *
   * @return True, if the view was modified. False, if it was not.
   */
  public boolean wasModified() {
    return m_wasModified;
  }

  /**
   * Updates the view on relevant changes in its edges.
   */
  private class InternalEdgeListener extends CNaviEdgeListenerAdapter {
    @Override
    public void appendedGlobalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.appendedGlobalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void appendedLocalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.appendedLocalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedColor(final CViewEdge<?> edge, final Color color) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedColor(view, (CNaviViewEdge) edge, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedVisibility(final IViewEdge<?> edge, final boolean visibility) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedVisibility(view, edge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedGlobalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.deletedGlobalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedLocalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.deletedLocalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedGlobalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.editedGlobalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedLocalEdgeComment(final INaviEdge naviEdge, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.editedLocalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedGlobalEdgeComment(final INaviEdge naviEdge,
        final List<IComment> comments) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.initializedGlobalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedLocalEdgeComment(final INaviEdge naviEdge,
        final List<IComment> comments) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.initializedLocalEdgeComment(view, naviEdge);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  /**
   * Updates the view on relevant changes in its nodes.
   */
  private class InternalNodeListener
      implements
      INaviViewNodeListener,
      INaviCodeNodeListener,
      INaviGroupNodeListener,
      INaviFunctionNodeListener {
    @Override
    public void addedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
      setModified(true);
    }

    @Override
    public void addedInstruction(final INaviCodeNode codeNode, final INaviInstruction instruction) {
      setModified(true);
    }

    @Override
    public void appendedFunctionNodeComment(final INaviFunctionNode node, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.appendedLocalFunctionNodeComment(view, node, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void appendedGlobalCodeNodeComment(final INaviCodeNode codeNode,
        final IComment comment) {}

    @Override
    public void appendedGroupNodeComment(final INaviGroupNode node, final IComment comment) {}

    @Override
    public void appendedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {}

    @Override
    public void appendedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {}

    @Override
    public void changedBorderColor(final IViewNode<?> node, final Color color) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedBorderColor(view, node, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedColor(final IViewNode<?> node, final Color color) {
      setModified(true);

      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedColor(view, node, color);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedInstructionColor(final CCodeNode codeNode,
        final INaviInstruction instruction, final int level, final Color color) {}

    @Override
    public void changedParentGroup(final INaviViewNode node, final INaviGroupNode groupNode) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedParentGroup(view, node, groupNode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedSelection(final IViewNode<?> node, final boolean selected) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedSelection(view, node, selected);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedState(final INaviGroupNode node) {}

    @Override
    public void changedVisibility(final IViewNode<?> node, final boolean visible) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.changedVisibility(view, node, visible);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedFunctionNodeComment(final INaviFunctionNode node, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.deletedLocalFunctionNodeComment(view, node, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void deletedGlobalCodeNodeComment(final INaviCodeNode codenode, final IComment comment) {}

    @Override
    public void deletedGroupNodeComment(final INaviGroupNode node, final IComment comment) {}

    @Override
    public void deletedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {}

    @Override
    public void deletedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {}

    @Override
    public void editedFunctionNodeComment(final INaviFunctionNode node, final IComment comment) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.editedLocalFunctionNodeComment(view, node, comment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void editedGlobalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {}

    @Override
    public void editedGroupNodeComment(final INaviGroupNode node, final IComment comment) {}

    @Override
    public void editedLocalCodeNodeComment(final INaviCodeNode codeNode, final IComment comment) {}

    @Override
    public void editedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final IComment comment) {}

    @Override
    public void heightChanged(final IViewNode<?> node, final double height) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.heightChanged(view, node, height);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void initializedFunctionNodeComment(final INaviFunctionNode node,
        final List<IComment> comment) {}

    @Override
    public void initializedGlobalCodeNodeComment(final INaviCodeNode codeNode,
        final List<IComment> comments) {}

    @Override
    public void initializedGroupNodeComment(final INaviGroupNode node,
        final List<IComment> comment) {}

    @Override
    public void initializedLocalCodeNodeComment(final INaviCodeNode codeNode,
        final List<IComment> comments) {}

    @Override
    public void initializedLocalInstructionComment(final INaviCodeNode codeNode,
        final INaviInstruction instruction, final List<IComment> comments) {}

    @Override
    public void removedElement(final INaviGroupNode groupNode, final INaviViewNode node) {
      setModified(true);
      if (groupNode.getNumberOfElements() == 0) {
        deleteNode(groupNode);
      }
    }

    @Override
    public void removedInstruction(final INaviCodeNode codeNode,
        final INaviInstruction instruction) {
      m_reilFunction = null;
      setModified(true);
    }

    @Override
    public void taggedNode(final INaviViewNode node, final CTag tag) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.taggedNode(view, node, tag);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void untaggedNodes(final INaviViewNode node, final List<CTag> tags) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.untaggedNodes(view, node, tags);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void widthChanged(final IViewNode<?> node, final double width) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.widthChanged(view, node, width);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void xposChanged(final IViewNode<?> node, final double xpos) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.xposChanged(view, node, xpos);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void yposChanged(final IViewNode<?> node, final double ypos) {
      for (final INaviViewListener listener : listeners) {
        try {
          listener.yposChanged(view, node, ypos);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
