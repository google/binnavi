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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

// ESCA-JAVA0014:
/**
 * This class provides queries that can be used to load information about module flow graph views.
 */
public final class PostgreSQLModuleFlowgraphsLoader extends PostgreSQLModuleViewsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLModuleFlowgraphsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the flow graphs of a module.
   * 
   * @param provider The connection to the database.
   * @param module The module whose flow graph views are loaded.
   * @param viewTagManager The tag manager responsible for tagging the views.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * @param viewType The type of the views to load.
   * 
   * @return The loaded views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  private static ImmutableList<IFlowgraphView> loadModuleFlowgraphs(
      final AbstractSQLProvider provider, final CModule module, final CTagManager viewTagManager,
      final CTagManager nodeTagManager, final ViewType viewType) throws CouldntLoadDataException {
    checkArguments(provider, module, viewTagManager);

    final String query = " SELECT * FROM load_module_flow_graphs(?, ?) ";

    final CConnection connection = provider.getConnection();
    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, module.getConfiguration().getId());
      statement.setObject(2, viewType == ViewType.Native ? "native" : "non-native", Types.OTHER);
      final ResultSet resultSet = statement.executeQuery();

      final Map<Integer, Set<CTag>> tags = loadTags(connection, module, viewTagManager);

      return new ImmutableList.Builder<IFlowgraphView>().addAll(
          processQueryResults(resultSet, module, tags, nodeTagManager, provider,
              new ArrayList<CView>(), viewType, GraphType.FLOWGRAPH)).build();
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  /**
   * Loads the non-native flow graph views of a module.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The module from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return A list of non-native flow graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static ImmutableList<IFlowgraphView> loadFlowgraphs(final AbstractSQLProvider provider,
      final CModule module, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    return loadModuleFlowgraphs(provider, module, viewTagManager, nodeTagManager,
        ViewType.NonNative);
  }

  /**
   * Loads the native flow graph views of a module.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param module The module from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return A list of non-native flow graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static ImmutableList<IFlowgraphView> loadNativeFlowgraphs(
      final AbstractSQLProvider provider, final CModule module, final CTagManager viewTagManager,
      final CTagManager nodeTagManager) throws CouldntLoadDataException {
    return loadModuleFlowgraphs(provider, module, viewTagManager, nodeTagManager, ViewType.Native);
  }

  public static ImmutableNaviViewConfiguration loadFlowGraphInformation(final SQLProvider provider,
      final INaviModule module, final Integer viewId) throws CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE02275: provider argument can not be null");
    Preconditions.checkNotNull(module, "IE02394: module argument can not be null");
    Preconditions.checkNotNull(viewId, "IE02419: viewId argument can not be null");

    final CConnection connection = provider.getConnection();
    final String query = " SELECT * FROM load_module_flowgraph_information(?,?) ";

    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, module.getConfiguration().getId());
      statement.setInt(2, viewId);
      final ResultSet resultSet = statement.executeQuery();

      while (resultSet.next()) {
        final int databaseViewId = resultSet.getInt("view_id");
        final String name = PostgreSQLHelpers.readString(resultSet, "name");
        final String description = PostgreSQLHelpers.readString(resultSet, "description");
        final ViewType viewType =
            resultSet.getString("type").equalsIgnoreCase("native") ? ViewType.Native
                : ViewType.NonNative;
        final Timestamp creationDate = resultSet.getTimestamp("creation_date");
        final Timestamp modificationDate = resultSet.getTimestamp("modification_date");
        final boolean isStared = resultSet.getBoolean("stared");
        final int nodeCount = resultSet.getInt("bbcount");
        final int edgeCount = resultSet.getInt("edgecount");

        final ImmutableNaviViewConfiguration viewConfiguration =
            new ImmutableNaviViewConfiguration(databaseViewId, name, description, viewType,
                creationDate, modificationDate, isStared, nodeCount, edgeCount);
        return viewConfiguration;
      }
      return null;

    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
