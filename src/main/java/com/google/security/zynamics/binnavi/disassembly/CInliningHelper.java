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
package com.google.security.zynamics.binnavi.disassembly;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CInliningResult;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.general.Triple;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.types.graphs.DirectedGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.grouping.GroupHelpers;

/**
 * Helper class for inlining functions.
 */
public final class CInliningHelper {
  /**
   * Private constructor because this class is a static helper class.
   */
  private CInliningHelper() {
    // You are not supposed to instantiate this class
  }

  /**
   * Creates the view edges of the inlined function.
   * 
   * @param view The view where the inlining operation takes place.
   * @param edges The edges of the inlined function.
   * @param map Maps between the basic blocks of the inlined function and their code nodes in the
   *        view.
   */
  private static void createEdges(final INaviView view, final List<IBlockEdge> edges,
      final Map<IBlockNode, CCodeNode> map) {
    for (final IBlockEdge edge : edges) {
      final CCodeNode sourceNode = map.get(edge.getSource());
      final CCodeNode targetNode = map.get(edge.getTarget());

      view.getContent().createEdge(sourceNode, targetNode, edge.getType());
    }
  }

  /**
   * Creates a code node that represents a block node.
   * 
   * @param view The view where the inlining operation takes place.
   * @param function The inlined function.
   * @param blockNode The block node for which a code node is created.
   * @param map Maps between the basic blocks of the inlined function and their code nodes in the
   *        view.
   * @param parentGroup Parent group the created code nodes are added to.
   */
  private static void createNode(final INaviView view, final INaviFunction function,
      final IBlockNode blockNode, final Map<IBlockNode, CCodeNode> map,
      final INaviGroupNode parentGroup) {
    final List<INaviInstruction> instructions = new ArrayList<INaviInstruction>();

    final List<CCodeNode> createNodes = new ArrayList<CCodeNode>();

    final INaviBasicBlock block = blockNode.getBlock();

    for (final INaviInstruction instruction : block) {
      instructions.add(instruction.cloneInstruction());
    }

    if (!instructions.isEmpty()) {
      final CCodeNode node = view.getContent().createCodeNode(function, instructions);

      node.setColor(new Color(221, 234, 244));

      if (node.getAddress().equals(function.getAddress()) && (blockNode.getChildren().size() == 0)) {
        node.setBorderColor(new Color(-6250496));
      } else if (node.getAddress().equals(function.getAddress())) {
        node.setBorderColor(new Color(-16736256));
      } else if (blockNode.getChildren().size() == 0) {
        node.setBorderColor(new Color(-6291456));
      }

      if (parentGroup != null) {
        parentGroup.addElement(node);
      }

      createNodes.add(node);

      map.put(blockNode, node);
    }
  }

  /**
   * Creates code nodes for the basic blocks of the inlined function.
   * 
   * @param view The view where the inlining operation takes place.
   * @param function The inlined function.
   * @param parentGroup Parent group the created code nodes are added to.
   * 
   * @return Maps between the basic blocks of the inlined function and their code nodes in the view.
   */
  private static Map<IBlockNode, CCodeNode> createNodes(final INaviView view,
      final INaviFunction function, final INaviGroupNode parentGroup) {
    final LinkedHashMap<IBlockNode, CCodeNode> map = new LinkedHashMap<IBlockNode, CCodeNode>();

    for (final IBlockNode blockNode : function.getBasicBlocks()) {
      createNode(view, function, blockNode, map, parentGroup);
    }

    return map;
  }

  /**
   * Finds the outgoing code references of an operand tree node and its children.
   * 
   * @param node The operand tree node to check.
   * @param references The outgoing code references of that instruction.
   */
  private static void getCodeReference(final IOperandTreeNode node,
      final List<IReference> references) {
    final List<IReference> nodeReferences = node.getReferences();

    for (final IReference reference : nodeReferences) {
      if ((reference != null) && ReferenceType.isCodeReference(reference.getType())) {
        references.add(reference);
        break;
      }
    }

    for (final IOperandTreeNode child : node.getChildren()) {
      getCodeReference(child, references);
    }
  }

  /**
   * Finds the outgoing code references of an instruction.
   * 
   * @param instruction The instruction to check.
   * 
   * @return The outgoing code references of that instruction.
   */
  private static List<IReference> getCodeReferences(final INaviInstruction instruction) {
    final List<IReference> references = new ArrayList<IReference>();

    for (final IOperandTree operand : instruction.getOperands()) {
      getCodeReference(operand.getRootNode(), references);
    }

    return references;
  }

  /**
   * Finds special nodes from the list of inlined nodes.
   * 
   * @param function The function that was inlined.
   * @param nodes The nodes created during inlining.
   * 
   * @return <Entry Node, Exit Nodes, All Inserted Nodes>
   */
  private static Triple<CCodeNode, List<CCodeNode>, ArrayList<CCodeNode>> getRelevantNodes(
      final INaviFunction function, final ArrayList<CCodeNode> nodes) {
    final List<CCodeNode> exitNodes = new ArrayList<CCodeNode>();

    CCodeNode entryNode = null;

    for (final CCodeNode node : nodes) {
      if (node.getOutgoingEdges().isEmpty()) {
        exitNodes.add(node);
      }

      if (node.getAddress().equals(function.getAddress())) {
        entryNode = node;
      }
    }

    return new Triple<CCodeNode, List<CCodeNode>, ArrayList<CCodeNode>>(entryNode, exitNodes, nodes);
  }

  /**
   * From a list of nodes, those function nodes are returned that represent the target of a given
   * reference.
   * 
   * @param reference The reference.
   * @param graphNodes The nodes to search through.
   * 
   * @return The nodes that represent the reference target.
   */
  private static List<INaviViewNode> getTargetNode(final IReference reference,
      final List<INaviViewNode> graphNodes) {
    final List<INaviViewNode> nodes = new ArrayList<INaviViewNode>();

    for (final INaviViewNode node : graphNodes) {
      if (node instanceof INaviFunctionNode) {
        final INaviFunctionNode fnode = (INaviFunctionNode) node;

        if (fnode.getFunction().getAddress().equals(reference.getTarget())) {
          nodes.add(node);
        }
      }
    }

    return nodes;
  }

  /**
   * Inserts the nodes and edges of a function into a given view.
   * 
   * @param view The view where the function is inserted.
   * @param function The function to insert.
   * @param parentGroup Group the created nodes are added to.
   * 
   * @return <Entry Node, Exit Nodes, All Inserted Nodes>
   */
  private static Triple<CCodeNode, List<CCodeNode>, ArrayList<CCodeNode>> insertNodes(
      final INaviView view, final INaviFunction function, final INaviGroupNode parentGroup) {
    final DirectedGraph<IBlockNode, IBlockEdge> graph = function.getGraph();

    final Map<IBlockNode, CCodeNode> map = createNodes(view, function, parentGroup);

    createEdges(view, graph.getEdges(), map);

    return getRelevantNodes(function, Lists.newArrayList(map.values()));
  }

  /**
   * Inlines the basic blocks of a function into a code node.
   * 
   * @param view The view where the inlining operation takes place.
   * @param originalNode The node where the inlining operation takes place.
   * @param inlineInstruction The function call instruction after which the function is inlined.
   * @param functionToInline The function to be inlined.
   * 
   * @return Contains information about the inlining result.
   */
  public static CInliningResult inlineCodeNode(final INaviView view,
      final INaviCodeNode originalNode, final INaviInstruction inlineInstruction,
      final INaviFunction functionToInline) {

    Preconditions.checkNotNull(view, "IE00108: View argument can not be null");
    Preconditions.checkNotNull(originalNode, "IE00109: Node argument can not be null");
    Preconditions.checkNotNull(inlineInstruction, "IE00110: Instruction argument can not be null");
    Preconditions.checkArgument(originalNode.hasInstruction(inlineInstruction),
        "IE00111: Instruction is not part of the code node");
    Preconditions.checkNotNull(functionToInline, "IE00112: Function argument can not be null");

    Preconditions.checkArgument(view.isLoaded(),
        "IE00113: View must be loaded before it can be inlined");
    Preconditions.checkArgument(view.getGraph().getNodes().contains(originalNode),
        "IE00114: Code node does not belong to the view");
    Preconditions.checkArgument(functionToInline.isLoaded(),
        "IE00115: Function must be loaded before it can be inlined");
    Preconditions.checkArgument(functionToInline.getBasicBlockCount() != 0,
        "IE00116: Functions with 0 blocks can not be inlined");

    final INaviGroupNode parentGroup = originalNode.getParentGroup();
    GroupHelpers.expandParents(originalNode);

    final List<INaviEdge> oldIncomingEdges = originalNode.getIncomingEdges();
    final List<INaviEdge> oldOutgoingEdges = originalNode.getOutgoingEdges();

    // At first we find out which instructions will be part of the new first block
    // and which instructions will be part of the new second block.

    final List<INaviInstruction> upperInstructions = new ArrayList<INaviInstruction>();
    final List<INaviInstruction> lowerInstructions = new ArrayList<INaviInstruction>();

    List<INaviInstruction> currentBlock = upperInstructions;

    for (final INaviInstruction currentInstruction : originalNode.getInstructions()) {
      currentBlock.add(currentInstruction);

      if (currentInstruction == inlineInstruction) {
        currentBlock = lowerInstructions;
      }
    }

    // Now we create the new nodes from the instructions blocks
    INaviCodeNode firstNode;
    final List<INaviViewNode> continueNodes = new ArrayList<INaviViewNode>();

    final boolean keepOriginalBlock = lowerInstructions.isEmpty();

    CCodeNode returnNode = null;

    if (keepOriginalBlock) {
      // There are no instructions in the second block => therefore the call instruction
      // is the last instruction of the block => therefore no splitting is necessary =>
      // therefore we can just reuse the original block.

      firstNode = originalNode;

      for (final INaviEdge edge : originalNode.getOutgoingEdges()) {
        continueNodes.add(edge.getTarget());

        view.getContent().deleteEdge(edge);
      }
    } else {
      // The second block is not empty => the call instruction is somewhere in the middle =>
      // the block must be split => the original block becomes useless and must be replaced by
      // two new blocks.

      final boolean recolor =
          (originalNode.getIncomingEdges().size() == 1)
              && (originalNode.getIncomingEdges().get(0).getType() == EdgeType.ENTER_INLINED_FUNCTION)
              && (originalNode.getOutgoingEdges().size() == 1)
              && (originalNode.getOutgoingEdges().get(0).getType() == EdgeType.LEAVE_INLINED_FUNCTION);

      view.getContent().deleteNode(originalNode);

      try {
        firstNode =
            view.getContent().createCodeNode(originalNode.getParentFunction(), upperInstructions);
      } catch (final MaybeNullException exception) {
        firstNode = view.getContent().createCodeNode(null, upperInstructions);
      }

      firstNode.setColor(originalNode.getColor());
      firstNode.setBorderColor(originalNode.getBorderColor());

      try {
        returnNode =
            view.getContent().createCodeNode(originalNode.getParentFunction(), lowerInstructions);
      } catch (final MaybeNullException e1) {
        returnNode = view.getContent().createCodeNode(null, lowerInstructions);
      }

      returnNode.setColor(originalNode.getColor());

      if (recolor) {
        firstNode.setBorderColor(new Color(-16736256));
        returnNode.setBorderColor(new Color(-6291456));
      }

      if (parentGroup != null) {
        parentGroup.addElement(firstNode);
        parentGroup.addElement(returnNode);
      }

      // Copy the tags of the original node too
      final Iterator<CTag> it = originalNode.getTagsIterator();
      while (it.hasNext()) {
        final CTag tag = it.next();
        try {
          firstNode.tagNode(tag);
          returnNode.tagNode(tag);
        } catch (final CouldntSaveDataException e) {
          CUtilityFunctions.logException(e);
        }
      }

      continueNodes.add(returnNode);
    }

    // Insert the nodes and edges from the loaded function
    final Triple<CCodeNode, List<CCodeNode>, ArrayList<CCodeNode>> nodes =
        insertNodes(view, functionToInline, parentGroup);

    final INaviCodeNode entryNode = nodes.first();
    final List<CCodeNode> exitNodes = nodes.second();

    if (!keepOriginalBlock) {
      for (final INaviEdge incomingEdge : oldIncomingEdges) {
        // Create edges from the parent nodes of the original block to the upper
        // part of the split block.

        if (incomingEdge.getSource() == originalNode) {
          final EdgeType edgeType = incomingEdge.getType();

          view.getContent().createEdge(returnNode, firstNode, edgeType);
        } else {
          final EdgeType edgeType = incomingEdge.getType();

          view.getContent().createEdge(incomingEdge.getSource(), firstNode, edgeType);
        }
      }
    }

    // Create an edge from the upper part of the split block to the entry node
    // of the inlined function.
    view.getContent().createEdge(firstNode, entryNode, EdgeType.ENTER_INLINED_FUNCTION);

    // Create edges between all exit nodes of the inlined function and the nodes
    // of the original function where control flow continues.
    for (final INaviCodeNode exitNode : exitNodes) {
      for (final INaviViewNode continueNode : continueNodes) {
        view.getContent().createEdge(exitNode, continueNode, EdgeType.LEAVE_INLINED_FUNCTION);
      }
    }

    if (!keepOriginalBlock) {
      for (final INaviEdge oldChild : oldOutgoingEdges) {
        for (final INaviViewNode continueNode : continueNodes) {
          // Create edges between the lower half of the split block and the
          // child blocks of the original block.

          if (oldChild.getTarget() != originalNode) {
            view.getContent().createEdge(continueNode, oldChild.getTarget(), oldChild.getType());
          }
        }
      }
    }

    return new CInliningResult(firstNode, returnNode);
  }

  /**
   * Replaces a function node in a view with the basic blocks of the functions represented by the
   * function node.
   * 
   * @param view View where the inline operation takes place.
   * @param node Node that is inlined.
   */
  public static void inlineFunctionNode(final INaviView view, final INaviFunctionNode node) {
    Preconditions.checkNotNull(view, "IE00119: View argument can not be null");
    Preconditions.checkNotNull(node, "IE00120: Node argument can not be null");
    Preconditions.checkArgument(view.isLoaded(),
        "IE00122: View must be loaded before it can be inlined");
    Preconditions.checkArgument(view.getGraph().getNodes().contains(node),
        "IE00123: Code node does not belong to the view");
    Preconditions.checkArgument(node.getFunction().isLoaded(),
        "IE00124: Function must be loaded before it can be inlined");
    Preconditions.checkArgument(node.getFunction().getBasicBlockCount() != 0,
        "IE00125: Functions with 0 blocks can not be inlined");

    GroupHelpers.expandParents(node);

    final INaviGroupNode parentGroup = node.getParentGroup();

    final List<INaviEdge> oldIncomingEdges = node.getIncomingEdges();

    view.getContent().deleteNode(node);

    // Insert the nodes and edges from the loaded function
    final Triple<CCodeNode, List<CCodeNode>, ArrayList<CCodeNode>> nodes =
        CInliningHelper.insertNodes(view, node.getFunction(), parentGroup);

    final INaviCodeNode entryNode = nodes.first();
    final ArrayList<CCodeNode> returnNodes = nodes.third();

    for (final INaviEdge incomingEdge : oldIncomingEdges) {
      // Create edges from the parent nodes of the original block to entry node
      // of the inlined function.

      final EdgeType edgeType = incomingEdge.getType();

      view.getContent().createEdge(incomingEdge.getSource(), entryNode, edgeType);
    }

    final List<INaviViewNode> graphNodes = view.getGraph().getNodes();

    for (final INaviCodeNode newNode : returnNodes) {
      newNode.setX(node.getX());
      newNode.setY(node.getY());

      for (final INaviInstruction instruction : newNode.getInstructions()) {
        final List<IReference> references = getCodeReferences(instruction);

        for (final IReference reference : references) {
          final List<INaviViewNode> targetNodes = getTargetNode(reference, graphNodes);

          for (final INaviViewNode targetNode : targetNodes) {
            view.getContent().createEdge(newNode, targetNode, EdgeType.JUMP_UNCONDITIONAL);
          }
        }
      }
    }
  }
}
