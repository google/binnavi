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
package com.google.security.zynamics.binnavi.Gui.Debug.SearchMemory;

import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Dialog that is used to search for data in memory.
 */
public final class CSearchDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2835590563881465026L;

  /**
   * Combobox that is used to select the data type to search for
   */
  private JComboBox<ISearcher> m_typeBox;

  /**
   * Alternative display field for the input value.
   */
  private final JTextField m_altField = new JTextField();

  /**
   * Input field where the user can enter what he wants to search for.
   */
  private JFormattedTextField m_inputField;

  /**
   * Name of the alternative display field.
   */
  private JLabel m_otherLabel;

  /**
   * Search data that was entered by the user.
   */
  private byte[] m_searchData;

  /**
   * Creates a new search dialog.
   *
   * @param parent The parent frame of the search dialog.
   */
  public CSearchDialog(final Window parent) {
    super(parent, "Find", ModalityType.DOCUMENT_MODAL);

    new CDialogEscaper(this);

    setLayout(new BorderLayout());

    add(createSearchPane(), BorderLayout.CENTER);
    add(new CPanelTwoButtons(new InternalActionListener(), "OK", "Cancel"), BorderLayout.SOUTH);

    pack();

    GuiHelper.centerChildToParent(parent, this, true);

    setVisible(true);
  }

  /**
   * Creates the content of the dialog.
   *
   * @return The content panel.
   */
  private JPanel createSearchPane() {
    final JPanel panel = new JPanel();

    panel.setLayout(new GridLayout(3, 2));

    panel.setBorder(new TitledBorder("Search for" + " ..."));

    panel.add(new JLabel("Type"));

    m_typeBox = new JComboBox<ISearcher>();

    m_typeBox.addItem(new AsciiSearcher());
    m_typeBox.addItem(new UnicodeSearcher());
    m_typeBox.addItem(new HexSearcher());

    m_typeBox.setSelectedIndex(0);

    m_typeBox.addActionListener(new InternalTypeListener());

    panel.add(m_typeBox);

    panel.add(new JLabel("Value"));

    m_inputField = new JFormattedTextField();
    m_inputField.getDocument().addDocumentListener(new InternalTextListener());
    panel.add(m_inputField);

    m_otherLabel = new JLabel("Hex");

    panel.add(m_otherLabel);

    m_altField.setEnabled(false);

    panel.add(m_altField);

    panel.setSize(500, 300);

    return panel;
  }

  /**
   * When the user OKs the dialog, this method prepares the search data for easy access from the
   * calling method.
   */
  private void prepareData() {
    final ISearcher searcher = (ISearcher) m_typeBox.getSelectedItem();

    if (searcher != null) {
      m_searchData = searcher.getSearchData(m_inputField.getText());
    }
  }

  /**
   * Updates the alternative display field if necessary.
   */
  private void updateAlternativeField() {
    final ISearcher searcher = (ISearcher) m_typeBox.getSelectedItem();

    if (searcher != null) {
      m_altField.setText(searcher.getAlternativeString(m_inputField.getText()));
    }
  }

  /**
   * Returns the entered search data.
   *
   * @return The entered search data.
   */
  public byte[] getSearchData() {
    return m_searchData;
  }

  /**
   * Prepares the entered search data for easy access from the calling method after the user hit the
   * OK button.
   */
  private class InternalActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("OK")) {
        prepareData();
      }

      dispose();
    }
  }

  /**
   * Makes the necessary updates to the GUI when the user modifies the search data.
   */
  private class InternalTextListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent arg0) {
      // TODO: Why does this not update?
    }

    @Override
    public void insertUpdate(final DocumentEvent arg0) {
      updateAlternativeField();
    }

    @Override
    public void removeUpdate(final DocumentEvent arg0) {
      updateAlternativeField();
    }

  }

  /**
   * Makes the necessary updates to the GUI when the user selected another search type.
   */
  private class InternalTypeListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final ISearcher searcher = (ISearcher) m_typeBox.getSelectedItem();

      if (searcher != null) {
        m_inputField.setFormatterFactory(searcher.getFormatterFactory());
        m_otherLabel.setText(searcher.getAlternativeName());
        updateAlternativeField();
      }
    }
  }
}
