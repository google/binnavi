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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CAbstractOperatorPanel;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CConditionCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Not.CNotCriterium;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeListener;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.ICriteriumTreeNode;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.CExpressionTreeValidator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTreeNode;


/**
 * Keeps the GUI updated on changes to the current formula.
 */
public final class CDialogUpdater {
  /**
   * Used to signal malformed formulas.
   */
  private static final Color INVALID_OPERATOR_COLOR = new Color(160, 0, 0);

  /**
   * Used to signal valid formulas.
   */
  private static final Color VALID_OPERATOR_COLOR = new Color(0, 0, 0);

  /**
   * The visible tree.
   */
  private final JCriteriumTree m_jtree;

  /**
   * The model tree that backs the visible tree.
   */
  private final CCriteriumTree m_ctree;

  /**
   * Panel where the active criterium can be configured.
   */
  private final JPanel m_defineConditionPanel;

  /**
   * Button used to add a new criteria to the tree.
   */
  private final JButton m_addConditionButton;

  /**
   * Button used to execute the current formula on the graph.
   */
  private final JButton m_executeButton;

  /**
   * Displays the criterium specific configuration panels on changes to the visible criterium tree.
   */
  private final InternalTreeSelectionListener m_treeSelectionListener =
      new InternalTreeSelectionListener();

  /**
   * Updates the dialog on changes to the criteria tree model.
   */
  private final InternalCriteriumTreeListener m_treeCriteriumlListener =
      new InternalCriteriumTreeListener();

  /**
   * Creates a new updater object.
   *
   * @param jtree The visible criteria tree.
   * @param ctree Backs the visible criteria tree.
   * @param conditionPanel Panel where the active criterium can be configured.
   * @param conditionButton Button used to add a new criteria to the tree.
   * @param executeButton Button used to execute the current formula on the graph.
   */
  public CDialogUpdater(final JCriteriumTree jtree, final CCriteriumTree ctree,
      final JPanel conditionPanel, final JButton conditionButton, final JButton executeButton) {
    m_jtree = jtree;
    m_ctree = ctree;
    m_defineConditionPanel = conditionPanel;
    m_addConditionButton = conditionButton;
    m_executeButton = executeButton;

    m_jtree.addTreeSelectionListener(m_treeSelectionListener);
    m_ctree.addListener(m_treeCriteriumlListener);
  }

  /**
   * Updates the condition configuration panel.
   *
   * @param node The actively selected node.
   */
  private void updateDefineConditionPanel(final JCriteriumTreeNode node) {
    final Component component = node.getCriterium().getCriteriumPanel();

    m_defineConditionPanel.removeAll();
    m_defineConditionPanel.setBorder(null);

    if (component == null) {
      final JPanel defaultPanel = new JPanel(new BorderLayout());
      defaultPanel.setBorder(new TitledBorder("Define Condition"));

      m_defineConditionPanel.add(defaultPanel, BorderLayout.CENTER);
    } else {
      m_defineConditionPanel.add(component);
    }

    m_defineConditionPanel.updateUI();
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_jtree.removeTreeSelectionListener(m_treeSelectionListener);
    m_ctree.removeListener(m_treeCriteriumlListener);
  }

  /**
   * Updates the dialog on changes to the criteria tree model.
   */
  private class InternalCriteriumTreeListener implements ICriteriumTreeListener {
    /**
     * Updates the dialog according to the state of the active formula.
     */
    private void update() {
      updateInfoField();

      m_executeButton.setEnabled(CExpressionTreeValidator.isValid(m_jtree));
    }

    /**
     * Updates the criterium node selection after nodes were removed.
     */
    private void updateCurrentCriteriumPath() {
      if (m_jtree.getSelectionPath() == null) {
        m_jtree.setCurrentCriteriumPath(m_jtree.getPathForRow(0));
      } else {
        m_jtree.setCurrentCriteriumPath(m_jtree.getSelectionPath());
      }

      updateDefineConditionPanel(
          (JCriteriumTreeNode) m_jtree.getCurrentCriteriumPath().getLastPathComponent());
    }

    /**
     * Updates the output field that provides additional information about the state of the active
     * formula.
     */
    private void updateInfoField() {
      final Enumeration<?> nodes =
          ((JCriteriumTreeNode) m_jtree.getModel().getRoot()).breadthFirstEnumeration();

      while (nodes.hasMoreElements()) {
        final JCriteriumTreeNode node = (JCriteriumTreeNode) nodes.nextElement();

        final JPanel panel = node.getCriterium().getCriteriumPanel();

        if (panel instanceof CAbstractOperatorPanel) {
          final int count = node.getChildCount();

          final ICriterium criterium = node.getCriterium();

          final JTextArea infoField = ((CAbstractOperatorPanel) panel).getInfoField();

          if (count == 1 && (criterium instanceof CNotCriterium || node.getLevel() == 0)
              || count > 1 && !(criterium instanceof CNotCriterium)) {
            infoField.setForeground(VALID_OPERATOR_COLOR);
            infoField.setText(((CAbstractOperatorPanel) panel).getValidInfoString());
          } else {
            infoField.setForeground(INVALID_OPERATOR_COLOR);
            infoField.setText(((CAbstractOperatorPanel) panel).getInvalidInfoString());
          }
        }

        panel.updateUI();
      }
    }

    @Override
    public void appendedNode(final CCriteriumTree criteriumTree, final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      update();
    }

    @Override
    public void insertedNode(final CCriteriumTree criteriumTree, final ICriteriumTreeNode parent,
        final ICriteriumTreeNode child) {
      update();
    }

    @Override
    public void removedAll(final CCriteriumTree criteriumTree) {
      updateCurrentCriteriumPath();
      update();
    }

    @Override
    public void removedNode(final CCriteriumTree criteriumTree, final ICriteriumTreeNode node) {
      updateCurrentCriteriumPath();
      update();
    }

  }

  /**
   * Displays the criterium specific configuration panels on changes to the visible criterium tree.
   */
  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final TreePath path = event.getPath();

      if (path == null) {
        m_addConditionButton.setEnabled(false);
        updateDefineConditionPanel((JCriteriumTreeNode) m_jtree.getModel().getRoot());
      } else {
        final JCriteriumTreeNode selectedNode = (JCriteriumTreeNode) path.getLastPathComponent();

        boolean enable = selectedNode.allowAppend(CConditionCriterium.class);

        if (!enable && selectedNode.getLevel() > 0) {
          final JCriteriumTreeNode parentNode = (JCriteriumTreeNode) selectedNode.getParent();

          if (parentNode.getLevel() != 0 && !(parentNode.getCriterium() instanceof CNotCriterium)
              && !(selectedNode.getCriterium() instanceof CNotCriterium)) {
            enable = true;
          }
        }

        m_addConditionButton.setEnabled(enable);
        updateDefineConditionPanel(selectedNode);
      }
    }
  }
}
