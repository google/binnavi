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
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.ViewType;



// ESCA-JAVA0014:
/**
 * This class provides SQL queries that can be used to load information about module mixed graph
 * views.
 */
public final class PostgreSQLProjectMixedGraphsLoader extends PostgreSQLProjectViewsLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLProjectMixedGraphsLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Loads the non-native mixed-graph views of a project.
   * 
   * The project, the view tag manager, and the node tag manager must be stored in the database
   * connected to by the provider argument.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param project The project from where the views are loaded.
   * @param viewTagManager View tag manager that contains all view tags of the database.
   * @param nodeTagManager The tag manager responsible for tagging view nodes.
   * 
   * @return A list of non-native mixed-graph views.
   * 
   * @throws CouldntLoadDataException Thrown if the views could not be loaded.
   */
  public static List<INaviView> loadMixedgraphs(final AbstractSQLProvider provider,
      final CProject project, final CTagManager viewTagManager, final CTagManager nodeTagManager)
      throws CouldntLoadDataException {
    checkArguments(provider, project, viewTagManager);

    final String query = "SELECT * FROM load_module_mixed_graph(?)";

    try {
      final CConnection connection = provider.getConnection();
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      statement.setInt(1, project.getConfiguration().getId());
      final ResultSet resultSet = statement.executeQuery();

      final Map<Integer, Set<CTag>> tags = loadTags(connection, project, viewTagManager);

      return new ArrayList<INaviView>(processQueryResults(resultSet, project, tags, nodeTagManager,
          provider, new ArrayList<CView>(), ViewType.NonNative, GraphType.MIXED_GRAPH));
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
