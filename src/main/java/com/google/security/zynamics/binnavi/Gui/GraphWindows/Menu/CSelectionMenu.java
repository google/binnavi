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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache.CCriteriumCache;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Cache.ICriteriumCacheListener;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionExpandSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionExpandSelectionDown;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionExpandSelectionUp;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionInvertSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionRedoSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectByCriteria;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectChildren;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectParents;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShrinkSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShrinkSelectionDown;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionShrinkSelectionUp;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionUndoSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CExecuteCachedCriterium;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CGroupAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CToggleSelectedGroupsAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CUngroupSelectedGroupsAction;


/**
 * Selection menu of the graph window menu bar.
 */
public final class CSelectionMenu extends JMenu {
  /**
   * Menu that contains the previously executed Select by Criteria formulas.
   */
  private final JMenu m_previousCriteriaMenu = new JMenu("Previous Criteria");

  /**
   * Model of the graph to select.
   */
  private final CGraphModel m_model;

  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2997329400894556417L;

  /**
   * Caches executed Select by Criteria operations for reuse.
   */
  private final CCriteriumCache m_criteriumCache = new CCriteriumCache();

  /**
   * Updates the Select by Criteria menu on changes in the cache.
   */
  private final ICriteriumCacheListener m_criteriumCacheListener = new ICriteriumCacheListener() {
    @Override
    public void changedCriteria(final CCriteriumCache criteriumCache) {
      rebuildCriteriumCacheMenu();
    }
  };

  /**
   * Creates the Selection
   *
   * @param model Model of the graph to select.
   */
  public CSelectionMenu(final CGraphModel model) {
    super("Selection");

    m_model = model;

    setMnemonic("HK_MENU_SELECTION".charAt(0));

    add(CActionProxy.proxy(new CActionUndoSelection(model.getSelectionHistory())));
    add(CActionProxy.proxy(new CActionRedoSelection(model.getSelectionHistory())));

    addSeparator();

    add(CActionProxy.proxy(new CGroupAction(model.getGraph())));
    add(CActionProxy.proxy(new CUngroupSelectedGroupsAction(model.getGraph())));
    add(CActionProxy.proxy(new CToggleSelectedGroupsAction(model.getGraph())));

    addSeparator();

    add(CActionProxy.proxy(new CActionSelectChildren(model.getGraph(), false)));
    add(CActionProxy.proxy(new CActionSelectParents(model.getGraph(), false)));
    add(CActionProxy.proxy(new CActionInvertSelection(model.getGraph(), false)));

    addSeparator();

    add(CActionProxy.proxy(new CActionExpandSelectionDown(model.getGraph())));
    add(CActionProxy.proxy(new CActionExpandSelectionUp(model.getGraph())));
    add(CActionProxy.proxy(new CActionExpandSelection(model.getGraph())));
    add(CActionProxy.proxy(new CActionShrinkSelectionDown(model.getGraph())));
    add(CActionProxy.proxy(new CActionShrinkSelectionUp(model.getGraph())));
    add(CActionProxy.proxy(new CActionShrinkSelection(model.getGraph())));

    addSeparator();

    add(CActionProxy.proxy(new CActionSelectByCriteria(model.getGraphPanel(), false)));
    add(m_previousCriteriaMenu);

    m_previousCriteriaMenu.setVisible(false);

    m_criteriumCache.addListener(m_criteriumCacheListener);
  }

  /**
   * Rebuilds the menu that shows the previously executed Select By Criteria formulas.
   */
  private void rebuildCriteriumCacheMenu() {
    m_previousCriteriaMenu.removeAll();
    m_previousCriteriaMenu.setVisible(true);

    for (final CCachedExpressionTree tree : m_criteriumCache.getTrees()) {
      m_previousCriteriaMenu.add(
          new JMenuItem(new CExecuteCachedCriterium(m_model.getGraph(), tree)));
    }
  }

  /**
   * Disposes allocated resources.
   */
  public void dispose() {
    m_criteriumCache.removeListener(m_criteriumCacheListener);
  }

  /**
   * Returns the criterium cache of the menu.
   *
   * @return The criterium cache of the menu.
   */
  public CCriteriumCache getCriteriumCache() {
    return m_criteriumCache;
  }
}
