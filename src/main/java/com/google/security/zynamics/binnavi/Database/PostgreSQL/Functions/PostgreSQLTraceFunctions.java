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
import java.util.Locale;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

public final class PostgreSQLTraceFunctions {
  /**
   * You are not supposed to intantiate this class.
   */
  private PostgreSQLTraceFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Creates a new trace list object in the database.
   *
   * @param provider The connection to the database.
   * @param tracesTable The traces table where the trace is added.
   * @param tracesColumn Identifies the view container column for which the trace is created.
   * @param containerTable Identifies the view container table.
   * @param containerId ID of the view container for which the trace is created.
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   *
   * @return The created trace list.
   *
   * @throws CouldntSaveDataException Thrown if the trace list could not be created.
   */
  // ESCA-JAVA0138:
  private static TraceList createTrace(final AbstractSQLProvider provider,
      final String tracesTable,
      final String tracesColumn,
      final String containerTable,
      final int containerId,
      final String name,
      final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00568: Name argument can not be null");

    Preconditions.checkNotNull(description, "IE00569: Description argument can not be null");

    final CConnection connection = provider.getConnection();

    final String query = "INSERT INTO " + CTableNames.TRACES_TABLE
        + "(view_id, name, description) VALUES(?, ?, ?) RETURNING id";

    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query,
          ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

      Integer listId = null;

      try {
        statement.setInt(1, 0);
        statement.setString(2, name);
        statement.setString(3, description);

        final ResultSet resultSet = statement.executeQuery();
        try {
          while (resultSet.next()) {
            if (resultSet.isFirst()) {
              listId = resultSet.getInt(1);
            }
          }
        } finally {
          resultSet.close();
        }
      } finally {
        statement.close();
      }

      connection.executeUpdate("INSERT INTO " + tracesTable + "(" + tracesColumn + ", trace_id) "
          + " VALUES(" + containerId + ", " + listId + ")", true);

      PostgreSQLHelpers.updateModificationDate(connection, containerTable, containerId);

      return new TraceList(listId, name, description, provider);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Saves the events of a trace.
   *
   * @param connection Connection to the database.
   * @param trace The trace whose events are saved.
   *
   * @throws CouldntSaveDataException Thrown if the events could not be saved.
   */
  private static void saveEvents(final CConnection connection, final TraceList trace)
      throws CouldntSaveDataException {
    final String queryPrefix = "INSERT INTO " + CTableNames.TRACE_EVENT_TABLE
        + "(trace_id, position, tid, module_id, address, type) VALUES";

    final StringBuilder stringBuilder = new StringBuilder(queryPrefix);

    int position = 0;

    for (final ITraceEvent traceEvent : trace) {
      final String moduleString = traceEvent.getOffset().getModule() == null ? "null"
          : String.valueOf(traceEvent.getOffset().getModule().getConfiguration().getId());

      stringBuilder.append(String.format(Locale.ENGLISH,
          "(%d, %d, %d, %s, %s, %d)",
          trace.getId(),
          position,
          traceEvent.getThreadId(),
          moduleString,
          traceEvent.getOffset().getAddress().getAddress().toBigInteger().toString(),
          1));

      ++position;

      if (trace.getEventCount() > position) {
        stringBuilder.append(", ");
      }
    }

    try {
      final PreparedStatement prep =
          connection.getConnection().prepareStatement(stringBuilder.toString());
      try {
        prep.execute();
      } catch (final SQLException exception) {
        throw new CouldntSaveDataException(exception);
      } finally {
        prep.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Creates a new trace in a module.
   *
   * @param provider The SQL provider that provides the connection.
   * @param module The module where the trace is created.
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   *
   * @return The created trace.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be created.
   */
  public static TraceList createTrace(final AbstractSQLProvider provider, final INaviModule module,
      final String name, final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00570: Provider argument can not be null");
    Preconditions.checkNotNull(module, "IE00571: Module argument can not be null");
    Preconditions.checkNotNull(name, "IE02192: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE02193: Description argument can not be null");
    Preconditions.checkArgument(module.inSameDatabase(provider),
        "IE00572: The given module is not part of this database");
    return createTrace(provider,
        CTableNames.MODULE_TRACES_TABLE,
        "module_id",
        CTableNames.MODULES_TABLE,
        module.getConfiguration().getId(),
        name,
        description);
  }

  /**
   * Creates a new trace in a project.
   *
   * @param provider The SQL provider that provides the connection.
   * @param project The project where the trace is created.
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   *
   * @return The created trace.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be created.
   */
  public static TraceList createTrace(final AbstractSQLProvider provider,
      final INaviProject project, final String name, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00573: Provider argument can not be null");
    Preconditions.checkNotNull(project, "IE00574: Project argument can not be null");
    Preconditions.checkNotNull(name, "IE00575: Name argument can not be null");
    Preconditions.checkNotNull(description, "IE02194: Description argument can not be null");
    return createTrace(provider,
        CTableNames.PROJECT_TRACES_TABLE,
        "project_id",
        CTableNames.PROJECTS_TABLE,
        project.getConfiguration().getId(),
        name,
        description);
  }

  /**
   * Deletes a trace from the database.
   *
   * @param provider The SQL provider that provides the connection.
   * @param trace The trace to be deleted.
   *
   * @throws CouldntDeleteException Thrown if the trace could not be deleted.
   */
  public static void deleteTrace(final AbstractSQLProvider provider, final TraceList trace)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(provider, "IE00576: Provider argument can not be null");
    Preconditions.checkNotNull(trace, "IE00577: Trace argument can not be null");
    Preconditions.checkArgument(trace.inSameDatabase(provider),
        "IE00578: Trace list is not part of this database");

    final CConnection connection = provider.getConnection();

    final String query = "DELETE FROM " + CTableNames.TRACES_TABLE + " WHERE id = " + trace.getId();

    try {
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntDeleteException(e);
    }
  }

  /**
   * Saves a trace to the database.
   *
   * @param provider The SQL provider that provides the connection.
   * @param trace The trace to save to the database.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be saved to the database.
   */
  public static void save(final AbstractSQLProvider provider, final TraceList trace)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00579: Provider argument can not be null");
    Preconditions.checkNotNull(trace, "IE00580: List argument can not be null");
    Preconditions.checkArgument(trace.inSameDatabase(provider),
        "IE00581: List is not part of this database");

    final CConnection connection = provider.getConnection();

    if (trace.getEventCount() != 0) {
      saveEvents(connection, trace);
      saveEventValues(connection, trace);
    }
  }

  /**
   * Saves the event values of a trace.
   *
   * @param connection Connection to the database.
   * @param trace Trace whose event values are saved.
   *
   * @throws CouldntSaveDataException Thrown if the data could not be saved.
   */
  public static void saveEventValues(final CConnection connection, final TraceList trace)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(connection, "IE02412: connection argument can not be null");
    Preconditions.checkNotNull(trace, "IE02413: trace argument can not be null");

    final String query = "INSERT INTO " + CTableNames.TRACE_EVENT_VALUES_TABLE
        + "(trace_id, position, register_name, register_value, memory_value) VALUES "
        + "(?, ?, ?, ?, ?)";

    try {
      final PreparedStatement preparedStatement =
          connection.getConnection().prepareStatement(query);

      int position = 0;

      for (final ITraceEvent traceEvent : trace) {
        for (final TraceRegister register : traceEvent.getRegisterValues()) {
          preparedStatement.setInt(1, trace.getId());
          preparedStatement.setInt(2, position);
          preparedStatement.setString(3, register.getName());
          preparedStatement.setLong(4, register.getValue().toLong());
          preparedStatement.setBytes(5, register.getMemory());

          preparedStatement.addBatch();
        }
        ++position;
      }
      preparedStatement.executeBatch();

      preparedStatement.close();
    }
 catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Changes the description of a trace.
   *
   * @param provider The SQL provider that provides the connection.
   * @param trace The trace whose description is changed.
   * @param description The new description of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the description of the trace could not be changed.
   */
  public static void setDescription(final AbstractSQLProvider provider, final TraceList trace,
      final String description) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00582: Provider argument can not be null");
    Preconditions.checkNotNull(trace, "IE00583: Trace list argument can not be null");
    Preconditions.checkNotNull(description, "IE00584: Description argument can not be null");
    Preconditions.checkArgument(trace.inSameDatabase(provider),
        "IE00585: Trace list is not part of this database");

    final CConnection connection = provider.getConnection();
    final String query = "UPDATE " + CTableNames.TRACES_TABLE + " SET description = ? WHERE id = ?";
    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      try {
        statement.setString(1, description);
        statement.setInt(2, trace.getId());

        statement.executeUpdate();
      } finally {
        statement.close();
      }

    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the name of a trace.
   *
   * @param provider The SQL provider that provides the connection.
   * @param trace The trace whose name is changed.
   * @param name The new name of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the name of the trace could not be changed.
   */
  public static void setName(final AbstractSQLProvider provider, final TraceList trace,
      final String name) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00586: Provider argument can not be null");
    Preconditions.checkNotNull(trace, "IE00587: Trace list argument can not be null");
    Preconditions.checkNotNull(name, "IE00588: Name argument can not be null");
    Preconditions.checkArgument(trace.inSameDatabase(provider),
        "IE00589: Trace list is not part of this database");

    final CConnection connection = provider.getConnection();
    final String query = "UPDATE " + CTableNames.TRACES_TABLE + " SET name = ? WHERE id = ?";
    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      try {
        statement.setString(1, name);
        statement.setInt(2, trace.getId());

        statement.executeUpdate();
      } finally {
        statement.close();
      }
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }
}
