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

/**
 * Represents a single parenthesed expression in a memory expression.
 */
public class SubExpression implements MemoryExpressionElement {
  /**
   * The content of the parentheses.
   */
  private final MemoryExpressionElement child;

  /**
   * Creates a new expression object.
   *
   * @param child The content of the parentheses.
   */
  public SubExpression(final MemoryExpressionElement child) {
    this.child = child;
  }

  /**
   * Returns the child expression of the expression.
   *
   * @return The child expression of the expression.
   */
  public MemoryExpressionElement getChild() {
    return child;
  }

  @Override
  public String toString() {
    return "(" + child.toString() + ")";
  }

  @Override
  public void visit(final MemoryExpressionVisitor visitor) {
    visitor.visit(this);
  }
}
