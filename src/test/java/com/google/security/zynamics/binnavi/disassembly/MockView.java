/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.cache.EdgeCache;
import com.google.security.zynamics.binnavi.Database.cache.InstructionCache;
import com.google.security.zynamics.binnavi.Database.cache.NodeCache;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.CViewConfiguration;
import com.google.security.zynamics.binnavi.disassembly.views.CViewContent;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContent;
import com.google.security.zynamics.binnavi.disassembly.views.ViewManager;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ICodeEdge;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.MutableDirectedGraph;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.awt.Color;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MockView implements INaviView, ICallgraphView, IFlowgraphView {
  private final ListenerProvider<INaviViewListener> m_listeners =
      new ListenerProvider<INaviViewListener>();

  private CViewContent m_content;
  private final SecureRandom random = new SecureRandom();
  private SQLProvider m_provider;
  private CViewConfiguration m_configuration;

  @Override
  public int hashCode() {
    return Objects.hash(m_configuration.getId());
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof INaviView) {
      final INaviView other = (INaviView) obj;
      return Objects.equals(m_configuration.getId(), other.getConfiguration().getId());
    } else {
      return false;
    }
  }

  public MockView() {
    this(new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
        new ArrayList<INaviEdge>()), new MockSqlProvider(), ViewType.Native, new BigInteger(31,
        new SecureRandom()).intValue());
  }

  public MockView(final List<INaviViewNode> nodes, final List<INaviEdge> edges,
      final SQLProvider provider) {
    this(new MutableDirectedGraph<INaviViewNode, INaviEdge>(nodes, edges), provider,
        ViewType.Native, new BigInteger(31, new SecureRandom()).intValue());
    m_configuration =
        new CViewConfiguration(this, m_listeners, provider, new BigInteger(31, random).intValue(),
            "Mock View Description", "Mock View", ViewType.Native, new Date(), new Date(),
            new LinkedHashSet<CTag>(), false);
    m_provider = provider;
  }

  public MockView(final MutableDirectedGraph<INaviViewNode, INaviEdge> graph,
      final SQLProvider provider, final ViewType type, final Integer viewId) {
    m_content = new CViewContent(this, m_listeners, provider, graph);
    m_configuration =
        new CViewConfiguration(this, m_listeners, provider, viewId, "Mock View Description",
            "Mock View", type, new Date(), new Date(), new LinkedHashSet<CTag>(), false);
    m_provider = provider;
    ViewManager.get(provider).putView(this);
  }

  public MockView(final SQLProvider provider) {
    this(new MutableDirectedGraph<INaviViewNode, INaviEdge>(new ArrayList<INaviViewNode>(),
        Lists.<INaviEdge>newArrayList()), provider, ViewType.NonNative, new BigInteger(31,
        new SecureRandom()).intValue());
    m_provider = provider;
  }

  public static INaviView getFullView(final SQLProvider sql, final ViewType type,
      final Integer viewId) {
    final Integer realViewId =
        viewId == null ? new BigInteger(31, new SecureRandom()).intValue() : viewId;

    final MockFunction function = new MockFunction(4608);

    final List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();
    final List<INaviEdge> edges = new ArrayList<INaviEdge>();

    nodes.add(new CCodeNode(1111, 0, 0, 0, 0, Color.RED, Color.RED, false, true, Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "GC I")),
        function, new LinkedHashSet<CTag>(), sql));
    nodes.add(new CCodeNode(2222, 0, 0, 0, 0, Color.RED, Color.RED, false, true, Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "GC II")),
        function, new LinkedHashSet<CTag>(), sql));
    nodes.add(new CCodeNode(3333, 0, 0, 0, 0, Color.RED, Color.RED, false, true, Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "GC III")),
        function, new LinkedHashSet<CTag>(), sql));
    nodes.add(new CCodeNode(4444, 0, 0, 0, 0, Color.RED, Color.RED, false, true, Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "GC IV")),
        function, new LinkedHashSet<CTag>(), sql));
    nodes.add(new CCodeNode(5555, 0, 0, 0, 0, Color.RED, Color.RED, false, true, Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "GC V")),
        function, new LinkedHashSet<CTag>(), sql));

    nodes.add(new CFunctionNode(6666, new MockFunction(sql, 4608), 0, 0, 0, 0, Color.GREEN, false,
        true, null, new HashSet<CTag>(), sql));

    nodes.add(new CTextNode(7777, 0, 0, 0, 0, Color.YELLOW, false, true, new LinkedHashSet<CTag>(),
        null, sql));

    nodes.add(new CGroupNode(8888, 0, 0, 0, 0, Color.BLACK, false, true, new LinkedHashSet<CTag>(),
        null, false, sql));

    ((INaviCodeNode) nodes.get(0)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(0)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(0)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(1)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(0)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(2)), function.getModule(), sql), null);

    ((INaviCodeNode) nodes.get(1)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(3)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(1)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(4)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(1)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(5)), function.getModule(), sql), null);

    ((INaviCodeNode) nodes.get(2)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(6)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(2)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(7)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(2)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(8)), function.getModule(), sql), null);

    ((INaviCodeNode) nodes.get(3)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(9)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(3)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(10)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(3)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(11)), function.getModule(), sql), null);

    ((INaviCodeNode) nodes.get(4)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(12)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(4)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(13)), function.getModule(), sql), null);
    ((INaviCodeNode) nodes.get(4)).addInstruction(MockCreator.createInstructionWithOperand(function
        .getAddress().toBigInteger().add(BigInteger.valueOf(14)), function.getModule(), sql), null);

    final CNaviViewEdge edge1 =
        new CNaviViewEdge(1111, nodes.get(0), nodes.get(2), EdgeType.JUMP_UNCONDITIONAL, 0, 0, 0,
            0, Color.BLACK, false, false, null, new FilledList<CBend>(), sql);
    final CNaviViewEdge edge2 =
        new CNaviViewEdge(2222, nodes.get(1), nodes.get(2), EdgeType.JUMP_UNCONDITIONAL, 0, 0, 0,
            0, Color.BLACK, false, false, null, new FilledList<CBend>(), sql);
    final CNaviViewEdge edge3 =
        new CNaviViewEdge(3333, nodes.get(2), nodes.get(3), EdgeType.JUMP_UNCONDITIONAL, 0, 0, 0,
            0, Color.BLACK, false, false, null, new FilledList<CBend>(), sql);
    final CNaviViewEdge edge4 =
        new CNaviViewEdge(4444, nodes.get(2), nodes.get(4), EdgeType.JUMP_UNCONDITIONAL, 0, 0, 0,
            0, Color.BLACK, false, false, null, new FilledList<CBend>(), sql);

    edges.add(edge1);
    edges.add(edge2);
    edges.add(edge3);
    edges.add(edge4);

    nodes.get(0).addOutgoingEdge(edge1);
    nodes.get(2).addIncomingEdge(edge1);

    nodes.get(1).addOutgoingEdge(edge2);
    nodes.get(2).addIncomingEdge(edge2);

    nodes.get(2).addOutgoingEdge(edge3);
    nodes.get(3).addIncomingEdge(edge3);

    nodes.get(2).addOutgoingEdge(edge4);
    nodes.get(4).addIncomingEdge(edge4);

    assert nodes.get(2).getParents().size() == 2;

    NodeCache.get(sql).addNodes(nodes);
    EdgeCache.get(sql).addEdges(edges);

    for (final INaviViewNode node : nodes) {
      if (node instanceof INaviCodeNode) {
        InstructionCache.get(sql).addInstructions(((INaviCodeNode) node).getInstructions());
      }
    }

    return new MockView(new MutableDirectedGraph<INaviViewNode, INaviEdge>(nodes, edges), sql,
        type, realViewId);
  }

  @Override
  public void addListener(final INaviViewListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean close() {
    m_content = null;
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
    return m_content;
  }

  @Override
  public List<INaviView> getDerivedViews() {
    return new ArrayList<INaviView>();
  }

  @Override
  public int getEdgeCount() {
    return m_content.getEdgeCount();
  }

  @Override
  public MutableDirectedGraph<INaviViewNode, INaviEdge> getGraph() {
    return m_content.getGraph();
  }

  @Override
  public GraphType getGraphType() {
    return GraphType.FLOWGRAPH;
  }

  @Override
  public int getLoadState() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public String getName() {
    return m_configuration.getName();
  }

  @Override
  public int getNodeCount() {
    return m_content.getGraph().nodeCount();
  }

  @Override
  public Set<CTag> getNodeTags() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public ViewType getType() {
    return m_configuration.getType();
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(m_provider);
  }

  @Override
  public boolean isLoaded() {
    return true;
  }

  @Override
  public boolean isStared() {
    return false;
  }

  @Override
  public void load() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Map<String, String> loadSettings() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void removeListener(final INaviViewListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void save() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void saveSettings(final Map<String, String> settings) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean wasModified() {
    throw new IllegalStateException("Not yet implemented");
  }
}
