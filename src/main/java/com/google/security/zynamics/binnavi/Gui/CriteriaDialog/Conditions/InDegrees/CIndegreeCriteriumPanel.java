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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.InDegrees;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CRelationBox;
import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CTextInputField;
import com.google.security.zynamics.zylib.gui.CDecFormatter;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;



/**
 * Panel shown in the criteria dialog when Indegree criteria nodes are selected.
 */
public final class CIndegreeCriteriumPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 907465542398746982L;

  /**
   * Input field where the number of indegree can be selected.
   */
  private final CTextInputField m_inputField;

  /**
   * Used to select the relation between indegrees.
   */
  private final CRelationBox m_operatorBox;

  /**
   * Creates a new panel object.
   *
   * @param criterium Criterium shown in the panel.
   */
  public CIndegreeCriteriumPanel(final CIndegreeCriterium criterium) {
    super(new BorderLayout());

    m_operatorBox = new CRelationBox(criterium);
    m_inputField = new CTextInputField(criterium, new CDecFormatter(8));

    initPanel();
  }

  /**
   * Creates the GUI of the panel.
   */
  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Indegree Condition"));

    final JPanel operatorPanel = new JPanel(new BorderLayout());
    operatorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    operatorPanel.add(m_operatorBox, BorderLayout.CENTER);

    final JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(new EmptyBorder(5, 0, 5, 5));
    inputPanel.add(m_inputField, BorderLayout.CENTER);

    final JPanel containerPanel = new JPanel(new BorderLayout());
    containerPanel.add(operatorPanel, BorderLayout.WEST);
    containerPanel.add(inputPanel, BorderLayout.CENTER);

    mainPanel.add(containerPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_operatorBox.dispose();
    m_inputField.dispose();
  }

  /**
   * Returns the indegree entered by the user.
   *
   * @return The indegree entered by the user.
   */
  public int getIndegree() {
    return m_inputField.getText().isEmpty() ? 0 : Integer.valueOf(m_inputField.getText());
  }

  /**
   * Returns the relation selected by the user.
   *
   * @return The relation selected by the user.
   */
  public String getOperator() {
    return m_operatorBox.getSelectedItem().toString();
  }
}
