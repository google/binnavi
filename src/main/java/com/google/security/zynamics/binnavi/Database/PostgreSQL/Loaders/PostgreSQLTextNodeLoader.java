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

import java.awt.Color;
import java.sql.PreparedStatement;
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
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Contains code for loading text nodes.
 */
public final class PostgreSQLTextNodeLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLTextNodeLoader() {
  }

  /**
   * Loads the text nodes of a view.
   * 
   * @param provider The connection to the database.
   * @param view The view whose text nodes are loaded.
   * @param nodes The loaded nodes are stored here.
   * 
   * @throws CouldntLoadDataException
   */
  public static void load(final AbstractSQLProvider provider, final INaviView view,
      final List<INaviViewNode> nodes) throws CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE02516: provider argument can not be null");
    Preconditions.checkNotNull(view, "IE02517: view argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02518: nodes argument can not be null");

    final Map<Integer, INaviTextNode> commentIdToTextNode = new HashMap<Integer, INaviTextNode>();

    final String query =
        "SELECT id, comment_id, x, y, width, height, color, selected, visible " + " FROM "
            + CTableNames.NODES_TABLE + " JOIN " + CTableNames.TEXT_NODES_TABLE
            + " ON id = node_id " + " WHERE view_id = " + view.getConfiguration().getId();

    // TODO (timkornau): the SQL code in here needs to go into a stored procedure in the
    // database. Also the commentId code has to be checked again and other conditions need to be
    // made possible.

    try {
      final PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query);

      final ResultSet resultSet = statement.executeQuery();

      try {
        while (resultSet.next()) {
          final int nodeId = resultSet.getInt("id");
          Integer commentId = resultSet.getInt("comment_id");
          if (resultSet.wasNull()) {
            commentId = null;
          }

          final double xPos = resultSet.getDouble("x");
          final double yPos = resultSet.getDouble("y");
          final double width = resultSet.getDouble("width");
          final double height = resultSet.getDouble("height");
          final Color color = new Color(resultSet.getInt("color"));
          final boolean selected = resultSet.getBoolean("selected");
          final boolean visible = resultSet.getBoolean("visible");

          final INaviTextNode textNode =
              new CTextNode(nodeId, xPos, yPos, width, height, color, selected, visible,
                  new HashSet<CTag>(), null, provider);

          if (commentId != null) {
            commentIdToTextNode.put(commentId, textNode);
          }

          nodes.add(textNode);
        }
      } finally {
        resultSet.close();
      }

      if (!commentIdToTextNode.isEmpty()) {
        final HashMap<Integer, ArrayList<IComment>> commentIdToComments =
            PostgreSQLCommentFunctions.loadMultipleCommentsById(provider,
                commentIdToTextNode.keySet());

        for (final Entry<Integer, ArrayList<IComment>> commentIdToComment : commentIdToComments
            .entrySet()) {
          commentIdToTextNode.get(commentIdToComment.getKey()).initializeComment(
              commentIdToComment.getValue());
        }
      }
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
