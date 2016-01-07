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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Savers;

import java.sql.SQLException;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.CView;

public final class PostgreSQLViewSaver {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLViewSaver() {
    // You are not supposed to instantiate this class.
  }

  protected static void checkArguments(final AbstractSQLProvider provider, final CView view) {
    Preconditions.checkNotNull(provider, "IE01888: Provider argument can not be null");
    Preconditions.checkNotNull(view, "IE01940: View argument can not be null");
    Preconditions.checkArgument(view.inSameDatabase(provider),
        "IE02028: View is not part of this database");
  }

  /**
   * Deletes the nodes of a view from the databases.
   * 
   * @param connection The connection to the database.
   * @param viewId ID of the view whose nodes are deleted.
   * 
   * @throws SQLException Thrown if the nodes could not be deleted.
   */
  protected static void deleteNodes(final CConnection connection, final int viewId)
      throws SQLException {
    final String query = "DELETE FROM " + CTableNames.NODES_TABLE + " WHERE view_id = " + viewId;

    connection.executeUpdate(query, true);
  }

  /**
   * Saves a view to the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param view The view to save to the database.
   * 
   * @throws CouldntSaveDataException Thrown if the view could not be saved to the database.
   */
  public static void save(final AbstractSQLProvider provider, final CView view)
      throws CouldntSaveDataException {
    PostgreSQLViewSaver.checkArguments(provider, view);

    final CConnection connection = provider.getConnection();

    try {
      PostgreSQLHelpers.beginTransaction(connection);

      final int viewId = view.getConfiguration().getId();

      final List<INaviViewNode> nodes = view.getGraph().getNodes();
      final List<INaviEdge> edges = view.getGraph().getEdges();

      PostgreSQLViewSaver.deleteNodes(connection, viewId);

      // Store all nodes
      PostgreSQLNodeSaver.writeNodes(provider, viewId, nodes);

      // Store all edges
      PostgreSQLEdgeSaver.writeEdges(provider, edges);

      PostgreSQLHelpers.endTransaction(connection);
    } catch (final SQLException exception) {
      try {
        PostgreSQLHelpers.rollback(connection);
      } catch (final SQLException e) {
        CUtilityFunctions.logException(e);
      }

      throw new CouldntSaveDataException(exception);
    }
  }

}
