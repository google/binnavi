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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.ContainerFinder;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class PostgreSQLHelpers {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLHelpers() {
    // You are not supposed to instantiate this class
  }

  private static void endTransaction(final Connection connection) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00598: Connection argument can not be null");
    Preconditions.checkArgument(!connection.getAutoCommit(),
        "IE00599: Commit can only be performed when auto commit is false");
    connection.commit();
    connection.setAutoCommit(true);
  }

  /**
   * Starts a transaction.
   *
   * @param connection Connection to a SQL database where the transaction is started.
   *
   * @throws SQLException Thrown if the transaction could not be started.
   */
  public static void beginTransaction(final CConnection connection) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00443: Connection argument can not be null");
    connection.getConnection().setAutoCommit(false);
  }

  /**
   * Deletes all rows in the specified table column matching the column value specified.
   *
   * @param connection Connection to a SQL database.
   * @param tableName Name of the table from which to delete the rows.
   * @param columnName Name of the column in the table from which to delete the rows.
   * @param columnValue Value which indicates the rows in the table identified by column to delete.
   *
   * @throws CouldntDeleteException if the specified rows could not be deleted.
   */
  public static void deleteByColumnValue(final CConnection connection, final String tableName,
      final String columnName, final int columnValue) throws CouldntDeleteException {

    Preconditions.checkNotNull(connection, "IE00487: Connection argument can not be null");
    Preconditions.checkNotNull(tableName, "IE00499: Table name argument can not be null");
    Preconditions.checkNotNull(columnName, "IE00593: Column name argument can not be null");
    Preconditions.checkArgument(
        columnValue >= 0, "IE00594: Column value argument can not be smaller then zero");

    try {
      connection.executeUpdate(
          String.format("DELETE FROM %s WHERE %s = %d", tableName, columnName, columnValue), true);
    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Deletes a row that is identified by an ID from a table.
   *
   * @param connection Connection to the SQL database.
   * @param tableName Name of the table from which the row is deleted.
   * @param id ID of the row to delete.
   *
   * @throws CouldntDeleteException Thrown if the row could not be deleted.
   */
  public static void deleteById(final CConnection connection, final String tableName, final int id)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(connection, "IE00595: Connection argument can not be null");
    Preconditions.checkNotNull(tableName, "IE00596: Table name argument can not be null");
    Preconditions.checkArgument(id > 0, "IE00597: Id argument can not be less or equal zero");

    try {
      connection.executeUpdate(String.format("DELETE FROM %s WHERE id = %d", tableName, id), true);
    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }
  }

  /**
   * Deletes a table from the database.
   *
   * @param connection The connection to the database.
   * @param table The name of the table to delete.
   *
   * @throws SQLException Thrown if the table could not be deleted.
   */
  public static void deleteTable(final CConnection connection, final String table)
      throws SQLException {
    connection.executeUpdate("DROP TABLE IF EXISTS " + table + " CASCADE", true);
  }

  /**
   * Commits and finishes a formerly started transaction.
   *
   * @param connection Connection to the SQL database.
   *
   * @throws SQLException Thrown if the transaction could not be committed.
   */
  public static void endTransaction(final CConnection connection) throws SQLException {
    endTransaction(connection.getConnection());
  }

  /**
   * This function retrieves the backend PID from a PostreSQL server
   *
   * @param connection The connection to the server.
   * @return The backend PID of the server process for this connection.
   */
  public static int getBackendPID(final CConnection connection) {
    Preconditions.checkNotNull(connection, "IE02410: connection argument can not be null");

    final String query = "SELECT pg_backend_pid() AS pid";
    try (ResultSet pidResult = connection.executeQuery(query, true)) {

        while (pidResult.next()) {
          return pidResult.getInt("pid");
        }

    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);
    }

    throw new IllegalStateException("IE02411: Could not retrieve backend PID from server");
  }

  /**
   * Extracts the name of a database from a connection object.
   *
   * @param connection A connection to a SQL database.
   *
   * @return The name of the database the connection is working on.
   */
  public static String getDatabaseName(final CConnection connection) {
    Preconditions.checkNotNull(connection, "IE00600: Connection argument can not be null");

    final String url = connection.getURL();

    final int index = url.lastIndexOf('/');

    if (index == -1) {
      return url;
    } else {
      return url.substring(index + 1);
    }
  }

  /**
   * Returns the modification date of a row identified by an ID in a given table.
   *
   * @param connection Connection to a SQL database.
   * @param targetTable Table from where the modification date is read.
   * @param id ID that identifies an entry in the table.
   *
   * @return The modification date that was read from the table.
   *
   * @throws CouldntLoadDataException Thrown if the modification date could not be read.
   */
  public static Date getModificationDate(
      final CConnection connection, final String targetTable, final int id)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(targetTable, "IE00601: Target table argument can not be null");
    Preconditions.checkNotNull(connection, "IE00602: Connection argument can not be null");
    Preconditions.checkArgument(id >= 0, "IE00605: Id argument can not less then zero");

    final String query = "SELECT modification_date FROM " + targetTable + " WHERE id = " + id;
    try (ResultSet dateResult = connection.executeQuery(query, true)) {

        while (dateResult.next()) {
          return dateResult.getTimestamp("modification_date");
        }
      
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }

    throw new IllegalStateException("IE00606: Could not retrieve modification date");
  }

  /**
   * Searches for the views with a given address.
   *
   * @param connection Connection to a SQL database.
   * @param query SQL query to issue for the search.
   * @param columnName Name of the column from which the container ID is read.
   * @param finder Finder object that is used to find the view object from a view ID.
   *
   * @return A list of views that was found by the query.
   *
   * @throws CouldntLoadDataException Thrown if the search failed with an error.
   */
  // TODO (timkornau): find out if there is a better way to have the sql query build in here rather
  // then in the caller.
  // It just seems to be wrong like this.
  public static IFilledList<INaviView> getViewsWithAddress(final CConnection connection,
      final String query, final String columnName, final ContainerFinder finder)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(finder, "IE00607: Finder argument can not be null");
    Preconditions.checkNotNull(columnName, "IE00608: Column name argument can not be null");
    Preconditions.checkNotNull(query, "IE00627: Query argument can not be null");
    Preconditions.checkNotNull(connection, "IE00628: Connection argument can not be null");

    final IFilledList<INaviView> views = new FilledList<INaviView>();

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
        
        while (resultSet.next()) {
          final int containerId = resultSet.getInt(columnName);
          final int viewId = resultSet.getInt("view_id");
          final INaviView view = finder.findView(containerId, viewId);
          views.add(view);
        }

      return views;
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Determines whether a column with a given name is present in a given table.
   *
   * @param connection Connection to a PostgreSQL database.
   * @param tableName Name of the table to search for.
   * @param columnName Name of the column to search for.
   *
   * @return True, if the column with the given name is present in the table. False, otherwise.
   */
  public static boolean hasColumn(
      final CConnection connection, final String tableName, final String columnName) {

    Preconditions.checkNotNull(columnName, "IE02035: Column name argument can not be null");
    Preconditions.checkNotNull(tableName, "IE02036: Table name argument can not be null");
    Preconditions.checkNotNull(connection, "IE02037: Connection argument can not be null");

    final String query = String.format("SELECT attname FROM pg_attribute WHERE attrelid = "
        + " (SELECT oid FROM pg_class WHERE relname = '%s') AND attname = '%s';", tableName,
        columnName);

    try (ResultSet result = connection.executeQuery(query, true)) {

      return result.first();

    } catch (final SQLException e) {
      return false;
    }
  }

  /**
   * Determines whether a table with a given name is present in a PostgreSQL database.
   *
   * @param connection Connection to a PostgreSQL database.
   * @param tableName Name of the table to search for.
   *
   * @return True, if the table with the given name is present in the database. False, otherwise.
   *
   * @throws CouldntLoadDataException Thrown if checking for the existence of the table failed.
   */
  public static boolean hasTable(final CConnection connection, final String tableName)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(tableName, "IE02038: Table name argument can not be null");
    Preconditions.checkNotNull(connection, "IE02039: Connection argument can not be null");

    final String query = "SELECT relname FROM pg_class WHERE relname = '" + tableName + "'";

    try (ResultSet result = connection.executeQuery(query, true)) {

      return result.first();
      
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  /**
   * Determines the number of tables known to the database. The given list of table names is used as
   * query information source.
   *
   * @param connection The {@link CConnection} to access the database.
   * @param tableNames The List of table names which are queried for in the database.
   * @return The number of tables in the database
   * @throws CouldntLoadDataException if the query execution failed.
   */
  public static int getTableCount(final CConnection connection, final List<String> tableNames)
      throws CouldntLoadDataException {

    Preconditions.checkNotNull(connection, "Error: connection argument can not be null");

    final StringBuilder builder =
        new StringBuilder("SELECT count(*) FROM pg_class WHERE relname in (");

    for (String tableName : tableNames) {
      builder.append("'" + tableName + "',");
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(")");

    try (ResultSet result = connection.executeQuery(builder.toString(), true)) {

        while (result.next()) {
          return result.getInt("count");
        }
      
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
    throw new IllegalStateException("Error: Could not retrieve table count from server");
  }


  /**
   * Loads an address value from the database.
   *
   * @param resultSet The result set to read from.
   * @param columnName The name of the column to read from.
   *
   * @return The read address.
   *
   * @throws SQLException Thrown if the address could not be read.
   */
  public static IAddress loadAddress(final ResultSet resultSet, final String columnName)
      throws SQLException {
    Preconditions.checkNotNull(resultSet, "IE00500: Result set argument can not be null");
    Preconditions.checkNotNull(columnName, "IE00501: Column name argument can not be null");

    final String addressString = readString(resultSet, columnName);
    return resultSet.wasNull() ? null : new CAddress(new BigInteger(addressString));
  }

  /**
   * Helper function that exists primarily to fool poedit to stop localizing column names. This
   * function simply reads a string from a result set.
   *
   * @param resultSet The result set to read from.
   * @param columnName Name of the column to read.
   *
   * @return The string value read from the given column.
   *
   * @throws SQLException Thrown if the column could not be read.
   */
  public static String readString(final ResultSet resultSet, final String columnName)
      throws SQLException {
    return resultSet.getString(columnName);
  }

  /**
   * Rolls back the current transaction.
   *
   * @param connection Connection to a SQL database.
   *
   * @throws SQLException Thrown if rolling back the transaction failed.
   */
  public static void rollback(final CConnection connection) throws SQLException {
    Preconditions.checkNotNull(connection, "IE00629: Connection argument can not be null");

    if (!connection.getConnection().getAutoCommit()) {
      connection.getConnection().rollback();
      connection.getConnection().setAutoCommit(true);
    } else {
      throw new IllegalStateException(
          "IE00712: Auto commit status must be false for manual rollback");
    }
  }

  /**
   * Sets the 'description' column of a row identified by an ID in a given table.
   *
   * @param connection Connection to a SQL database.
   * @param id ID of the row whose 'description' column is changed.
   * @param description The new value of the description column.
   * @param table Name of the table where the row is found.
   *
   * @throws CouldntSaveDataException Thrown if the 'description' column could not be updated.
   */
  public static void setDescription(
      final CConnection connection, final int id, final String description, final String table)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(table, "IE01226: Table argument can not be null");
    Preconditions.checkNotNull(description, "IE01484: Description argument can not be null");
    Preconditions.checkNotNull(connection, "IE01485: Connection argument can not be null");

    final String query =
        "UPDATE " + table + " SET description = ?, modification_date = NOW() WHERE id = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
    
      statement.setString(1, description);
      statement.setInt(2, id);
      statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Sets the 'name' column of a row identified by an ID in a given table.
   *
   * @param connection Connection to a SQL database.
   * @param id ID of the row whose 'name' column is changed.
   * @param name The new value of the name column.
   * @param tableName Name of the table where the row is found.
   *
   * @throws CouldntSaveDataException Thrown if the 'name' column could not be updated.
   */
  public static void setName(
      final CConnection connection, final int id, final String name, final String tableName)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(tableName, "IE02210: Table name argument can not be null");
    Preconditions.checkNotNull(name, "IE02243: Name argument can not be null");
    Preconditions.checkNotNull(connection, "IE02252: Connection argument can not be null");

    Preconditions.checkArgument(id >= 0, "Error: Id argument can not less then zero");

    final String query =
        "UPDATE " + tableName + " SET name = ?, modification_date = NOW() WHERE id = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
    
      statement.setString(1, name);
      statement.setInt(2, id);
      statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Updates the modification date column of a row identified by an ID in a given table.
   *
   * @param connection Connection to a SQL database.
   * @param tableName table name of the table that contains the row to update.
   * @param id ID of the row to update.
   * @throws CouldntSaveDataException
   */
  public static void updateModificationDate(
      final CConnection connection, final String tableName, final int id)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(tableName, "IE01486: Table name argument can not be null");
    Preconditions.checkNotNull(connection, "IE01858: Connection argument can not be null");

    final String query = "UPDATE " + tableName + " SET modification_date = NOW() WHERE id = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {
      
      statement.setInt(1, id);
      statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Converts a ViewType object to a database enumeration string.
   *
   * @param type The ViewType object to convert.
   *
   * @return The database string that corresponds to the given object.
   */
  public static String viewTypeToString(final ViewType type) {
    return type == ViewType.Native ? "'native'" : "'non-native'";
  }
}
