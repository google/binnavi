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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The panel that allows the user to create new array types.
 *
 */
public class ArrayTypePanel extends TypeDialogPanel {

  private TypeComboBox baseTypes;
  private JTextArea preview;
  private JSpinner numberElements;

  public ArrayTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null);
  }

  public ArrayTypePanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    super(parent, typeManager, existingType);
    createControls();
    populateControls();
  }

  private void createControls() {
    setLayout(new BorderLayout());
    final JPanel contentPanel = new JPanel();
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    add(contentPanel, BorderLayout.CENTER);
    final GridBagLayout gbl_m_contentPanel = new GridBagLayout();
    gbl_m_contentPanel.columnWidths = new int[] {0, 0, 0};
    gbl_m_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0};
    gbl_m_contentPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gbl_m_contentPanel.rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    contentPanel.setLayout(gbl_m_contentPanel);
    final JLabel lblSize = new JLabel("Number of elements:");
    final GridBagConstraints gbc_lblSize = new GridBagConstraints();
    gbc_lblSize.anchor = GridBagConstraints.WEST;
    gbc_lblSize.insets = new Insets(0, 0, 5, 5);
    gbc_lblSize.gridx = 0;
    gbc_lblSize.gridy = 1;
    contentPanel.add(lblSize, gbc_lblSize);

    numberElements = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    numberElements.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        updatePreview();
      }
    });
    final GridBagConstraints gbc_numberElements = new GridBagConstraints();
    gbc_numberElements.anchor = GridBagConstraints.WEST;
    gbc_numberElements.insets = new Insets(0, 0, 5, 0);
    gbc_numberElements.gridx = 1;
    gbc_numberElements.gridy = 1;
    contentPanel.add(numberElements, gbc_numberElements);

    final JLabel lblFillWith = new JLabel("Element type:");
    final GridBagConstraints gbc_lblFillWith = new GridBagConstraints();
    gbc_lblFillWith.anchor = GridBagConstraints.WEST;
    gbc_lblFillWith.insets = new Insets(0, 0, 5, 5);
    gbc_lblFillWith.gridx = 0;
    gbc_lblFillWith.gridy = 0;
    contentPanel.add(lblFillWith, gbc_lblFillWith);

    baseTypes = new TypeComboBox(
        new TypeListModel(typeManager.getTypes(), new TypeListModel.PrototypesFilter()));
    baseTypes.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updatePreview();
      }
    });
    final GridBagConstraints gbc_baseTypes = new GridBagConstraints();
    gbc_baseTypes.insets = new Insets(0, 0, 5, 0);
    gbc_baseTypes.fill = GridBagConstraints.HORIZONTAL;
    gbc_baseTypes.gridx = 1;
    gbc_baseTypes.gridy = 0;
    contentPanel.add(baseTypes, gbc_baseTypes);

    final JLabel lblPreview = new JLabel("Preview:");
    final GridBagConstraints gbc_lblPreview = new GridBagConstraints();
    gbc_lblPreview.anchor = GridBagConstraints.WEST;
    gbc_lblPreview.insets = new Insets(0, 0, 5, 5);
    gbc_lblPreview.gridx = 0;
    gbc_lblPreview.gridy = 2;
    contentPanel.add(lblPreview, gbc_lblPreview);

    final JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBorder(null);
    final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
    gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
    gbc_scrollPane.fill = GridBagConstraints.BOTH;
    gbc_scrollPane.gridx = 1;
    gbc_scrollPane.gridy = 2;
    contentPanel.add(scrollPane, gbc_scrollPane);

    preview = new JTextArea();
    preview.setEditable(false);
    scrollPane.setViewportView(preview);
  }

  private Integer getNumberElements() {
    return (Integer) numberElements.getValue();
  }

  private BaseType getSelectedType() {
    if (baseTypes.getSelectedIndex() != -1) {
      final TypeListModel model = baseTypes.getModel();
      return (BaseType) model.getSelectedItem();
    } else {
      return null;
    }
  }

  private void populateControls() {
    if (existingType == null) {
      return;
    }
    final TypeMember arrayMember = existingType.iterator().next();
    numberElements.setValue(arrayMember.getNumberOfElements().get());
    baseTypes.getModel().selectByBaseType(arrayMember.getBaseType());
    updatePreview();
  }

  private void updatePreview() {
    if (!UserInputTypeValidation.validateComboBox(baseTypes)) {
      return;
    }
    preview.setText(
        String.format("%s my_new_type[%d];", getSelectedType().getName(), getNumberElements()));
  }

  @Override
  protected void updateExistingType() throws CouldntSaveDataException {
    typeManager.updateArray(existingType, getSelectedType(), getNumberElements());
  }

  @Override
  protected void createNewType() throws CouldntSaveDataException {
    typeManager.createArray(getSelectedType(), getNumberElements());
  }

  @Override
  protected boolean validateModel() {
    return UserInputTypeValidation.validateComboBox(parent, baseTypes);
  }
}
