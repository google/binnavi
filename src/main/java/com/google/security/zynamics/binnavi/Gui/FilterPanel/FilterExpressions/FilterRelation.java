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
package com.google.security.zynamics.binnavi.Gui.FilterPanel.FilterExpressions;

/**
 * Specifies all possible relations between numerical values that can be used in filter strings.
 */
public enum FilterRelation {
  /**
   * Used to test two numerical values for equality.
   */
  EQUAL_TO,

  /**
   * Used to test two numerical values for non-equality.
   */
  NOT_EQUAL_TO,

  /**
   * Used to test whether the first numerical value is less than the second numerical value.
   */
  LESS_THAN,

  /**
   * Used to test whether the first numerical value is greater than the second numerical value.
   */
  GREATER_THAN,

  /**
   * Used to test whether the first numerical value is less than or equal to the second numerical
   * value.
   */
  LESS_EQUAL_TO,

  /**
   * Used to test whether the first numerical value is greater than or equal to the second numerical
   * value.
   */
  GREATER_EQUAL_THAN;

  /**
   * Parses a relation string into a relation object.
   * 
   * @param input The string to parse.
   * 
   * @return The parsed filter expression.
   */
  public static FilterRelation parse(final String input) {
    if ("==".equals(input)) {
      return EQUAL_TO;
    } else if ("!=".equals(input) || "<>".equals(input)) {
      return NOT_EQUAL_TO;
    } else if ("<".equals(input)) {
      return LESS_THAN;
    } else if (">".equals(input)) {
      return GREATER_THAN;
    } else if ("<=".equals(input)) {
      return LESS_EQUAL_TO;
    } else if (">=".equals(input)) {
      return GREATER_EQUAL_THAN;
    } else {
      throw new IllegalStateException("IE01143: Invalid relation string");
    }
  }
}
