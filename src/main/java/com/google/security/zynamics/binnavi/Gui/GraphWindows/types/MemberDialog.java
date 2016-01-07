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

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
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
import javax.swing.border.EmptyBorder;

/**
 * The dialog which allows the user to create or edit a member type.
 */
public class MemberDialog extends JDialog {

  private final JPanel contentPanel = new JPanel();
  private JTextField memberName;
  private boolean wasCanceled = true;
  private TypeComboBox memberType;

  private MemberDialog(final JFrame owner, final TypeManager typeManager) {
    super(owner, true);
    createControls(typeManager);
  }

  private MemberDialog(final JFrame owner, final TypeManager typeManager, final TypeMember member) {
    super(owner, true);
    createControls(typeManager);
    populateControls(member);
  }

  /**
   * Instantiates the member dialog to create a new member.
   *
   * @param owner The parent component that owns this dialog.
   * @param typeManager The type manager that contains all types in the current type system.
   * @return An instance of the member dialog.
   */
  public static MemberDialog createBuildNewMemberDialog(final JFrame owner,
      final TypeManager typeManager) {
    return new MemberDialog(owner, typeManager);
  }

  /**
   * Instantiates the member dialog with a pre-selected type member in order to edit the member.
   *
   * @param owner THe parent component that owns this dialog.
   * @param typeManager The type manager that contains all types in the current type system.
   * @param member The member that should be edited.
   * @return An instance of the member dialog.
   */
  public static MemberDialog createEditMemberDialog(final JFrame owner,
      final TypeManager typeManager, final TypeMember member) {
    return new MemberDialog(owner, typeManager, member);
  }

  private void createControls(final TypeManager typeManager) {
    setBounds(100, 100, 450, 215);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    final GridBagLayout gbl_contentPanel = new GridBagLayout();
    gbl_contentPanel.columnWidths = new int[] {0, 0, 0};
    gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0};
    gbl_contentPanel.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gbl_contentPanel.rowWeights = new double[] {0.0, 0.0, 0.0, Double.MIN_VALUE};
    contentPanel.setLayout(gbl_contentPanel);
    {
      final JLabel lblMemberName = new JLabel("Member name:");
      final GridBagConstraints gbc_lblMemberName = new GridBagConstraints();
      gbc_lblMemberName.insets = new Insets(0, 0, 5, 5);
      gbc_lblMemberName.anchor = GridBagConstraints.EAST;
      gbc_lblMemberName.gridx = 0;
      gbc_lblMemberName.gridy = 0;
      contentPanel.add(lblMemberName, gbc_lblMemberName);
    }
    {
      memberName = new JTextField();
      final GridBagConstraints gbc_memberName = new GridBagConstraints();
      gbc_memberName.insets = new Insets(0, 0, 5, 0);
      gbc_memberName.fill = GridBagConstraints.HORIZONTAL;
      gbc_memberName.gridx = 1;
      gbc_memberName.gridy = 0;
      contentPanel.add(memberName, gbc_memberName);
      memberName.setColumns(10);
    }
    {
      final JLabel lblMemberType = new JLabel("Member type:");
      final GridBagConstraints gbc_lblMemberType = new GridBagConstraints();
      gbc_lblMemberType.anchor = GridBagConstraints.EAST;
      gbc_lblMemberType.insets = new Insets(0, 0, 5, 5);
      gbc_lblMemberType.gridx = 0;
      gbc_lblMemberType.gridy = 1;
      contentPanel.add(lblMemberType, gbc_lblMemberType);
    }
    {
      memberType = new TypeComboBox(
          new TypeListModel(typeManager.getTypes(), new TypeListModel.PrototypesFilter()));
      final GridBagConstraints gbc_memberType = new GridBagConstraints();
      gbc_memberType.insets = new Insets(0, 0, 5, 0);
      gbc_memberType.fill = GridBagConstraints.HORIZONTAL;
      gbc_memberType.gridx = 1;
      gbc_memberType.gridy = 1;
      contentPanel.add(memberType, gbc_memberType);
    }
    {
      final JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new OkActionListener());
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(final ActionEvent e) {
            dispose();
          }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
  }

  private void populateControls(final TypeMember member) {
    memberName.setText(member.getName());
    memberType.getModel().selectByBaseType(member.getBaseType());
  }

  /**
   * Returns the base type that was selected. Null if no selection was made.
   *
   * @return The base type that was selected.
   */
  public BaseType getBaseType() {
    return (BaseType) memberType.getModel().getSelectedItem();
  }

  /**
   * Returns the name of the new member.
   *
   * @return The name of the new member.
   */
  public String getMemberName() {
    return memberName.getText();
  }

  /**
   * Returns true iff the dialog was closed without using the ok button.
   *
   * @return True iff the dialog was closed without using the ok button.
   */
  public boolean wasCanceled() {
    return wasCanceled;
  }

  /**
   * Validates the user inputs before closing the dialog.
   */
  private class OkActionListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent e) {
      if (getBaseType() == null) {
        CMessageBox.showWarning(MemberDialog.this, "Please select a base type.");
        return;
      }
      if (getMemberName().isEmpty()) {
        CMessageBox.showWarning(MemberDialog.this, "Please enter a name for the member.");
        return;
      }
      wasCanceled = false;
      dispose();
    }
  }
}
