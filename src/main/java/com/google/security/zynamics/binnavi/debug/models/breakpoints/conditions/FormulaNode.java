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

import com.google.security.zynamics.zylib.strings.Commafier;

import java.util.List;

/**
 * Represents a single arithmetic expression of a breakpoint condition.
 */
public class FormulaNode extends BaseNode {
  /**
   * Arithmetic operator.
   */
  private final String operator;

  /**
   * Creates a new formula node object.
   *
   * @param operator Arithmetic operator.
   * @param children Subexpressions of the node.
   */
  public FormulaNode(final String operator, final List<ConditionNode> children) {
    super(children);
    this.operator = operator;
  }

  /**
   * Returns the formula operator.
   *
   * @return The formula operator.
   */
  public String getOperator() {
    return operator;
  }

  @Override
  public String toString() {
    return Commafier.commafy(getChildren(), " " + operator + " ");
  }
}
