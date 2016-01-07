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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Selection;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay a selection criterium with constant values.
 */
public final class CCachedSelectionCriterium implements ICachedCriterium {
  /**
   * Selection state to compare to the recursion state of the nodes.
   */
  private final SelectionState m_selectionState;

  /**
   * Creates a new criterium object.
   *
   * @param selectionState Selection state to compare to the selection state of the nodes.
   */
  public CCachedSelectionCriterium(final SelectionState selectionState) {
    m_selectionState = selectionState;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    return m_selectionState == SelectionState.SELECTED ? "SELECTED" : "NOT SELECTED";
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CSelectionEvaluator.evaluate(node, m_selectionState);
  }
}
