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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FunctionSelection.CFunctionSelectionDialog;
import com.google.security.zynamics.binnavi.Gui.ResolveFunctions.CResolveAllFunctionDialog;
import com.google.security.zynamics.binnavi.Gui.ResolveFunctions.CResolveFunctionDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.Window;

/**
 * Contains helper functions for working with functions.
 */
public final class CFunctionHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CFunctionHelpers() {}

  /**
   * Tests whether a given function can be forwarded to another module.
   *
   * @param function The function to test.
   *
   * @return True, if the function can be forwarded. False, otherwise.
   */
  public static boolean isForwardableFunction(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE02334: function argument can not be null");

    return ((function.getType() == FunctionType.IMPORT)
        || (function.getType() == FunctionType.THUNK));
  }

  /**
   * Removes resolving information from a given function.
   *
   * @param parent Parent window used for dialogs.
   * @param function The functions whose forwarding information is removed.
   */
  public static void removeResolvedFunction(final Window parent, final INaviFunction function) {
    try {
      function.removeForwardedFunction();
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00132: " + "Could not remove function forwarding";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The forwarding of function '%s' could not be removed.", function.getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The function forwarding of the selected function remains unchanged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  public static void resolveAllFunctions(final Window parent, final IDatabase database) {
    new CResolveAllFunctionDialog(parent, database);
  }

  public static void resolveAllFunctions(final Window parent, final IDatabase database,
      final INaviAddressSpace addressSpace) {
    new CResolveAllFunctionDialog(parent, database, addressSpace);
  }

  public static void resolveAllFunctions(final Window parent, final IDatabase database,
      final INaviModule module) {
    new CResolveAllFunctionDialog(parent, database, module);
  }

  public static void resolveAllFunctions(final Window parent, final IDatabase database,
      final INaviProject project) {
    new CResolveAllFunctionDialog(parent, database, project);
  }

  /**
   * Shows a dialog where the user can resolve a function.
   *
   * @param parent Parent window used for dialogs.
   * @param database Database the function belongs to.
   * @param function Function to be forwarded to another module.
   */
  public static void resolveFunction(final Window parent, final IDatabase database,
      final INaviFunction function) {
    final CFunctionSelectionDialog dlg = new CFunctionSelectionDialog(parent, database);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);

    final INaviFunction selectedFunction = dlg.getSelectedFunction();

    if (selectedFunction != null) {
      try {
        function.setForwardedFunction(selectedFunction);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Shows a dialog where the user can resolve all functions of a module.
   *
   * @param parent Parent window used for resolving.
   * @param database Database the module belongs to.
   * @param module Module whose functions are resolved.
   */
  public static void resolveFunctions(final Window parent, final IDatabase database,
      final INaviModule module) {
    final CResolveFunctionDialog dlg = new CResolveFunctionDialog(parent, database, module);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);
  }
}
