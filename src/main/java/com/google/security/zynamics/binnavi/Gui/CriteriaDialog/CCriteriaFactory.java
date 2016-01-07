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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin;
import com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin.IFixedCriterium;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICachedCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.InDegrees.CIndegreeCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor.CColorCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.OutDegree.COutdegreeCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Selection.CSelectionCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag.CTagCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text.CTextCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Visibillity.CVisibilityCriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCachedExpressionTreeNode;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.api2.plugins.IPlugin;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;

/**
 * Provides all available individual criteria for the criteria dialog.
 */
public final class CCriteriaFactory {
  /**
   * Graph whose nodes are selected.
   */
  private final ZyGraph m_graph;

  /**
   * API graph whose nodes are selected.
   */
  private final View2D m_view2D;

  /**
   * Provides the available node tags.
   */
  private final ITagManager m_tagManager;

  /**
   * Creates a new factory object.
   *
   * @param graph Graph whose nodes are selected.
   * @param view2D API graph whose nodes are selected.
   * @param tagManager Provides the available node tags.
   */
  public CCriteriaFactory(final ZyGraph graph, final View2D view2D, final ITagManager tagManager) {
    m_graph = Preconditions.checkNotNull(graph, "IE01316: Graph argument can not be null");
    // m_view2D = Preconditions.checkNotNull(view2D, "IE01794: View 2D argument can not be null");
    m_view2D = view2D;
    // m_tagManager = Preconditions.checkNotNull(tagManager,
    // "IE02088: tagManager argument can not be null");
    m_tagManager = tagManager;
  }

  /**
   * Returns all available criterium creators.
   *
   * @return All available criterium creators.
   */
  public List<ICriteriumCreator> getConditions() {
    final List<ICriteriumCreator> conditions = new ArrayList<ICriteriumCreator>();

    conditions.add(new CTextCriteriumCreator());
    conditions.add(new CTagCriteriumCreator(m_tagManager));
    conditions.add(new CColorCriteriumCreator(m_graph));
    conditions.add(new CIndegreeCriteriumCreator());
    conditions.add(new COutdegreeCriteriumCreator());
    conditions.add(new CVisibilityCriteriumCreator());
    conditions.add(new CSelectionCriteriumCreator());

    for (
        @SuppressWarnings("rawtypes")
    final IPlugin plugin : PluginInterface.instance().getPluginRegistry()) {
      if (plugin instanceof ICriteriaSelectionPlugin) {
        final ICriteriaSelectionPlugin cplugin = (ICriteriaSelectionPlugin) plugin;

        conditions.add(new CPluginCriteriumCreator(m_view2D, cplugin));
      }
    }

    return conditions;
  }

  /**
   * Wraps plugin criteria.
   */
  private static class CPluginCachedCriterium implements ICachedCriterium {
    /**
     * API graph whose nodes are selected.
     */
    private final View2D m_view2D;

    /**
     * The wrapped plugin object.
     */
    private final com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin.ICriterium m_plugin;

    /**
     * Object that contains a fixed point version of the values from the input panel.
     */
    private final IFixedCriterium m_value;

    /**
     * Creates a new cached criterium.
     *
     * @param view2D API graph whose nodes are selected.
     * @param plugin The wrapped plugin object.
     */
    public CPluginCachedCriterium(
        final View2D view2D, final com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin.ICriterium plugin) {
      m_view2D = view2D;
      m_plugin = plugin;

      m_value = plugin.getFixedCriterium();
    }

    @Override
    public String getFormulaString(final List<CCachedExpressionTreeNode> children) {
      try {
        return m_plugin.getFormulaString();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        return "???";
      }
    }

    @Override
    public boolean matches(final NaviNode node) {
      try {
        return m_value.matches(m_view2D.getNode(node.getRawNode()));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        return false;
      }
    }
  }

  /**
   * Wraps a plugin criterium.
   */
  private static class CPluginCriterium extends CConditionCriterium {
    /**
     * API graph whose nodes are selected.
     */
    private final View2D m_view2D;

    /**
     * The wrapped plugin object.
     */
    private final com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin.ICriterium m_plugin;

    /**
     * Creates a new criterium object.
     *
     * @param view2D API graph whose nodes are selected.
     * @param plugin The wrapped plugin object.
     */
    public CPluginCriterium(
        final View2D view2D, final com.google.security.zynamics.binnavi.API.plugins.ICriteriaSelectionPlugin.ICriterium plugin) {
      m_view2D = view2D;
      m_plugin = plugin;
    }

    @Override
    public ICachedCriterium createCachedCriterium() {
      return new CPluginCachedCriterium(m_view2D, m_plugin);
    }

    @Override
    public String getCriteriumDescription() {
      try {
        return m_plugin.getCriteriumDescription();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        return "???";
      }
    }

    @Override
    public JPanel getCriteriumPanel() {
      try {
        return m_plugin.getCriteriumPanel();
      } catch (final Exception exception) {
        return new JPanel();
      }
    }

    @Override
    public Icon getIcon() {
      return null;
    }

    @Override
    public boolean matches(final NaviNode node) {
      return m_plugin.matches(m_view2D.getNode(node.getRawNode()));
    }
  }

  /**
   * Wraps plugin criteria creators.
   */
  private static class CPluginCriteriumCreator implements ICriteriumCreator {
    /**
     * API graph whose nodes are selected.
     */
    private final View2D m_view2D;

    /**
     * The wrapped plugin.
     */
    private final ICriteriaSelectionPlugin m_plugin;

    /**
     * Creates a new creator object.
     *
     * @param view2D API graph whose nodes are selected.
     * @param plugin The wrapped plugin.
     */
    public CPluginCriteriumCreator(final View2D view2D, final ICriteriaSelectionPlugin plugin) {
      m_view2D = view2D;
      m_plugin = plugin;
    }

    @Override
    public ICriterium createCriterium() {
      try {
        return new CPluginCriterium(m_view2D, m_plugin.getCriterium(m_view2D));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        return null;
      }
    }

    @Override
    public String getCriteriumDescription() {
      return m_plugin.getCriteriumDescription();
    }
  }
}
