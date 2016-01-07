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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.OutDegree;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay an outdegree criterium with constant values.
 */
public final class CCachedOutdegreeCriterium implements ICachedCriterium {
  /**
   * Operator used to compare outdegree values.
   */
  private final String m_operator;

  /**
   * Constant indegree value to compare the node outdegree values to.
   */
  private final int m_outdegree;

  /**
   * Creates a new outdegree criterium.
   *
   * @param operator Operator used to compare indegree values.
   * @param outdegree Constant outdegree value to compare the node indegree values to.
   */
  public CCachedOutdegreeCriterium(final String operator, final int outdegree) {
    m_operator = operator;
    m_outdegree = outdegree;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    return "OUTDEGREE" + m_operator + " " + m_outdegree;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return COutdegreeEvaluator.matches(node, m_operator, m_outdegree);
  }
}
