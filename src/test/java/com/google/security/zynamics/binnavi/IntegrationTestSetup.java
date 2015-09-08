/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi;

import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Log.NaviLogger;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class serves the purpose of setting up the stored database for integration test runs. It
 * also has the capability to dump table data via JDBC to a set of files. These files can be used as
 * input for the integration tests.
 */
public class IntegrationTestSetup {

  /**
   * Name for the database specifications for disassembly integration tests.
   */
  private static final String TEST_DISASSEMBLY = "test_disassembly";

  /**
   * Name for the database specifications for import integration tests.
   */
  private static final String TEST_IMPORT = "test_import";

  /**
   * Directory name where the test data is stored.
   */
  private static final String TEST_DATA_DIRECTORY = "testdata/";

  /**
   * Properties for the connection to the database.
   */
  private static final Properties databaseProperties = new Properties();

  /**
   * The url for the database host.
   */
  private static String url;

  /**
   * The complete absolute path to the directory where the test data will be stored.
   */
  private static File path;

  public IntegrationTestSetup() throws IOException {
    final String[] parts = CConfigLoader.loadPostgreSQL();
    databaseProperties.setProperty("user", parts[1]);
    databaseProperties.setProperty("password", parts[2]);
    databaseProperties.setProperty("allowEncodingChanges", "true");
    url = "jdbc:postgresql://" + parts[0] + "/";
  }

  /**
   * Dumps the database given as argument table by table to the defined test data directory. The
   * host where the database is located is configured via the {@link CConfigLoader loader}.
   *
   * @param databaseName The name of the database to be dumped.
   */
  private static void createTestDataset(final String databaseName) {
    try (Connection connection =
          DriverManager.getConnection(url + databaseName, databaseProperties);
          PreparedStatement statement = connection.prepareStatement(
          "SELECT table_name FROM information_schema.tables WHERE table_schema = \'public\'");
          ResultSet resultSet = statement.executeQuery()) {
      
      final File testDataDirectory = new File(path + "/" + databaseName);
      if (!testDataDirectory.exists()) {
        if (!testDataDirectory.mkdirs()) {
          throw new IllegalStateException("Error: could not create directory.");
        }
      }

        while (resultSet.next()) {
          dumpTableInformation(connection, testDataDirectory, resultSet.getString(1));
        }
        
    } catch (IOException | SQLException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Uses the given {@link Connection connection} to COPY the table information from the table given
   * as table name to the directory given as testDataDirectory.
   *
   * @param connection The connection over which to execute the COPY from the database.
   * @param testDataDirectory The test data directory where the result of the COPY is stored.
   * @param tableName The name of the table which is currently dumped from the database.
   *
   * @throws SQLException if the COPY command on the SQL server fails.
   * @throws IOException if the dump result could not be written successfully.
   */
  private static void dumpTableInformation(
      final Connection connection, final File testDataDirectory, final String tableName)
          throws SQLException, IOException {
    final CopyManager manager = new CopyManager((BaseConnection) connection);
    try (FileWriter fw = new FileWriter(new File(testDataDirectory, tableName + ".sql"))) {
      manager.copyOut("COPY " + tableName + " TO STDOUT", fw);
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);
    } 
  }

  public static void main(final String[] args) {
    try {
      new IntegrationTestSetup();
    } catch (final IOException exception) {
      CUtilityFunctions.logException(exception);
    }
    if (args.length > 0 && !args[0].isEmpty()) {
      path = new File(new File(args[0]), TEST_DATA_DIRECTORY);
    } else {
      path = new File(new File("/tmp/"), TEST_DATA_DIRECTORY);
    }
    System.out.println("Saving test data to: " + path);
    createTestDataset(TEST_DISASSEMBLY);
    createTestDataset(TEST_IMPORT);
  }

  /**
   * Uses the stored data in the test data directory to fill a database with information. The table
   * structure is initialized first than the tables are filled. Finally the constraints for the
   * database are initialized. This order is essential to avoid foreign key violations.
   *
   * @param databaseName The name of the database to create.
   */
  private void createDatabase(final String databaseName) {

    try {
      final Connection connection =
          DriverManager.getConnection(url + databaseName, databaseProperties);

      try {
        NaviLogger.info("[i] Generating database tables for %s.", databaseName);
        connection.prepareStatement(AbstractSQLProvider.parseResourceAsSQLFile(
            this.getClass().getResourceAsStream(TEST_DATA_DIRECTORY + "database_schema.sql")))
            .execute();
      } catch (final IOException exception) {
        CUtilityFunctions.logException(exception);
      }

      final File testDataDir = new File(
          "./third_party/zynamics/javatests/com/google/security/zynamics/binnavi/testdata/"
              + databaseName + "/");

      final CopyManager manager = new CopyManager((BaseConnection) connection);

      for (final File currentFile : testDataDir.listFiles()) {
        try (FileReader reader = new FileReader(currentFile)) {
          final String tableName = currentFile.getName().split(".sql")[0];
          NaviLogger.info("[i] Importing: %s.%s from %s", databaseName, tableName,
              currentFile.getAbsolutePath());
          manager.copyIn("COPY " + tableName + " FROM STDIN", reader);
        } catch (final IOException exception) {
          CUtilityFunctions.logException(exception);
        }
      }

      try {
        NaviLogger.warning("[i] Generating constraints  for %s.", databaseName);
        connection.prepareStatement(AbstractSQLProvider.parseResourceAsSQLFile(
            this.getClass().getResourceAsStream(TEST_DATA_DIRECTORY + "database_constraints.sql")))
            .execute();
      } catch (final IOException exception) {
        CUtilityFunctions.logException(exception);
      }

      final String findSequencesQuery = "SELECT 'SELECT SETVAL(' ||quote_literal(S.relname)|| "
          + "', MAX(' ||quote_ident(C.attname)|| ') ) FROM ' ||quote_ident(T.relname)|| ';' "
          + " FROM pg_class AS S, pg_depend AS D, pg_class AS T, pg_attribute AS C "
          + " WHERE S.relkind = 'S' AND S.oid = D.objid AND D.refobjid = T.oid "
          + " AND D.refobjid = C.attrelid AND D.refobjsubid = C.attnum ORDER BY S.relname; ";

      try (PreparedStatement statement = connection.prepareStatement(findSequencesQuery);
           ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          final PreparedStatement fixSequence = connection.prepareStatement(resultSet.getString(1));
          fixSequence.execute();
        }
      } catch (final SQLException exception) {
        CUtilityFunctions.logException(exception);
      }

    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);
    }
  }

  /**
   * Drops and recreates the databases needed for the integration tests.
   *
   * @throws SQLException if one of the queries for drop or create fail.
   * @throws IOException
   */
  public void createIntegrationTestDatabase() throws SQLException {

    final Connection connection = DriverManager.getConnection(url + "postgres", databaseProperties);

    connection.prepareStatement("DROP DATABASE IF EXISTS test_disassembly").execute();
    connection.prepareStatement("DROP DATABASE IF EXISTS test_import").execute();
    connection.prepareStatement("DROP DATABASE IF EXISTS test_empty").execute();

    connection.prepareStatement("CREATE DATABASE test_disassembly").execute();
    connection.prepareStatement("CREATE DATABASE test_import").execute();
    connection.prepareStatement("CREATE DATABASE test_empty").execute();

    connection.close();

    createDatabase(TEST_IMPORT);
    createDatabase(TEST_DISASSEMBLY);
  }
}
