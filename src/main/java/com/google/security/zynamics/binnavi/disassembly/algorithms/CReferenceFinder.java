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
package com.google.security.zynamics.binnavi.disassembly.algorithms;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Helper class for finding outgoing references.
 */
public final class CReferenceFinder {
  /**
   * You are not supposed to instantiate this class.
   */
  private CReferenceFinder() {
  }

  /**
   * Searches for outgoing references of a tree node and its children.
   * 
   * @param node The start node of the search,
   * @param instruction The instruction the node belongs to.
   * @param functions List where the found references are stored.
   */
  private static void fetchReferenceList(final IOperandTreeNode node,
      final INaviInstruction instruction,
      final List<Pair<INaviInstruction, INaviFunction>> functions) {
    final List<IReference> references = node.getReferences();

    for (final IReference reference : references) {
      if (ReferenceType.isCodeReference(reference.getType())) {
        final IAddress target = reference.getTarget();

        final INaviFunction function =
            instruction.getModule().getContent().getFunctionContainer().getFunction(target);

        if (function != null) {
          functions.add(new Pair<INaviInstruction, INaviFunction>(instruction, function));
        }
      }
    }

    for (final IOperandTreeNode child : node.getChildren()) {
      fetchReferenceList(child, instruction, functions);
    }
  }

  /**
   * Fetch for outgoing code references of a tree node and all of its children.
   * 
   * @param node The start node of the search.
   * @param instruction The instruction the operand tree belongs to.
   * @param functions The map of code references.
   */
  private static void fetchReferenceMap(final IOperandTreeNode node,
      final INaviInstruction instruction, final Map<INaviInstruction, INaviFunction> functions) {
    final List<IReference> references = node.getReferences();

    for (final IReference reference : references) {
      if (ReferenceType.isCodeReference(reference.getType())) {
        final IAddress target = reference.getTarget();

        final INaviFunction function =
            instruction.getModule().getContent().getFunctionContainer().getFunction(target);

        if (function != null) {
          functions.put(instruction, function);
        }
      }
    }

    for (final IOperandTreeNode child : node.getChildren()) {
      fetchReferenceMap(child, instruction, functions);
    }
  }

  /**
   * Finds all outgoing code references of a code node that go to the beginning of a function.
   * 
   * @param node The node to search through.
   * 
   * @return A list of <instruction, function> pairs of outgoing code references.
   */
  public static List<Pair<INaviInstruction, INaviFunction>> getCodeReferenceList(
      final INaviCodeNode node) {
    final List<Pair<INaviInstruction, INaviFunction>> functions =
        new ArrayList<Pair<INaviInstruction, INaviFunction>>();

    for (final INaviInstruction instruction : node.getInstructions()) {
      for (final IOperandTree operand : instruction.getOperands()) {
        fetchReferenceList(operand.getRootNode(), instruction, functions);
      }
    }

    return functions;
  }

  /**
   * Finds all outgoing code references to other functions for the given code node.
   * 
   * @param node The node to search through.
   * @return A map which associates instructions with their code references (i.e. functions).
   */
  public static HashMap<INaviInstruction, INaviFunction> getCodeReferenceMap(
      final INaviCodeNode node) {
    final HashMap<INaviInstruction, INaviFunction> references =
        new HashMap<INaviInstruction, INaviFunction>();

    for (final INaviInstruction instruction : node.getInstructions()) {
      for (final IOperandTree operand : instruction.getOperands()) {
        fetchReferenceMap(operand.getRootNode(), instruction, references);
      }
    }

    return references;
  }
}
