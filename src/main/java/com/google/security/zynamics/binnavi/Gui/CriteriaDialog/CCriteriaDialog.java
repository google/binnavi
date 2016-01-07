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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.ICriteriumCreator;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionModel.CCriteriumTree;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.ExpressionTree.JCriteriumTree;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Dialog class that provides the option to select nodes of a graph according to certain criteria
 * (node color, contains text XYZ, ...).
 */
public final class CCriteriaDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3407098516337379404L;

  /**
   * Currently active criterium tree.
   */
  private final CCriteriumTree m_ctree = new CCriteriumTree();

  /**
   * Updates the dialog.
   */
  private final CDialogUpdater m_updater;

  /**
   * Flag that indicates whether the dialog was closed by clicking the OK button or not.
   */
  private boolean m_closedByOk;

  private final JCriteriumTree m_jtree;

  /**
   * Creates a new dialog object.
   *
   * @param owner Parent window of the dialog.
   * @param conditionFactory Creates the individual criteria.
   */
  public CCriteriaDialog(final JFrame owner, final CCriteriaFactory conditionFactory) {
    super(owner, "Select by Criteria", true);

    Preconditions.checkNotNull(
        conditionFactory, "IE01315: Condition factory argument can not be null");

    final List<ICriteriumCreator> criteria = conditionFactory.getConditions();

    m_jtree = new JCriteriumTree(m_ctree, criteria);

    final CConditionBox selectionBox = new CConditionBox(criteria);

    final CAddConditionButtonListener addConditionButtonListner =
        new CAddConditionButtonListener(m_jtree, m_ctree, selectionBox);
    final JButton addConditionButton = new JButton(addConditionButtonListner);

    final CPanelTwoButtons okCancelPanel =
        new CPanelTwoButtons(new InternalOkCancelButttonListener(), "Execute", "Cancel");

    final JPanel defineConditionPanel = new JPanel(new BorderLayout());

    initDialog(m_jtree, selectionBox, defineConditionPanel, okCancelPanel, addConditionButton);

    GuiHelper.centerChildToParent(owner, this, true);

    m_updater = new CDialogUpdater(
        m_jtree, m_ctree, defineConditionPanel, addConditionButton, okCancelPanel.getFirstButton());
  }

  /**
   * Creates the GUI of the dialog.
   *
   * @param jtree Tree component shown in the dialog.
   * @param selectionBox Used to select new criteria.
   * @param defineConditionPanel Panel where the condition is shown.
   * @param okCancelPanel Panel that contains the OK and Cancel buttons.
   * @param addConditionButton Add Condition button.
   */
  private void initDialog(final JCriteriumTree jtree, final CConditionBox selectionBox,
      final JPanel defineConditionPanel, final CPanelTwoButtons okCancelPanel,
      final JButton addConditionButton) {
    final JPanel mainPanel = new JPanel(new BorderLayout());

    final JPanel deviderBorderPanel = new JPanel(new BorderLayout());
    deviderBorderPanel.setBorder(new EmptyBorder(2, 2, 2, 2));// (""));

    final JPanel deviderPanel = new JPanel(new GridLayout(1, 2));

    final JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setBorder(new TitledBorder("Expression Tree"));

    final JPanel rightPanel = new JPanel(new BorderLayout());
    final JPanel rightTopPanel = new JPanel(new BorderLayout());
    rightTopPanel.setBorder(new TitledBorder("Create Condition"));

    final JPanel rightTopComboPanel = new JPanel(new BorderLayout());
    rightTopComboPanel.setBorder(new EmptyBorder(1, 5, 5, 5));

    final JPanel rightTopAddPanel = new JPanel(new BorderLayout());
    rightTopAddPanel.setBorder(new EmptyBorder(1, 0, 5, 5));

    mainPanel.add(deviderBorderPanel, BorderLayout.CENTER);
    mainPanel.add(okCancelPanel, BorderLayout.SOUTH);
    okCancelPanel.getFirstButton().setEnabled(jtree.getSelectionPath() != null);

    deviderBorderPanel.add(deviderPanel, BorderLayout.CENTER);

    deviderPanel.add(leftPanel);
    deviderPanel.add(rightPanel);

    final JScrollPane pane = new JScrollPane(jtree);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    leftPanel.add(pane, BorderLayout.CENTER);

    defineConditionPanel.setBorder(new TitledBorder("Define Condition"));

    rightPanel.add(rightTopPanel, BorderLayout.NORTH);
    rightPanel.add(defineConditionPanel, BorderLayout.CENTER);

    rightTopPanel.add(rightTopComboPanel, BorderLayout.CENTER);
    rightTopPanel.add(rightTopAddPanel, BorderLayout.EAST);

    rightTopComboPanel.add(selectionBox, BorderLayout.CENTER);

    addConditionButton.setText("Add");

    addConditionButton.setEnabled(false);
    rightTopAddPanel.add(addConditionButton, BorderLayout.CENTER);

    add(mainPanel);

    setIconImage(null);

    pack();
  }

  /**
   * Frees allocated resources.
   */
  public void delete() {
    m_jtree.delete();
    m_updater.dispose();
  }

  /**
   * Returns the currently active criterium tree.
   *
   * @return The currently active criterium tree.
   */
  public CCriteriumTree getCriteriumTree() {
    return m_ctree;
  }

  /**
   * Flag that determines whether dialog was closed using the OK button.
   *
   * @return True, if the dialog was closed through the OK button. False, otherwise.
   */
  public boolean isClosedOk() {
    return m_closedByOk;
  }

  /**
   * Listens on the OK and Cancel buttons.
   */
  private class InternalOkCancelButttonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      m_closedByOk = event.getActionCommand().equals("Execute");

      dispose();
    }
  }
}
