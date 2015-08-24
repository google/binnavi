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

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.gui.CMessageBox;



/**
 * Contains functions for working with databases.
 */
public final class CDatabaseFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDatabaseFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Adds a new debugger to the database.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database where the debugger is added.
   * @param name The name of the new debugger.
   * @param host The host information of the new debugger.
   * @param port The port information of the new debugger.
   * @param updater Updates the project tree after the action is complete.
   */
  public static void addDebugger(final JFrame parent, final IDatabase database, final String name,
      final String host, final int port, final INodeSelectionUpdater updater) {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation = new CDefaultProgressOperation("", false, true);

        try {
          operation.getProgressPanel().setMaximum(1);

          operation.getProgressPanel().setText("Creating new debugger");

          final DebuggerTemplate template =
              database.getContent().getDebuggerTemplateManager().createDebugger(name, host, port);

          updater.setObject(template);
          updater.update();
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String message = "E00028: " + "Debugger could not be created";
          final String description =
              CUtilityFunctions.createDescription(String.format(
                  "The debugger '%s' could not be created. " + "Try creating the debugger again. "
                      + "If the problem persists, disconnect from and reconnect "
                      + "to the database, restart BinNavi, or contact the BinNavi support.", name),
                  new String[] {"Database connection problems."},
                  new String[] {"The debugger was not created."});

          NaviErrorDialog.show(parent, message, description, exception);
        } finally {
          operation.getProgressPanel().next();
          operation.stop();
        }
      }
    }.start();
  }

  /**
   * Adds a new database with default information to the list of known databases.
   * 
   * @param projectTree Project tree of the main window.
   */
  public static void addNewDatabase(final JTree projectTree) {
    final CDatabase newDatabase =
        new CDatabase("New Database", CJdbcDriverNames.jdbcPostgreSQLDriverName, "localhost",
            "new_database", "user", "password", "identity", false, false);

    CDatabaseManager.instance().addDatabase(newDatabase);

    CNodeExpander.setSelectionPath(projectTree, newDatabase);
  }

  /**
   * Deletes a debugger template from the database.
   * 
   * @param parent Parent frame used for dialogs.
   * @param database The database where the debugger template is stored.
   * @param debuggers The debugger templates to be deleted.
   * @param updater Updates the project tree if deletion was successful.
   */
  public static void deleteDebuggers(final JFrame parent, final IDatabase database,
      final DebuggerTemplate[] debuggers, final ITreeUpdater updater) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following debuggers from the database?\n\n%s",
        CNameListGenerators.getNameList(debuggers))) == JOptionPane.YES_OPTION) {
      for (final DebuggerTemplate debugger : debuggers) {
        new Thread() {
          @Override
          public void run() {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", false, true);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText("Removing debugger" + ": " + debugger.getName());
            operation.getProgressPanel().next();

            try {
              database.getContent().getDebuggerTemplateManager().removeDebugger(debugger);

              updater.update();
            } catch (final CouldntDeleteException exception) {
              CUtilityFunctions.logException(exception);

              final String message = "E00030: " + "Debugger could not be deleted";
              final String description =
                  CUtilityFunctions.createDescription(String.format(
                      "The debugger '%s' could not be deleted. "
                          + "Try to delete the debugger again. "
                          + "If the problem persists, disconnect from and "
                          + "reconnect to the database, restart BinNavi, "
                          + "or contact the BinNavi support.", debugger.getName()),
                      new String[] {"Database connection problems."},
                      new String[] {"The debugger still exists."});

              NaviErrorDialog.show(parent, message, description, exception);
            } finally {
              operation.stop();
            }
          }
        }.start();
      }
    }
  }

  /**
   * Deletes a project from the database.
   * 
   * @param parent Parent frame used for dialogs.
   * @param database The database the project belongs to.
   * @param projects The project to be deleted.
   * @param updater Updates the project tree if deletion was successful.
   */
  public static void deleteProjects(final JFrame parent, final IDatabase database,
      final INaviProject[] projects, final ITreeUpdater updater) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following projects?\n\n%s",
        CNameListGenerators.getNameList(projects))) == JOptionPane.YES_OPTION) {
      for (final INaviProject project : projects) {
        new Thread() {
          @Override
          public void run() {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", false, true);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText(
                "Deleting project" + ": " + project.getConfiguration().getName());

            try {
              database.getContent().delete(project);

              operation.getProgressPanel().next();

              updater.update();
            } catch (final CouldntDeleteException exception) {
              CUtilityFunctions.logException(exception);

              final String message = "E00032: " + "Project could not be deleted";
              final String description =
                  CUtilityFunctions.createDescription(String.format(
                      "The project '%s' could not be deleted. "
                          + "Try to delete the project again. If the problem persists, "
                          + "disconnect from and reconnect to the database, "
                          + "restart BinNavi, or contact the BinNavi support.", project
                          .getConfiguration().getName()),
                      new String[] {"Database connection problems."},
                      new String[] {"The project still exists."});

              NaviErrorDialog.show(parent, message, description, exception);
            } finally {
              operation.stop();
            }
          }
        }.start();
      }
    }
  }

  /**
   * Refreshes the information about raw modules stored in the database.
   * 
   * @param parent Parent frame used for dialogs.
   * @param database The database where the raw modules are stored.
   */
  public static void refreshRawModules(final JFrame parent, final IDatabase database) {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation = new CDefaultProgressOperation("", true, false);

        try {
          operation.getProgressPanel().setMaximum(1);

          operation.getProgressPanel().setText("Refreshing modules");

          try {
            database.getContent().refreshRawModules();
          } catch (final CouldntLoadDataException exception) {
            CUtilityFunctions.logException(exception);

            final String message = "E00036: " + "Could not refresh raw modules";
            final String description =
                CUtilityFunctions.createDescription(
                    "The list of raw modules could not be refreshed. "
                        + "Try refreshing the raw modules again.",
                    new String[] {"Database connection problems."},
                    new String[] {"More raw modules than those shown in "
                        + "the raw modules list might exist in the database."});

            NaviErrorDialog.show(parent, message, description, exception);
          }
        } finally {
          operation.getProgressPanel().next();
          operation.stop();
        }
      }
    }.start();
  }

  /**
   * Removes a database configuration.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database to remove.
   * @param updater Updates the project tree after the action is complete.
   */
  public static void removeDatabase(final JFrame parent, final IDatabase database,
      final ITreeUpdater updater) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to remove the database configuration '%s' from BinNavi?", database
            .getConfiguration().getDescription())) == JOptionPane.YES_OPTION) {
      CDatabaseManager.instance().removeDatabase(database);

      updater.update();
      ConfigManager.instance().saveSettings(parent);
    }
  }
}
