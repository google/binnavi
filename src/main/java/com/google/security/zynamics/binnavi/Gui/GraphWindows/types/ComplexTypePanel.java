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
import com.google.security.zynamics.binnavi.disassembly.types.BaseTypeCategory;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

/**
 * A panel that allows the user to create a new complex, i.e. compound type.
 */
public abstract class ComplexTypePanel extends TypeDialogPanel {

  JTextField name;
  MemberTable members;

  public ComplexTypePanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    super(parent, typeManager, existingType);
    createControls(typeManager.getTypes());
    populateControls();
  }

  private void populateControls() {
    if (existingType == null) {
      return;
    }
    name.setText(existingType.getName());
    final MemberTableModel model = members.getModel();
    for (final TypeMember member : existingType) {
      model.addRow(new MemberTableRowData(member));
    }
  }

  private void createControls(final List<BaseType> baseTypes) {
    setBorder(new EmptyBorder(5, 5, 5, 5));
    final GridBagLayout gridBagLayout = new GridBagLayout();
    gridBagLayout.columnWidths = new int[] {0, 0, 0};
    gridBagLayout.rowHeights = new int[] {0, 0, 0, 0, 0};
    gridBagLayout.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
    gridBagLayout.rowWeights = new double[] {0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
    setLayout(gridBagLayout);

    final JLabel lblName = new JLabel("Name:");
    GridBagConstraints gbcLblName = new GridBagConstraints();
    gbcLblName.insets = new Insets(0, 0, 5, 5);
    gbcLblName.anchor = GridBagConstraints.WEST;
    gbcLblName.gridx = 0;
    gbcLblName.gridy = 0;
    add(lblName, gbcLblName);

    name = new JTextField();
    GridBagConstraints gbcName = new GridBagConstraints();
    gbcName.insets = new Insets(0, 0, 5, 0);
    gbcName.fill = GridBagConstraints.HORIZONTAL;
    gbcName.gridx = 1;
    gbcName.gridy = 0;
    add(name, gbcName);
    name.setColumns(10);

    final JLabel lblMembers = new JLabel("Members:");
    final GridBagConstraints gbcLblMembers = new GridBagConstraints();
    gbcLblMembers.anchor = GridBagConstraints.WEST;
    gbcLblMembers.insets = new Insets(0, 0, 5, 5);
    gbcLblMembers.gridx = 0;
    gbcLblMembers.gridy = 2;
    add(lblMembers, gbcLblMembers);

    final MemberTableModel model = new MemberTableModel();
    model.addTableModelListener(new MemberTableModelListener());
    members = new MemberTable(model, baseTypes);
    final TableRowSorter<MemberTableModel> sorter = new TableRowSorter<MemberTableModel>(model);
    // Triggers immediate re-sort when offset is updated.
    sorter.setSortsOnUpdates(true);
    sorter.setSortable(MemberTableModel.NAME_COLUMN, false);
    sorter.setSortable(MemberTableModel.TYPE_COLUMN, false);
    // Initial state is ascending sort order.
    sorter.toggleSortOrder(MemberTableModel.INDEX_COLUMN);
    members.setRowSorter(sorter);

    final GridBagConstraints gbcMembers = new GridBagConstraints();
    gbcMembers.insets = new Insets(0, 0, 5, 0);
    gbcMembers.fill = GridBagConstraints.BOTH;
    gbcMembers.gridx = 1;
    gbcMembers.gridy = 2;
    add(new JScrollPane(members), gbcMembers);

    final JLabel lblPreview = new JLabel("Preview:");
    final GridBagConstraints gbcLblPreview = new GridBagConstraints();
    gbcLblPreview.anchor = GridBagConstraints.WEST;
    gbcLblPreview.insets = new Insets(0, 0, 0, 5);
    gbcLblPreview.gridx = 0;
    gbcLblPreview.gridy = 3;
    add(lblPreview, gbcLblPreview);

    final JTextArea preview = new JTextArea();
    final GridBagConstraints gbcPreview = new GridBagConstraints();
    gbcPreview.fill = GridBagConstraints.BOTH;
    gbcPreview.gridx = 1;
    gbcPreview.gridy = 3;
    add(preview, gbcPreview);
  }

  /**
   * Validates that the given row contains valid data to describe a single member. Since the table
   * is always sorted the row index has to be converted into a model index.
   */
  protected boolean validateTableRow(final int rowIndex) {
    final MemberTableRowData row =
        members.getModel().getRow(members.convertRowIndexToModel(rowIndex));
    return (!row.getName().isEmpty() && row.getBaseType() != null);
  }

  private boolean hasMemberChanged(final TypeMember typeMember, final MemberTableRowData row) {
    final boolean condition = !row.getName().equals(typeMember.getName())
        || !row.getBaseType().equals(typeMember.getBaseType());
    if (typeMember.getParentType().getCategory() == BaseTypeCategory.FUNCTION_PROTOTYPE) {
      return condition || !row.getIndex().equals(typeMember.getArgumentIndex());
    } else {
      return condition || !row.getIndex().equals(typeMember.getByteOffset());
    }
  }

  protected void createOrUpdateMembers(final BaseType containingType, final MemberTableModel model)
      throws CouldntSaveDataException {
    for (int i = 0; i < model.getRowCount(); i++) {
      final MemberTableRowData row = model.getRow(i);
      final int index = row.getIndex();
      final int bitSize = index * 8;
      final boolean isNew = row.getExistingMember() == null;
      switch (containingType.getCategory()) {
        case STRUCT:
          if (isNew) {
            typeManager.createStructureMember(containingType, row.getBaseType(), row.getName(),
                bitSize);
          } else if (hasMemberChanged(row.getExistingMember(), row)) {
            typeManager.updateStructureMember(row.getExistingMember(), row.getBaseType(),
                row.getName(), bitSize);
          }
          break;
        case UNION:
          if (isNew) {
            typeManager.createUnionMember(containingType, row.getBaseType(), row.getName());
          } else if (hasMemberChanged(row.getExistingMember(), row)) {
            typeManager.updateUnionMember(row.getExistingMember(), row.getBaseType(),
                row.getName());
          }
          break;
        case FUNCTION_PROTOTYPE:
          if (isNew) {
            typeManager.createFunctionPrototypeMember(containingType, row.getBaseType(),
                row.getName(), index);
          } else if (hasMemberChanged(row.getExistingMember(), row)) {
            typeManager.updateFunctionPrototypeMember(row.getExistingMember(), row.getBaseType(),
                row.getName(), index);
          }
          break;
        default:
          throw new IllegalStateException("Error: can not update member in non compound type.");
      }
    }
  }

  /**
   * Marks all (invalid) rows whenever the table model is changed.
   */
  private class MemberTableModelListener implements TableModelListener {

    @Override
    public void tableChanged(final TableModelEvent event) {
      if (event.getType() == TableModelEvent.INSERT || event.getType() == TableModelEvent.UPDATE) {
        final MemberTableModel model = members.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
          final int modelIndex = members.convertRowIndexToModel(i);
          boolean validate = validateTableRow(i);
          model.markRow(modelIndex, validate);
        }
      }
    }
  }
}
