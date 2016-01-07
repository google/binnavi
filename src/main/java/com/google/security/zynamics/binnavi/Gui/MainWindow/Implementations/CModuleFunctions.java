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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Loaders.CModuleLoader;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Importers.CFailedImport;
import com.google.security.zynamics.binnavi.Importers.CImporterFactory;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CViewInserter;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;

/**
 * Contains helper functions for working with modules.
 */
public final class CModuleFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CModuleFunctions() {
  }

  /**
   * Copies a view into a module view.
   * 
   * @param parent Parent window used for dialogs.
   * @param module Module where the copied view is stored.
   * @param view The view to copy.
   */
  public static void copyView(final JFrame parent, final INaviModule module, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE01832: Parent argument can not be null");
    Preconditions.checkNotNull(module, "IE01833: Module argument can not be null");
    Preconditions.checkNotNull(view, "IE01834: View argument can not be null");

    if (module.getContent().getViewContainer().hasView(view)) {
      if (!view.isLoaded()) {
        try {
          view.load();
        } catch (final CouldntLoadDataException e) {
          CUtilityFunctions.logException(e);

          final String innerMessage = "E00133: View could not be copied";
          final String innerDescription =
              CUtilityFunctions.createDescription(String.format(
                  "The view '%s' could not be copied because it could not be loaded.",
                  view.getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The new view was not created."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);

          return;
        } catch (CPartialLoadException | LoadCancelledException e) {
          CUtilityFunctions.logException(e);
          return;
        } 
      }

      final CView newView =
          module.getContent().getViewContainer()
              .createView(String.format("Copy of %s", view.getName()), null);

      CViewInserter.insertView(view, newView);
    }
  }

  /**
   * Deletes a module from the database.
   * 
   * @param parent Parent frame used for dialogs.
   * @param database The database where the module is stored.
   * @param modules The module to be deleted.
   * @param updater Updates the project tree if deletion was successful.
   */
  public static void deleteModules(final JFrame parent, final IDatabase database,
      final INaviModule[] modules, final ITreeUpdater updater) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following modules from the database?\n\n%s",
        CNameListGenerators.getNameList(modules))) == JOptionPane.YES_OPTION) {
      for (final INaviModule module : modules) {
        new Thread() {
          @Override
          public void run() {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", true, false);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText(
                "Deleting module" + ": " + module.getConfiguration().getName());

            try {
              database.getContent().delete(module);

              operation.getProgressPanel().next();

              updater.update();
            } catch (final CouldntDeleteException e) {
              CUtilityFunctions.logException(e);

              final String message = "E00031: " + "Module could not be deleted";
              final String description =
                  CUtilityFunctions
                      .createDescription(
                          String
                              .format(
                                  "The module '%s' could not be deleted. Try to delete the module again. If the problem persists, disconnect from and reconnect to the database, restart com.google.security.zynamics.binnavi, or contact the BinNavi support.",
                                  module.getConfiguration().getName()),
                          new String[] {"Database connection problems."},
                          new String[] {"The module still exists."});

              NaviErrorDialog.show(parent, message, description, e);
            } finally {
              operation.stop();
            }
          }
        }.start();
      }
    }
  }

  /**
   * Imports a module into the database using IDA Pro.
   * 
   * @param parent Parent frame used for dialogs.
   * @param database The database where the imported project is stored.
   */
  public static void importModule(final JFrame parent, final IDatabase database) {
    new Thread() {
      @Override
      public void run() {
        try {
          final List<CFailedImport> failedImports = new ArrayList<CFailedImport>();
          final boolean hasImportedModules =
              CImporterFactory.importModules(parent, database, failedImports);
          for (final CFailedImport failedImport : failedImports) {
            final String message = "E00043: " + "IDB file could not be imported";
            final String description =
                CUtilityFunctions.createDescription(String.format(
                    "The IDB file '%s' could not be imported. Please check the stack "
                        + "trace for more information.", failedImport.geFileName()), new String[] {
                    "Database connection problems.", "Bug in the IDB exporter."},
                    new String[] {"The IDB file was imported partially. A raw module in an "
                        + "incosistent state was created. This raw module should be deleted."});

            NaviErrorDialog.show(parent, message, description, failedImport.getImportException());
          }

          if (database.isConnected() && hasImportedModules) {
            CDatabaseFunctions.refreshRawModules(parent, database);
          }
        } catch (final FileNotFoundException exception) {
          CUtilityFunctions.logException(exception);

          final String message = "E00034: " + "IDA Pro executable could not be found";
          final String description =
              CUtilityFunctions.createDescription(
                  "The selected IDB file could not be imported because the IDA Pro executable "
                      + "file could not be found.",
                  new String[] {"Invalid IDA Pro executable file specified.",},
                  new String[] {"The IDB file was not imported."});

          NaviErrorDialog.show(parent, message, description, exception);
        } catch (final InterruptedException exception) {
          CUtilityFunctions.logException(exception);

          final String message = "Import of IDB files failed";
          final String description =
              CUtilityFunctions.createDescription(
                  "Importing failed because one of the background workers was interrupted.",
                  new String[] {"Background threads were interrupted.",},
                  new String[] {"One or more IDB files were not imported."});

          NaviErrorDialog.show(parent, message, description, exception);
          Thread.currentThread().interrupt();
        } catch (final ExecutionException exception) {
          CUtilityFunctions.logException(exception);

          final String message = "Import of IDB files failed";
          final String description =
              CUtilityFunctions.createDescription(
                  "Importing failed because the background workers threw an exception.",
                  new String[] {"Background threads threw an exception.",},
                  new String[] {"One or more IDB files were not imported."});

          NaviErrorDialog.show(parent, message, description, exception);
        }
      }
    }.start();
  }

  /**
   * Loads one or more modules.
   * 
   * @param projectTree Project tree of the main window.
   * @param modules The modules to load.
   */
  public static void loadModules(final JTree projectTree, final INaviModule[] modules) {
    for (final INaviModule module : modules) {
      if (module.isInitialized()) {
        CModuleLoader.loadModule(projectTree, module);
      } else {
        CModuleInitializationFunctions.initializeAndLoadModule(projectTree, module);
      }
    }
  }
}
