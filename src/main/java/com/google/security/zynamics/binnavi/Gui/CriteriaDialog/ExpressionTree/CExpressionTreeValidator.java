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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree;

import java.util.Enumeration;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And.CAndCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.CNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Or.COrCriterium;


/**
 * Validates visible trees.
 */
public final class CExpressionTreeValidator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CExpressionTreeValidator() {
  }

  /**
   * Checks whether a given tree contains a valid boolean formula.
   *
   * @param tree The tree to check.
   *
   * @return True, if the tree is valid. False, otherwise.
   */
  public static boolean isValid(final JCriteriumTree tree) {
    final JCriteriumTreeNode root = (JCriteriumTreeNode) tree.getModel().getRoot();

    if (root.getChildCount() != 1) {
      return false;
    }

    final Enumeration<?> enumeration = root.breadthFirstEnumeration();

    while (enumeration.hasMoreElements()) {
      final JCriteriumTreeNode node = (JCriteriumTreeNode) enumeration.nextElement();
      final ICriterium type = node.getCriterium();
      final int count = node.getChildCount();

      if ((type instanceof CAndCriterium || type instanceof COrCriterium) && count < 2) {
        return false;
      } else if (type instanceof CNotCriterium && count != 1) {
        return false;
      }
    }
    return true;
  }
}
