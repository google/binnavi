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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.zylib.net.NetHelpers;

/**
 * This class contains PostgreSQL queries for working with debuggers.
 */
public final class PostgresSQLDebuggerFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgresSQLDebuggerFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Creates a new debugger template in the database.
   * 
   * @param provider SQL provider of the new debugger template.
   * @param name Name of the new debugger template. This argument must be non-empty.
   * @param host Host of the new debugger template. This argument must be non-empty.
   * @param port Port of the new debugger template. This argument must be a valid port number.
   * 
   * @return The new debugger template.
   * 
   * @throws CouldntSaveDataException Thrown if the new debugger template could not be written to
   *         the database.
   */
  public static DebuggerTemplate createDebuggerTemplate(final AbstractSQLProvider provider,
      final String name, final String host, final int port) throws CouldntSaveDataException {

    Preconditions.checkNotNull(name, "IE00417: Debugger names can not be null");
    Preconditions.checkArgument(!name.isEmpty(), "IE00418: Debugger names can not be empty");
    Preconditions.checkNotNull(host, "IE00419: Debugger host can not be null");
    Preconditions.checkArgument(!host.isEmpty(), "IE00418: Debugger host can not be empty");

    Preconditions.checkArgument((port > 0) && (port <= 65535),
        "IE00421: Debugger port is out of bounds");

    NaviLogger.info("Creating new debugger %s (%s:%d)", name, host, port);

    final CConnection connection = provider.getConnection();

    final String query =
          "INSERT INTO " + CTableNames.DEBUGGERS_TABLE
              + "(name, host, port) VALUES(?, ?, ?) RETURNING id";
              
    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {

      statement.setString(1, name);
      statement.setString(2, host);
      statement.setInt(3, port);

      int id = -1;
      try (ResultSet resultSet = statement.executeQuery()) {    
        while (resultSet.next()) {
          id = resultSet.getInt("id");
        }
      }
      return new DebuggerTemplate(id, name, host, port, provider);

    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Deletes a debugger template from the database.
   * 
   * The given debugger template must be stored in the database connected to by the provider
   * argument.
   * 
   * @param provider The connection to the database.
   * @param debugger The debugger template to delete.
   * 
   * @throws CouldntDeleteException Thrown if the debugger template could not be deleted.
   */
  public static void deleteDebugger(final AbstractSQLProvider provider,
      final DebuggerTemplate debugger) throws CouldntDeleteException {
    Preconditions.checkNotNull(debugger, "IE00709: Debugger template argument can not be null");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00710: Debugger template is not part of this database");
    NaviLogger.info("Deleting debugger %d", debugger.getId());
    PostgreSQLHelpers.deleteById(provider.getConnection(), CTableNames.DEBUGGERS_TABLE,
        debugger.getId());
  }

  /**
   * Loads all debugger templates of a database.
   * 
   * The debugger template manager must belong to the database connected to by the provider
   * argument.
   * 
   * @param provider The connection to the database.
   * @param manager Debugger template manager where the loaded debuggers are added to.
   * 
   * @throws CouldntLoadDataException Thrown if the debugger templates could not be loaded.
   */
  public static void loadDebuggers(final AbstractSQLProvider provider,
      final DebuggerTemplateManager manager) throws CouldntLoadDataException {
    final CConnection connection = provider.getConnection();
    final String query = "SELECT * FROM " + CTableNames.DEBUGGERS_TABLE;

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
        
        while (resultSet.next()) {
          final DebuggerTemplate debugger =
              new DebuggerTemplate(resultSet.getInt("id"), PostgreSQLHelpers.readString(resultSet,
                  "name"), PostgreSQLHelpers.readString(resultSet, "host"),
                  resultSet.getInt("port"), provider);

          manager.addDebugger(debugger);
        }
        
    } catch (final SQLException e) {
      throw new CouldntLoadDataException(e);
    }
  }

  /**
   * Changes the host of an existing debugger template.
   * 
   * The debugger must be stored in the database the provider argument is connected to.
   * 
   * @param provider The connection to the database.
   * @param debugger The debugger whose host value is changed.
   * @param host The new host value of the debugger template.
   * 
   * @throws CouldntSaveDataException Thrown if the host value could not be updated.
   */
  public static void setHost(final AbstractSQLProvider provider, final DebuggerTemplate debugger,
      final String host) throws CouldntSaveDataException {
    Preconditions.checkNotNull(debugger, "IE00422: Debugger argument can not be null");
    Preconditions.checkNotNull(host, "IE00423: Host argument can not be null");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00424: Debugger is not part of this database");

    final String query = "UPDATE " + CTableNames.DEBUGGERS_TABLE + " SET host = ? WHERE id = ?";

    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query)) {
      
        statement.setString(1, host);
        statement.setInt(2, debugger.getId());
        statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the name of an existing debugger template.
   * 
   * The debugger must be stored in the database the provider argument is connected to.
   * 
   * @param provider The connection to the database.
   * @param debugger The debugger whose name value is changed.
   * @param name The new name value of the debugger template.
   * 
   * @throws CouldntSaveDataException Thrown if the name value could not be updated.
   */
  public static void setName(final AbstractSQLProvider provider, final DebuggerTemplate debugger,
      final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(debugger, "IE00425: Debugger argument can not be null");
    Preconditions.checkNotNull(name, "IE00426: Name argument can not be null");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00427: Debugger is not part of this database");
    final String query = "UPDATE " + CTableNames.DEBUGGERS_TABLE + " SET name = ? WHERE id = ?";

    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query)) {

        statement.setString(1, name);
        statement.setInt(2, debugger.getId());
        statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the port of an existing debugger template.
   * 
   * The debugger must be stored in the database the provider argument is connected to.
   * 
   * @param provider The connection to the database.
   * @param debugger The debugger whose port value is changed.
   * @param port The new port value of the debugger template. This argument must be a valid port
   *        number.
   * 
   * @throws CouldntSaveDataException Thrown if the port value could not be updated.
   */
  public static void setPort(final AbstractSQLProvider provider, final DebuggerTemplate debugger,
      final int port) throws CouldntSaveDataException {
    Preconditions.checkNotNull(debugger, "IE00428: Debugger argument can not be null");
    Preconditions.checkArgument(NetHelpers.isValidPort(port), "IE00429: Invalid port argument");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00430: Debugger is not part of this database");

    final String query = "UPDATE " + CTableNames.DEBUGGERS_TABLE + " SET port = ? WHERE id = ?";

    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query)) {
      
        statement.setInt(1, port);
        statement.setInt(2, debugger.getId());
        statement.executeUpdate();
      
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }
}
