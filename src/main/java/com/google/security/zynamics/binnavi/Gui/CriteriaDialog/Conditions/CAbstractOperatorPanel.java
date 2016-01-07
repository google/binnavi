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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Abstract base class for panels that are shown when nodes are selected that do not represent
 * simple criteria.
 */
public abstract class CAbstractOperatorPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -273158189123712869L;

  /**
   * Text area where notification messages are shown.
   */
  private final JTextArea m_infoField = new JTextArea();

  /**
   * Creates a new panel object.
   */
  public CAbstractOperatorPanel() {
    super(new BorderLayout());

    final JPanel mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBorder(new TitledBorder(getBorderTitle()));

    final JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    m_infoField.setBackground(infoPanel.getBackground());
    m_infoField.setLineWrap(true);
    m_infoField.setWrapStyleWord(true);
    m_infoField.setEditable(false);

    infoPanel.add(m_infoField, BorderLayout.CENTER);

    mainPanel.add(infoPanel, BorderLayout.CENTER);

    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Returns the border title.
   *
   * @return The border title.
   */
  public abstract String getBorderTitle();

  /**
   * Returns the info field.
   *
   * @return The info field.
   */
  public JTextArea getInfoField() {
    return m_infoField;
  }

  /**
   * Returns the string to display in case the formula in the tree is invalid.
   *
   * @return The invalid formula string.
   */
  public abstract String getInvalidInfoString();

  /**
   * Returns the string to display in case the formula in the tree is valid.
   *
   * @return The valid formula string.
   */
  public abstract String getValidInfoString();
}
