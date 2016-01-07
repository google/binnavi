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

import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that can be used to collect all the node IDs of a condition tree.
 */
public final class NodeIdCollector {
  /**
   * You are not supposed to instantiate this class.
   */
  private NodeIdCollector() {}

  /**
   * Writes the node IDs of all nodes in the flattened tree into a map.
   *
   * @param root The root node of the tree.
   * @param map Maps nodes of the tree onto their flattened identifiers.
   */
  private static void getNodeIds(final ConditionNode root,
      final Map<ConditionNode, Integer> map) {
    map.put(root, map.size());

    for (final ConditionNode child : root.getChildren()) {
      getNodeIds(child, map);
    }
  }

  /**
   * Returns the node IDs of all nodes in the flattened tree.
   *
   * @param root The root node of the tree.
   *
   * @return Maps nodes of the tree onto their flattened identifiers.
   */
  public static Map<ConditionNode, Integer> getNodeIds(final ConditionNode root) {
    final Map<ConditionNode, Integer> map = new HashMap<>();
    getNodeIds(root, map);
    return map;
  }
}
