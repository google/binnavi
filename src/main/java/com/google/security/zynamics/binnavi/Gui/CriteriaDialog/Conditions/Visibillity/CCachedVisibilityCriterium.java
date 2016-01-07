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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Visibillity;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay a visibility criterium with constant values.
 */
public final class CCachedVisibilityCriterium implements ICachedCriterium {
  /**
   * Visibility state to compare to the recursion state of the nodes.
   */
  private final VisibilityState m_visibilityState;

  /**
   * Creates a new criterium object.
   *
   * @param visibilityState Visibility state to compare to the selection state of the nodes.
   */
  public CCachedVisibilityCriterium(final VisibilityState visibilityState) {
    m_visibilityState = visibilityState;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    return m_visibilityState == VisibilityState.VISIBLE ? "VISIBLE" : "NOT VISIBLE";
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CVisibilityEvaluator.evaluate(node, m_visibilityState);
  }
}
