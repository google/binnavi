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
package com.google.security.zynamics.binnavi.Database;

import java.awt.Window;

import javax.swing.JOptionPane;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntUpdateDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseListener;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLErrorCodes;
import com.google.security.zynamics.binnavi.Gui.Database.implementations.CDatabaseCreator;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;



/**
 * Contains helper classes for loading a database.
 */
public final class CDatabaseLoader {
  /**
   * Static helper class.
   */
  private CDatabaseLoader() {
    // You are not supposed to instantiate this class.
  }

  private static String getErrorCode(final CouldntUpdateDatabaseException upgradeException) {
    return String.valueOf(upgradeException.getErrorCode());
  }

  /**
   * Loads the content of a database.
   * 
   * @param parent Parent window used for dialogs.
   * @param database The database to load.
   */
  public static void loadDatabase(final Window parent, final IDatabase database) {
    final CDatabaseLoaderOperation operation = new CDatabaseLoaderOperation(database);

    try {
      database.connect();
      database.load();
    } catch (final CouldntLoadDriverException exception) {
      final String message = "E00012: " + "Database driver could not be loaded";
      final String description =
          CUtilityFunctions.createDescription(String.format(
              "BinNavi could not create a database connection because the database "
                  + "driver '%s' could not be loaded", database.getConfiguration().getDriver()),
              new String[] {"The database driver string is wrong.",
                  "The database driver file could not be found."},
              new String[] {"BinNavi can not load data from the given database until the "
                  + "problem is resolved."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final CouldntLoadDataException exception) {
      final String message = "E00014: " + "Could not load data from the database";
      final String description =
          CUtilityFunctions.createDescription(
              "An error occurred when loading data from the database.", new String[] {
                  "The connection to the database was dropped while the data was loaded.",
                  "The database contains inconsistent information."},
              new String[] {"Close the database and open it again. Maybe close and re-start "
                  + "BinNavi too. If the program persists, please contact the BinNavi support."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final InvalidDatabaseException exception) {
      final String message = "E00015: " + "Database is in an inconsistent state";
      final String description =
          CUtilityFunctions.createDescription(
              "The selected database contains an invalid combination of BinNavi tables.",
              new String[] {
                  "An earlier connection attempt failed and left the database in an "
                      + "inconsistent state.",
                  "Some BinNavi tables were deleted accidentally by an outside program."},
              new String[] {"BinNavi can not use this database anymore. If the database is "
                  + "empty, please delete the database and create a new database to work with "
                  + "BinNavi. If the database already contains data please contact the BinNavi "
                  + "support."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final CouldntInitializeDatabaseException exception) {
      final String message = "E00016: Database could not be initialized";
      final String description =
          CUtilityFunctions.createDescription(
              "BinNavi could not initialize the tables required for storing disassembly data "
                  + "in the database.",
              new String[] {"There might have been a communication problem with the database."},
              new String[] {"The database is probably corrupted at this point. It is "
                  + "recommended to delete the database. Afterwards you can try again with a "
                  + "fresh database. If you do not want to do this please contact the BinNavi "
                  + "support to find out what other options exist for you."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final InvalidExporterDatabaseFormatException exception) {
      final String message = "E00017: " + "Database has invalid exporter tables";
      final String description =
          CUtilityFunctions.createDescription(
              "BinNavi could not load data from the selected database because the database "
                  + "contains invalid exporter tables",
              new String[] {"The database is too old to use with BinNavi."},
              new String[] {"It is recommended to create a database for this version of "
                  + "BinNavi. If you do not want to do this please contact the BinNavi support "
                  + "to find out what other options exist for you."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final InvalidDatabaseVersionException exception) {
      final String exceptionVersion = exception.getVersion().getString();
      if (!exceptionVersion.equals("4.0.0") || !exceptionVersion.equals("5.0.0")) {
        CMessageBox.showInformation(parent,
            String
                .format("You are trying to connect to an outdated BinNavi %s database.\n\n"
                    + "Unfortunately you can not upgrade this database. Please create a "
                    + "new database and export your modules again.", exceptionVersion));
      } else {
        CMessageBox.showInformation(parent,
            String
                .format("You are trying to connect to an outdated BinNavi %s database.\n\n"
                    + "You have the option to update the database.", exceptionVersion));

        if (JOptionPane.YES_OPTION == CMessageBox.showYesNoQuestion(parent,
            "Do you want to upgrade the database now?\n\n(The upgrade process can take "
                + "very long depending on the size of your current database)\n\n(Make "
                + "sure that the identity field contains a user name)")) {

          final CDefaultProgressOperation updateOperation =
              new CDefaultProgressOperation("Upgrading database", true, false);
          updateOperation.getProgressPanel().setText("Upgrading database");

          try {
            database.update();
            database.close();
          } catch (final CouldntUpdateDatabaseException upgradeException) {
            CUtilityFunctions.logException(upgradeException);

            final String message = "E00018: " + "Database could not be upgraded";
            final String description =
                CUtilityFunctions.createDescription(String.format(
                    "BinNavi could not upgrade the database (database error %d). "
                        + "This is a serious problem because the database could be "
                        + "left in an inconsistent state. Please try to fix the "
                        + "problem that led to the error and try to update the "
                        + "database again.", upgradeException.getErrorCode()),
                    new String[] {getErrorCode(upgradeException)},
                    new String[] {"Please note that nobody must work with this database "
                        + "until the database conversion process is complete. If someone "
                        + "works with the database in its current state, partial or total "
                        + "data loss could happen."});

            NaviErrorDialog.show(parent, message, description, exception);
          } finally {
            updateOperation.stop();
          }

          loadDatabase(parent, database);
        } else {
          database.close();
        }
      }

    } catch (final CouldntConnectException exception) {
      final CDatabaseConfiguration config = database.getConfiguration();
      if (exception.getSqlState().equalsIgnoreCase(PostgreSQLErrorCodes.INVALID_PASSWORD)) {
        CMessageBox.showInformation(
            parent,
            String.format("The password for user '%s' on database '%s' is invalid",
                config.getUser(), config.getUrl()));
        return;
      }
      if (exception.getSqlState().equalsIgnoreCase(
          PostgreSQLErrorCodes.POSTGRES_INVALID_CATALOG_NAME)) {

        if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(
            parent,
            String.format("The database '%s' does not exist. Do you want to create it now?",
                config.getUrl()))) {
          CDatabaseCreator.createDatabase(parent, config);
        }
      } else {
        final String message = "E00013: Database connection could not be established";
        final String description =
            CUtilityFunctions.createDescription(String.format(
                "BinNavi could not connect to the database '%s'", database.getConfiguration()
                    .getName()), new String[] {exception.getMessage()},
                new String[] {"BinNavi can not load data from the given database until the "
                    + "problem is resolved."});

        NaviErrorDialog.show(parent, message, description, exception);
      }
    } catch (final LoadCancelledException exception) {
      // We do not signal to the user that he cancelled loading.
    }

    finally {
      operation.stop();
    }
  }

  /**
   * Progress management object for the database load operation.
   */
  private static class CDatabaseLoaderOperation implements IProgressOperation {
    /**
     * Database to be loaded.
     */
    private final IDatabase m_database;

    /**
     * Shows loading progress of the database.
     */
    private final CProgressPanel m_loadProgressPanel = new CProgressPanel("", false, true) {
      @Override
      protected void closeRequested() {
        setText("Cancelling database loading");

        m_continue = false;
      }
    };

    /**
     * Flag that indicates whether database loading should continue or not.
     */
    private boolean m_continue = true;

    /**
     * Description texts for load progress. //TODO (timkornau) this is not nice as there is a hidden
     * dependency to LoadEvents
     */
    private static final String[] DESCRIPTION_TEXTS = new String[] {"Connecting to the database",
        "Checking exporter table format", "Checking BinNavi databases tables status",
        "Initializing BinNavi database tables", "Determining BinNavi database version",
        "Loading debuggers", "Loading modules", "Loading raw modules", "Loading projects",
        "Loading node tags", "Loading view tags", "Loading data", "Loading users",
        "Finished loading"};

    /**
     * Internal listener for displaying database loading progress information.
     */
    private final IDatabaseListener m_databaseListener = new CDatabaseListenerAdapter() {
      /**
       * Flag that indicates whether the next event to arrive is the first one for a database load
       * operation.
       */
      private boolean m_first = true;

      @Override
      public boolean loading(final LoadEvents event, final int counter) {
        if (!m_continue) {
          m_continue = true;

          return false;
        }

        m_loadProgressPanel.next();

        if (event == LoadEvents.LOADING_FINISHED) {
          m_loadProgressPanel.setVisible(false);

          m_first = true;
          m_continue = true;
        } else if (m_first) {
          m_loadProgressPanel.setValue(counter);

          m_first = false;
        }

        m_loadProgressPanel.setText(DESCRIPTION_TEXTS[event.ordinal()]);

        return true;
      }
    };

    /**
     * Creates a new operation object.
     * 
     * @param database Database to be loaded.
     */
    public CDatabaseLoaderOperation(final IDatabase database) {
      m_database = database;
      m_loadProgressPanel.start();
      database.addListener(m_databaseListener);
      CGlobalProgressManager.instance().add(this);
    }

    @Override
    public String getDescription() {
      return "Loading database";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      m_loadProgressPanel.setMaximum(LoadEvents.values().length);

      m_loadProgressPanel.setVisible(true);

      return m_loadProgressPanel;
    }

    /**
     * Stops the operations object.
     */
    public void stop() {
      m_loadProgressPanel.stop();
      m_database.removeListener(m_databaseListener);

      CGlobalProgressManager.instance().remove(this);
    }
  }
}
