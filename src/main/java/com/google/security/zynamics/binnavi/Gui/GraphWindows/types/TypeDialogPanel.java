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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;

import java.awt.Component;

import javax.swing.JPanel;

/**
 * The abstract class that must be implemented by the different type dialog panels.
 */
public abstract class TypeDialogPanel extends JPanel {

  protected final Component parent;
  protected final TypeManager typeManager;
  // Refers to an existing type if the panel was opened for editing a type. Can be null if a new
  // type should be created.
  protected final BaseType existingType;

  /**
   * Creates a new type panel to be placed inside the tabs of the type dialog.
   *
   * @param parent The parent GUI component.
   * @param typeManager The type manager that holds the current type system.
   * @param existingType The base type instance that should be edited by this type panel. This
   *        argument can be null. In this case a new base type is created from scratch.
   */
  public TypeDialogPanel(final Component parent, final TypeManager typeManager,
      final BaseType existingType) {
    this.parent = Preconditions.checkNotNull(parent, "Error: parent can not be null.");
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "Error: type manager can not be null.");
    this.existingType = existingType;
  }

  /**
   * Validates user input and creates a new type or updates an existing type based on the user input
   * given in the respective panel. If the user input was invalid, no type is created or updated and
   * false is returned.
   *
   * @return Returns true iff the user input was valid and the type was created or updated.
   *
   * @throws CouldntSaveDataException Thrown if the new type could not be saved to the database.
   * @throws CouldntDeleteException Thrown if elements of the type could not be deleted from the
   *         database during the update process.
   */
  public boolean createOrUpdateType() throws CouldntSaveDataException, CouldntDeleteException {
    if (!validateModel()) {
      return false;
    }
    if (existingType == null) {
      createNewType();
    } else {
      updateExistingType();
    }
    return true;
  }

  /**
   * Tests if the type model meets all constraints in order to create or update a corresponding
   * type. The implementation should display an error if that's not the case.
   */
  protected abstract boolean validateModel();

  /**
   * Updates the given type in the type system.
   *
   * @throws CouldntSaveDataException Thrown if the changes couldn't be saved to the type backend.
   * @throws CouldntDeleteException Thrown if the changes couldn't be deleted from the type backend.
   */
  protected abstract void updateExistingType() throws CouldntSaveDataException,
      CouldntDeleteException;

  /**
   * Creates a new type in the type system.
   *
   * @throws CouldntSaveDataException Thrown if the new type couldn't be stored in the type backend.
   */
  protected abstract void createNewType() throws CouldntSaveDataException;
}
