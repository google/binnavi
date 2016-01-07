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
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLCommentFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains code for loading function nodes.
 */
public final class PostgreSQLFunctionNodeLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private PostgreSQLFunctionNodeLoader() {
  }

  /**
   * Loads the function nodes of a view.
   * 
   * @param provider The connection to the database.
   * @param view The view whose function nodes are loaded.
   * @param nodes The loaded nodes are stored here.
   * 
   * @throws CPartialLoadException Thrown if loading the nodes failed because a necessary module was
   *         not loaded.
   * @throws CouldntLoadDataException
   */
  public static void load(final AbstractSQLProvider provider, final INaviView view,
      final List<INaviViewNode> nodes) throws CPartialLoadException, CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE02510: provider argument can not be null");
    Preconditions.checkNotNull(view, "IE02511: view argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02512: nodes argument can not be null");

    // TODO (timkornau): query needs to go into the database.
    final String query =
        "SELECT nodes.view_id, nodes.id, functions.module_id, "
            + " function, fnodes.comment_id as local_comment, x, y, width, height, "
            + " color, selected, visible FROM " + CTableNames.NODES_TABLE + " AS nodes JOIN "
            + CTableNames.FUNCTION_NODES_TABLE + " AS fnodes "
            + " ON nodes.id = fnodes.node_id JOIN " + CTableNames.FUNCTIONS_TABLE
            + " AS functions ON functions.address = fnodes.function "
            + " AND functions.module_id = fnodes.module_id  WHERE view_id = ?";

    final Map<Integer, INaviFunctionNode> commentIdToFunctionNode =
        new HashMap<Integer, INaviFunctionNode>();

    try {
      final PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query);

      statement.setInt(1, view.getConfiguration().getId());

      final ResultSet resultSet = statement.executeQuery();

      try {
        while (resultSet.next()) {
          final int moduleId = resultSet.getInt("module_id");
          final INaviModule module = provider.findModule(moduleId);

          if (!module.isLoaded()) {
            try {
              module.load();
            } catch (final CouldntLoadDataException e) {
              throw new CPartialLoadException(
                  "E00064: The view could not be loaded because not all modules that form the view are loaded",
                  module);
            } catch (final LoadCancelledException e) {
              throw new CPartialLoadException(
                  "E00065: The view could not be loaded because not all modules that form the view are loaded",
                  module);
            }
          }

          final IAddress address = PostgreSQLHelpers.loadAddress(resultSet, "function");
          final INaviFunction function =
              module.getContent().getFunctionContainer().getFunction(address);
          final int nodeId = resultSet.getInt("id");
          Integer commentId = resultSet.getInt("local_comment");
          if (resultSet.wasNull()) {
            commentId = null;
          }
          final double posX = resultSet.getDouble("x");
          final double posY = resultSet.getDouble("y");
          final double width = resultSet.getDouble("width");
          final double height = resultSet.getDouble("height");
          final Color color = new Color(resultSet.getInt("color"));
          final boolean selected = resultSet.getBoolean("selected");
          final boolean visible = resultSet.getBoolean("visible");

          final INaviFunctionNode functionNode =
              new CFunctionNode(nodeId, function, posX, posY, width, height, color, selected,
                  visible, null, new HashSet<CTag>(), provider);
          nodes.add(functionNode);

          if (commentId != null) {
            commentIdToFunctionNode.put(commentId, functionNode);
          }
        }
      } finally {
        resultSet.close();
      }

      if (!commentIdToFunctionNode.isEmpty()) {
        final HashMap<Integer, ArrayList<IComment>> commentIdsToComments =
            PostgreSQLCommentFunctions.loadMultipleCommentsById(provider,
                commentIdToFunctionNode.keySet());

        for (final Entry<Integer, ArrayList<IComment>> commentIdToComment : commentIdsToComments
            .entrySet()) {
          commentIdToFunctionNode.get(commentIdToComment.getKey()).initializeLocalFunctionComment(
              commentIdToComment.getValue());
        }
      }

    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }
  }
}
