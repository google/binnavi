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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.And;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CAbstractCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Represent an AND criteria.
 */
public final class CAndCriterium extends CAbstractCriterium implements IAbstractAndCriterium {
  /**
   * Icon shown in the tree for this criteria.
   */
  private static final ImageIcon ADD_ICON =
      new ImageIcon(CMain.class.getResource("data/selectbycriteriaicons/add.png"));

  /**
   * Panel shown when the node is selected in the tree.
   */
  private final CAndCriteriumPanel m_panel = new CAndCriteriumPanel();

  @Override
  public ICachedCriterium createCachedCriterium() {
    return new CCachedAndCriterium();
  }

  @Override
  public String getCriteriumDescription() {
    return "AND";
  }

  @Override
  public JPanel getCriteriumPanel() {
    return m_panel;
  }

  @Override
  public Icon getIcon() {
    return ADD_ICON;
  }

  @Override
  public boolean matches(final NaviNode node) {
    return true;
  }
}
