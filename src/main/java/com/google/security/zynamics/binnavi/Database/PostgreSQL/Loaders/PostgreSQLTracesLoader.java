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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * Contains functions for working with traces in the database.
 */
public final class PostgreSQLTracesLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLTracesLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Searches for a module with the given ID.
   *
   * @param modules The modules to search through.
   * @param moduleId Module ID to search for.
   *
   * @return The module with the ID or null if no module was found.
   */
  private static INaviModule findModule(final List<? extends INaviModule> modules,
      final int moduleId) {
    for (final INaviModule module : modules) {
      if (module.getConfiguration().getId() == moduleId) {
        return module;
      }
    }

    return null;
  }

  /**
   * Loads the trace events of a trace list from the database.
   *
   * @param connection The connection to the database.
   * @param traceList The trace list whose events are loaded.
   * @param modules List of all modules stored in the database.
   *
   * @throws SQLException Thrown if the trace events could not be loaded.
   */
  private static void loadTraceEvents(final CConnection connection, final TraceList traceList,
      final List<? extends INaviModule> modules) throws SQLException {
    final List<List<TraceRegister>> values = loadTraceEventValues(connection, traceList);

    final String query = "select tid, module_id, address, type from "
        + CTableNames.TRACE_EVENT_TABLE + " where trace_id = " + traceList.getId()
        + " order by position asc";

    final ResultSet resultSet = connection.executeQuery(query, true);

    int counter = 0;

    try {
      while (resultSet.next()) {
        final long tid = resultSet.getLong("tid");

        final int moduleId = resultSet.getInt("module_id");
        final INaviModule module = resultSet.wasNull() ? null : findModule(modules, moduleId);

        final BreakpointAddress address = new BreakpointAddress(module,
            new UnrelocatedAddress(PostgreSQLHelpers.loadAddress(resultSet, "address")));
        final int event = resultSet.getInt("type");

        traceList.addEvent(new TraceEvent(tid, address, TraceEventType.parseInt(event),
            values.isEmpty() ? new ArrayList<TraceRegister>() : values.get(counter)));
        counter++;
      }
    } finally {
      resultSet.close();
    }
  }

  /**
   * Loads the event values of a trace from the database.
   *
   * @param connection Connection to the database.
   * @param traceList The trace to load.
   *
   * @return The event values.
   *
   * @throws SQLException Thrown if the values could not be loaded.
   */
  private static List<List<TraceRegister>> loadTraceEventValues(final CConnection connection,
      final TraceList traceList) throws SQLException {
    final List<List<TraceRegister>> values = new ArrayList<>();

    final String query = "select position, register_name, register_value, memory_value from "
        + CTableNames.TRACE_EVENT_VALUES_TABLE + " where trace_id = " + traceList.getId()
        + " order by position asc";

    final ResultSet resultSet = connection.executeQuery(query, true);

    int currentPosition = -1;

    try {
      List<TraceRegister> registers = new ArrayList<TraceRegister>();

      while (resultSet.next()) {
        final int position = resultSet.getInt("position");

        if (position != currentPosition) {
          if (!registers.isEmpty()) {
            values.add(Lists.newArrayList(registers));
            registers = new FilledList<TraceRegister>();
          }

          currentPosition = position;
        }

        final String name = PostgreSQLHelpers.readString(resultSet, "register_name");
        final long value = resultSet.getLong("register_value");
        final byte[] memory = resultSet.getBytes("memory_value");

        registers.add(new TraceRegister(name, new CAddress(value), memory));
      }

      if (!registers.isEmpty()) {
        values.add(Lists.newArrayList(registers));
      }
    } finally {
      resultSet.close();
    }

    return values;
  }

  /**
   * Loads all traces of a view container.
   *
   * @param provider The connection to the database.
   * @param tableName The table name of the view container.
   * @param columnName The column name of the view container.
   * @param containerId The ID of the view container.
   * @param modules List of all modules stored in the database.
   *
   * @return The loaded traces.
   *
   * @throws CouldntLoadDataException Thrown if loading the traces failed.
   */
  public static IFilledList<TraceList> loadTraces(final AbstractSQLProvider provider,
      final String tableName, final String columnName, final int containerId,
      final List<? extends INaviModule> modules) throws CouldntLoadDataException {
    Preconditions.checkNotNull(provider, "IE00590: Provider argument can not be null");
    Preconditions.checkNotNull(tableName, "IE00591: Table name argument can not be null");
    Preconditions.checkNotNull(columnName, "IE00592: Column name argument can not be null");

    final String query = "select id, name, description from " + CTableNames.TRACES_TABLE + " join "
        + tableName + " on " + tableName + ".trace_id = " + CTableNames.TRACES_TABLE + ".id where "
        + tableName + "." + columnName + " = " + containerId;

    final CConnection connection = provider.getConnection();

    final IFilledList<TraceList> traces = new FilledList<TraceList>();

    try {
      final ResultSet resultSet = connection.executeQuery(query, true);

      try {
        while (resultSet.next()) {
          final int traceId = resultSet.getInt("id");
          final String name = PostgreSQLHelpers.readString(resultSet, "name");
          final String description = PostgreSQLHelpers.readString(resultSet, "description");

          final TraceList traceList = new TraceList(traceId, name, description, provider);

          loadTraceEvents(connection, traceList, modules);

          traces.add(traceList);
        }
      } finally {
        resultSet.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    return traces;
  }
}
