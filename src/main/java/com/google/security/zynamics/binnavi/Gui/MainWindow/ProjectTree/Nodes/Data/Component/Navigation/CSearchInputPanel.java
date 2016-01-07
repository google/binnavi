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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation;

import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Panel where the user can enter search options and search strings for searching through the binary
 * data of a module.
 */
class CSearchInputPanel extends JPanel {
  /**
   * Hex view that contains the data to search through.
   */
  private final JHexView m_hexView;

  /**
   * Contains the search results after a search operation.
   */
  private final CSearchResultModel m_model;

  /**
   * Used to switch between textual input and hexadecimal input.
   */
  private final JComboBox<String> m_modeBox = new JComboBox<>(new String[] {"Text", "Hex"});

  /**
   * Used to enter the search string.
   */
  private final JFormattedTextField m_inputField = new JFormattedTextField();

  /**
   * Creates a new panel object.
   *
   * @param hexView Hex view that contains the data to search through.
   * @param model Contains the search results after a search operation.
   */
  public CSearchInputPanel(final JHexView hexView, final CSearchResultModel model) {
    super(new BorderLayout());

    m_hexView = hexView;
    m_model = model;

    m_modeBox.setPreferredSize(new Dimension(150, m_modeBox.getMinimumSize().height));
    m_inputField.setPreferredSize(new Dimension(150, m_inputField.getMinimumSize().height));

    m_modeBox.setBorder(new EmptyBorder(0, 0, 0, 5));

    final JPanel innerPanel = new JPanel(new BorderLayout());

    innerPanel.add(m_modeBox, BorderLayout.WEST);
    innerPanel.add(m_inputField, BorderLayout.EAST);

    add(innerPanel, BorderLayout.WEST);

    m_modeBox.addItemListener(new InternalItemListener());
    m_inputField.addActionListener(new InternalActionListener());
  }

  /**
   * Executes a search with the parameter the users entered in the panel.
   */
  private void search() {
    if (m_modeBox.getSelectedIndex() == 0) {
      searchText();
    } else {
      searchHex();
    }
  }

  /**
   * Executes a search for the given data.
   *
   * @param data The bytes to search for in the module data.
   */
  private void search(final byte[] data) {
    m_hexView.uncolorizeAll();
    final byte[] hexData = m_hexView.getData().getData();
    final List<CSearchResult> results = new ArrayList<>();
    for (int i = 0; i < hexData.length; i++) {
      boolean equal = true;
      int counter = 0;
      for (final byte element : data) {
        if (hexData[i + counter] != element) {
          equal = false;
          break;
        }
        counter++;
      }
      if (equal) {
        results.add(new CSearchResult(i, data.length));
      }
    }
    m_model.setResults(results);
  }

  /**
   * Executes a hex search with the parameter the users entered in the panel.
   */
  private void searchHex() {
    final String text = m_inputField.getText();

    if ((text.length() % 2) != 0) {
      m_inputField.setBackground(Color.RED);

      return;
    }

    m_inputField.setBackground(Color.WHITE);

    final byte[] data = new byte[text.length() / 2];

    for (int i = 0; i < text.length(); i += 2) {
      final String substr = text.substring(i, i + 2);
      data[i / 2] = (byte) (int) Integer.valueOf(substr, 16);
    }

    search(data);
  }

  /**
   * Executes a text search with the parameter the users entered in the panel.
   */
  private void searchText() {
    search(m_inputField.getText().getBytes());
  }

  /**
   * Updates the input field according to parameters selected in the panel.
   */
  private void updateTextFieldMask() {
    if (m_modeBox.getSelectedIndex() == 0) {
      m_inputField.setFormatterFactory(new DefaultFormatterFactory());
    } else {
      m_inputField.setFormatterFactory(new DefaultFormatterFactory(new CHexFormatter(8)));
    }
  }

  /**
   * Action class used for searching through the module data.
   */
  private class InternalActionListener extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      search();
    }
  }

  /**
   * Updates the search field according to the currently selected search mode.
   */
  private class InternalItemListener implements ItemListener {
    @Override
    public void itemStateChanged(final ItemEvent event) {
      updateTextFieldMask();
    }
  }
}
