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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Criterium used to select nodes by their tag state.
 */
public final class CTagCriterium extends CConditionCriterium {
  /**
   * Icon shown for this criterium in the criteria tree.
   */
  private static final ImageIcon TAG_CONDITION_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/tag_condition.png"));

  /**
   * Panel shown when the node that represents this criterium is selected.
   */
  private final CTagCriteriumPanel m_panel;

  /**
   * Creates a new criterium object.
   *
   * @param tagManager Tag manager that provides tagging information.
   */
  public CTagCriterium(final ITagManager tagManager) {
    m_panel = new CTagCriteriumPanel(this, tagManager);
  }

  @Override
  public ICachedCriterium createCachedCriterium() {
    return new CCachedTagCriterium(m_panel.isAny(), m_panel.getTag());
  }

  @Override
  public void dispose() {
    m_panel.dispose();
  }

  @Override
  public String getCriteriumDescription() {
    return m_panel.isAny() ? String.format("Nodes with any Tag") : String.format(
        "Nodes with Tag '%s'", m_panel.getTag() == null ? "" : m_panel.getTag().getName());
  }

  @Override
  public JPanel getCriteriumPanel() {
    return m_panel;
  }

  @Override
  public Icon getIcon() {
    return TAG_CONDITION_ICON;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CTagEvaluator.evaluate(node, m_panel.isAny(), m_panel.getTag());
  }
}
