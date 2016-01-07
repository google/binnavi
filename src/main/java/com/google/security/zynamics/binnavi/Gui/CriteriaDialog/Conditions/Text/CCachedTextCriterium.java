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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay a text criterium with constant values.
 */
public final class CCachedTextCriterium implements ICachedCriterium {
  /**
   * The text to search for.
   */
  private final String m_text;

  /**
   * Flag that enables or disables regular expression search.
   */
  private final boolean m_regularExpression;

  /**
   * Flag that enables or disables case sensitive search.
   */
  private final boolean m_caseSensitive;

  /**
   * Creates a new criterium object.
   *
   * @param text The text to search for.
   * @param regularExpression Flag that enables or disables regular expression search.
   * @param caseSensitive Flag that enables or disables case sensitive search.
   */
  public CCachedTextCriterium(
      final String text, final boolean regularExpression, final boolean caseSensitive) {
    m_text = text;
    m_regularExpression = regularExpression;
    m_caseSensitive = caseSensitive;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    final boolean additionalInformation = m_regularExpression || m_caseSensitive;

    final StringBuilder formula = new StringBuilder("CONTAINS" + "=" + m_text);

    if (additionalInformation) {
      formula.append('(');

      if (m_regularExpression) {
        formula.append("REGEX");
      }

      if (m_caseSensitive) {
        if (m_regularExpression) {
          formula.append(", ");
        }

        formula.append("CASE SENSITIVE");
      }

      formula.append('(');
    }

    return formula.toString();
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CTextEvaluator.evaluate(node, m_text, m_regularExpression, m_caseSensitive);
  }
}
