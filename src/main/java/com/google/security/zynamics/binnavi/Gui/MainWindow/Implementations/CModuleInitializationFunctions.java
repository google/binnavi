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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import java.awt.Window;

import javax.swing.JTree;
import javax.swing.SwingUtilities;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Loaders.CModuleLoader;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Contains methods for initializing modules.
 */
public final class CModuleInitializationFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CModuleInitializationFunctions() {
  }

  /**
   * Initializes a module in non-threaded way.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module to initialize.
   */
  private static void initializeModuleInternal(final Window parent, final INaviModule module) {
    if (!module.getConfiguration().getRawModule().isComplete()) {
      final String innerMessage =
          "E00059: "
              + "The module could not be initialized because the imported data is incomplete";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The module '%s' could not be initialized.", module.getConfiguration().getName()),
              new String[] {"The imported data is incomplete because the exporter failed."},
              new String[] {"The module was not initialized."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription);

      return;
    }

    if (module.isInitializing()) {
      return;
    }

    final CModuleInitializerOperation operation = new CModuleInitializerOperation(module);

    try {
      module.initialize();
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00060: " + "The module could not be initialized";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The module '%s' could not be initialized.", module.getConfiguration().getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The module was not initialized."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);

      return;
    } finally {
      operation.stop();
    }
  }

  /**
   * Initializes and loads a module in a thread.
   * 
   * @param projectTree The project tree to expand once the module was loaded.
   * @param module The module to load.
   */
  public static void initializeAndLoadModule(final JTree projectTree, final INaviModule module) {
    new Thread() {
      @Override
      public void run() {
        initializeModuleInternal(SwingUtilities.getWindowAncestor(projectTree), module);

        if (module.isInitialized()) {
          CModuleLoader.loadModule(projectTree, module);
        }
      }
    }.start();
  }

  /**
   * Initializes a module in a thread.
   * 
   * @param projectTree The project tree to expand once the module was loaded.
   * @param module The module to load.
   */
  public static void initializeModule(final JTree projectTree, final INaviModule module) {
    new Thread() {
      @Override
      public void run() {
        initializeModuleInternal(SwingUtilities.getWindowAncestor(projectTree), module);
      }
    }.start();
  }
}
