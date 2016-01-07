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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.disassembly.CInliningHelper;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CInliningResult;

// ! Offers convenience functions for working with view graphs.
/**
 * Offers convenience functions for working with view graphs. Please note that many convenience
 * functions are just straight-forward implementations of commonly used algorithms and therefore can
 * have significant runtime costs.
 */
public final class ViewGraphHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private ViewGraphHelpers() {
  }

  // ! Finds a code node with a given address.
  /**
   * Returns the code node of a view graph that starts at a given address. Since addresses do not
   * uniquely identify code nodes it is possible that there is more than one code node with the
   * given address in the graph. In case of multiple code nodes that start at the given address it
   * is undefined exactly which of those code nodes is returned.
   *
   *  This function is guaranteed to work in O(n) where n is the number of nodes in the graph.
   *
   * @param graph The graph to search through.
   * @param address The address to search for.
   *
   * @return The code node that starts at the given address or null if there is no such code node.
   */
  public static CodeNode getCodeNode(final ViewGraph graph, final Address address) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    for (final ViewNode node : graph) {
      if (node instanceof CodeNode) {
        final CodeNode cnode = (CodeNode) node;

        if (cnode.getAddress().equals(address)) {
          return cnode;
        }
      }
    }

    return null;
  }

  // ! Finds a code node with given address.
  /**
   * Returns the code node of a view graph that starts at a given address. Since addresses do not
   * uniquely identify code nodes it is possible that there is more than one code node with the
   * given address in the graph. In case of multiple code nodes that start at the given address it
   * is undefined exactly which of those code nodes is returned.
   *
   *  This function is guaranteed to work in O(n) where n is the number of nodes in the graph.
   *
   * @param graph The graph to search through.
   * @param address The address to search for.
   *
   * @return The code node that starts at the given address or null if there is no such code node.
   */
  public static CodeNode getCodeNode(final ViewGraph graph, final long address) {
    return getCodeNode(graph, new Address(address));
  }

  // ! Returns the code nodes of the graph.
  /**
   * Returns a list of all code nodes of a graph.
   *
   *  This function is guaranteed to work in O(n) where n is the number of nodes in the graph.
   *
   * @param graph The graph to search through.
   *
   * @return The code nodes that are part of the graph.
   */
  public static List<CodeNode> getCodeNodes(final ViewGraph graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    final List<CodeNode> codeNodes = new ArrayList<CodeNode>();

    for (final ViewNode node : graph) {
      if (node instanceof CodeNode) {
        codeNodes.add((CodeNode) node);
      }
    }

    return codeNodes;
  }

  // ! Finds the function node with a given function name.
  /**
   * Returns the function node of a view graph that represents a function of a given name. Since
   * names do not uniquely identify function nodes it is possible that there is more than one
   * function node with the given name in the graph. In case of multiple function nodes with that
   * name it is undefined exactly which of those function nodes is returned.
   *
   *  This function is guaranteed to work in O(n) where n is the number of nodes in the graph.
   *
   * @param graph The graph to search through.
   * @param functionName The name of the function to search for.
   *
   * @return The function node that represents a function with the given name or null if there is no
   *         such function node.
   */
  public static FunctionNode getFunctionNode(final ViewGraph graph, final String functionName) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    Preconditions.checkNotNull(functionName, "Error: Function name argument can not be null");

    for (final ViewNode viewNode : graph) {
      if (viewNode instanceof FunctionNode) {
        final FunctionNode fnode = (FunctionNode) viewNode;

        if (fnode.getFunction().getName().equals(functionName)) {
          return fnode;
        }
      }
    }

    return null;
  }

  // ! Returns the function nodes of a graph.
  /**
   * Returns a list of all function nodes of a graph.
   *
   *  This function is guaranteed to work in O(n) where n is the number of nodes in the graph.
   *
   * @param graph The graph to search through.
   *
   * @return The function nodes that are part of the graph.
   */
  public static List<FunctionNode> getFunctionNodes(final ViewGraph graph) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    final List<FunctionNode> functionNodes = new ArrayList<FunctionNode>();

    for (final ViewNode node : graph) {
      if (node instanceof FunctionNode) {
        functionNodes.add((FunctionNode) node);
      }
    }

    return functionNodes;
  }

  // ! Finds an instruction with a given address.
  /**
   * Returns the instruction of a view graph that starts at a given address. Since addresses do not
   * uniquely identify instructions it is possible that there is more than one instruction with the
   * given address in the graph. In case of multiple instructions that start at the given address it
   * is undefined exactly which of those instructions is returned.
   *
   *  This function is guaranteed to work in O(m + n) where m is the number of nodes in the graph
   * and n is the number of instructions in the graph.
   *
   * @param graph The graph to search through.
   * @param address The address to search for.
   *
   * @return The instruction that starts at the given address or null if there is no such
   *         instruction.
   */
  public static Instruction getInstruction(final ViewGraph graph, final Address address) {
    Preconditions.checkNotNull(graph, "Error: Graph argument can not be null");

    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    for (final ViewNode node : graph) {
      if (node instanceof CodeNode) {
        final CodeNode cnode = (CodeNode) node;

        for (final Instruction instruction : cnode.getInstructions()) {
          if (instruction.getAddress().equals(address)) {
            return instruction;
          }
        }
      }
    }

    return null;
  }

  // ! Finds an instruction with a given address.
  /**
   * Returns the instruction of a view graph that starts at a given address. Since addresses do not
   * uniquely identify instructions it is possible that there is more than one instruction with the
   * given address in the graph. In case of multiple instructions that start at the given address it
   * is undefined exactly which of those instructions is returned.
   *
   *  This function is guaranteed to work in O(m + n) where m is the number of nodes in the graph
   * and n is the number of instructions in the graph.
   *
   * @param graph The graph to search through.
   * @param address The address to search for.
   *
   * @return The instruction that starts at the given address or null if there is no such
   *         instruction.
   */
  public static Instruction getInstruction(final ViewGraph graph, final long address) {
    return getInstruction(graph, new Address(address));
  }

  // ! Inlines a function call into a code node.
  /**
   * Inserts the code nodes of a function into a view and splits an existing code node (if
   * necessary) to call the function.
   *
   * @param view The view where the inlining operation takes place.
   * @param codeNode The code node that is split.
   * @param instruction The sub-function call instruction.
   * @param function The function to be inlined.
   *
   * @return Gives information about the inlining process.
   */
  public static InliningResult inlineFunctionCall(final View view, final CodeNode codeNode,
      final Instruction instruction, final Function function) {
    Preconditions.checkNotNull(view, "Error: View argument can not be null");

    Preconditions.checkNotNull(codeNode, "Error: Code node argument can not be null");

    Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null");

    Preconditions.checkNotNull(function, "Error: Function argument can not be null");

    final CInliningResult result =
        CInliningHelper.inlineCodeNode(view.getNative(), codeNode.getNative(), instruction.getNative(), function.getNative());

    final CodeNode firstNode =
        (CodeNode) ObjectFinders.getObject(result.getFirstNode(), view.getGraph().getNodes());
    final CodeNode secondNode = (CodeNode) (result.getSecondNode() == null ? null : ObjectFinders
        .getObject(result.getSecondNode(), view.getGraph().getNodes()));

    return new InliningResult(firstNode, secondNode);
  }
}
