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
package com.google.security.zynamics.binnavi.ZyGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;
import com.google.security.zynamics.binnavi.disassembly.MockInstruction;
import com.google.security.zynamics.binnavi.disassembly.MockViewGenerator;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader.CGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.functions.NodeFunctions;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.GraphHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.IEdgeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public final class ZyGraphTest {
  private ZyGraph m_graph;
  private INaviView m_view;
  private SQLProvider m_provider;
  private MockModule m_module;
  private MockFunction m_function;

  private Pair<CNaviViewEdge, com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge>
      createEdgeForAddedEdgeTests() {
    final INaviViewNode source = m_view.getGraph().getNodes().get(0);
    final INaviViewNode target = m_view.getGraph().getNodes().get(1);

    final CNaviViewEdge edge = m_graph.getRawView().getContent()
        .createEdge(source, target, EdgeType.ENTER_INLINED_FUNCTION);
    final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge cnn =
        searchEdge(getEdges(m_graph), edge);

    return new Pair<CNaviViewEdge, com.google.security.zynamics.binnavi.yfileswrap.zygraph
                                       .NaviEdge>(edge, cnn);
  }

  private ArrayList<com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> getEdges(
      final ZyGraph graph) {
    final ArrayList<com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> edges =
        new ArrayList<com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge>();

    graph.iterateEdges(new IEdgeCallback<com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge>() {
      @Override
      public IterationMode nextEdge(
          final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge node) {
        edges.add(node);

        return IterationMode.CONTINUE;
      }
    });

    return edges;
  }

  private com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge searchEdge(
      final ArrayList<com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> edges,
      final CNaviViewEdge edge) {
    for (final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge e : edges) {
      if (e.getRawEdge() == edge) {
        return e;
      }
    }

    return null;
  }

  private NaviNode searchNode(final List<NaviNode> nodes, final INaviViewNode node) {
    for (final NaviNode n : nodes) {
      if (n.getRawNode() == node) {
        return n;
      }
    }

    return null;
  }

  @Before
  public void setUp() throws IllegalStateException, FileReadException, LoadCancelledException {
    ConfigManager.instance().read();

    final ZyGraphViewSettings settings = ConfigManager.instance().getDefaultFlowGraphSettings();
    settings.getProximitySettings().setProximityBrowsingActivationThreshold(50);
    settings.getProximitySettings().setProximityBrowsingChildren(2);
    settings.getProximitySettings().setProximityBrowsingParents(2);
    ConfigManager.instance().updateFlowgraphSettings(settings);

    m_provider = new MockSqlProvider();
    m_module = new MockModule(m_provider);
    m_function = new MockFunction(m_provider);
    m_view = MockViewGenerator.generate(m_provider, m_module, m_function);
    m_graph = CGraphBuilder.buildGraph(m_view);
  }

  @Test
  public void testAddedCodeNode() {
    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    final MockFunction function = new MockFunction(m_provider);

    final List<INaviInstruction> instructions =
        Lists.newArrayList((INaviInstruction) new MockInstruction(new CAddress(0x123456), "mov",
            new ArrayList<COperandTree>(), null, m_module));

    final CCodeNode codeNode =
        m_graph.getRawView().getContent().createCodeNode(function, instructions);

    assertEquals(8, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(97, m_graph.getRawView().getNodeCount());

    final List<NaviNode> nodes = GraphHelpers.getNodes(m_graph);

    final NaviNode cnn = searchNode(nodes, codeNode);

    assertEquals(codeNode, cnn.getRawNode());

    assertTrue(codeNode.isVisible());
    assertEquals(codeNode.isVisible(), cnn.isVisible());

    codeNode.setVisible(false);

    assertFalse(codeNode.isVisible());
    assertEquals(codeNode.isVisible(), cnn.isVisible());

    codeNode.setVisible(true);

    assertTrue(codeNode.isVisible());
    assertEquals(codeNode.isVisible(), cnn.isVisible());

    assertFalse(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    codeNode.setSelected(false);

    assertFalse(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    codeNode.setSelected(true);

    assertTrue(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    assertEquals(codeNode.getColor(), cnn.getRealizer().getFillColor());

    codeNode.setColor(Color.GREEN);
    assertEquals(Color.GREEN, codeNode.getColor());
    assertEquals(codeNode.getColor(), cnn.getRealizer().getFillColor());

    codeNode.setX(100);
    assertEquals(100, codeNode.getX(), 0.1);
    assertEquals(codeNode.getX(), cnn.getX(), 0.1);

    codeNode.setY(200);
    assertEquals(200, codeNode.getY(), 0.1);
    assertEquals(codeNode.getY(), cnn.getY(), 0.1);
  }

  @Test
  public void testAddedEdgeCheckColor() {
    final Pair<CNaviViewEdge, com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> p =
        createEdgeForAddedEdgeTests();
    final CNaviViewEdge edge = p.first();
    final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge cnn = p.second();

    assertEquals(edge.getColor(), cnn.getRealizerLineColor());

    edge.setColor(Color.GREEN);
    assertEquals(Color.GREEN, edge.getColor());
    assertEquals(edge.getColor(), cnn.getRealizerLineColor());
  }

  @Test
  public void testAddedEdgeCheckSelection() {
    final Pair<CNaviViewEdge, com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> p =
        createEdgeForAddedEdgeTests();
    final CNaviViewEdge edge = p.first();
    final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge cnn = p.second();

    assertFalse(edge.isSelected());
    assertEquals(edge.isSelected(), cnn.isSelected());

    edge.setSelected(false);

    assertFalse(edge.isSelected());
    assertEquals(edge.isSelected(), cnn.isSelected());

    edge.setSelected(true);

    assertTrue(edge.isSelected());
    assertEquals(edge.isSelected(), cnn.isSelected());
  }

  @Test
  public void testAddedEdgeCheckVisibility() {
    final Pair<CNaviViewEdge, com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge> p =
        createEdgeForAddedEdgeTests();
    final CNaviViewEdge edge = p.first();
    final com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge cnn = p.second();

    assertTrue(edge.isVisible());
    assertEquals(edge.isVisible(), cnn.isVisible());

    edge.setVisible(false);

    assertFalse(edge.isVisible());
    assertEquals(edge.isVisible(), cnn.isVisible());

    edge.setVisible(true);

    assertTrue(edge.isVisible());
    assertEquals(edge.isVisible(), cnn.isVisible());
  }

  @Test
  public void testAddedFunctionNode() {
    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    final CFunctionNode functionNode =
        m_graph.getRawView().getContent().createFunctionNode(new MockFunction(m_provider));

    assertEquals(8, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(97, m_graph.getRawView().getNodeCount());

    final List<NaviNode> nodes = GraphHelpers.getNodes(m_graph);

    final NaviNode cnn = searchNode(nodes, functionNode);

    assertTrue(functionNode.isVisible());
    assertEquals(functionNode.isVisible(), cnn.isVisible());

    functionNode.setVisible(false);

    assertFalse(functionNode.isVisible());
    assertEquals(functionNode.isVisible(), cnn.isVisible());

    functionNode.setVisible(true);

    assertTrue(functionNode.isVisible());
    assertEquals(functionNode.isVisible(), cnn.isVisible());

    assertFalse(functionNode.isSelected());
    assertEquals(functionNode.isSelected(), cnn.isSelected());

    functionNode.setSelected(false);

    assertFalse(functionNode.isSelected());
    assertEquals(functionNode.isSelected(), cnn.isSelected());

    functionNode.setSelected(true);

    assertTrue(functionNode.isSelected());
    assertEquals(functionNode.isSelected(), cnn.isSelected());

    assertEquals(functionNode.getColor(), cnn.getRealizer().getFillColor());

    functionNode.setColor(Color.GREEN);
    assertEquals(Color.GREEN, functionNode.getColor());
    assertEquals(functionNode.getColor(), cnn.getRealizer().getFillColor());

    functionNode.setX(100);
    assertEquals(100, functionNode.getX(), 0.1);
    assertEquals(functionNode.getX(), cnn.getX(), 0.1);

    functionNode.setY(200);
    assertEquals(200, functionNode.getY(), 0.1);
    assertEquals(functionNode.getY(), cnn.getY(), 0.1);
  }

  @Test
  public void testAddedTextNode() {
    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());



    final CTextNode textNode = m_graph.getRawView().getContent().createTextNode(Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "Hannes")));

    assertEquals(8, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(97, m_graph.getRawView().getNodeCount());

    final List<NaviNode> nodes = GraphHelpers.getNodes(m_graph);

    final NaviNode cnn = searchNode(nodes, textNode);

    assertTrue(textNode.isVisible());
    assertEquals(textNode.isVisible(), cnn.isVisible());

    textNode.setVisible(false);

    assertFalse(textNode.isVisible());
    assertEquals(textNode.isVisible(), cnn.isVisible());

    textNode.setVisible(true);

    assertTrue(textNode.isVisible());
    assertEquals(textNode.isVisible(), cnn.isVisible());

    assertFalse(textNode.isSelected());
    assertEquals(textNode.isSelected(), cnn.isSelected());

    textNode.setSelected(false);

    assertFalse(textNode.isSelected());
    assertEquals(textNode.isSelected(), cnn.isSelected());

    textNode.setSelected(true);

    assertTrue(textNode.isSelected());
    assertEquals(textNode.isSelected(), cnn.isSelected());

    assertEquals(textNode.getColor(), cnn.getRealizer().getFillColor());

    textNode.setColor(Color.GREEN);
    assertEquals(Color.GREEN, textNode.getColor());
    assertEquals(textNode.getColor(), cnn.getRealizer().getFillColor());

    textNode.setX(100);
    assertEquals(100, textNode.getX(), 0.1);
    assertEquals(textNode.getX(), cnn.getX(), 0.1);

    textNode.setY(200);
    assertEquals(200, textNode.getY(), 0.1);
    assertEquals(textNode.getY(), cnn.getY(), 0.1);
  }

  @Test
  public void testDeleteEdge() {
    m_view.getContent().deleteEdge(m_view.getGraph().getEdges().get(0));
  }

  @Test
  public void testDeleteNode() {
    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    final List<NaviNode> nodes = GraphHelpers.getNodes(m_graph);

    final INaviViewNode oldChild = m_view.getGraph().getNodes().get(1);

    assertEquals(1, searchNode(nodes, oldChild).getParents().size());

    m_view.getContent().deleteNode(m_view.getGraph().getNodes().get(0));

    assertEquals(6, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(95, m_graph.getRawView().getNodeCount());

    assertEquals(0, searchNode(nodes, oldChild).getParents().size());

    m_view.getContent().deleteNodes(Lists.newArrayList(m_view.getGraph().getNodes().get(0),
        m_view.getGraph().getNodes().get(1)));

    assertEquals(4, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(93, m_graph.getRawView().getNodeCount());
  }

  @Test
  public void testNode() {
    final List<NaviNode> nodes = GraphHelpers.getNodes(m_graph);

    final NaviNode cnn = nodes.get(0);

    final CCodeNode codeNode = (CCodeNode) cnn.getRawNode();

    assertEquals(codeNode.isVisible(), cnn.isVisible());

    codeNode.setVisible(false);

    assertFalse(codeNode.isVisible());
    assertEquals(codeNode.isVisible(), cnn.isVisible());

    codeNode.setVisible(true);

    assertTrue(codeNode.isVisible());
    assertEquals(codeNode.isVisible(), cnn.isVisible());

    assertFalse(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    codeNode.setSelected(false);

    assertFalse(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    codeNode.setSelected(true);

    assertTrue(codeNode.isSelected());
    assertEquals(codeNode.isSelected(), cnn.isSelected());

    assertEquals(codeNode.getColor(), cnn.getRealizer().getFillColor());

    codeNode.setColor(Color.GREEN);
    assertEquals(Color.GREEN, codeNode.getColor());
    assertEquals(codeNode.getColor(), cnn.getRealizer().getFillColor());

    codeNode.setX(100);
    assertEquals(100, codeNode.getX(), 0.1);
    assertEquals(codeNode.getX(), cnn.getX(), 0.1);

    codeNode.setY(200);
    assertEquals(200, codeNode.getY(), 0.1);
    assertEquals(codeNode.getY(), cnn.getY(), 0.1);
  }

  @Test
  public void testVisibility() {
    assertEquals(7, m_graph.visibleNodeCount());
    assertEquals(89, NodeFunctions.getInvisibleNodes(m_graph).size());
    assertEquals(96, m_graph.getRawView().getNodeCount());

    int visibilityCounter = 0;
    int totalCounter = 0;

    for (final NaviNode node : GraphHelpers.getNodes(m_graph)) {
      assertEquals(node.isVisible(), node.getRawNode().isVisible());

      if (node.isVisible()) {
        visibilityCounter++;
      }

      totalCounter++;
    }

    assertEquals(7, visibilityCounter);
    assertEquals(96, totalCounter);
  }
}
