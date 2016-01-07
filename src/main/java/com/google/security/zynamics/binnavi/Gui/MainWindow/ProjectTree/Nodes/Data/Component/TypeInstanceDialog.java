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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeComboBox;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeListModel;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog to create a new or edit an existing type instance.
 */
public class TypeInstanceDialog extends JDialog {

  private static final long serialVersionUID = -7912640983889588677L;
  private JTextField instanceName;
  private TypeComboBox types;
  private boolean okClicked;
  private JTextField sectionOffset;
  private final Section section;
  private static Long sanitizedOffset;

  private TypeInstanceDialog(final JFrame owner,
      final String dialogTitle,
      final TypeListModel model,
      final TypeInstance instance,
      final Section section,
      final Long offset) {
    super(owner, dialogTitle, true);
    this.section = section;
    createControls(model);
    populateControls(instance, offset);
  }

  private static boolean isValidSectionOffset(final JTextField textField, final Section section) {
    Preconditions.checkNotNull(textField, "Error: textField argument can not be null");
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    if (textField.getText().isEmpty()) {
      return false;
    }
    try {
      String textFieldString = textField.getText().toLowerCase();
      boolean hex = false;
      if (textFieldString.startsWith("0x")) {
        textFieldString = textFieldString.substring(2);
        hex = true;
      }

      final Long offset = textFieldString.matches("^[0-9A-Fa-f]+$") && hex == true ? Long.parseLong(
          textFieldString, 16)
          : Long.parseLong(textFieldString, 10);

      if (section.isValidOffset(offset)) {
        sanitizedOffset = offset;
        return true;
      }
      if (section.isValidAddress(offset)) {
        sanitizedOffset = offset - section.getStartAddress().toLong();
        return true;
      }
      return false;
    } catch (final NumberFormatException exception) {
      return false;
    }
  }

  /**
   * Creates a new instance of the type instance dialog in order to create a new type instance. The
   * dialog doesn't actually create a new type instance in the type system, instead it provides
   * validated user input.
   *
   * @param owner The window which owns the instantiated dialog.
   * @param typeManager The type manager that holds the current type system.
   * @param section The section where the type instance should be created.
   * @param offset The section relative offset of the type instance. The user needs to enter a valid
   *        offset in the dialog if this value is null.
   * @return A new instance of the dialog.
   */
  public static TypeInstanceDialog instantiateCreateTypeInstanceDialog(final JFrame owner,
      final TypeManager typeManager, final Section section, final Long offset) {
    return new TypeInstanceDialog(owner,
        "Create type instance",
        new TypeListModel(typeManager.getTypes(), new TypeListModel.PrototypesFilter()),
        null,
        section,
        offset);
  }

  /**
   * Creates a new instance of the type instance dialog in order to edit an existing type instance.
   * The dialog doesn't actually perform the changes on the type instance, instead it provides
   * validated user input.
   *
   * @param owner The window which owns the instantiated dialog.
   * @param typeManager The type manager that holds the current type system.
   * @param instance The existing type instance.
   * @return A new instance of the dialog.
   */
  public static TypeInstanceDialog instantiateEditTypeInstanceDialog(final JFrame owner,
      final TypeManager typeManager, final TypeInstance instance) {
    final TypeInstanceDialog dialog = new TypeInstanceDialog(owner,
        "Edit type instance",
        new TypeListModel(typeManager.getTypes(), new TypeListModel.PrototypesFilter()),
        instance,
        instance.getSection(),
        instance.getAddress().getOffset());
    // For now, we only allow the user to change the name of the type instance
    // so we disable all other controls.
    dialog.types.setEnabled(false);
    dialog.sectionOffset.setEnabled(false);
    return dialog;
  }

  private void createControls(final TypeListModel model) {
    final JPanel panel = new JPanel();
    getContentPane().add(panel, BorderLayout.SOUTH);
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    final JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (validateUserInput()) {
          okClicked = true;
          dispose();
        }
      }
    });
    panel.add(okButton);

    final JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dispose();
      }
    });
    panel.add(cancelButton);

    types = new TypeComboBox(model);
    getContentPane().add(types, BorderLayout.CENTER);

    final JPanel panel_1 = new JPanel();
    getContentPane().add(panel_1, BorderLayout.NORTH);
    final GridBagLayout gbl_panel_1 = new GridBagLayout();
    gbl_panel_1.columnWidths = new int[] {166, 109, 0};
    gbl_panel_1.rowHeights = new int[] {0, 0, 15, 0};
    gbl_panel_1.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gbl_panel_1.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
    panel_1.setLayout(gbl_panel_1);

    final JLabel lblInstanceName_1 = new JLabel("Instance name:");
    final GridBagConstraints gbc_lblInstanceName_1 = new GridBagConstraints();
    gbc_lblInstanceName_1.anchor = GridBagConstraints.WEST;
    gbc_lblInstanceName_1.insets = new Insets(0, 0, 5, 5);
    gbc_lblInstanceName_1.gridx = 0;
    gbc_lblInstanceName_1.gridy = 0;
    panel_1.add(lblInstanceName_1, gbc_lblInstanceName_1);

    instanceName = new JTextField();
    final GridBagConstraints gbc_instanceName = new GridBagConstraints();
    gbc_instanceName.insets = new Insets(0, 0, 5, 0);
    gbc_instanceName.fill = GridBagConstraints.HORIZONTAL;
    gbc_instanceName.gridx = 1;
    gbc_instanceName.gridy = 0;
    panel_1.add(instanceName, gbc_instanceName);
    instanceName.setColumns(10);

    final JLabel lblSectionOffset = new JLabel("Section offset:");
    final GridBagConstraints gbc_lblSectionOffset = new GridBagConstraints();
    gbc_lblSectionOffset.anchor = GridBagConstraints.WEST;
    gbc_lblSectionOffset.insets = new Insets(0, 0, 5, 5);
    gbc_lblSectionOffset.gridx = 0;
    gbc_lblSectionOffset.gridy = 1;
    panel_1.add(lblSectionOffset, gbc_lblSectionOffset);

    sectionOffset = new JTextField();
    final GridBagConstraints gbc_sectionOffset = new GridBagConstraints();
    gbc_sectionOffset.insets = new Insets(0, 0, 5, 0);
    gbc_sectionOffset.fill = GridBagConstraints.HORIZONTAL;
    gbc_sectionOffset.gridx = 1;
    gbc_sectionOffset.gridy = 1;
    panel_1.add(sectionOffset, gbc_sectionOffset);
    sectionOffset.setColumns(10);

    pack();
    new CDialogEscaper(this);
  }

  private void populateControls(final TypeInstance instance, final Long offset) {
    if (instance != null) {
      instanceName.setText(instance.getName());
      types.getModel().selectByBaseType(instance.getBaseType());
    }
    if (offset != null) {
      sectionOffset.setText(String.valueOf(offset));
    }
  }

  private boolean validateUserInput() {
    if (getInstanceType() == null || getInstanceName() == null) {
      CMessageBox.showWarning(this,
          "Please enter a name for the type instance and select a base type.");
      return false;
    } else if (!isValidSectionOffset(sectionOffset, section)) {
      CMessageBox.showWarning(this, String.format(
          "Please enter a valid section offset!\n The valid range for '%s' is [0x%X-0x%X).",
          section.getName(), section.getStartAddress().toLong(), section.getEndAddress().toLong()));
      return false;
    } else {
      return true;
    }
  }

  public String getInstanceName() {
    return instanceName.getText();
  }

  public BaseType getInstanceType() {
    return (BaseType) types.getSelectedItem();
  }

  public long getSectionOffset() {
    return sanitizedOffset;
  }

  public boolean wasOkClicked() {
    return okClicked;
  }
}
