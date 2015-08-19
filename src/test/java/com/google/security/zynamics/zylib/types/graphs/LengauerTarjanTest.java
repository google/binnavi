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
package com.google.security.zynamics.zylib.types.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.zylib.types.graphs.algorithms.LengauerTarjan;
import com.google.security.zynamics.zylib.types.graphs.algorithms.MalformedGraphException;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public class LengauerTarjanTest {
  private void assertDifferent(final MockNode... candidates) {
    assertEquals(candidates.length, new HashSet<MockNode>(Lists.newArrayList(candidates)).size());
  }

  private void assertPossible(final MockNode object, final MockNode... mocks) {
    for (final MockNode candidate : mocks) {
      if (object == candidate) {
        return;
      }
    }

    fail();
  }

  private ITreeNode<MockNode> findNode(final ITreeNode<MockNode> treeNode, final MockNode b) {
    if (treeNode.getObject() == b) {
      return treeNode;
    }

    for (final ITreeNode<MockNode> child : treeNode.getChildren()) {
      final ITreeNode<MockNode> m = findNode(child, b);

      if (m != null) {
        return m;
      }
    }

    return null;
  }

  @Test
  public void testEmpty() throws MalformedGraphException {
    final List<MockNode> nodes = new ArrayList<MockNode>();
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    try {
      @SuppressWarnings("unused")
      final Tree<MockNode> tree = LengauerTarjan.calculate(graph, null).first();
      fail();
    } catch (final NullPointerException ex) {
    }
  }

  @Test
  public void testExample() throws MalformedGraphException {
    // Example 19.8 / Page 449 / Modern Compiler Implementation in Java

    final MockNode a = new MockNode("A");
    final MockNode b = new MockNode("B");
    final MockNode c = new MockNode("C");
    final MockNode d = new MockNode("D");
    final MockNode e = new MockNode("E");
    final MockNode f = new MockNode("F");
    final MockNode g = new MockNode("G");
    final MockNode h = new MockNode("H");
    final MockNode i = new MockNode("I");
    final MockNode j = new MockNode("J");
    final MockNode k = new MockNode("K");
    final MockNode l = new MockNode("L");
    final MockNode m = new MockNode("M");

    MockNode.link(a, b);
    MockNode.link(a, c);
    MockNode.link(b, d);
    MockNode.link(b, g);
    MockNode.link(d, f);
    MockNode.link(d, g);
    MockNode.link(f, i);
    MockNode.link(f, k);
    MockNode.link(g, j);
    MockNode.link(j, i);
    MockNode.link(i, l);
    MockNode.link(k, l);
    MockNode.link(l, b);
    MockNode.link(l, m);
    MockNode.link(c, e);
    MockNode.link(c, h);
    MockNode.link(e, c);
    MockNode.link(e, h);
    MockNode.link(h, m);

    final List<MockNode> nodes = Lists.newArrayList(a, b, c, d, e, f, g, h, i, j, k, l, m);
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, a).first();

    assertEquals(a, tree.getRootNode().getObject());

    // Dominated by A
    assertEquals(3, tree.getRootNode().getChildren().size());
    assertPossible(tree.getRootNode().getChildren().get(0).getObject(), b, c, m);
    assertPossible(tree.getRootNode().getChildren().get(1).getObject(), b, c, m);
    assertPossible(tree.getRootNode().getChildren().get(2).getObject(), b, c, m);
    assertDifferent(tree.getRootNode().getChildren().get(0).getObject(), tree.getRootNode()
        .getChildren().get(1).getObject(), tree.getRootNode().getChildren().get(2).getObject());

    // Dominated by B
    final ITreeNode<MockNode> bnode = findNode(tree.getRootNode(), b);
    assertEquals(4, bnode.getChildren().size());
    assertPossible(bnode.getChildren().get(0).getObject(), d, g, i, l);
    assertPossible(bnode.getChildren().get(1).getObject(), d, g, i, l);
    assertPossible(bnode.getChildren().get(2).getObject(), d, g, i, l);
    assertPossible(bnode.getChildren().get(3).getObject(), d, g, i, l);
    assertDifferent(bnode.getChildren().get(0).getObject(), bnode.getChildren().get(1).getObject(),
        bnode.getChildren().get(2).getObject(), bnode.getChildren().get(3).getObject());

    // Dominated by C
    final ITreeNode<MockNode> cnode = findNode(tree.getRootNode(), c);
    assertEquals(2, cnode.getChildren().size());
    assertPossible(cnode.getChildren().get(0).getObject(), e, h);
    assertPossible(cnode.getChildren().get(1).getObject(), e, h);
    assertDifferent(cnode.getChildren().get(0).getObject(), cnode.getChildren().get(1).getObject());

    // Dominated by D
    final ITreeNode<MockNode> dnode = findNode(tree.getRootNode(), d);
    assertEquals(1, dnode.getChildren().size());
    assertPossible(dnode.getChildren().get(0).getObject(), f);

    // Dominated by E
    assertEquals(0, findNode(tree.getRootNode(), e).getChildren().size());

    // Dominated by F
    final ITreeNode<MockNode> fnode = findNode(tree.getRootNode(), f);
    assertEquals(1, fnode.getChildren().size());
    assertPossible(fnode.getChildren().get(0).getObject(), k);

    // Dominated by G
    final ITreeNode<MockNode> gnode = findNode(tree.getRootNode(), g);
    assertEquals(1, gnode.getChildren().size());
    assertPossible(gnode.getChildren().get(0).getObject(), j);

    // Dominated by H
    assertEquals(0, findNode(tree.getRootNode(), h).getChildren().size());

    // Dominated by I
    assertEquals(0, findNode(tree.getRootNode(), i).getChildren().size());

    // Dominated by J
    assertEquals(0, findNode(tree.getRootNode(), j).getChildren().size());

    // Dominated by K
    assertEquals(0, findNode(tree.getRootNode(), k).getChildren().size());

    // Dominated by L
    assertEquals(0, findNode(tree.getRootNode(), l).getChildren().size());

    // Dominated by M
    assertEquals(0, findNode(tree.getRootNode(), m).getChildren().size());
  }

  @Test
  public void testSelfLoop() throws MalformedGraphException {
    final MockNode rootNode = new MockNode("root");

    final MockEdge edge = new MockEdge(rootNode, rootNode);

    MockNode.link(rootNode, rootNode);

    final List<MockNode> nodes = Lists.newArrayList(rootNode);
    final List<MockEdge> edges = Lists.newArrayList(edge);

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, rootNode).first();

    assertNotNull(tree.getRootNode());
    assertEquals(0, tree.getRootNode().getChildren().size());
  }

  @Test
  public void testSelfLoop2() throws MalformedGraphException {
    final MockNode node504C = new MockNode("504C");
    final MockNode node5090 = new MockNode("5090");
    final MockNode node50A0 = new MockNode("50A0");
    final MockNode node50B0 = new MockNode("50B0");
    final MockNode node50BC = new MockNode("50BC");
    final MockNode node50C0 = new MockNode("50C0");
    final MockNode node5068 = new MockNode("5068");

    final MockEdge edge9 = new MockEdge(node504C, node5068);
    final MockEdge edge1 = new MockEdge(node504C, node5090);
    final MockEdge edge2 = new MockEdge(node5090, node50A0);
    final MockEdge edge3 = new MockEdge(node5090, node50B0);
    final MockEdge edge4 = new MockEdge(node50A0, node50A0);
    final MockEdge edge5 = new MockEdge(node50A0, node50B0);
    final MockEdge edge6 = new MockEdge(node50B0, node504C);
    final MockEdge edge7 = new MockEdge(node50B0, node50BC);
    final MockEdge edge8 = new MockEdge(node50BC, node50C0);
    final MockEdge edge10 = new MockEdge(node5068, node50C0);

    MockNode.link(node504C, node5068);
    MockNode.link(node504C, node5090);
    MockNode.link(node5090, node50A0);
    MockNode.link(node5090, node50B0);
    MockNode.link(node50A0, node50A0);
    MockNode.link(node50A0, node50B0);
    MockNode.link(node50B0, node50BC);
    MockNode.link(node50B0, node504C);
    MockNode.link(node5068, node50C0);
    MockNode.link(node50BC, node50C0);

    final List<MockNode> nodes =
        Lists.newArrayList(node504C, node5090, node50A0, node50B0, node50BC, node50C0, node5068);
    final List<MockEdge> edges =
        Lists.newArrayList(edge9, edge7, edge1, edge2, edge3, edge4, edge5, edge6, edge8, edge10);

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    // TODO: Remove debug code left-over
    // System.out.println(GmlConverter.toGml(graph));

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, node504C).first();

    assertNotNull(tree.getRootNode());
    assertEquals(3, tree.getRootNode().getChildren().size());
  }

  @Test
  public void testSingleBlock() throws MalformedGraphException {
    final MockNode rootNode = new MockNode("root");

    final List<MockNode> nodes = Lists.newArrayList(rootNode);
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, rootNode).first();

    assertNotNull(tree.getRootNode());
    assertEquals(0, tree.getRootNode().getChildren().size());
  }

  @Test
  public void testTwoBlocks() throws MalformedGraphException {
    final MockNode rootNode = new MockNode("root");
    final MockNode secondNode = new MockNode("child");

    MockNode.link(rootNode, secondNode);

    final List<MockNode> nodes = Lists.newArrayList(rootNode, secondNode);
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, rootNode).first();

    assertNotNull(tree.getRootNode());
    assertEquals(1, tree.getRootNode().getChildren().size());
    assertEquals(rootNode, tree.getRootNode().getObject());
    assertEquals(0, tree.getRootNode().getChildren().get(0).getChildren().size());
    assertEquals(secondNode, tree.getRootNode().getChildren().get(0).getObject());
  }

  @Test
  public void testTwoChildren() throws MalformedGraphException {
    final MockNode rootNode = new MockNode("root");
    final MockNode child1 = new MockNode("child1");
    final MockNode child2 = new MockNode("child2");

    MockNode.link(rootNode, child1);
    MockNode.link(rootNode, child2);

    final List<MockNode> nodes = Lists.newArrayList(rootNode, child1, child2);
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, rootNode).first();

    assertNotNull(tree.getRootNode());
    assertEquals(2, tree.getRootNode().getChildren().size());
    assertEquals(rootNode, tree.getRootNode().getObject());
    assertPossible(tree.getRootNode().getChildren().get(0).getObject(), child1, child2);
    assertPossible(tree.getRootNode().getChildren().get(1).getObject(), child1, child2);
    assertDifferent(tree.getRootNode().getChildren().get(0).getObject(), tree.getRootNode()
        .getChildren().get(1).getObject());
  }

  @Test
  public void testTwoChildrenLinearly() throws MalformedGraphException {
    final MockNode rootNode = new MockNode("root");
    final MockNode child1 = new MockNode("child1");
    final MockNode child2 = new MockNode("child2");

    MockNode.link(rootNode, child1);
    MockNode.link(child1, child2);

    final List<MockNode> nodes = Lists.newArrayList(rootNode, child1, child2);
    final List<MockEdge> edges = new ArrayList<MockEdge>();

    final DirectedGraph<MockNode, MockEdge> graph =
        new DirectedGraph<MockNode, MockEdge>(nodes, edges);

    final Tree<MockNode> tree = LengauerTarjan.calculate(graph, rootNode).first();

    assertNotNull(tree.getRootNode());
    assertEquals(1, tree.getRootNode().getChildren().size());
    assertEquals(rootNode, tree.getRootNode().getObject());
    assertEquals(1, tree.getRootNode().getChildren().get(0).getChildren().size());
    assertEquals(child1, tree.getRootNode().getChildren().get(0).getObject());
    assertEquals(0, tree.getRootNode().getChildren().get(0).getChildren().get(0).getChildren()
        .size());
    assertEquals(child2, tree.getRootNode().getChildren().get(0).getChildren().get(0).getObject());
  }
}
