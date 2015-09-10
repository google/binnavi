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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Creators;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CProject;

public final class PostgreSQLProjectCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLProjectCreator() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Loads a project from the database.
   * 
   * @param provider The connection to the database.
   * @param projectId The ID of the project to load.
   * 
   * @return The loaded project.
   * 
   * @throws SQLException Thrown if the project could not be loaded.
   */
  protected static CProject loadProject(final AbstractSQLProvider provider, final int projectId)
      throws SQLException {
    final String query =
        "select id, name, description, creation_date, modification_date from "
            + CTableNames.PROJECTS_TABLE + " where id = " + projectId;

    try (ResultSet resultSet = provider.getConnection().executeQuery(query, true)) {
      while (resultSet.next()) {
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final int addressSpaceCount = 0;
        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");

        return new CProject(projectId, name, description, creationDate, modificationDate,
            addressSpaceCount, new ArrayList<DebuggerTemplate>(), provider);
      }
    }
    
    return null;
  }

  /**
   * Creates a new project in the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param name The name of the new project.
   * 
   * @return The created project.
   * 
   * @throws CouldntSaveDataException Thrown if the project could not be created.
   */
  public static CProject createProject(final AbstractSQLProvider provider, final String name)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00513: Provider argument can not be null");
    Preconditions.checkNotNull(name, "IE00514: Project names can not be null");
    Preconditions.checkArgument(!("".equals(name)), "IE00515: Project names can not be empty");

    final CConnection connection = provider.getConnection();

    NaviLogger.info("Creating new project %s", name);

      final String query = "INSERT INTO "
              + CTableNames.PROJECTS_TABLE
              + "(name, description, creation_date, modification_date) VALUES(?, '', NOW(), NOW()) RETURNING id";

        try (PreparedStatement statement =
          connection.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY)) {
        statement.setString(1, name);
        
        ResultSet resultSet = statement.executeQuery();

        Integer id = null;

          while (resultSet.next()) {
            if (resultSet.isFirst()) {
              id = resultSet.getInt(1);
              break;
            }
          }
        
        Preconditions.checkNotNull(id,
            "IE02044: Error id for a project after creation may not be null");

        return PostgreSQLProjectCreator.loadProject(provider, id);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }
}
