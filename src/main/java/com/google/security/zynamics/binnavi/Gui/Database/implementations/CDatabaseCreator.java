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
package com.google.security.zynamics.binnavi.Gui.Database.implementations;

import java.awt.Window;
import java.sql.SQLException;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLErrorCodes;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;



/**
 * Contains methods for creating databases.
 */
public final class CDatabaseCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CDatabaseCreator() {
  }

  /**
   * Creates a new database in a given SQL database.
   * 
   * @param parent Window used for dialogs.
   * @param configuration The configuration with which to connect to the database.
   */
  private static void createDatabaseInternal(final Window parent,
      final CDatabaseConfiguration configuration) {
    final String databaseName = configuration.getName();

    try {
      CDatabaseConnector.initialize(configuration);
      CMessageBox.showInformation(parent,
          String.format("The database '%s' was created successfully.", databaseName));
    } catch (final CouldntLoadDriverException exception) {
      final String message = "E00047: " + "Database driver could not be loaded";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not create a database connection because "
                  + "the database driver '%s' could not be loaded", configuration.getDriver()),
              new String[] {"The database driver string is wrong.",
                  "The database driver file could not be found."},
              new String[] {"BinNavi can not load data from the given database until "
                  + "the problem is resolved."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final CouldntConnectException exception) {
      if (exception.getSqlState().equalsIgnoreCase(PostgreSQLErrorCodes.INVALID_PASSWORD)) {
        CMessageBox.showInformation(
            parent,
            String.format("The password for user '%s' on database '%s' is invalid",
                configuration.getUser(), configuration.getUrl()));
        return;
      }
      final String message = "E00048: " + "Database connection could not be established";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not connect to the database '%s'",
                  configuration.getUrl()), new String[] {exception.getMessage()},
              new String[] {"BinNavi can not load data from the given database until the "
                  + "problem is resolved."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final SQLException exception) {
      final String message = "E00049: " + "Database could not be created";
      final String description =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not create the database '%s'", databaseName),
              new String[] {exception.getMessage()}, new String[] {
                  "There was a problem with the database connection.",
                  "The database was not created. Please try creating the database again or "
                      + "create it manually if necessary."});

      NaviErrorDialog.show(parent, message, description, exception);
    }
  }

  /**
   * Creates a new database.
   * 
   * @param parent The Window used for dialogs.
   * @param configuration The configuration with which to connect to the database.
   */
  public static void createDatabase(final Window parent, final CDatabaseConfiguration configuration) {
    new Thread() {
      @Override
      public void run() {
        final CProgressPanel panel =
            new CProgressPanel(String.format("Creating database '%s'", configuration.getUrl()),
                true, false, false);

        final IProgressOperation operation = new IProgressOperation() {
          @Override
          public String getDescription() {
            return "Creating database";
          }

          @Override
          public CProgressPanel getProgressPanel() {
            return panel;
          }
        };

        CGlobalProgressManager.instance().add(operation);
        panel.start();
        createDatabaseInternal(parent, configuration);
        CGlobalProgressManager.instance().remove(operation);
      }
    }.start();
  }
}
