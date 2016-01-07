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
package com.google.security.zynamics.binnavi.API.helpers;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.ITreeNode;

public final class TreeAlgorithms {
  private TreeAlgorithms() {
    // You are not supposed to instantiate this class.
  }

  // ! Tests if a tree node dominates another node
  /**
   * Tests whether a node dominates another node.
   *
   * @param node The node of the dominator tree where the search starts.
   * @param target The dominator node.
   * @param source The dominated node.
   *
   * @return True, if the dominator node dominates the dominated node. False, otherwise.
   */
  public static <NodeType> boolean dominates(final ITreeNode<NodeType> node,
      final ITreeNode<NodeType> target, final ITreeNode<NodeType> source) {
    Preconditions.checkNotNull(node, "Error: Node argument can not be null");
    Preconditions.checkNotNull(target, "Error: target argument can not be null");
    Preconditions.checkNotNull(source, "Error: Source argument can not be null");

    return com.google.security.zynamics.zylib.types.trees.TreeAlgorithms.dominates(node, target, source);
  }
}
