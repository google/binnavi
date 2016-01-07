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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Criterium that can be used to select nodes by color in the Select by Criteria dialog.
 */
public final class CColorCriterium extends CConditionCriterium {
  /**
   * Icon shown for tree nodes that represent this criterium.
   */
  private static final ImageIcon COLOR_CONDITION_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/color_condition2.png"));

  /**
   * Panel shown when the node that represents this criterium is selected.
   */
  private final CColorCriteriumPanel m_panel;

  /**
   * Creates a new criterium object.
   *
   * @param graph The graph on which Select by Criteria is executed.
   */
  public CColorCriterium(final ZyGraph graph) {
    m_panel = new CColorCriteriumPanel(this, graph);
  }

  @Override
  public ICachedCriterium createCachedCriterium() {
    return new CCachedColorCriterium(getColor());
  }

  @Override
  public void dispose() {
    m_panel.delete();
  }

  /**
   * Returns the color selected by the user.
   *
   * @return The color selected by the user.
   */
  public Color getColor() {
    return m_panel.getColor();
  }

  @Override
  public String getCriteriumDescription() {
    return String.format("Nodes with Color %06X", getColor().getRGB() & 0xFFFFFF);
  }

  @Override
  public JPanel getCriteriumPanel() {
    return m_panel;
  }

  @Override
  public Icon getIcon() {
    return COLOR_CONDITION_ICON;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return CColorEvaluator.evaluate(node, m_panel.getColor());
  }
}
