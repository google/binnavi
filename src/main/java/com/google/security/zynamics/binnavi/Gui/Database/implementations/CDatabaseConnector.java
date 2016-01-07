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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLErrorCodes;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Window;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.swing.JOptionPane;



/**
 * Contains methods for connecting to a database.
 */
public final class CDatabaseConnector {
  private static String POSTGRES_DEFAULT_DB = "postgres";
  private static String POSTGRES_DRIVER = "jdbc:postgresql:";

  /**
   * You are not supposed to instantiate this class.
   */
  private CDatabaseConnector() {
    // You are not supposed to instantiate this class.
  }

  private static void checkDriver(final CDatabaseConfiguration configuration)
      throws CouldntLoadDriverException {
    // ESCA-JAVA0166: Catching Exception here is OK because we show
    // the same error message no matter what exception happened.
    try {
      Class.forName(configuration.getDriver()).newInstance();
    } catch (final Exception e) {
      CUtilityFunctions.logException(e);

      throw new CouldntLoadDriverException(
          String.format("Could not load database driver '%s'.", configuration.getDriver()));
    }
  }

  /**
   * Tries to connect to the database using the given connection options.
   *
   * @param configuration The database configuration used to connect to the database.
   *
   * @return The connection to the database.
   *
   * @throws CouldntLoadDriverException if the database driver could not be loaded.
   * @throws CouldntConnectException if the connection to the database could not be established.
   */
  public static CConnection connect(final CDatabaseConfiguration configuration)
      throws CouldntLoadDriverException, CouldntConnectException {
    checkDriver(configuration);

    try {
      return new CConnection(configuration);
    } catch (final SQLException e) {
      CUtilityFunctions.logException(e);

      throw new CouldntConnectException(e, e.getErrorCode(), e.getSQLState());
    }
  }

  /**
   * Initializes a new PostgreSQL database
   *
   * @param configuration The configuration where the database name for database creation is taken
   *        from.
   *
   * @throws CouldntLoadDriverException if the JDBC driver could not be loaded.
   * @throws CouldntConnectException if the JDBC driver could not establish a connection to the
   *         database.
   *
   * @throws SQLException if the statement executed on the database failed.
   */
  public static void initialize(final CDatabaseConfiguration configuration)
      throws CouldntLoadDriverException, CouldntConnectException, SQLException {
    checkDriver(configuration);

    final String url = POSTGRES_DRIVER + "//" + configuration.getHost() + "/" + POSTGRES_DEFAULT_DB;

    Connection connection = null;

    try {
      connection =
          DriverManager.getConnection(url, configuration.getUser(), configuration.getPassword());
    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);
      throw new CouldntConnectException(
          exception, exception.getErrorCode(), exception.getSQLState());
    }

    final String statement = "CREATE DATABASE \"" + configuration.getName() + "\"";

    final PreparedStatement preparedStatement = connection.prepareStatement(statement);

    try {
      preparedStatement.execute();
    } finally {
      preparedStatement.close();
      connection.close();
    }
  }

  /**
   * Tests a connection to a database.
   *
   * @param parent Parent window for dialogs.
   * @param configuration {@link CDatabaseConfiguration} for the connection test.
   */
  public static void testConnection(
      final Window parent, final CDatabaseConfiguration configuration) {
    // ESCA-JAVA0166: Catching Exception here is OK because we show
    // the same error message no matter what exception happened.
    try {
      connect(configuration).closeConnection();

      CMessageBox.showInformation(parent, "Successfully connected to the database.");
    } catch (final CouldntLoadDriverException exception) {
      final String message = "E00021: " + "Database driver could not be loaded";
      final String description = CUtilityFunctions.createDescription(String.format(
          "BinNavi could not create a database connection because the database "
          + "driver '%s' could not be loaded", configuration.getDriver()), new String[] {
          "The database driver string is wrong.",
          "The database driver file could not be found."}, new String[] {
          "BinNavi can not load data from the given database until the " + "problem is resolved."});

      NaviErrorDialog.show(parent, message, description, exception);
    } catch (final CouldntConnectException exception) {
      if (exception.getSqlState().equalsIgnoreCase(PostgreSQLErrorCodes.INVALID_PASSWORD)) {
        CMessageBox.showInformation(parent, String.format(
            "The password for user '%s' on database '%s' is invalid", configuration.getUser(),
            configuration.getUrl()));
        return;
      } else if (exception.getSqlState()
          .equalsIgnoreCase(PostgreSQLErrorCodes.POSTGRES_INVALID_CATALOG_NAME)) {
        if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(parent, String.format(
            "The database '%s' does not exist. Do you want to create it now?",
            configuration.getUrl()))) {
          CDatabaseCreator.createDatabase(parent, configuration);
        }
      } else {
        final String message = "E00022: " + "Database connection could not be established";
        final String description = CUtilityFunctions.createDescription(String.format(
            "BinNavi could not connect to the database '%s'", configuration.getUrl()),
            new String[] {exception.getMessage()}, new String[] {
                "BinNavi can not load data from the given database until the "
                + "problem is resolved."});

        NaviErrorDialog.show(parent, message, description, exception);
      }
    }
  }
}
