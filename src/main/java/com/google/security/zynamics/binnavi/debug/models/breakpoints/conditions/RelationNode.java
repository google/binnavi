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

import com.google.common.collect.Lists;

/**
 * Represents a single relational expression of a breakpoint condition.
 */
public class RelationNode extends BaseNode {
  /**
   * Relational operator.
   */
  private final String operator;

  /**
   * Creates a new node object.
   *
   * @param operator Relational operator.
   * @param lhs Left-hand side of the relation.
   * @param rhs Right-hand side of the relation.
   */
  public RelationNode(final String operator, final ConditionNode lhs, final ConditionNode rhs) {
    super(Lists.newArrayList(lhs, rhs));
    this.operator = operator;
  }

  /**
   * Returns the relational operator.
   *
   * @return The relational operator.
   */
  public String getOperator() {
    return operator;
  }

  @Override
  public String toString() {
    return getChildren().get(0) + " " + operator + " " + getChildren().get(1);
  }
}
