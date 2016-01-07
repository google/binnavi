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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.CTextInputField;


/**
 * Panel shown in the criteria dialog when Select by Text criteria nodes are selected.
 */
public final class CTextCriteriumPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7537050587424468959L;

  /**
   * Field where the user can enter the search text.
   */
  private final CTextInputField m_inputField;

  /**
   * Used to toggle case sensitive search.
   */
  private final JCheckBox m_caseSensitiveBox = new JCheckBox("Case Sensitive");

  /**
   * Used to toggle regular expression search.
   */
  private final JCheckBox m_regExBox = new JCheckBox("Regular Expression");

  /**
   * Creates a new panel object.
   *
   * @param criterium The criterium edited in this panel.
   */
  public CTextCriteriumPanel(final CTextCriterium criterium) {
    super(new BorderLayout());

    m_inputField = new CTextInputField(criterium, null);

    initPanel();
  }

  /**
   * Creates the panel GUI.
   */
  private void initPanel() {
    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder("Edit Text Condition"));

    final JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    m_inputField.setPreferredSize(new Dimension(m_inputField.getPreferredSize().width, 23));
    inputPanel.add(m_inputField, BorderLayout.NORTH);

    final JPanel checkboxesPanel = new JPanel(new GridLayout(2, 1));
    checkboxesPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

    checkboxesPanel.add(m_caseSensitiveBox);
    checkboxesPanel.add(m_regExBox);

    inputPanel.add(checkboxesPanel, BorderLayout.CENTER);

    mainPanel.add(inputPanel, BorderLayout.NORTH);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Returns the text entered by the user.
   *
   * @return The text entered by the user.
   */
  public String getText() {
    return m_inputField.getText();
  }

  /**
   * Returns the case sensitive search switch.
   *
   * @return True, to search case sensitively. False, to ignore it.
   */
  public boolean isCaseSensitive() {
    return m_caseSensitiveBox.isSelected();
  }

  /**
   * Returns the regular expression switch.
   *
   * @return True, to search with a regular expression. False, to search for plain text.
   */
  public boolean isRegularExpression() {
    return m_regExBox.isSelected();
  }
}
