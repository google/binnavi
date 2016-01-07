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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Selection;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Panel shown in the criteria dialog when Selection state criteria nodes are selected.
 */
public final class CSelectionCriteriumPanel extends JPanel {
  /**
   * The criterium edited in this panel.
   */
  private final CSelectionCriterium m_criterium;

  /**
   * Allows the user to select the selection state.
   */
  private final JComboBox<SelectionState> selectionStateBox = new JComboBox<SelectionState>();

  /**
   * Updates the GUI on user input.
   */
  private final InternalComboboxListener selectionStateBoxListener = new InternalComboboxListener();

  /**
   * Creates a new panel object.
   *
   * @param criterium The criterium edited in this panel.
   */
  public CSelectionCriteriumPanel(final CSelectionCriterium criterium) {
    super(new BorderLayout());
    m_criterium = criterium;
    selectionStateBox.addActionListener(selectionStateBoxListener);
    initPanel();
  }

  /**
   * Creates the GUI of the panel.
   */
  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Selection Condition"));

    final JPanel comboPanel = new JPanel(new BorderLayout());
    comboPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    selectionStateBox.addItem(SelectionState.SELECTED);
    selectionStateBox.addItem(SelectionState.UNSELECTED);

    comboPanel.add(selectionStateBox, BorderLayout.CENTER);

    mainPanel.add(comboPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    selectionStateBox.removeActionListener(selectionStateBoxListener);
  }

  /**
   * Returns the selection state selected by the user.
   *
   * @return The selection state selected by the user.
   */
  public SelectionState getSelectionState() {
    return (SelectionState) selectionStateBox.getSelectedItem();
  }

  /**
   * Updates the GUI on user input.
   */
  private class InternalComboboxListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      m_criterium.notifyListeners();
    }
  }
}
