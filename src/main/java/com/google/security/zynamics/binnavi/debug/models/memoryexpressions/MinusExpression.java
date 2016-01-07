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
package com.google.security.zynamics.binnavi.debug.models.memoryexpressions;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a minus expression in a memory expression tree.
 */
public class MinusExpression implements MemoryExpressionElement {
  /**
   * The operands of the minus expression.
   */
  private final List<MemoryExpressionElement> children;

  /**
   * Creates a new expression object.
   *
   * @param children The operands of the minus expression.
   */
  public MinusExpression(final List<MemoryExpressionElement> children) {
    this.children = children;
  }

  /**
   * Returns the children of the expression.
   *
   * @return The children of the expression.
   */
  public List<MemoryExpressionElement> getChildren() {
    return new ArrayList<>(children);
  }

  @Override
  public String toString() {
    final StringBuilder ret = new StringBuilder();

    for (final MemoryExpressionElement child : children) {
      ret.append(child.toString());
      if (child != children.get(children.size() - 1)) {
        ret.append('-');
      }
    }
    return ret.toString();
  }

  @Override
  public void visit(final MemoryExpressionVisitor visitor) {
    for (final MemoryExpressionElement child : children) {
      child.visit(visitor);
    }
    visitor.visit(this);
  }
}
