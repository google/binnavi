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
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;

/**
 * This class provides PostgreSQL queries that can be used to load information about project call
 * graph views.
 */
public final class PostgreSQLProjectCallgraphLoader extends PostgreSQLProjectViewsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLProjectCallgraphLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the non-native call graph views of a project.
   * 
   * The project, the node tag manager, and the view tag manager must be stored in the database
   * connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param project The project from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return A list of non-native call graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static List<ICallgraphView> loadCallgraphViews(final AbstractSQLProvider provider,
      final CProject project, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    checkArguments(provider, project, viewTagManager);

    final String query = " SELECT * FROM load_project_call_graphs(?, ?) ";

    try {
      final CConnection connection = provider.getConnection();
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, project.getConfiguration().getId());
      statement.setObject(2, "non-native", Types.OTHER);
      final ResultSet resultSet = statement.executeQuery();

      final Map<Integer, Set<CTag>> tags = loadTags(connection, project, viewTagManager);

      return new ArrayList<ICallgraphView>(
          processQueryResults(resultSet, project, tags, nodeTagManager, provider,
              new ArrayList<CView>(), ViewType.NonNative, GraphType.CALLGRAPH));
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
