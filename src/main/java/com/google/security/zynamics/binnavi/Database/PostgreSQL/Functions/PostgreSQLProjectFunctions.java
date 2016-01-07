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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CProjectViewFinder;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class PostgreSQLProjectFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLProjectFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Checks the validity of a given SQL provider and a given project. If there is a problem with the
   * arguments, an exception is thrown.
   *
   * @param provider The SQL provider to check.
   * @param project The project to check.
   */
  private static void checkArguments(final AbstractSQLProvider provider, final INaviProject project) {
    Preconditions.checkNotNull(provider, "IE00516: Provider argument can not be null");
    Preconditions.checkNotNull(project, "IE00517: Project argument can not be null");
    Preconditions.checkArgument(project.inSameDatabase(provider),
        "IE00518: Project is not part of this database");
  }

  /**
   * Adds a debugger to a project.
   *
   * The project and the debugger must be stored in the database connected to by the provider
   * argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param project The project the debugger is added to.
   * @param debugger The debugger to add to the project.
   *
   * @throws CouldntSaveDataException Thrown if the debugger could not be added to the project.
   */
  public static void addDebugger(final AbstractSQLProvider provider, final INaviProject project,
      final DebuggerTemplate debugger) throws CouldntSaveDataException {
    checkArguments(provider, project);
    Preconditions.checkNotNull(debugger, "IE00519: Debugger argument can't be null");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00520: The given debugger template is not part of this database");

    final CConnection connection = provider.getConnection();

    try {
      final String insertQuery =
          String.format("INSERT INTO %s values(?, ?)", CTableNames.PROJECT_DEBUGGERS_TABLE);
      final PreparedStatement insertStatement =
          connection.getConnection().prepareStatement(insertQuery);

      final String deleteQuery =
          String.format("DELETE FROM %s WHERE project_id = ? AND debugger_id = ?",
              CTableNames.PROJECT_DEBUGGERS_TABLE);
      final PreparedStatement deleteStatement =
          connection.getConnection().prepareStatement(deleteQuery);

      try {
        PostgreSQLHelpers.beginTransaction(connection);

        deleteStatement.setInt(1, project.getConfiguration().getId());
        deleteStatement.setInt(2, debugger.getId());
        deleteStatement.execute();

        insertStatement.setInt(1, project.getConfiguration().getId());
        insertStatement.setInt(2, debugger.getId());
        insertStatement.execute();

        PostgreSQLHelpers.endTransaction(connection);
      } finally {
        deleteStatement.close();
        insertStatement.close();
      }
    } catch (final SQLException exception) {
      try {
        PostgreSQLHelpers.rollback(connection);
      } catch (final SQLException e) {
        throw new CouldntSaveDataException("E00056: Could not rollback transaction");
      }
      throw new CouldntSaveDataException("E00057: Could not update project debugger");
    }
    PostgreSQLHelpers.updateModificationDate(connection, "" + CTableNames.PROJECTS_TABLE + "",
        project.getConfiguration().getId());
  }

  /**
   * Creates an address space in a project.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param project The project where the address space is created.
   * @param name The name of the created address space.
   *
   * @return The created address space.
   *
   * @throws CouldntSaveDataException Thrown if the address space could not be created.
   */
  public static CAddressSpace createAddressSpace(final AbstractSQLProvider provider,
      final INaviProject project, final String name) throws CouldntSaveDataException {
    checkArguments(provider, project);
    Preconditions.checkNotNull(name, "IE00521: Address space names can not be null");
    Preconditions
        .checkArgument(!("".equals(name)), "IE00522: Address space names can not be empty");

    final CConnection connection = provider.getConnection();

    final int projectId = project.getConfiguration().getId();

    NaviLogger.info("Creating a new address space with name %s in project %s (%d)", name, project
        .getConfiguration().getName(), projectId);

    try {
      // The new address space gets the current date as creation and
      // modification dates.
      final String query =
          "insert into "
              + CTableNames.ADDRESS_SPACES_TABLE
              + "(project_id, name, description, creation_date, modification_date) values(?, ?, '', NOW(), NOW()) returning id";

      final PreparedStatement statement =
          connection.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE,
              ResultSet.CONCUR_READ_ONLY);

      try {
        statement.setInt(1, projectId);
        statement.setString(2, name);

        Integer addressSpaceId = null;

        final ResultSet resultSet = statement.executeQuery();

        try {
          while (resultSet.next()) {
            if (resultSet.isFirst()) {
              addressSpaceId = resultSet.getInt(1);
              break;
            }
          }
        } finally {
          resultSet.close();
        }
        Preconditions.checkNotNull(addressSpaceId,
            "IE02130: Error address space id may not be null after project creation");

        return PostgreSQLProjectFunctions.readAddressSpace(provider, addressSpaceId, project);
      } finally {
        statement.close();
      }
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Deletes a project from the database.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param project The project to delete.
   *
   * @throws CouldntDeleteException Thrown if the project could not be deleted.
   */
  public static void deleteProject(final AbstractSQLProvider provider, final INaviProject project)
      throws CouldntDeleteException {
    checkArguments(provider, project);

    NaviLogger.info("Deleting project %s", project.getConfiguration().getName());

    PostgreSQLHelpers.deleteById(provider.getConnection(), CTableNames.PROJECTS_TABLE, project
        .getConfiguration().getId());
  }

  /**
   * Reads the modification date of a project from the database.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param project The project whose modification date is loaded.
   *
   * @return The last modification date of the project.
   *
   * @throws CouldntLoadDataException Thrown if the modification date could not be loaded.
   */
  public static Date getModificationDate(final AbstractSQLProvider provider,
      final INaviProject project) throws CouldntLoadDataException {
    checkArguments(provider, project);

    return PostgreSQLHelpers.getModificationDate(provider.getConnection(), ""
        + CTableNames.PROJECTS_TABLE + "", project.getConfiguration().getId());
  }

  /**
   * Searches for the views of the project that contain a given address.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param project The project to search through.
   * @param addresses The addresses to search for.
   * @param all True, to search for views that contain all addresses. False, for any addresses.
   *
   * @return The views that contain the search addresses.
   *
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static List<INaviView> getViewsWithAddresses(final AbstractSQLProvider provider,
      final INaviProject project, final List<UnrelocatedAddress> addresses,
      final boolean all) throws CouldntLoadDataException {
    checkArguments(provider, project);

    Preconditions.checkNotNull(addresses, "IE00523: Addresses argument can not be null");

    final StringBuilder queryBuilder = new StringBuilder();

    if (addresses.size() == 0) {
      return new ArrayList<INaviView>();
    } else if (addresses.size() == 1) {
      queryBuilder.append("SELECT " + CTableNames.PROJECT_VIEWS_TABLE + ".project_id, "
          + CTableNames.PROJECT_VIEWS_TABLE + ".view_id " + " FROm "
          + CTableNames.PROJECT_VIEWS_TABLE + " JOIN " + CTableNames.NODES_TABLE + " ON "
          + CTableNames.PROJECT_VIEWS_TABLE + ".view_id = " + CTableNames.NODES_TABLE + ".view_id "
          + " JOIN " + CTableNames.CODENODE_INSTRUCTIONS_TABLE + " ON " + CTableNames.NODES_TABLE
          + ".id = " + CTableNames.CODENODE_INSTRUCTIONS_TABLE + ".node_id "
          + " WHERE project_id = " + project.getConfiguration().getId() + " AND "
          + CTableNames.CODENODE_INSTRUCTIONS_TABLE + ".address = "
          + addresses.get(0).getAddress().toBigInteger().toString());
    } else if (all) {
      boolean needsComma = false;

      int counter = 0;

      queryBuilder.append("select view_id from ");

      for (final UnrelocatedAddress address : addresses) {
        if (needsComma) {
          queryBuilder.append(" inner join ");
        }

        needsComma = true;

        queryBuilder.append("(select " + CTableNames.PROJECT_VIEWS_TABLE + ".project_id, "
            + CTableNames.PROJECT_VIEWS_TABLE + ".view_id " + " from "
            + CTableNames.PROJECT_VIEWS_TABLE + " " + " join " + CTableNames.NODES_TABLE + " on "
            + CTableNames.PROJECT_VIEWS_TABLE + ".view_id = " + CTableNames.NODES_TABLE
            + ".view_id " + " join " + CTableNames.CODENODE_INSTRUCTIONS_TABLE + " on "
            + CTableNames.NODES_TABLE + ".id = " + CTableNames.CODENODE_INSTRUCTIONS_TABLE
            + ".node_id " + " where " + CTableNames.PROJECT_VIEWS_TABLE + ".project_id = "
            + project.getConfiguration().getId() + " " + " and "
            + CTableNames.CODENODE_INSTRUCTIONS_TABLE + ".address = "
            + address.getAddress().toLong() + ") as t" + counter);

        counter++;
      }

      queryBuilder.append(" using (view_id)");
    } else {
      queryBuilder.append("select " + CTableNames.PROJECT_VIEWS_TABLE + ".project_id, "
          + CTableNames.PROJECT_VIEWS_TABLE + ".view_id from " + CTableNames.PROJECT_VIEWS_TABLE
          + " join " + CTableNames.NODES_TABLE + " on " + CTableNames.PROJECT_VIEWS_TABLE
          + ".view_id = " + CTableNames.NODES_TABLE + ".view_id join "
          + CTableNames.CODENODE_INSTRUCTIONS_TABLE + " on " + CTableNames.NODES_TABLE + ".id = "
          + CTableNames.CODENODE_INSTRUCTIONS_TABLE + ".node_id where "
          + CTableNames.PROJECT_VIEWS_TABLE + ".project_id = " + project.getConfiguration().getId()
          + " and " + CTableNames.CODENODE_INSTRUCTIONS_TABLE + ".address in (");

      boolean needsComma = false;

      for (final UnrelocatedAddress address : addresses) {
        if (needsComma) {
          queryBuilder.append(", ");
        }

        needsComma = true;

        queryBuilder.append(address.getAddress().toLong());
      }

      queryBuilder.append(") group by " + CTableNames.PROJECT_VIEWS_TABLE + ".view_id, "
          + CTableNames.PROJECT_VIEWS_TABLE + ".project_id");
    }

    return PostgreSQLHelpers.getViewsWithAddress(provider.getConnection(), queryBuilder.toString(),
        "project_id", new CProjectViewFinder(provider));
  }

  /**
   * Loads an address space from the database.
   *
   * The address space ID must belong to an address space in the database connected to by the
   * provider argument.
   *
   * @param provider The connection to the database.
   * @param addressSpaceId The ID of the address space to load.
   * @param project The optional project for the address space.
   * @return The loaded address space.
   *
   * @throws SQLException Thrown if loading the address space failed.
   */
  public static CAddressSpace readAddressSpace(final AbstractSQLProvider provider,
      final int addressSpaceId, final INaviProject project) throws SQLException {
    final String query =
        "SELECT name, description, creation_date, modification_date " + " FROM "
            + CTableNames.ADDRESS_SPACES_TABLE + " WHERE id = " + addressSpaceId;

    final ResultSet resultSet = provider.getConnection().executeQuery(query, true);

    try {
      while (resultSet.next()) {
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");

        return new CAddressSpace(addressSpaceId, name, description == null ? "" : description,
            creationDate, modificationDate, new HashMap<INaviModule, IAddress>(), null, provider,
            project);
      }
    } finally {
      resultSet.close();
    }

    return null;
  }

  /**
   * Removes a debugger from a project.
   *
   * The project and the debugger must be stored in the database connected to by the provider
   * argument.
   *
   * @param provider The connection to the database.
   * @param project The project from which the debugger is removed.
   * @param debugger The debugger to remove from the project.
   *
   * @throws CouldntSaveDataException Thrown if the debugger could not be removed from the project.
   */
  public static void removeDebugger(final AbstractSQLProvider provider, final INaviProject project,
      final DebuggerTemplate debugger) throws CouldntSaveDataException {
    checkArguments(provider, project);
    Preconditions.checkNotNull(debugger, "IE00524: Debugger argument can not be null");
    Preconditions.checkArgument(debugger.inSameDatabase(provider),
        "IE00525: Debugger is not part of this database");

    final String query =
        String.format("delete from " + CTableNames.PROJECT_DEBUGGERS_TABLE
            + " where project_id = %d and debugger_id = %d", project.getConfiguration().getId(),
            debugger.getId());

    try {
      provider.getConnection().executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }

    PostgreSQLHelpers.updateModificationDate(provider.getConnection(), ""
        + CTableNames.PROJECTS_TABLE + "", project.getConfiguration().getId());
  }

  /**
   * Changes the description of a project.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param project The project whose description is changed.
   * @param description The new description of the project.
   *
   * @throws CouldntSaveDataException Thrown if the description could not be changed.
   */
  public static void setDescription(final AbstractSQLProvider provider, final INaviProject project,
      final String description) throws CouldntSaveDataException {
    checkArguments(provider, project);
    Preconditions.checkNotNull(description, "IE00526: Description argument can not be null");
    PostgreSQLHelpers.setDescription(provider.getConnection(), project.getConfiguration().getId(),
        description, CTableNames.PROJECTS_TABLE);
  }

  /**
   * Changes the name of a project.
   *
   * The project must be stored in the database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param project The project whose name is changed.
   * @param name The new name of the project.
   *
   * @throws CouldntSaveDataException Thrown if the name could not be changed.
   */
  public static void setName(final AbstractSQLProvider provider, final INaviProject project,
      final String name) throws CouldntSaveDataException {
    checkArguments(provider, project);
    Preconditions.checkNotNull(name, "IE00527: Name argument can not be null");
    PostgreSQLHelpers.setName(provider.getConnection(), project.getConfiguration().getId(), name,
        CTableNames.PROJECTS_TABLE);
  }
}
