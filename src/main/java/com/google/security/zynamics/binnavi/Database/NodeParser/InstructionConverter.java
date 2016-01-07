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
package com.google.security.zynamics.binnavi.Database.NodeParser;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Turns instruction data from the database into a proper instruction object.
 */
public final class InstructionConverter {

  private InstructionConverter() {
    // You are not supposed to instantiate this class
  }

  /**
   * Converts a raw operand tree into a proper operand tree.
   *
   * @param rawTree The raw operand tree.
   * @param provider The connection to the database.
   * @param module
   *
   * @return The proper operand tree.
   */
  private static COperandTree generateTree(
      final OperandTree rawTree, final SQLProvider provider, final INaviModule module) {
    final ArrayList<COperandTreeNode> realNodes = new ArrayList<COperandTreeNode>();

    final HashMap<COperandTreeNode, OperandTreeNode> realToRawMapping =
        new HashMap<COperandTreeNode, OperandTreeNode>();
    final HashMap<Integer, COperandTreeNode> idToRealMapping =
        new HashMap<Integer, COperandTreeNode>();

    COperandTreeNode root = null;

    final TypeManager typeManager = module.getTypeManager();
    final TypeInstanceContainer instanceContainer = module.getContent().getTypeInstanceContainer();
    for (final OperandTreeNode rawNode : rawTree.getNodes()) {
      final COperandTreeNode node = new COperandTreeNode(
          rawNode.getId(), rawNode.getType(), rawNode.getValue(), rawNode.getReplacement(),
          rawNode.getReferences(), provider, typeManager, instanceContainer);
      if (rawNode.getTypeSubstitution() != null) {
        typeManager.initializeTypeSubstitution(node, rawNode.getTypeSubstitution());
      }
      if (rawNode.getTypeInstanceId() != null) {
        instanceContainer.initializeTypeInstanceReference(
            rawNode.getAddress(), rawNode.getPosition(), rawNode.getId(), node);
      }

      realToRawMapping.put(node, rawNode);
      idToRealMapping.put(rawNode.getId(), node);
      if (rawNode.getParentId() == null) {
        root = node;
      }
      realNodes.add(node);
    }

    for (final COperandTreeNode realNode : realNodes) {
      // Link the real nodes here.
      // To link two real nodes, it is necessary to know
      // which node is the parent and which node is the
      // child.

      final OperandTreeNode rawNode = realToRawMapping.get(realNode);
      final Integer parentId = rawNode.getParentId();
      if (parentId == null) {
        continue;
      }
      final COperandTreeNode realParent = idToRealMapping.get(parentId);
      COperandTreeNode.link(realParent, realNode);
    }
    return new COperandTree(root, provider, typeManager, instanceContainer);
  }

  /**
   * This line creates an instruction from the information of a raw instruction line.
   *
   * @param line The instruction line.
   * @param provider The connection to the database.
   *
   * @return The instruction object generated from the instruction line.
   */
  public static CInstruction createInstruction(
      final InstructionLine line, final SQLProvider provider) {
    final ArrayList<COperandTree> operands = new ArrayList<COperandTree>();

    final INaviModule module = line.getModule();
    for (final OperandTree rawTree : line.getOperands()) {
      operands.add(generateTree(rawTree, provider, module));
    }

    final IAddress address = line.getAddress();
    final String mnemonic = line.getMnemonic();
    final String architecture = line.getArchitecture();

    final CInstruction instruction = new CInstruction(
        true, module, address, mnemonic, operands, line.getData(), architecture, provider);

    return instruction;
  }
}
