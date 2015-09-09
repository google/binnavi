/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.awt.Color;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Contains code for loading group nodes.
 */
public final class PostgreSQLGroupNodeLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLGroupNodeLoader() {
  }

  /**
   * Loads the group nodes of a view.
   * 
   * @param provider The connection to the database.
   * @param view The view whose group nodes are loaded.
   * @param nodes The loaded nodes are stored here.
   * 
   * @throws SQLException Thrown of loading the nodes failed.
   * @throws CouldntLoadDataException
   */
  public static void load(final AbstractSQLProvider provider, final INaviView view,
      final List<INaviViewNode> nodes) throws SQLException, CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE02513: provider argument can not be null");
    Preconditions.checkNotNull(view, "IE02514: view argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02515: nodes argument can not be null");

    final Map<Integer, INaviGroupNode> commentIdToGroupNode =
        new HashMap<Integer, INaviGroupNode>();

    final String query =
        "SELECT id, comment_id , collapsed, x, y, width, height, color, selected, visible "
            + " FROM " + CTableNames.NODES_TABLE + " JOIN " + CTableNames.GROUP_NODES_TABLE
            + " ON id = node_id WHERE view_id = " + view.getConfiguration().getId();

    try (ResultSet resultSet = provider.getConnection().executeQuery(query, true)) {
      while (resultSet.next()) {
        final int nodeId = resultSet.getInt("id");
        Integer commentId = resultSet.getInt("comment_id");
        if (resultSet.wasNull()) {
          commentId = null;
        }
        final boolean collapsed = resultSet.getBoolean("collapsed");
        final double posX = resultSet.getDouble("x");
        final double posY = resultSet.getDouble("y");
        final double width = resultSet.getDouble("width");
        final double height = resultSet.getDouble("height");
        final Color color = new Color(resultSet.getInt("color"));
        final boolean selected = resultSet.getBoolean("selected");
        final boolean visible = resultSet.getBoolean("visible");

        final INaviGroupNode groupNode =
            new CGroupNode(nodeId, posX, posY, width, height, color, selected, visible,
                new HashSet<CTag>(), null, collapsed, provider);

        if (commentId != null) {
          commentIdToGroupNode.put(commentId, groupNode);
        }

        nodes.add(groupNode);
      }

      if (!commentIdToGroupNode.isEmpty()) {
        final HashMap<Integer, ArrayList<IComment>> commentIdsToComments =
            PostgreSQLCommentFunctions.loadMultipleCommentsById(provider,
                commentIdToGroupNode.keySet());

        for (final Entry<Integer, ArrayList<IComment>> commentIdToComment : commentIdsToComments
            .entrySet()) {
          commentIdToGroupNode.get(commentIdToComment.getKey()).initializeComment(
              commentIdToComment.getValue());
        }
      }

    }
  }

  /**
   * Sets up the elements of the group nodes of a view.
   * 
   * @param connection The connection to the database.
   * @param view The view whose group nodes are set up.
   * @param nodes The nodes of the view.
   * 
   * @throws SQLException Thrown if setting up the group nodes fails.
   */
  public static void setupGroupNodes(final CConnection connection, final INaviView view,
      final List<INaviViewNode> nodes) throws SQLException {
    final String query =
        "SELECT id, parent_id FROM " + CTableNames.NODES_TABLE + " WHERE view_id = "
            + view.getConfiguration().getId() + " ORDER BY id";

    int counter = 0;

    int firstId = -1;

    try (ResultSet resultSet = connection.executeQuery(query, true)) {
      while (resultSet.next()) {
        if (firstId == -1) {
          firstId = resultSet.getInt("id");
        }

        final int parentId = resultSet.getInt("parent_id");

        if (!resultSet.wasNull()) {
          final INaviViewNode node = nodes.get(counter);

          final INaviViewNode parent = nodes.get(parentId - firstId);

          ((CGroupNode) parent).addElement(node);
        }

        counter++;
      }
    }
  }
}
