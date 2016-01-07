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
package com.google.security.zynamics.binnavi.debug.connection.packets.commands.conditions;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNodeSwitcher;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNodeSwitcher.NodeSwitcher;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ExpressionNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.FormulaNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.IdentifierNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.MemoryNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.NumberNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.RelationNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.SubNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;
import com.google.security.zynamics.zylib.general.ByteHelpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class that flattens a breakpoint condition tree into a byte array.
 */
public final class ConditionTreeFlattener {
  /**
   * Identifier of expression nodes.
   */
  private static final int ID_EXPRESSION_NODE = 0;

  /**
   * Identifier of formula nodes.
   */
  private static final int ID_FORMULA_NODE = 1;

  /**
   * Identifier of identifier nodes.
   */
  private static final int ID_IDENTIFIER_NODE = 2;

  /**
   * Identifier of memory nodes.
   */
  private static final int ID_MEMORY_NODE = 3;

  /**
   * Identifier of number nodes.
   */
  private static final int ID_NUMBER_NODE = 4;

  /**
   * Identifier of relation nodes.
   */
  private static final int ID_RELATION_NODE = 5;

  /**
   * Identifier of sub nodes.
   */
  private static final int ID_SUB_NODE = 6;

  /**
   * You are not supposed to instantiate this class.
   */
  private ConditionTreeFlattener() {}

  /**
   * Appends a byte array to a byte list.
   *
   * @param list The list to extend.
   * @param data The data to append.
   */
  private static void addAll(final List<Byte> list, final byte[] data) {
    for (final byte b : data) {
      list.add(b);
    }
  }

  /**
   * Flattens a condition tree into a byte list.
   *
   * @param root The root node of the condition tree.
   * @param nodeIds Maps condition node objects onto their integer identifiers in the flattened
   *        trees.
   *
   * @return The byte list that contains the flattened tree.
   */
  private static List<Byte> flatten(final ConditionNode root,
      final Map<ConditionNode, Integer> nodeIds) {
    final List<Byte> flattenedTree = new ArrayList<Byte>();

    addAll(flattenedTree, getType(root));

    final List<Byte> payload = getPayload(root);

    addAll(flattenedTree, ByteHelpers.toBigEndianDword(payload.size()));

    flattenedTree.addAll(payload);

    addAll(flattenedTree, ByteHelpers.toBigEndianDword(root.getChildren().size()));

    for (final ConditionNode child : root.getChildren()) {
      addAll(flattenedTree, ByteHelpers.toBigEndianDword(nodeIds.get(child)));
    }

    for (final ConditionNode child : root.getChildren()) {
      flattenedTree.addAll(flatten(child, nodeIds));
    }

    return flattenedTree;
  }

  /**
   * Returns the flattened node payload for a node.
   *
   * @param root The node whose flattened payload is returned.
   *
   * @return The flattened payload.
   */
  private static List<Byte> getPayload(final ConditionNode root) {
    final List<Byte> data = new ArrayList<Byte>();

    ConditionNodeSwitcher.process(root, new NodeSwitcher<Void>() {
      @Override
      public Void process(final ExpressionNode node) {
        addAll(data, node.getOperator().getBytes());
        return null;
      }

      @Override
      public Void process(final FormulaNode node) {
        addAll(data, node.getOperator().getBytes());
        return null;
      }

      @Override
      public Void process(final IdentifierNode node) {
        addAll(data, node.getName().getBytes());
        return null;
      }

      @Override
      public Void process(final MemoryNode node) {
        return null;
      }

      @Override
      public Void process(final NumberNode node) {
        addAll(data, ByteHelpers.toBigEndianDword(node.getValue()));
        return null;
      }

      @Override
      public Void process(final RelationNode node) {
        addAll(data, node.getOperator().getBytes());
        return null;
      }

      @Override
      public Void process(final SubNode node) {
        return null;
      }
    });

    return data;
  }

  /**
   * Returns the flattened type identifier of a node.
   *
   * @param node The node whose flattened identifier is returned.
   *
   * @return The flattened identifier of the node.
   */
  private static byte[] getType(final ConditionNode node) {
    return ByteHelpers.toBigEndianDword(
        ConditionNodeSwitcher.process(node, new NodeSwitcher<Integer>() {
          @Override
          public Integer process(final ExpressionNode node) {
            return ID_EXPRESSION_NODE;
          }

          @Override
          public Integer process(final FormulaNode node) {
            return ID_FORMULA_NODE;
          }

          @Override
          public Integer process(final IdentifierNode node) {
            return ID_IDENTIFIER_NODE;
          }

          @Override
          public Integer process(final MemoryNode node) {
            return ID_MEMORY_NODE;
          }

          @Override
          public Integer process(final NumberNode node) {
            return ID_NUMBER_NODE;
          }

          @Override
          public Integer process(final RelationNode node) {
            return ID_RELATION_NODE;
          }

          @Override
          public Integer process(final SubNode node) {
            return ID_SUB_NODE;
          }
        }));
  }

  /**
   * Flattens a condition tree into a byte array.
   *
   * @param root Root node of the condition tree.
   *
   * @return The flattened byte array.
   */
  public static byte[] flatten(final ConditionNode root) {
    return ByteHelpers.toArray(flatten(root, NodeIdCollector.getNodeIds(root)));
  }
}
