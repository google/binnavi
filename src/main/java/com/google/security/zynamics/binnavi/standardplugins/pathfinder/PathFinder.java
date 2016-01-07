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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.BlockEdge;
import com.google.security.zynamics.binnavi.API.disassembly.Callgraph;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.disassembly.FlowGraph;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionBlock;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionNode;
import com.google.security.zynamics.binnavi.API.disassembly.FunctionType;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Operand;
import com.google.security.zynamics.binnavi.API.disassembly.OperandExpression;
import com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException;
import com.google.security.zynamics.binnavi.API.disassembly.Reference;
import com.google.security.zynamics.binnavi.API.disassembly.ReferenceType;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewEdge;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.API.helpers.GraphAlgorithms;
import com.google.security.zynamics.binnavi.API.helpers.Logger;
import com.google.security.zynamics.binnavi.CUtilityFunctions;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements the pathfinding algorithm.
 */
public final class PathFinder {
  /**
   * Standard color for new code nodes.
   */
  private static final Color DEFAULT_BLOCK_COLOR = new Color(-68902);

  /**
   * Standard color for inlining edges-
   */
  private static final Color DEFAULT_INLINING_EDGE_COLOR = new Color(-3360768);

  /**
   * Standard color for conditional jumps that are taken.
   */
  private static final Color DEFAULT_TRUE_JUMP_COLOR = new Color(-16736256);

  /**
   * Standard color for conditional jumps that are not taken.
   */
  private static final Color DEFAULT_FALSE_JUMP_COLOR = new Color(-6291456);

  /**
   * Checks whether an instruction calls a function.
   *
   * @param instruction The instruction to check.
   * @param function The called function to check for.
   *
   * @return True, if the instruction calls the function. False, if it does not.
   */
  private static boolean callsFunction(final Instruction instruction, final Function function) {
    for (final Operand operand : instruction.getOperands()) {
      final OperandExpression rootNode = operand.getRootNode();

      if (hasFunctionCallReference(rootNode, function)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Connects the functions in the view using inlining edges.
   *
   * @param view The view where the edges are created.
   * @param startNode The start node of the path.
   * @param targetNode The target node of the path.
   * @param passedFunctions All functions that lie on the path.
   * @param entryNodes Keeps track of the entry nodes of all functions.
   * @param exitNodes Keeps track of the exit nodes of all functions.
   * @param functionMap Keeps track to what function a node belongs to.
   *
   * @return Node pair that contains the updated start node and target node.
   */
  private static NodePair connectFunctions(final View view,
      final ViewNode startNode,
      final ViewNode targetNode,
      final Collection<FunctionBlock> passedFunctions,
      final Map<Function, ViewNode> entryNodes,
      final ArrayListMultimap<Function, ViewNode> exitNodes,
      final Map<ViewNode, Function> functionMap) {

    ViewNode realStartNode = startNode;
    ViewNode realTargetNode = targetNode;

    final Set<ViewNode> handled = new HashSet<ViewNode>();

    while (true) {
      boolean splitNode = false;

      start: for (final ViewNode node : view.getGraph().getNodes()) {
        if (handled.contains(node)) {
          continue;
        }

        if (!(node instanceof CodeNode)) {
          continue;
        }

        final CodeNode cnode = (CodeNode) node;

        for (final Instruction instruction : cnode.getInstructions()) {
          for (final FunctionBlock functionBlock : passedFunctions) {
            final Function function = functionBlock.getFunction();

            if (callsFunction(instruction, function)) {
              // A function call to a function on the path was found.
              // At this point we have to split the code node after
              // the function call.

              final NodePair result = splitBlock(view, functionMap.get(cnode), cnode, instruction);

              if (realStartNode == cnode) {
                // Of course it is possible that the start node was split,
                // therefore we have to update the start node to the upper
                // part of the new node.

                realStartNode = result.getFirst();
              }

              if (realTargetNode == cnode) {
                // Of course it is possible that the target node was split,
                // therefore we have to update the target node to the upper
                // part of the new node.

                realTargetNode = result.getFirst();
              }

              // Furthermore it is possible that the entry and exit nodes
              // we determined earlier were split. These need to be updated
              // too.
              for (final FunctionBlock functionBlock2 : passedFunctions) {
                final Function function2 = functionBlock2.getFunction();

                if (entryNodes.get(function2) == cnode) {
                  // Update the entry nodes
                  entryNodes.put(function2, result.getFirst());
                }

                if (exitNodes.get(function2).contains(cnode)) {
                  // Update the exit nodes
                  if (result.getSecond() != null) {
                    exitNodes.remove(function2, cnode);
                    exitNodes.put(function2, result.getSecond());
                  }
                }
              }

              if (functionMap.containsKey(cnode)) {
                final Function f = functionMap.get(cnode);

                functionMap.remove(cnode);
                functionMap.put(result.getFirst(), f);
              }

              handled.add(result.getFirst());

              if (result.getSecond() == null) {
                // The input node was not split. The outgoing edges are replaced by a single
                // edge that goes to the called function. At the end of the called function we
                // insert edges that return to the original children of the input node.

                for (final ViewEdge edge : node.getOutgoingEdges()) {
                  for (final ViewNode currentExitNode : exitNodes.get(function)) {
                    final ViewEdge leaveEdge = view.createEdge(currentExitNode, edge.getTarget(),
                        EdgeType.LeaveInlinedFunction);
                    leaveEdge.setColor(DEFAULT_INLINING_EDGE_COLOR);
                  }
                  view.deleteEdge(edge);
                }

                final ViewEdge enterEdge = view.createEdge(result.getFirst(),
                    entryNodes.get(function), EdgeType.EnterInlinedFunction);
                enterEdge.setColor(DEFAULT_INLINING_EDGE_COLOR);

                handled.add(cnode);
              } else {
                // The node was split. We simply have to connect both split parts to the
                // called function.

                final ViewEdge enterEdge = view.createEdge(result.getFirst(),
                    entryNodes.get(function), EdgeType.EnterInlinedFunction);
                enterEdge.setColor(DEFAULT_INLINING_EDGE_COLOR);

                for (final ViewNode currentExitNode : exitNodes.get(function)) {
                  final ViewEdge leaveEdge = view.createEdge(currentExitNode, result.getSecond(),
                      EdgeType.LeaveInlinedFunction);
                  leaveEdge.setColor(DEFAULT_INLINING_EDGE_COLOR);
                }
              }

              splitNode = true;

              break start;
            }
          }
        }

        handled.add(cnode);
      }

      if (!splitNode) {
        break;
      }
    }

    return new NodePair(realStartNode, realTargetNode);
  }

  /**
   * Creates the initial nodes for all basic blocks in the passed functions.
   *
   * @param view The view where the nodes are created.
   *
   * @param passedFunctions All functions that lie on the path.
   * @param nodeMap Maps basic blocks of the functions on the path to their corresponding view
   *        nodes.
   * @param functionMap Keeps track to what function a node belongs to.
   *
   * @throws CouldntLoadDataException Thrown if a function could not be loaded.
   */
  private static void createInitialBlocks(final View view,
      final Collection<FunctionBlock> passedFunctions, final Map<BasicBlock, ViewNode> nodeMap,
      final Map<ViewNode, Function> functionMap) throws CouldntLoadDataException {
    for (final FunctionBlock functionBlock : passedFunctions) {
      final Function function = functionBlock.getFunction();

      if (function.getType() == FunctionType.Import) {
        // Imported functions to not have any basic blocks, for those functions
        // we simply create a function node.

        final FunctionNode newNode = view.createFunctionNode(function);

        functionMap.put(newNode, function);

        // TODO (timkornau): Assign a proper color to the node.
        // TODO (timkornau): Properly treat forwarded functions.
      }
 else {
        function.load();

        for (final BasicBlock block : function.getGraph().getNodes()) {
          final CodeNode newNode = view.createCodeNode(function, block.getInstructions());

          newNode.setColor(DEFAULT_BLOCK_COLOR);

          nodeMap.put(block, newNode);
          functionMap.put(newNode, function);
        }
      }
    }
  }

  /**
   * Creates view edges for all edges in the passed functions.
   *
   * @param view The view where the edges are created.
   * @param passedFunctions All functions that lie on the path.
   * @param nodeMap Maps between the basic blocks of the functions and their corresponding code
   *        nodes.
   */
  private static void createInitialEdges(final View view,
      final Collection<FunctionBlock> passedFunctions, final Map<BasicBlock, ViewNode> nodeMap) {
    for (final FunctionBlock functionBlock : passedFunctions) {
      final Function function = functionBlock.getFunction();

      for (final BlockEdge edge : function.getGraph().getEdges()) {
        final ViewEdge newEdge = view.createEdge(nodeMap.get(edge.getSource()),
            nodeMap.get(edge.getTarget()), edge.getType());
        newEdge.setColor(getEdgeColor(edge));
      }
    }
  }

  /**
   * Deletes all nodes that are not on the path.
   *
   * @param view View from which the nodes are deleted.
   * @param startNode Start node of the path.
   * @param targetNode Target node of the path.
   */
  private static void deleteNodesNotOnPath(final View view, final ViewNode startNode,
      final ViewNode targetNode) {
    final Set<ViewNode> succs = GraphAlgorithms.getSuccessors(startNode);
    final Set<ViewNode> preds = GraphAlgorithms.getPredecessors(targetNode);

    final HashSet<ViewNode> combined = new HashSet<ViewNode>(succs);
    combined.retainAll(preds);
    combined.add(startNode);
    combined.add(targetNode);

    final List<ViewNode> nodesToDelete = new ArrayList<ViewNode>();
    for (final ViewNode node : view.getGraph().getNodes()) {
      if (!combined.contains(node)) {
        nodesToDelete.add(node);
      }
    }

    for (final ViewNode node : nodesToDelete) {
      view.deleteNode(node);
    }
  }

  /**
   * Finds the node that represents a given function in a Call graph.
   *
   * @param callgraph The Call graph to search through.
   * @param function The function to search for.
   *
   * @return The Call graph node that represents the function.
   */
  private static FunctionBlock findBlock(final Callgraph callgraph, final Function function) {
    for (final FunctionBlock callgraphNode : callgraph) {
      if (function == callgraphNode.getFunction()) {
        return callgraphNode;
      }
    }

    throw new IllegalStateException("Error: Call graph node of unknown function");
  }

  /**
   * Finds the entry nodes and exit nodes of all functions that lie on the path. This is necessary
   * for function inlining.
   *
   * @param passedFunctions All functions that lie on the path.
   * @param nodeMap Maps between the basic blocks of the functions and their corresponding code
   *        nodes.
   * @param functionMap Keeps track to what function a view node belongs to.
   * @param entryNodes Keeps track of the entry nodes of all functions.
   * @param exitNodes Keeps track of the exit nodes of all functions.
   */
  private static void findEntryExitNodes(final Collection<FunctionBlock> passedFunctions,
      final Map<BasicBlock, ViewNode> nodeMap, final Map<ViewNode, Function> functionMap,
      final Map<Function, ViewNode> entryNodes,
      final ArrayListMultimap<Function, ViewNode> exitNodes) {
    // At first we find the entry and exist nodes for all functions which
    // actually do have basic blocks.
    for (final FunctionBlock functionBlock : passedFunctions) {
      final Function function = functionBlock.getFunction();

      if (function.getType() != FunctionType.Import) {
        entryNodes.put(function, nodeMap.get(findEntryNode(function)));
        for (final BasicBlock block : findExitNode(function.getGraph())) {
          exitNodes.put(function, nodeMap.get(block));
        }
      }
    }

    // Afterwards we find the entry and exit nodes of the imported functions.
    for (final Map.Entry<ViewNode, Function> p : functionMap.entrySet()) {
      final Function function = p.getValue();

      if (function.getType() == FunctionType.Import) {
        final ViewNode node = p.getKey();

        entryNodes.put(function, node);
        exitNodes.put(function, node);
      }
    }
  }

  private static BasicBlock findEntryNode(final Function function) {

    for (final BasicBlock block : function.getGraph()) {
      if (block.getAddress().equals(function.getAddress())) {
        return block;
      }
    }

    throw new IllegalStateException(
        "Error: The given function has no block with the same address as the function address "
        + "which is an illegal state");
  }

  /**
   * Finds the exit nodes of a graph.
   *
   * @param graph The graph whose exit node is determined.
   *
   * @return The exit node of the graph.
   */
  private static List<BasicBlock> findExitNode(final FlowGraph graph) {

    final ArrayList<BasicBlock> exitNodes = new ArrayList<BasicBlock>();

    for (final BasicBlock block : graph) {
      if (block.getChildren().size() == 0) {
        exitNodes.add(block);
      }
    }
    return exitNodes;
  }

  /**
   * Determines all functions that lie on all possible paths between a given start function and a
   * given target function.
   *
   * @param callgraph The Call graph that contains all function call information.
   * @param startFunction The start function of the path.
   * @param targetFunction The target function of the path.
   *
   * @return A set of all functions that are passed on the possible paths between the start function
   *         and the end function.
   */
  private static LinkedHashSet<FunctionBlock> findPassedFunctions(final Callgraph callgraph,
      final Function startFunction, final Function targetFunction) {
    // Find the graph nodes that correspond to the functions in the graph
    final FunctionBlock sourceCallgraphNode = findBlock(callgraph, startFunction);
    final FunctionBlock targetCallgraphNode = findBlock(callgraph, targetFunction);

    Logger.info("Source block: %s\n", sourceCallgraphNode.getFunction().getName());
    Logger.info("Target block: %s\n", targetCallgraphNode.getFunction().getName());

    // Passed functions = Intersection of the successors of the start function and the predecessors
    // of the target function.
    final Collection<FunctionBlock> successorFunctions =
        GraphAlgorithms.getSuccessors(sourceCallgraphNode);
    final Collection<FunctionBlock> predecessorFunctions =
        GraphAlgorithms.getPredecessors(targetCallgraphNode);

    final LinkedHashSet<FunctionBlock> sharedFunctions =
        new LinkedHashSet<FunctionBlock>(successorFunctions);
    sharedFunctions.retainAll(predecessorFunctions);

    sharedFunctions.add(sourceCallgraphNode);
    sharedFunctions.add(targetCallgraphNode);

    return sharedFunctions;
  }

  /**
   * Determines the view edge color for a block edge.
   *
   * @param edge The edge whose color is determined.
   *
   * @return The color of the edge.
   */
  private static Color getEdgeColor(final BlockEdge edge) {
    switch (edge.getType()) {
      case JumpConditionalTrue:
        return DEFAULT_TRUE_JUMP_COLOR;
      case JumpConditionalFalse:
        return DEFAULT_FALSE_JUMP_COLOR;
      default:
        return Color.BLACK;
    }
  }

  /**
   * Determines whether an operand expression or any of its children has a function call reference
   * to a given function.
   *
   * @param expression The operand expression to check.
   * @param function The called function to check for.
   *
   * @return True, if the operand expression or any of its children has a function call reference to
   *         the given function. False, otherwise.
   */
  private static boolean hasFunctionCallReference(final OperandExpression expression,
      final Function function) {
    // Each operand expression can have multiple references. If at least one
    // reference is a code reference to any of the functions, we have a match.

    final List<Reference> references = expression.getReferences();

    for (final Reference reference : references) {
      if ((reference != null) && ReferenceType.isCodeReference(reference.getType())) {
        if (function.getAddress().equals(reference.getTarget())) {
          return true;
        }
      }
    }

    for (final OperandExpression node : expression.getChildren()) {
      if (hasFunctionCallReference(node, function)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Splits a code node into two nodes at a function call. If the input node really is split, it is
   * removed from the view. If the input node is not split (because the calling instruction is the
   * last instruction of the code node) then the view nodes remain unchanged.
   *
   * @param view The view the code node belongs to.
   * @param function The function the code node belongs to.
   * @param node The node to split.
   * @param instruction The calling instruction after which the node is split.
   *
   * @return A node pair that contains the two new nodes or the input node and null if the input
   *         node was not split.
   */
  private static NodePair splitBlock(final View view, final Function function, final CodeNode node,
      final Instruction instruction) {
    boolean before = true;

    final List<Instruction> beforeInstructions = new ArrayList<Instruction>();
    final List<Instruction> afterInstructions = new ArrayList<Instruction>();

    for (final Instruction nodeInstruction : node.getInstructions()) {
      if (before) {
        beforeInstructions.add(nodeInstruction);
      } else {
        afterInstructions.add(nodeInstruction);
      }

      if (nodeInstruction == instruction) {
        before = false;
      }
    }

    if (afterInstructions.isEmpty()) {
      return new NodePair(node, null);
    } else {
      final CodeNode firstNode = view.createCodeNode(function, beforeInstructions);
      final CodeNode secondNode = view.createCodeNode(function, afterInstructions);

      firstNode.setColor(node.getColor());
      secondNode.setColor(DEFAULT_BLOCK_COLOR);

      for (final ViewEdge edge : node.getIncomingEdges()) {
        final ViewEdge newEdge = view.createEdge(edge.getSource(), firstNode, edge.getType());

        newEdge.setColor(edge.getColor());
      }

      for (final ViewEdge edge : node.getOutgoingEdges()) {
        final ViewEdge newEdge = view.createEdge(secondNode, edge.getTarget(), edge.getType());

        newEdge.setColor(edge.getColor());
      }

      view.deleteNode(node);

      return new NodePair(firstNode, secondNode);
    }
  }

  /**
   * Creates a view that shows all possible paths between two blocks of a module.
   *
   * @param module The module for which the view is created.
   * @param startBlock The basic block where the path begins (must be null if startFunction is not
   *        null).
   * @param targetBlock The basic block where the path ends (must be null if targetFunction is not
   *        null).
   * @param startFunction The function where the path starts (must be null if startBlock is not
   *        null).
   * @param targetFunction The function where the path ends (must be null if targetBlock is not
   *        null).
   *
   * @return The view that contains all possible paths between the start block and the target block.
   *
   * @throws CouldntLoadDataException
   * @throws PartialLoadException
   * @throws IllegalArgumentException
   */
  public static View createPath(final Module module, final BasicBlock startBlock,
      final BasicBlock targetBlock, final Function startFunction, final Function targetFunction)
      throws CouldntLoadDataException, PartialLoadException {
    Preconditions.checkNotNull(module, "Error: Module argument can't be null");
    Preconditions.checkArgument(module.isLoaded(), "Error: Module is not loaded");

    if ((startBlock == null) && (startFunction == null)) {
      throw new IllegalArgumentException("Error: No valid start given");
    }

    if ((targetBlock == null) && (targetFunction == null)) {
      throw new IllegalArgumentException("Error: No valid target given");
    }

    if ((startFunction != null) && !startFunction.isLoaded()) {
      throw new IllegalArgumentException("Error: Start function is not loaded");
    }

    if ((targetFunction != null) && !targetFunction.isLoaded()) {
      throw new IllegalArgumentException("Error: Target function is not loaded");
    }

    // The algorithm works like this:
    //
    // 1. Find all functions that lie between the start function and the target function.
    // 2. Insert all of these functions into the new view.
    // 3. Connect the individual functions at function calls and split code nodes if necessary.
    // 4. Determine what nodes are actually on the path by taking the successors of the start
    // node and set-unioning those with the predecessors of the target node.
    // 5. Delete all the nodes which are not on the path.

    // At first we determine the function where the path starts and the function where the path
    // ends.
    final Function realStartFunction =
        startFunction != null ? startFunction : startBlock.getParentFunction();
    final Function realTargetFunction =
        targetFunction != null ? targetFunction : targetBlock.getParentFunction();

    if (realStartFunction.getGraph().nodeCount() == 0) {
      throw new IllegalArgumentException(
          "Error: Functions with zero nodes can not be used for pathfinding");
    }

    // Determine the real start and end blocks of the path with the help of the function flow graphs
    final BasicBlock realStartBlock =
        startBlock != null ? startBlock : findEntryNode(realStartFunction);
    final BasicBlock realTargetBlock =
        targetBlock != null ? targetBlock : findEntryNode(realTargetFunction);

    // Find out what functions are called on the way between the first block and the second block.
    final LinkedHashSet<FunctionBlock> passedFunctions =
        findPassedFunctions(module.getCallgraph(), realStartFunction, realTargetFunction);

    // Create the view that represents the calculated path
    final String endAddress = realTargetBlock != null ? realTargetBlock.getAddress().toHexString()
        : realTargetFunction.getAddress().toHexString();
    final View view = module.createView("New Pathfinder View",
        String.format("%s -> %s", realStartBlock.getAddress().toHexString(), endAddress));

    view.load();

    // Maps basic blocks of functions to their corresponding node in the new view.
    final Map<BasicBlock, ViewNode> nodeMap = new HashMap<BasicBlock, ViewNode>();

    // Keeps track of the entry nodes for each function,
    final Map<Function, ViewNode> entryNodes = new HashMap<Function, ViewNode>();

    // Keeps track of the exit nodes for each function,
    final ArrayListMultimap<Function, ViewNode> exitNodes = ArrayListMultimap.create();

    // Keeps track of the function a view node belongs to.
    // TODO (timkornau): This should actually be accessible from the plug in API.
    final Map<ViewNode, Function> functionMap = new HashMap<ViewNode, Function>();

    // Create a code node for all basic blocks of the passed functions.
    createInitialBlocks(view, passedFunctions, nodeMap, functionMap);

    // Create view edges for all edges in the passed functions.
    createInitialEdges(view, passedFunctions, nodeMap);

    // Find the entry and exit nodes for all passed functions.
    findEntryExitNodes(passedFunctions, nodeMap, functionMap, entryNodes, exitNodes);

    ViewNode startNode = nodeMap.get(realStartBlock);
    ViewNode targetNode =
        realTargetBlock == null ? entryNodes.get(realTargetFunction) : nodeMap.get(realTargetBlock);

    startNode.setColor(Color.GREEN);
    targetNode.setColor(Color.YELLOW);

    // Connect the nodes of the different functions on function calls.
    final NodePair splitResult = connectFunctions(view,
        startNode,
        targetNode,
        passedFunctions,
        entryNodes,
        exitNodes,
        functionMap);

    startNode = splitResult.getFirst();
    targetNode = splitResult.getSecond();

    // We can safely delete all the edges that leave the target block because
    // all paths end here.
    for (final ViewEdge edge : targetNode.getOutgoingEdges()) {
      view.deleteEdge(edge);
    }

    // Delete all nodes that are not on the path.
    deleteNodesNotOnPath(view, startNode, targetNode);

    if (startNode.getOutgoingEdges().isEmpty()) {
      // no path exists between the two nodes
      return null;
    }

    try {
      view.save();
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
    }

    return view;
  }

  /**
   * Small helper class for returning two nodes from functions.
   */
  private static class NodePair {
    private final ViewNode m_first;
    private final ViewNode m_second;

    public NodePair(final ViewNode first, final ViewNode second) {
      m_first = first;
      m_second = second;
    }

    public ViewNode getFirst() {
      return m_first;
    }

    public ViewNode getSecond() {
      return m_second;
    }
  }
}
