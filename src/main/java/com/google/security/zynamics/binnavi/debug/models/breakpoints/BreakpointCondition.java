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
package com.google.security.zynamics.binnavi.debug.models.breakpoints;

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.conditions.ConditionNode;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;

/**
 * Represents a single breakpoint condition.
 */
public class BreakpointCondition implements Condition {
  /**
   * Condition text entered by the user.
   */
  private final String condition;

  /**
   * Root node of the AST of the condition expression.
   */
  private final ConditionNode root;

  /**
   * Creates a new breakpoint condition.
   *
   * @param condition Condition text entered by the user.
   * @param root The expression inside the parentheses.
   */
  public BreakpointCondition(final String condition, final ConditionNode root) {
    this.condition = condition;
    this.root = root;
  }

  @Override
  public ConditionNode getRoot() throws MaybeNullException {
    if (root == null) {
      throw new MaybeNullException();
    }
    return root;
  }

  @Override
  public String toString() {
    return condition;
  }
}
