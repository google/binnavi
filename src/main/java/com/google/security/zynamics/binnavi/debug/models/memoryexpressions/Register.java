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
 * Represents a single register in a memory expression.
 */
public class Register implements MemoryExpressionElement {
  /**
   * The name of the register.
   */
  private final String name;

  /**
   * Creates a new register object.
   *
   * @param name The name of the register.
   */
  public Register(final String name) {
    this.name = name;
  }

  /**
   * Returns the name of the register.
   *
   * @return The name of the register.
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public void visit(final MemoryExpressionVisitor visitor) {
    visitor.visit(this);
  }
}
