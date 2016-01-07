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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;



// ESCA-JAVA0014:
/**
 * This class provides queries that can be used to load information about project flow graph views.
 */
public final class PostgreSQLProjectFlowgraphsLoader extends PostgreSQLProjectViewsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLProjectFlowgraphsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the flow graph views of a project.
   * 
   * @param provider The connection to the database.
   * @param project The project whose flow graph views are loaded.
   * @param viewTagManager Tag manager responsible for tagging the loaded views.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return The loaded flow graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the flow graph views could not be loaded.
   */
  public static List<IFlowgraphView> loadFlowgraphs(final SQLProvider provider,
      final INaviProject project, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    checkArguments(provider, project, viewTagManager);

    final CConnection connection = provider.getConnection();
    final String query = " SELECT * FROM load_project_flow_graphs(?, ?) ";

    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, project.getConfiguration().getId());
      statement.setObject(2, "non-native", Types.OTHER);

      final ResultSet resultSet = statement.executeQuery();

      final Map<Integer, Set<CTag>> tags = loadTags(connection, project, viewTagManager);

      return new ArrayList<IFlowgraphView>(
          processQueryResults(resultSet, project, tags, nodeTagManager, provider,
              new ArrayList<CView>(), ViewType.NonNative, GraphType.FLOWGRAPH));
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }

  public static ImmutableNaviViewConfiguration loadFlowGraphInformation(final SQLProvider provider,
      final INaviProject project, final Integer viewId) throws CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE02618: provider argument can not be null");
    Preconditions.checkNotNull(project, "IE02619: project argument can not be null");
    Preconditions.checkNotNull(viewId, "IE02620: viewId argument can not be null");

    final CConnection connection = provider.getConnection();
    final String query = " SELECT * FROM load_project_flowGraph(?,?) ";

    try {
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, project.getConfiguration().getId());
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
