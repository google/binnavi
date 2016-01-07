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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;

/**
 * Used to evaluate a criteria formula and to select all nodes of a graph that match the criteria
 * formula.
 */
public final class CCriteriumFormulaGenerator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CCriteriumFormulaGenerator() {
  }

  /**
   * Generates a formula string for a given tree node and all of its children.
   *
   * @param node The node to generate the string for.
   *
   * @return The generated string.
   */
  public static String generate(final CCachedExpressionTreeNode node) {
    return node.getCriterium().getFormulaString(node.getChildren());
  }
}
