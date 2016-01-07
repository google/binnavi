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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CCriteriumFormulaGenerator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Root.CCachedRootCriterium;

/**
 * Expression tree that holds cached Select by Criteria expressions.
 */
public final class CCachedExpressionTree implements IAbstractCriteriumTree {
  /**
   * Root node of the criterium tree.
   */
  private final CCachedExpressionTreeNode m_rootNode =
      new CCachedExpressionTreeNode(new CCachedRootCriterium());

  /**
   * Returns a string formula that represents the tree.
   *
   * @return The formula that represents the tree.
   */
  public String getFormulaString() {
    return CCriteriumFormulaGenerator.generate(m_rootNode);
  }

  /**
   * Returns the root node of the criterium tree.
   *
   * @return The root node of the criterium tree.
   */
  @Override
  public CCachedExpressionTreeNode getRoot() {
    return m_rootNode;
  }
}
