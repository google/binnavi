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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor;

import java.awt.Color;
import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay a color criterium with constant values.
 */
public final class CCachedColorCriterium implements ICachedCriterium {
  /**
   * Color to compare the node colors to.
   */
  private final Color m_color;

  /**
   * Creates a new color criterium.
   *
   * @param color Color to compare the node colors to.
   */
  public CCachedColorCriterium(final Color color) {
    m_color = color;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    return "COLOR" + "=[" + m_color.getRed() + ", " + m_color.getGreen() + ", " + m_color.getBlue()
        + "]";
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CColorEvaluator.evaluate(node, m_color);
  }
}
