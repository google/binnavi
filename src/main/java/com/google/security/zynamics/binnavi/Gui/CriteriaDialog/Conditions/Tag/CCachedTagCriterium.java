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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Class used to replay a tag criterium with constant values.
 */
public final class CCachedTagCriterium implements ICachedCriterium {
  /**
   * Flag that says whether any tagged node should match.
   */
  private final boolean m_any;

  /**
   * Tag to compare to the node tags. This value can be null if m_any is true.
   */
  private final CTag m_tag;

  /**
   * Creates a new tag criterium.
   *
   * @param any Flag that says whether any tagged node should match.
   * @param tag Tag to compare to the node tags. This value can be null if m_any is true.
   */
  public CCachedTagCriterium(final boolean any, final CTag tag) {
    m_any = any;
    m_tag = tag;
  }

  @Override
  public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
    if (m_any) {
      return "TAGGED";
    } else {
      return "TAGGED" + "=" + m_tag.getName();
    }
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CTagEvaluator.evaluate(node, m_any, m_tag);
  }
}
