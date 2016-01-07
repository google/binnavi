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

import com.google.common.base.Predicates;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The panel that allows the user to create pointer types.
 *
 * @author jannewger (Jan Newger)
 */
public class PointerTypePanel extends TypeDialogPanel {
  private JSpinner pointerLevel;
  private TypeComboBox baseTypes;
  private JTextArea preview;

  /**
   * Parameterless constructor to keep WindowBuilder happy.
   */
  @SuppressWarnings("unused")
  private PointerTypePanel() {
    this(null, null);
  }

  /**
   * Creates a new instance in order to build a new type from scratch.
   *
   * @param parent The parent control of the panel.
   * @param typeManager The type manager that holds the current type system.
   */
  public PointerTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null);
  }

  /**
   * Creates a new instance in order to edit an existing type.
   *
   * @param parent The parent control of the panel.
   * @param typeManager The type manager that holds the current type system.
   * @param existingType The base type that should be edited.
   */
  public PointerTypePanel(final Component parent, final TypeManager typeManager,
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

    final JLabel lblBaseType = new JLabel("Base type:");
    final GridBagConstraints gbc_lblBaseType = new GridBagConstraints();
    gbc_lblBaseType.anchor = GridBagConstraints.WEST;
    gbc_lblBaseType.insets = new Insets(0, 0, 5, 5);
    gbc_lblBaseType.gridx = 0;
    gbc_lblBaseType.gridy = 0;
    contentPanel.add(lblBaseType, gbc_lblBaseType);
    baseTypes = new TypeComboBox(new TypeListModel(typeManager.getTypes(), Predicates.and(
        new TypeListModel.ArrayTypesFilter(), new TypeListModel.PrototypesFilter())));
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
    final JLabel lblNewLabel_1 = new JLabel("Pointer level:");
    final GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
    gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
    gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
    gbc_lblNewLabel_1.gridx = 0;
    gbc_lblNewLabel_1.gridy = 1;
    contentPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
    pointerLevel = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
    pointerLevel.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        updatePreview();
      }
    });
    final GridBagConstraints gbc_m_pointerLevel = new GridBagConstraints();
    gbc_m_pointerLevel.anchor = GridBagConstraints.WEST;
    gbc_m_pointerLevel.insets = new Insets(0, 0, 5, 0);
    gbc_m_pointerLevel.gridx = 1;
    gbc_m_pointerLevel.gridy = 1;
    contentPanel.add(pointerLevel, gbc_m_pointerLevel);

    final JLabel lblPreview = new JLabel("Preview:");
    final GridBagConstraints gbc_lblPreview = new GridBagConstraints();
    gbc_lblPreview.insets = new Insets(0, 0, 5, 5);
    gbc_lblPreview.gridx = 0;
    gbc_lblPreview.gridy = 2;
    contentPanel.add(lblPreview, gbc_lblPreview);

    preview = new JTextArea();
    preview.setEditable(false);
    final GridBagConstraints gbc_preview = new GridBagConstraints();
    gbc_preview.insets = new Insets(0, 0, 5, 0);
    gbc_preview.fill = GridBagConstraints.BOTH;
    gbc_preview.gridx = 1;
    gbc_preview.gridy = 2;
    contentPanel.add(preview, gbc_preview);
  }

  private int getPointerLevel() {
    return (Integer) pointerLevel.getValue();
  }

  private BaseType getSelectedType() {
    return (BaseType) baseTypes.getModel().getSelectedItem();
  }

  private void populateControls() {
    if (existingType == null) {
      return;
    }
    pointerLevel.setValue(existingType.getPointerLevel());
    baseTypes.getModel().selectByBaseType(BaseType.getValueType(existingType));
    updatePreview();
  }

  private void updatePreview() {
    if (UserInputTypeValidation.validateComboBox(baseTypes)) {
      preview.setText(String.format("%s my_new_type;",
          BaseType.getPointerTypeName(getSelectedType(), getPointerLevel())));
    }
  }

  private void createOrUpdatePointer() throws CouldntSaveDataException {
    // Note: the pointer level of an existing type can not actually change since pointer types
    // with lower levels already exist and higher levels are simply created.
    final int pointerLevel = getPointerLevel();
    BaseType childBaseType = getSelectedType();
    final int existingLevel = childBaseType.getPointerLevel();
    for (int i = existingLevel + 1; i <= pointerLevel; ++i) {
      final BaseType parentType = typeManager.createPointerType(childBaseType);
      childBaseType = parentType;
    }
  }

  @Override
  protected boolean validateModel() {
    return UserInputTypeValidation.validateComboBox(parent, baseTypes);
  }

  @Override
  protected void updateExistingType() throws CouldntSaveDataException {
    createOrUpdatePointer();
  }

  @Override
  protected void createNewType() throws CouldntSaveDataException {
    createOrUpdatePointer();
  }
}
