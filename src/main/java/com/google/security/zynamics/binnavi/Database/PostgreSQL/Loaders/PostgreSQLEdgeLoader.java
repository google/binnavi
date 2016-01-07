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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.IAddressNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;

import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains PostgreSQL queries for loading the edges of a view.
 */
public final class PostgreSQLEdgeLoader {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLEdgeLoader() {
    // You are not supposed to instantiate this class
  }

  /**
   * Initializes the global comment for an edge.
   *
   * @param edge The edge whose global comment is initialized.
   * @param globalComments The global comment to set.
   */
  private static void initializeGlobalComment(final CNaviViewEdge edge,
      final ArrayList<IComment> globalComments, final SQLProvider provider) {
    final INaviViewNode source = edge.getSource();
    final INaviViewNode target = edge.getTarget();

    if ((source instanceof INaviCodeNode) && (target instanceof IAddressNode)) {
      CommentManager.get(provider).initializeGlobalEdgeComment(edge, globalComments);
    } else if ((source instanceof INaviFunctionNode) && (target instanceof IAddressNode)) {
      CommentManager.get(provider).initializeGlobalEdgeComment(edge, globalComments);
    }
  }

  /**
   * Loads the edges of a view.
   *
   * @param provider The connection to the database.
   * @param view The view whose edges are loaded.
   * @param nodeLookup Maps between node IDs and their corresponding node objects.
   * @param edgeToGlobalCommentMap Maps between edge IDs and their associated comments.
   *
   * @return The loaded edges.
   *
   * @throws CouldntLoadDataException
   */
  private static List<INaviEdge> loadEdges(final AbstractSQLProvider provider, final INaviView view,
      final Map<Integer, INaviViewNode> nodeLookup,
      final Map<Integer, ArrayList<IComment>> edgeToGlobalCommentMap)
      throws CouldntLoadDataException {
    final String query = "SELECT * FROM load_view_edges(" + view.getConfiguration().getId() + ")";

    List<CBend> currentPaths = new ArrayList<CBend>();
    final Map<Integer, INaviEdge> commentIdToEdge = new HashMap<Integer, INaviEdge>();
    final Map<Integer, INaviEdge> edgeIdToEdge = new HashMap<Integer, INaviEdge>();

    try {
      final CConnection connection = provider.getConnection();
      final PreparedStatement statement = connection.getConnection().prepareStatement(query);
      final ResultSet resultSet = statement.executeQuery();

      try {
        while (resultSet.next()) {
          final int edgeId = resultSet.getInt("id");
          if (edgeIdToEdge.containsKey(edgeId)) {
            final INaviEdge edge = edgeIdToEdge.get(edgeId);

            final double pathX = resultSet.getDouble("x");
            final double pathY = resultSet.getDouble("y");

            if (!resultSet.wasNull()) {
              edge.addBend(pathX, pathY);
            }
            continue;
          }
          final int sourceNode = resultSet.getInt("source_node_id");
          final int targetNode = resultSet.getInt("target_node_id");
          Integer localCommentId = resultSet.getInt("comment_id");
          if (resultSet.wasNull()) {
            localCommentId = null;
          }
          final double x1 = resultSet.getDouble("x1");
          final double y1 = resultSet.getDouble("y1");
          final double x2 = resultSet.getDouble("x2");
          final double y2 = resultSet.getDouble("y2");
          final EdgeType type = EdgeType.valueOf(resultSet.getString("type").toUpperCase());
          final Color color = new Color(resultSet.getInt("color"));
          final boolean visible = resultSet.getBoolean("visible");
          final boolean selected = resultSet.getBoolean("selected");

          final INaviViewNode source = nodeLookup.get(sourceNode);
          final INaviViewNode target = nodeLookup.get(targetNode);

          final double pathX = resultSet.getDouble("x");
          final double pathY = resultSet.getDouble("y");

          if (!resultSet.wasNull()) {
            currentPaths.add(new CBend(pathX, pathY));
          }

          final CNaviViewEdge edge = new CNaviViewEdge(edgeId,
              source,
              target,
              type,
              x1,
              y1,
              x2,
              y2,
              color,
              selected,
              visible,
              null,
              currentPaths,
              provider);

          if (localCommentId != null) {
            commentIdToEdge.put(localCommentId, edge);
          }

          final ArrayList<IComment> globalComments =
              edgeToGlobalCommentMap.containsKey(edgeId) ? edgeToGlobalCommentMap.get(edgeId)
                  : null;

          if ((globalComments != null) && (globalComments.size() != 0)) {
            initializeGlobalComment(edge, globalComments, provider);
          }

          source.addOutgoingEdge(edge);
          target.addIncomingEdge(edge);

          edgeIdToEdge.put(edge.getId(), edge);

          currentPaths = new ArrayList<CBend>();
        }

        if (!commentIdToEdge.isEmpty()) {
          final HashMap<Integer, ArrayList<IComment>> commentIdToComments =
              PostgreSQLCommentFunctions.loadMultipleCommentsById(provider,
                  commentIdToEdge.keySet());
          for (final Entry<Integer, ArrayList<IComment>> commentIdToComment :
              commentIdToComments.entrySet()) {
            commentIdToEdge.get(commentIdToComment.getKey()).initializeLocalComment(
                commentIdToComment.getValue());
          }
        }
      } finally {
        resultSet.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException("Error: Loading of view edges failed");
    }

    return Lists.newArrayList(edgeIdToEdge.values());
  }

  /**
   * Loads the edge comments of a view.
   *
   * @param provider The provider used to access the database.
   * @param viewId ID of the view whose edges are loaded.
   *
   * @return A map of <Edge ID, Edge Comment>.
   *
   * @throws CouldntLoadDataException
   */
  private static Map<Integer, ArrayList<IComment>> loadGlobalEdgeComments(
      final SQLProvider provider, final int viewId) throws CouldntLoadDataException {
    // TODO (timkornau): this query needs to go into the database and needs to use the get all
    // comment ancestors
    // to build the complete
    // comment structure for each edge comment.

    final String query = "SELECT e.id, gc.comment_id FROM " + CTableNames.GLOBAL_EDGE_COMMENTS_TABLE
        + " AS gc " + " join " + CTableNames.INSTRUCTIONS_TABLE
        + " AS src_inst on gc.src_address = src_inst.address "
        + "AND gc.src_module_id = src_inst.module_id " + " join " + CTableNames.INSTRUCTIONS_TABLE
        + " AS dst_inst on gc.dst_address = dst_inst.address "
        + "AND gc.dst_module_id = dst_inst.module_id " + " join "
        + CTableNames.CODENODE_INSTRUCTIONS_TABLE
        + " AS src_nodes on src_nodes.address = gc.src_address AND src_nodes.position = 0 "
        + "AND src_nodes.module_id = gc.src_module_id " + " join "
        + CTableNames.CODENODE_INSTRUCTIONS_TABLE
        + " AS dst_nodes on dst_nodes.address = gc.dst_address AND dst_nodes.position = 0 "
        + "AND dst_nodes.module_id = gc.dst_module_id " + " join " + CTableNames.EDGES_TABLE
        + " AS e ON e.source_node_id = src_nodes.node_id AND e.target_node_id = dst_nodes.node_id "
        + " join " + CTableNames.NODES_TABLE + " AS src_n ON e.source_node_id = src_n.id "
        + " join " + CTableNames.NODES_TABLE + " AS dst_n ON e.target_node_id = dst_n.id "
        + " WHERE src_n.view_id = " + viewId + " AND dst_n.view_id = " + viewId;

    final HashMap<Integer, Integer> commentIdsToEdgeIds = new HashMap<Integer, Integer>();
    final HashMap<Integer, ArrayList<IComment>> edgeIdsToCommentArray =
        new HashMap<Integer, ArrayList<IComment>>();

    try {
      final ResultSet resultSet = provider.getConnection().executeQuery(query, true);

      try {
        while (resultSet.next()) {
          final int edgeId = resultSet.getInt("id");
          final int commentId = resultSet.getInt("comment_id");
          commentIdsToEdgeIds.put(commentId, edgeId);
        }
      } finally {
        resultSet.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    if (!commentIdsToEdgeIds.isEmpty()) {
      final HashMap<Integer, ArrayList<IComment>> commentIdsToComments = PostgreSQLCommentFunctions
          .loadMultipleCommentsById(provider, commentIdsToEdgeIds.keySet());

      for (final Entry<Integer, ArrayList<IComment>> commentIdToComment :
          commentIdsToComments.entrySet()) {
        edgeIdsToCommentArray.put(commentIdsToEdgeIds.get(commentIdToComment.getKey()),
            commentIdToComment.getValue());
      }
    }
    return edgeIdsToCommentArray;
  }

  /**
   * Loads the edges of a view.
   *
   *  The view and all nodes from the node list must be stored in the database connected to by the
   * provider argument.
   *
   * @param provider The SQL provider that provides the database connection.
   * @param view The view from where the edges are loaded.
   * @param nodes The nodes of the view which must be loaded before.
   *
   * @return The edges of the view.
   *
   * @throws CouldntLoadDataException
   */
  public static List<INaviEdge> loadEdges(final AbstractSQLProvider provider, final INaviView view,
      final Collection<INaviViewNode> nodes) throws CouldntLoadDataException {
    final Map<Integer, INaviViewNode> nodeLookup = new HashMap<Integer, INaviViewNode>();

    final List<Integer> nodeIdList = new ArrayList<Integer>();

    for (final INaviViewNode viewNode : nodes) {
      nodeLookup.put(viewNode.getId(), viewNode);
      nodeIdList.add(viewNode.getId());
    }

    final Map<Integer, ArrayList<IComment>> edgeToGlobalCommentMap =
        loadGlobalEdgeComments(provider, view.getConfiguration().getId());

    return loadEdges(provider, view, nodeLookup, edgeToGlobalCommentMap);
  }
}
