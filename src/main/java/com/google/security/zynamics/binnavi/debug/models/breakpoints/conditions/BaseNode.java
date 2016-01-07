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
package com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions;

import java.util.ArrayList;
import java.util.List;

/**
 * Base node of all condition nodes.
 */
public class BaseNode implements ConditionNode {
  /**
   * Children of the node.
   */
  private final List<ConditionNode> children;

  /**
   * Creates a new base node object.
   */
  public BaseNode() {
    this(new ArrayList<ConditionNode>());
  }

  /**
   * Creates a new base node object with the given children.
   *
   * @param children Children of the node.
   */
  public BaseNode(final List<ConditionNode> children) {
    this.children = new ArrayList<>(children);
  }

  @Override
  public List<ConditionNode> getChildren() {
    return new ArrayList<>(children);
  }
}
