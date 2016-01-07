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

/**
 * Represents a single numerical value in a breakpoint condition tree.
 */
public class NumberNode extends BaseNode {
  /**
   * The numerical value.
   */
  private final long value;

  /**
   * Creates a new number node object.
   *
   * @param value The numerical value.
   */
  public NumberNode(final long value) {
    this.value = value;
  }

  /**
   * Returns the numerical value.
   *
   * @return The numerical value.
   */
  public long getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("0x%X", value);
  }
}
