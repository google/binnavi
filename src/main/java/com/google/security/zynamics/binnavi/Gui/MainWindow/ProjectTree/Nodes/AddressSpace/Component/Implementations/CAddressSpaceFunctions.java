/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Implementations;



import java.awt.Window;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;


/**
 * Contains implementations for the functions available from the address space component.
 */
public final class CAddressSpaceFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CAddressSpaceFunctions() {
  }

  /**
   * Saves the address space description to the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param addressSpace The address space whose description is changed.
   * @param description The new description of the address space.
   */
  public static void saveDescription(final Window parent, final INaviAddressSpace addressSpace,
      final String description) {
    try {
      addressSpace.getConfiguration().setDescription(description);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00154: " + "Could not save address space description";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The new description of the address space '%s' could not be saved.", addressSpace
                  .getConfiguration().getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The address space keeps its old description."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Saves the address space name to the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param addressSpace The address space whose name is changed.
   * @param name The new name of the address space.
   */
  public static void saveName(final Window parent, final INaviAddressSpace addressSpace,
      final String name) {
    try {
      addressSpace.getConfiguration().setName(name);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00153: " + "Could not save address space name";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The new name of the address space '%s' could not be saved.", addressSpace
                  .getConfiguration().getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The address space keeps its old name."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

}
