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
 * A panel implementation that displays a model that allows the user to edit and create function
 * prototype types.
 */
public class PrototypeTypePanel extends ComplexTypePanel {

  public PrototypeTypePanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    super(parent, typeManager, existingType);
  }

  public PrototypeTypePanel(final Component parent, final TypeManager typeManager) {
    this(parent, typeManager, null /* existing type */);
  }

  @Override
  protected boolean validateModel() {
    for (int i = 0; i < members.getModel().getRowCount(); i++) {
      if (!validateTableRow(i)) {
        CMessageBox.showError(parent, "Prototype arguments must not have the same argument index.");
        return false;
      }
    }
    return true;
  }

  @Override
  protected void updateExistingType() throws CouldntSaveDataException, CouldntDeleteException {
    if (!existingType.getName().equals(name.getText())) {
      typeManager.renameType(existingType, name.getText());
    }
    createOrUpdateMembers(existingType, members.getModel());
  }

  @Override
  protected void createNewType() throws CouldntSaveDataException {
    createOrUpdateMembers(typeManager.createPrototype(), members.getModel());
  }
}
