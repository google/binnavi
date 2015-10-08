/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.yfileswrap.algorithms.mono2.common.instructiongraph;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilEdge;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.ReilInstructionGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.ReilInstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraph;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Graph;
import y.base.Node;
import y.base.NodeCursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CReilInstructionGraph implements IInstructionGraph {
  private final Graph m_internalGraph = new Graph();
  private final Node m_entryNode;
  private final Node m_exitNode;

  // The reason why we have this bimap is for easy translation between the
  // external representation of our graph (IInstructionGraphEdge, IInstructionGraphNode)
  // and the internal representation (yFiles) which can be swapped out.
  // We are aware of the memory impact, but once we swap out the yGraph here,
  // the maps will no longer be required.

  private final BiMap<Edge, IInstructionGraphEdge> m_edgesMap = HashBiMap.create();
  private final BiMap<Node, IInstructionGraphNode> m_nodesMap = HashBiMap.create();

  public CReilInstructionGraph(final ReilGraph graph) {
    final HashMap<ReilBlock, Node> firstNodeMapping = new HashMap<ReilBlock, Node>();
    final HashMap<ReilBlock, Node> lastNodeMapping = new HashMap<ReilBlock, Node>();

    for (final ReilBlock block : graph) {
      Node lastNode = null;
      final ReilInstruction lastInstruction = Iterables.getLast(block.getInstructions());

      for (final ReilInstruction instruction : block) {
        final Node currentNode = createInstructionNode(instruction);

        if (instruction == lastInstruction) {
          lastNodeMapping.put(block, currentNode);
        }

        if (!firstNodeMapping.containsKey(block)) {
          firstNodeMapping.put(block, currentNode);
        }

        if (lastNode != null) {
          createInstructionEdge(lastNode, currentNode, true);
        }

        lastNode = currentNode;
      }
    }

    for (final ReilBlock block : graph) {
      for (final ReilEdge edge : block.getOutgoingEdges()) {
        createInstructionEdge(lastNodeMapping.get(block), firstNodeMapping.get(edge.getTarget()),
            EdgeType.isTrueEdge(edge.getType()));
      }
    }

    // Add edges from the entry node to all nodes of in degree zero,
    // and edges to the exit nodes to all nodes of out degree zero.
    final NodeCursor nodeCursor = m_internalGraph.nodes();
    final List<Node> entryNodes = new ArrayList<Node>();
    final List<Node> exitNodes = new ArrayList<Node>();

    while (nodeCursor.ok()) {
      if (((Node) nodeCursor.current()).inDegree() == 0) {
        entryNodes.add((Node) nodeCursor.current());
      }

      if (((Node) nodeCursor.current()).outDegree() == 0) {
        exitNodes.add((Node) nodeCursor.current());
      }
      nodeCursor.next();
    }

    m_entryNode = createInstructionNode(ReilHelpers.createNop(0));
    m_exitNode = createInstructionNode(ReilHelpers.createNop(0xFFFFFF00));

    for (final Node entryNode : entryNodes) {
      createInstructionEdge(m_entryNode, entryNode, true);
    }
    for (final Node exitNode : exitNodes) {
      createInstructionEdge(exitNode, m_exitNode, true);
    }
  }

  /**
   * Creates an instruction graph edge between the source node and the destination node and returns
   * the resulting yfiles edge.
   * 
   * @param sourceNode The source node of the edge to be created.
   * @param destinationNode The destination node of the edge to be created.
   * @param isTrueEdge Boolean parameter to determine if the edge is a conditional true edge.
   * 
   * @return The yfiles edge which has been created and inserted in the graph.
   */
  private Edge createInstructionEdge(final Node sourceNode, final Node destinationNode,
      final boolean isTrueEdge) {
    final ReilInstruction reilInstruction = m_nodesMap.get(destinationNode).getReilInstruction();

    boolean isExitEdge = false;

    if (reilInstruction != null) {
      final IAddress reilInstructionAddress = reilInstruction.getAddress();

      if ((reilInstructionAddress.toLong() & 0xFF) == 0) {
        isExitEdge = true;
      }
    }

    final Edge edge = m_internalGraph.createEdge(sourceNode, destinationNode);
    m_edgesMap.put(edge, new ReilInstructionGraphEdge(isTrueEdge, isExitEdge));
    return edge;
  }

  /**
   * Creates an instruction graph node
   * 
   * @param reilInstruction The REIL instruction which will be contained in the node.
   * 
   * @return The yfiles node which has been created and inserted in the graph.
   */
  private Node createInstructionNode(final ReilInstruction reilInstruction) {
    final Node node = m_internalGraph.createNode();
    m_nodesMap.put(node, new ReilInstructionGraphNode(reilInstruction));
    return node;
  }

  @Override
  public IInstructionGraphNode getDestination(final IInstructionGraphEdge instructionGraphEdge) {
    return m_nodesMap
        .get(m_internalGraph.getTarget(m_edgesMap.inverse().get(instructionGraphEdge)));
  }

  @Override
  public IInstructionGraphNode getEntryNode() {
    return m_nodesMap.get(m_entryNode);
  }

  @Override
  public IInstructionGraphNode getExitNode() {
    return m_nodesMap.get(m_exitNode);
  }

  @Override
  public Iterable<IInstructionGraphEdge> getIncomingEdges(final IInstructionGraphNode n) {
    return new EdgeCursorProxy(m_nodesMap.inverse().get(n), true);
  }

  /**
   * Convenience method to obtain the edge in the ReilInstructionGraph that corresponds to ENTERING
   * a particular native instruction
   * 
   * @param nativeInstructionAddress The address of the native instruction
   * 
   * @return The edge corresponding to entering the native instruction
   */
  public Iterable<IInstructionGraphEdge> getIncomingEdgesForAddress(
      final IAddress nativeInstructionAddress) {
    final ArrayList<IInstructionGraphEdge> result = new ArrayList<IInstructionGraphEdge>();

    final EdgeCursor edgeCursor = m_internalGraph.edges();

    while (edgeCursor.ok()) {
      final Edge currentEdge = (Edge) edgeCursor.current();

      final long targetAddress =
          m_nodesMap.get(currentEdge.target()).getReilInstruction().getAddress().toLong();

      if ((targetAddress >> 8) == nativeInstructionAddress.toLong()) {
        result.add(m_edgesMap.get(currentEdge));
      }

      edgeCursor.next();
    }
    return result;
  }

  @Override
  public Iterable<IInstructionGraphEdge> getOutgoingEdges(
      final IInstructionGraphNode instructionGraphNode) {
    return new EdgeCursorProxy(m_nodesMap.inverse().get(instructionGraphNode), false);
  }

  /**
   * Convenience method to obtain the edge in the ReilInstructionGraph that corresponds to LEAVING a
   * particular native instruction
   * 
   * @param nativeInstructionAddress The address of the native instruction
   * 
   * @return The edge corresponding to entering the native instruction
   */
  public Iterable<IInstructionGraphEdge> getOutgoingEdgesForAddress(
      final IAddress nativeInstructionAddress) {
    final ArrayList<IInstructionGraphEdge> result = new ArrayList<IInstructionGraphEdge>();

    final EdgeCursor edgeCursor = m_internalGraph.edges();

    while (edgeCursor.ok()) {
      final Edge edge = (Edge) edgeCursor.current();
      final long sourceAddress =
          m_nodesMap.get(edge.source()).getReilInstruction().getAddress().toLong();
      final long targetAddress =
          m_nodesMap.get(edge.target()).getReilInstruction().getAddress().toLong();

      if (((targetAddress & 0xFF) == 0)
          && ((sourceAddress >> 8) == nativeInstructionAddress.toLong())) {
        result.add(m_edgesMap.get(edge));
      }

      edgeCursor.next();
    }
    return result;
  }

  @Override
  public IInstructionGraphNode getSource(final IInstructionGraphEdge instructionGraphEdge) {
    return m_nodesMap
        .get(m_internalGraph.getSource(m_edgesMap.inverse().get(instructionGraphEdge)));
  }

  @Override
  public int size() {
    return m_internalGraph.edgeCount();
  }
  
  /**
   * Because Java iterators and yFiles YCursors are mutually unintelligible, we have to create a
   * proxy class that proxies an EdgeCursor to an Iterable<IInstructionGraphEdge>
   */
  class EdgeCursorProxy implements Iterable<IInstructionGraphEdge> {
    private final Node m_Node;
    private final boolean m_incoming;
    private boolean m_hasNext;

    EdgeCursorProxy(final Node node, final boolean incoming) {
      m_Node = node;
      m_incoming = incoming;
    }

    @Override
    public Iterator<IInstructionGraphEdge> iterator() {
      return new EdgeCursorProxyIterator(m_Node, m_incoming);
    }

    class EdgeCursorProxyIterator implements Iterator<IInstructionGraphEdge> {
      EdgeCursor m_edgecursor;
      Edge m_last;

      EdgeCursorProxyIterator(final Node node, final boolean incoming) {
        if (incoming) {
          m_edgecursor = node.inEdges();
        } else {
          m_edgecursor = node.outEdges();
        }
        if (m_edgecursor.ok()) {
          m_hasNext = true;
          m_edgecursor.toLast();
          m_last = (Edge) m_edgecursor.current();
          m_edgecursor.toFirst();
        } else {
          m_hasNext = false;
        }
      }

      @Override
      public boolean hasNext() {
        return m_hasNext;
      }

      @Override
      public IInstructionGraphEdge next() {
        if (m_hasNext == false) {
          throw new NoSuchElementException();
        }

        if (m_edgecursor.current() == m_last) {
          m_hasNext = false;
        }

        final IInstructionGraphEdge result = m_edgesMap.get(m_edgecursor.current());
        m_edgecursor.next();
        return result;
      }

      @Override
      public void remove() {
        throw new IllegalStateException("Error: Remove should never be called !");
      }
    }
  }
}
