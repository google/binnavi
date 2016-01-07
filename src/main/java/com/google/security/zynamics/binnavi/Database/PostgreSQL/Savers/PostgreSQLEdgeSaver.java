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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;

public final class PostgreSQLEdgeSaver {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLEdgeSaver() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Writes the data from the edge objects to the edges table.
   * 
   * @param connection Connection to a PostgreSQL database.
   * @param edges The edges to write.
   * 
   * @throws SQLException Thrown if storing the edges failed.
   */
  private static void fillEdgesTable(final CConnection connection, final List<INaviEdge> edges)
      throws SQLException {

    final String query =
        "INSERT INTO " + CTableNames.EDGES_TABLE
            + "(source_node_id, target_node_id, x1, y1, x2, y2, type, "
            + "color, visible, selected, comment_id) VALUES "
            + "( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

    final PreparedStatement preparedStatement =
        connection.getConnection().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);

    try {
      for (final INaviEdge edge : edges) {
        preparedStatement.setInt(1, edge.getSource().getId());
        preparedStatement.setInt(2, edge.getTarget().getId());
        preparedStatement.setDouble(3, edge.getX1());
        preparedStatement.setDouble(4, edge.getY1());
        preparedStatement.setDouble(5, edge.getX2());
        preparedStatement.setDouble(6, edge.getY2());
        preparedStatement.setObject(7, edge.getType().toString().toLowerCase(), Types.OTHER);
        preparedStatement.setInt(8, edge.getColor().getRGB());
        preparedStatement.setBoolean(9, edge.isVisible());
        preparedStatement.setBoolean(10, edge.isSelected());
        if (edge.getLocalComment() == null) {
          preparedStatement.setNull(11, Types.INTEGER);
        } else {
          preparedStatement.setInt(11, Iterables.getLast(edge.getLocalComment()).getId());
        }

        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();

      final ResultSet resultSet = preparedStatement.getGeneratedKeys();

      for (final INaviEdge edge : edges) {
        if (resultSet.next()) {
          edge.setId(resultSet.getInt(1));
        } else {
          throw new IllegalStateException(
              "Error: The number of keys generated does not match the number of edges");
        }
      }
    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);
      CUtilityFunctions.logException(exception.getNextException());
    } finally {
      preparedStatement.close();
    }
  }

  /**
   * Writes the data from the edge objects to the edge paths table.
   * 
   * @param connection Connection to a database.
   * @param edges The edges to write.
   * 
   * @throws SQLException Thrown if storing the edge paths failed.
   */
  protected static void fillEdgepathsTable(final CConnection connection, final List<INaviEdge> edges)
      throws SQLException {
    final String query =
        "INSERT INTO " + CTableNames.EDGE_PATHS_TABLE
            + "(edge_id, position, x, y) VALUES (?, ?, ?, ?)";

    final PreparedStatement preparedStatement = connection.getConnection().prepareStatement(query);

    try {
      for (final INaviEdge edge : edges) {
        for (final CBend bend : edge.getBends()) {
          preparedStatement.setInt(1, edge.getId());
          preparedStatement.setInt(2, edge.getBends().indexOf(bend));
          preparedStatement.setDouble(3, bend.getX());
          preparedStatement.setDouble(4, bend.getY());
          preparedStatement.addBatch();
        }
      }
      preparedStatement.executeBatch();

    } catch (final SQLException exception) {
      CUtilityFunctions.logException(exception);
      CUtilityFunctions.logException(exception.getNextException());
    } finally {
      preparedStatement.close();
    }
  }

  /**
   * Writes the edges of a view to the database.
   * 
   * @param provider The SQL provider that provides the connection.
   * @param edges The edges to write to the database.
   * 
   * @throws SQLException Thrown if the edges could not be saved.
   */
  public static void writeEdges(final SQLProvider provider, final List<INaviEdge> edges)
      throws SQLException {

    Preconditions.checkNotNull(provider, "IE02253: Provider argument can not be null");
    Preconditions.checkNotNull(edges, "IE02254: Edges argument can not be null");
    for (final INaviEdge edge : edges) {
      Preconditions.checkArgument(edge.inSameDatabase(provider),
          "IE02255: Edge list contains an edge that is not part of this database");
    }

    if (edges.isEmpty()) {
      return;
    }

    final CConnection connection = provider.getConnection();

    fillEdgesTable(connection, edges);
    fillEdgepathsTable(connection, edges);
  }
}
