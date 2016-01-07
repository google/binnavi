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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypesTree.TypeSelectionPath;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeHelpers;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.disassembly.types.TypeSubstitution;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

/**
 * This dialog lets the user create or update a type substitution that is assigned to an expression
 * in the disassembly listing, i.e. an operand tree node.
 *
 */
public class TypeSubstitutionDialog extends JDialog {
  private TypesTree types;
  private final TypeManager typeManager;
  private final INaviOperandTreeNode selectedNode;
  private final int addendValue;
  private final JTextArea preview = new JTextArea();

  public TypeSubstitutionDialog(final JFrame owner, final INaviOperandTreeNode selectedNode,
      final TypeManager typeManager, final BaseType stackFrame) {
    super(owner, "Select operand type", true);
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "IE02859: Type manager can not be null.");
    this.selectedNode =
        Preconditions.checkNotNull(selectedNode, "IE02860: Operand tree node can not be null.");
    // Type substitutions work with bit offsets so we convert the byte immediate value.
    addendValue =
        selectedNode.hasAddendSibling() ? (int) (selectedNode.determineAddendValue() * 8) : 0;
    createControls(stackFrame);
    populateControls(selectedNode.getTypeSubstitution());
  }

  private void createControls(final BaseType stackFrame) {
    setBounds(100, 100, 691, 470);
    final JPanel panel = new JPanel();
    getContentPane().add(panel, BorderLayout.SOUTH);
    panel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    final JButton buttonOk = new JButton("OK");
    buttonOk.addActionListener(new OkActionListener());
    panel.add(buttonOk);

    final JButton buttonCancel = new JButton("Cancel");
    buttonCancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        dispose();
      }
    });
    buttonCancel.setActionCommand("Cancel");
    panel.add(buttonCancel);

    final JPanel panel1 = new JPanel();
    getContentPane().add(panel1, BorderLayout.NORTH);
    final GridBagLayout gblPanel1 = new GridBagLayout();
    gblPanel1.columnWidths = new int[] {0, 143, 114, 0};
    gblPanel1.rowHeights = new int[] {23, 0};
    gblPanel1.columnWeights = new double[] {0.0, 0.0, 1.0, Double.MIN_VALUE};
    gblPanel1.rowWeights = new double[] {0.0, Double.MIN_VALUE};
    panel1.setLayout(gblPanel1);

    JCheckBox onlyFitting = new JCheckBox("Only show structs that fit immediate offset");
    onlyFitting.setEnabled(false);
    GridBagConstraints gbcOnlyFitting = new GridBagConstraints();
    gbcOnlyFitting.anchor = GridBagConstraints.WEST;
    gbcOnlyFitting.insets = new Insets(0, 0, 0, 5);
    gbcOnlyFitting.gridx = 0;
    gbcOnlyFitting.gridy = 0;
    panel1.add(onlyFitting, gbcOnlyFitting);

    types = new TypesTree();
    types.setModel(new TypesTreeModel(typeManager, new LocalTypesFilter(stackFrame)));
    types.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    types.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent event) {
        final TypeSelectionPath path = types.determineTypePath();
        if (validateUserInput(path)) {
          updatePreview(path);
        }
      }
    });
    final JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
    centerPanel.add(new JScrollPane(types));
    centerPanel.add(preview);
    getContentPane().add(centerPanel, BorderLayout.CENTER);
  }

  private void createOrUpdateSubstitution(final TypeSelectionPath path)
      throws CouldntSaveDataException {
    final int offset = path.determineTotalMemberOffset();
    final BaseType baseType = path.getRootType();
    final int position = selectedNode.getOperandPosition();
    final IAddress address = selectedNode.getInstructionAddress();
    final TypeSubstitution substitution = selectedNode.getTypeSubstitution();
    final List<TypeMember> memberPath = path.getMembers();
    if (substitution == null) {
      typeManager.createTypeSubstitution(
          selectedNode, baseType, memberPath, position, offset, address);
    } else {
      typeManager.updateTypeSubstitution(
          selectedNode, substitution, baseType, path.getMembers(), offset);
    }
  }

  private void populateControls(final TypeSubstitution typeSubstitution) {
    // TODO(jannewger): pre-select element in tree if type substitution already exists.
  }

  private boolean validateUserInput(final TypeSelectionPath path) {
    if (!path.hasSelection()) {
      preview.setText("Error: No type selected.");
      return false;
    }
    if (path.containsUnion() && path.containsBaseTypeOnly()) {
      preview.setText(
          "Selection is ambigious due to contained union. Please select a member explicitly.");
      return false;
    }
    final int effectiveOffset = path.determineTotalMemberOffset() + addendValue;
    if (!BaseTypeHelpers.isValidOffset(path.getRootType(), effectiveOffset)) {
      final int displayedOffset =
          effectiveOffset < 0 ? (effectiveOffset - 7) / 8 : (effectiveOffset + 7) / 8;
      preview.setText(String.format(
          "Cannot create substitution: offset %d points outside of %s.", displayedOffset,
          path.getRootType().getName()));
      return false;
    }
    preview.setText("");
    return true;
  }

  private void updatePreview(final TypeSelectionPath path) {
    // TODO(jannewger): create string preview of type substitution if applied to the operand
    // expression.
  }

  /**
   * Validates user input before closing the dialog.
   */
  private class OkActionListener implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
      try {
        final TypeSelectionPath path = types.determineTypePath();
        if (validateUserInput(path)) {
          createOrUpdateSubstitution(path);
          dispose();
        }
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);
        dispose();
      }
    }
  }
}
