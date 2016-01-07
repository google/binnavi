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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.Properties;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLErrorCodes;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Resources.Constants;

/**
 * wrapper class for database connections. The idea is to use this class to log and count SQL
 * queries in order to look for inefficiencies and slow queries
 */
public final class CConnection {
  /**
   * Status flag for debug output.
   */
  private static final boolean m_performanceOutput = true;

  /**
   * Maximum number of printed characters for each debug output query.
   */
  private static final int MAXIMUM_OUTPUT_SIZE = 2048;

  /**
   * Configuration for the database connection.
   */
  private static CDatabaseConfiguration m_databaseConfiguration;

  /**
   * Current database connection properties.
   */
  private static Properties m_properties;

  /**
   * Connection to the database.
   */
  private Connection m_connection;

  /**
   * Counts queries for debug output.
   */
  private long m_debugQueryCount = 0;

  /**
   * Creates a new connection object.
   * 
   * @param databaseConfiguration The configuration of the database used for this connection.
   * 
   * @throws SQLException Thrown if the connection could not be established.
   * @throws CouldntLoadDriverException
   */
  public CConnection(final CDatabaseConfiguration databaseConfiguration)
      throws CouldntLoadDriverException, SQLException {

    m_databaseConfiguration =
        Preconditions.checkNotNull(databaseConfiguration,
            "IE02409: m_databaseConfiguration argument can not be null");
    final String url =
        Preconditions.checkNotNull(databaseConfiguration.getUrl(),
            "IE03409: m_databaseConfiguration.getUrl() argument can not be null");
    Preconditions.checkNotNull(databaseConfiguration.getName(),
        "IE03410: m_databaseConfiguration.getName() argument can not be null");
    final String user =
        Preconditions.checkNotNull(databaseConfiguration.getUser(),
            "IE03411: databaseConfiguration.getUser() argument can not be null");
    final String password =
        Preconditions.checkNotNull(databaseConfiguration.getPassword(),
            "IE03412: databaseConfiguration.getPassword() argument can not be null");

    m_properties = new Properties();
    m_properties.put("user", user);
    m_properties.put("password", password);
    m_properties.put("application_name", Constants.PROJECT_NAME);

    testDriver();
    connect(url, m_properties);
  }

  /**
   * Connect to a database.
   * 
   * @param databaseUrl The database URL to use to connect to the database.
   * @param properties The {@link Properties} used for the connection to the database.
   * 
   * @throws SQLException if the was an issue setting up the connection to the database.
   */
  private void connect(final String databaseUrl, final Properties properties) throws SQLException {
    if (m_connection != null) {
      closeConnection();
    }
    try {
      m_connection = DriverManager.getConnection(databaseUrl, properties);
    } catch (final SQLException exception) {
      NaviLogger.severe("Error: Connection to the database server could not be established: %s",
          exception);

      throw exception;
    }
  }

  /**
   * Function builds the database URL.
   * 
   * @param baseUrl The base URL used to construct the URL.
   * @param databaseName The database name.
   * 
   * @return The database URL.
   */
  private String getDatabaseUrl(final String baseUrl, final String databaseName) {
    return baseUrl.endsWith("/") ? String.format("%s%s", baseUrl, databaseName) : String.format(
        "%s/%s", baseUrl, databaseName);
  }

  /**
   * This function tests whether the specified database driver in the configuration is available or
   * not.
   * 
   * @throws CouldntLoadDriverException
   */
  private void testDriver() throws CouldntLoadDriverException {
    try {
      Class.forName(m_databaseConfiguration.getDriver());
    } catch (final ClassNotFoundException exception) {
      throw new CouldntLoadDriverException("E00044: Couldn't load database driver "
          + m_databaseConfiguration.getDriver() + ".");
    }
  }

  /**
   * Closes the connection to the database.
   */
  public void closeConnection() {
    try {
      if (m_connection != null) {
        m_connection.close();
      }
    } catch (final SQLException exception) {
      NaviLogger.severe("Error: Closing the database connection failed with exception: %s",
          exception);
    }
    m_connection = null;
  }

  /**
   * Executes a query SQL statement.
   * 
   * @param query The query to execute.
   * @param retry Specifies if a query should retry execution upon broken connection.
   * 
   * @return The result of the query.
   * 
   * @throws SQLException Thrown if the query could not be executed.
   */
  public ResultSet executeQuery(final String query, final boolean retry) throws SQLException {
    if (!isConnectionValid()) {
      connect(getURL(), m_properties);
    }

    m_debugQueryCount++;

    long militime = 0;
    if (m_performanceOutput) {
      militime = new GregorianCalendar().getTimeInMillis();
    }

    ResultSet retSet = null;

    final PreparedStatement prep =
        m_connection.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
            ResultSet.CONCUR_READ_ONLY);

    try {
      retSet = prep.executeQuery();
    } catch (final SQLException error) {
      if (m_performanceOutput) {
        NaviLogger.severe(String.format("<%d>    <%d ms>    %s", m_debugQueryCount,
            Math.abs(militime), query));
        NaviLogger.severe("Error: Query failed on %s try: %s", retry ? "first" : "second", query);
      }

      if (((error.getSQLState() == PostgreSQLErrorCodes.CONNECTION_DOES_NOT_EXIST) || (error
          .getSQLState() == PostgreSQLErrorCodes.CONNECTION_FAILURE)) && retry) {
        // here we want to reconnect and try again once.
        connect(
            getDatabaseUrl(m_databaseConfiguration.getUrl(), m_databaseConfiguration.getName()),
            m_properties);
        executeQuery(query, false);
      } else {
        throw error;
      }
    }

    if (m_performanceOutput) {
      militime -= new GregorianCalendar().getTimeInMillis();
      NaviLogger.info("<%d>    <%d ms>    %s", m_debugQueryCount, Math.abs(militime),
          query.substring(0, Math.min(MAXIMUM_OUTPUT_SIZE, query.length())));
    }

    return retSet;
  }

  /**
   * Executes an updating SQL statement.
   * 
   * @param query The query to execute.
   * @param retry Specifies if a query should retry execution upon broken connection.
   * 
   * @return Result of the executeUpdate operation.
   * 
   * @throws SQLException Thrown if the query could not be executed.
   */
  public int executeUpdate(final String query, final boolean retry) throws SQLException {
    m_debugQueryCount++;

    long militime = 0;
    if (m_performanceOutput) {
      militime = new GregorianCalendar().getTimeInMillis();
    }

    int result = 0;

    try (PreparedStatement prep = m_connection.prepareStatement(query)) {
      result = prep.executeUpdate();
    } catch (final SQLException error) {
      if (m_performanceOutput) {
        NaviLogger.severe("<%d>    <%d ms>    %s", m_debugQueryCount, Math.abs(militime), query);
        NaviLogger.severe("Error: Query failed on %s try: %s", retry ? "first" : "second", query);
      }

      if ((error.getSQLState() == PostgreSQLErrorCodes.CONNECTION_DOES_NOT_EXIST) && retry) {
        // here we want to reconnect and try again once.
        connect(
            getDatabaseUrl(m_databaseConfiguration.getUrl(), m_databaseConfiguration.getName()),
            m_properties);
        executeUpdate(query, false);
      } else {
        throw error;
      }
    } 

    if (m_performanceOutput) {
      militime -= new GregorianCalendar().getTimeInMillis();
      NaviLogger.info("<%d>    <%d ms>    %s", m_debugQueryCount, Math.abs(militime),
          query.substring(0, Math.min(MAXIMUM_OUTPUT_SIZE, query.length())));
    }

    return result;
  }

  /**
   * Returns the connection to the SQL database.
   * 
   * @return The connection to the SQL database.
   */
  public Connection getConnection() {
    return m_connection;
  }

  /**
   * Returns the URL to the SQL database.
   * 
   * @return The URL to the SQL database.
   */
  public String getURL() {
    return m_databaseConfiguration.getUrl();
  }

  public boolean isConnectionValid() {
    if (m_connection == null) {
      return false;
    }
    try (Statement statement = m_connection.createStatement()) {
      // do something about the timeout.
        statement.execute("SELECT 1;");
      return true;
    } catch (final SQLException exception) {
      NaviLogger
          .severe(
              "Error: The connection to the database is in an invalid state as statement creation failed with: %s",
              exception);
      return false;
    }
  }
}
