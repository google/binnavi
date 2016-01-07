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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.InDegrees;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay an indegree criterium with constant values.
 */
public final class CCachedIndegreeCriterium implements ICachedCriterium {
  /**
   * Operator used to compare indegree values.
   */
  private final String m_operator;

  /**
   * Constant indegree value to compare the node indegree values to.
   */
  private final int m_indegree;

  /**
   * Creates a new indegree criterium.
   *
   * @param operator Operator used to compare indegree values.
   * @param indegree Constant indegree value to compare the node indegree values to.
   */
  public CCachedIndegreeCriterium(final String operator, final int indegree) {
    m_operator = operator;
    m_indegree = indegree;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    return "INDEGREE" + m_operator + m_indegree;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CIndegreeEvaluator.matches(node, m_operator, m_indegree);
  }
}
