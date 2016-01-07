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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * The panel that allows the user to create atomic value types.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class AtomicTypePanel extends TypeDialogPanel {

  private JTextField size;
  private JTextField name;
  private JCheckBox signed;
  private JLabel lblPreview;
  private JTextArea preview;

  /**
   * Parameterless constructor to keep WindowBuilder happy.
   */
  @SuppressWarnings("unused")
  private AtomicTypePanel() {
    this(null, null);
  }

  /**
   * Creates a new instance in order to build a new type from scratch.
   *
   * @param parent The parent control of the panel.
   * @param typeManager The type manager that holds the current type system.
   */
  public AtomicTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null);
  }

  /**
   *
   * Creates a new instance in order to edit an existing type.
   *
   * @param parent The parent control of the panel.
   * @param typeManager The type manager that holds the current type system.
   * @param existingType The base type that should be edited.
   */
  public AtomicTypePanel(
      final Component parent, final TypeManager typeManager, final BaseType existingType) {
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
    gbl_m_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0};
    gbl_m_contentPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gbl_m_contentPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
    contentPanel.setLayout(gbl_m_contentPanel);
    final JLabel lblNewLabel = new JLabel("Name:");
    final GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
    gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
    gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
    gbc_lblNewLabel.gridx = 0;
    gbc_lblNewLabel.gridy = 0;
    contentPanel.add(lblNewLabel, gbc_lblNewLabel);
    name = new JTextField();
    name.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        updatePreview();
      }
    });
    final GridBagConstraints gbc_m_name = new GridBagConstraints();
    gbc_m_name.insets = new Insets(0, 0, 5, 0);
    gbc_m_name.fill = GridBagConstraints.HORIZONTAL;
    gbc_m_name.gridx = 1;
    gbc_m_name.gridy = 0;
    contentPanel.add(name, gbc_m_name);
    name.setColumns(10);
    final JLabel lblSize = new JLabel("Size (bits):");
    final GridBagConstraints gbc_lblSize = new GridBagConstraints();
    gbc_lblSize.anchor = GridBagConstraints.WEST;
    gbc_lblSize.insets = new Insets(0, 0, 5, 5);
    gbc_lblSize.gridx = 0;
    gbc_lblSize.gridy = 1;
    contentPanel.add(lblSize, gbc_lblSize);
    size = new JTextField();
    final GridBagConstraints gbc_m_size = new GridBagConstraints();
    gbc_m_size.insets = new Insets(0, 0, 5, 0);
    gbc_m_size.fill = GridBagConstraints.HORIZONTAL;
    gbc_m_size.gridx = 1;
    gbc_m_size.gridy = 1;
    contentPanel.add(size, gbc_m_size);
    size.setColumns(10);
    signed = new JCheckBox("Signed");
    final GridBagConstraints gbc_m_signed = new GridBagConstraints();
    gbc_m_signed.anchor = GridBagConstraints.WEST;
    gbc_m_signed.insets = new Insets(0, 0, 5, 5);
    gbc_m_signed.gridx = 0;
    gbc_m_signed.gridy = 2;
    contentPanel.add(signed, gbc_m_signed);

    lblPreview = new JLabel("Preview:");
    final GridBagConstraints gbc_lblPreview = new GridBagConstraints();
    gbc_lblPreview.insets = new Insets(0, 0, 5, 5);
    gbc_lblPreview.gridx = 0;
    gbc_lblPreview.gridy = 3;
    contentPanel.add(lblPreview, gbc_lblPreview);

    preview = new JTextArea();
    preview.setEditable(false);
    final GridBagConstraints gbc_preview = new GridBagConstraints();
    gbc_preview.insets = new Insets(0, 0, 5, 0);
    gbc_preview.fill = GridBagConstraints.BOTH;
    gbc_preview.gridx = 1;
    gbc_preview.gridy = 3;
    contentPanel.add(preview, gbc_preview);
  }

  private void populateControls() {
    if (existingType == null) {
      return;
    }
    name.setText(existingType.getName());
    signed.setSelected(existingType.isSigned());
    size.setText(String.valueOf(existingType.getBitSize()));
    updatePreview();
  }

  private void updatePreview() {
    if (!name.getText().isEmpty()) {
      preview.setText(String.format("%s %s", name.getText(), " my_new_type;"));
    }
  }

  private boolean validateOnCreate() {
    return UserInputTypeValidation.validateTypeName(parent, typeManager, name)
        && UserInputTypeValidation.validateTypeSize(parent, size);
  }

  private boolean validateOnUpdate() {
    return !name.getText().isEmpty() && UserInputTypeValidation.validateTypeSize(parent, size);
  }

  @Override
  protected boolean validateModel() {
    return (existingType == null) ? validateOnCreate() : validateOnUpdate();
  }

  @Override
  protected void updateExistingType() throws CouldntSaveDataException {
    typeManager.updateType(
        existingType, name.getText(), Integer.parseInt(size.getText()), signed.isSelected());
  }

  @Override
  protected void createNewType() throws CouldntSaveDataException {
    typeManager.createAtomicType(
        name.getText(), Integer.parseInt(size.getText()), signed.isSelected());
  }
}
