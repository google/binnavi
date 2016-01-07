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
 * A panel implementation that displays a model that allows the user to specify a new union type.
 */
public class UnionTypePanel extends ComplexTypePanel {

  public UnionTypePanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    super(parent, typeManager, existingType);
    members.getModel().setOffsetEditable(false);
  }

  public UnionTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null /* existing type */);
  }

  @Override
  protected void updateExistingType() throws CouldntSaveDataException, CouldntDeleteException {
    if (!existingType.getName().equals(name.getText())) {
      typeManager.updateType(existingType, name.getText(), 0 /* size */, false /* is signed */);
    }
    createOrUpdateMembers(existingType, members.getModel());
  }

  @Override
  protected boolean validateTableRow(final int rowIndex) {
    return super.validateTableRow(rowIndex)
        && members.getModel().getRow(members.convertRowIndexToModel(rowIndex)).getIndex() == 0;
  }

  @Override
  protected void createNewType() throws NumberFormatException, CouldntSaveDataException {
    createOrUpdateMembers(typeManager.createUnion(name.getText()), members.getModel());
  }

  @Override
  protected boolean validateModel() {
    final MemberTableModel model = members.getModel();
    for (int i = 0; i < model.getRowCount(); i++) {
      if (!validateTableRow(i)) {
        CMessageBox.showError(parent,
            "Please assign a valid name and base type, and set the offset to zero for unions.");
        return false;
      }
    }
    if (name.getText().isEmpty()) {
      CMessageBox.showError(parent, "Please enter a valid name for the union.");
      name.requestFocusInWindow();
    }
    return true;
  }
}
