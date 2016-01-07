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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.CCriteriaDialog;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.CCriteriaFactory;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CCriteriumExecuter;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu.CGraphWindowMenuBar;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.helpers.CNodeColorizer;
import com.google.security.zynamics.binnavi.Gui.Scripting.CScriptingDialog;
import com.google.security.zynamics.binnavi.ZyGraph.helpers.CNodeColorCollector;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CColorChooser;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Color;
import java.util.HashMap;



/**
 * Encapsulates the dialog object instances that are specific to one instance of a graph panel. This
 * is useful for preserving state between different invocations of a dialog for one graph panel.
 */
public final class CGraphPanelDialogs {
  /**
   * Information about the graph displayed in the panel.
   */
  private final CGraphModel m_model;

  /**
   * The menu bar that is shown in the parent window when the graph panel is active.
   */
  private final CGraphWindowMenuBar m_menuBar;

  /**
   * Scripting dialog shown for this graph panel.
   */
  private final CScriptingDialog m_scriptingDlg;

  /**
   * Indicates whether the scripting dialog was shown before.
   */
  private boolean m_shownScriptingDialogBefore = false;

  /**
   * Select by Criteria dialog shown for this graph panel.
   */
  private final CCriteriaDialog m_criteriaDlg;

  /**
   * Creates a new dialogs object.
   *
   * @param model Information about the graph displayed in the panel.
   * @param menuBar The menu bar that is shown in the parent window when the graph panel is active.
   */
  public CGraphPanelDialogs(final CGraphModel model, final CGraphWindowMenuBar menuBar) {
    m_model = model;
    m_menuBar = menuBar;

    final CGraphWindow parent = model.getParent();
    final ZyGraph graph = model.getGraph();

    final String defaultLanguage =
        ConfigManager.instance().getGeneralSettings().getDefaultScriptingLanguage();

    final HashMap<String, Object> bindings = new HashMap<String, Object>();

    bindings.put("cg", model.getView2D());
    bindings.put("cf", model.getGraphFrame());
    bindings.put("cdb", model.getApiDatabase());

    m_scriptingDlg =
        new CScriptingDialog(parent, defaultLanguage, PluginInterface.instance(), bindings);
    m_scriptingDlg.setTitle(
        String.format("%s - %s", m_scriptingDlg.getTitle(), graph.getRawView().getName()));

    m_criteriaDlg = new CCriteriaDialog(model.getParent(), new CCriteriaFactory(
        model.getGraph(), model.getView2D(), model.getDatabase().getContent().getNodeTagManager()));

  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_scriptingDlg.dispose();
    m_criteriaDlg.delete();
  }

  /**
   * Shows the Select by Criteria dialog of this graph panel.
   */
  public void selectByCriteria() {
    GuiHelper.centerChildToParent(m_model.getParent(), m_criteriaDlg, true);

    m_criteriaDlg.setVisible(true);

    m_criteriaDlg.setVisible(false);

    if (m_criteriaDlg.isClosedOk()) {
      final CCriteriumTree tree = m_criteriaDlg.getCriteriumTree();

      m_menuBar.getCriteriumCache().add(tree.createCachedTree());

      CCriteriumExecuter.execute(tree, m_model.getGraph());
    }
  }

  /**
   * Shows the user a color selection dialog and colors all selected nodes with the selected color.
   */
  public void showColorizeNodesDialog() {
    final Color col = CColorChooser.showDialog(m_model.getParent(), "Color Nodes", Color.WHITE,
        CNodeColorCollector.getNodeColors(m_model.getGraph()).toArray(new Color[0]));
    if (col != null) {
      CNodeColorizer.colorizeSelectedNodes(m_model.getGraph(), col);
    }
  }

  /**
   * Shows the scripting dialog of the graph.
   */
  public void showScriptingDialog() {
    if (!m_shownScriptingDialogBefore) {
      GuiHelper.centerChildToParent(m_model.getParent(), m_scriptingDlg, true);
    }

    m_shownScriptingDialogBefore = true;

    if (m_scriptingDlg.isVisible()) {
      m_scriptingDlg.toFront();
    } else {
      m_scriptingDlg.setVisible(true);
    }
  }
}
