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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Criterium used to select nodes by the text they contain.
 */
public final class CTextCriterium extends CConditionCriterium {
  /**
   * Icon shown for this criterium in the criteria tree.
   */
  private static final ImageIcon TEXT_CONDITION_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/text_condition.png"));

  /**
   * Panel shown when the node that represents this criterium is selected.
   */
  private final CTextCriteriumPanel m_panel = new CTextCriteriumPanel(this);

  @Override
  public ICachedCriterium createCachedCriterium() {
    return new CCachedTextCriterium(m_panel.getText(), m_panel.isRegularExpression(), m_panel
        .isCaseSensitive());
  }

  @Override
  public String getCriteriumDescription() {
    return String.format("Nodes with Text '%s'", m_panel.getText());
  }

  @Override
  public JPanel getCriteriumPanel() {
    return m_panel;
  }

  @Override
  public Icon getIcon() {
    return TEXT_CONDITION_ICON;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CTextEvaluator.evaluate(
        node, m_panel.getText(), m_panel.isRegularExpression(), m_panel.isCaseSensitive());
  }
}
