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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Criterium used to select nodes by indegree.
 */
public final class CIndegreeCriterium extends CConditionCriterium {
  /**
   * Icon shown for this criterium in the criteria tree.
   */
  private static final ImageIcon INDEGREE_CONDITION_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/indegree_condition.png"));

  /**
   * Panel shown when the node that represents this criterium is selected.
   */
  private final CIndegreeCriteriumPanel m_panel = new CIndegreeCriteriumPanel(this);

  @Override
  public ICachedCriterium createCachedCriterium() {
    return new CCachedIndegreeCriterium(m_panel.getOperator(), m_panel.getIndegree());
  }

  @Override
  public void dispose() {
    m_panel.dispose();
  }

  @Override
  public String getCriteriumDescription() {
    return String.format("Nodes with Indegree %s %d", m_panel.getOperator(), m_panel.getIndegree());
  }

  @Override
  public JPanel getCriteriumPanel() {
    return m_panel;
  }

  @Override
  public Icon getIcon() {
    return INDEGREE_CONDITION_ICON;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CIndegreeEvaluator.matches(node, m_panel.getOperator(), m_panel.getIndegree());
  }
}
