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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Component;

/**
 * A panel implementation that displays a model that allows the user to specify a new structure
 * type.
 */
public class StructureTypePanel extends ComplexTypePanel {

  public StructureTypePanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    super(parent, typeManager, existingType);
  }

  public StructureTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null /* existing type */);
  }

  // Note: since the table is always sorted rowIndex is actually a view index that must be converted
  // to a model index.
  private static boolean isNonOverlappingMember(final MemberTable members, final int rowIndex) {
    final MemberTableModel model = members.getModel();
    final int modelIndex = members.convertRowIndexToModel(rowIndex);
    if (rowIndex + 1 < members.getRowCount()) {
      final MemberTableRowData row = model.getRow(modelIndex);
      final MemberTableRowData subsequentRow =
          model.getRow(members.convertRowIndexToModel(rowIndex + 1));
      if (row.getBaseType() == null) {
        return false;
      }
      if (subsequentRow.getIndex() < row.getIndex() + row.getBaseType().getByteSize()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void updateExistingType() throws CouldntDeleteException, CouldntSaveDataException {
    if (!existingType.getName().equals(name.getText())) {
      typeManager.renameType(existingType, name.getText());
    }
    createOrUpdateMembers(existingType, members.getModel());
  }

  @Override
  protected void createNewType() throws NumberFormatException, CouldntSaveDataException {
    createOrUpdateMembers(typeManager.createStructure(name.getText()), members.getModel());
  }

  @Override
  protected boolean validateTableRow(final int rowIndex) {
    return super.validateTableRow(rowIndex) && isNonOverlappingMember(members, rowIndex);
  }

  @Override
  protected boolean validateModel() {
    for (int i = 0; i < members.getModel().getRowCount(); i++) {
      if (!validateTableRow(i)) {
        CMessageBox.showError(parent,
            "Members must not overlap and need a valid name and base type.");
        return false;
      }
    }
    if (name.getText().isEmpty()) {
      CMessageBox.showError(parent, "Please enter a valid name for the structure.");
      name.requestFocusInWindow();
      return false;
    }
    return true;
  }
}
