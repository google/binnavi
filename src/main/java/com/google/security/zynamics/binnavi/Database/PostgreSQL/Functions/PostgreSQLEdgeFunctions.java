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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CViewNodeHelpers;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class provides PostgreSQL queries for working with edges.
 */
public final class PostgreSQLEdgeFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLEdgeFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Determines the module ID of a node. The module ID of a node is either the module ID of the
   * function the node represents, or the module ID of the first instruction in the node.
   *
   * @param node The node whose module ID is returned.
   *
   * @return The module ID of the node.
   *
   * @throws MaybeNullException Thrown if the module ID could not be determined.
   */
  private static int getModuleId(final INaviViewNode node) throws MaybeNullException {
    if (node instanceof INaviCodeNode) {
      return ((INaviCodeNode) node).getParentFunction().getModule().getConfiguration().getId();
    } else if (node instanceof INaviFunctionNode) {
      return ((INaviFunctionNode) node).getFunction().getModule().getConfiguration().getId();
    } else {
      throw new IllegalArgumentException(
          "IE00432: Edge's source node must be an instance of CCodeNode or CFunctionNode");
    }
  }

  /**
   * Appends a global edge comment to the list of global edge comments associated with this edge.
   *
   * @param provider The provider to access the database with.
   * @param edge The Edge where to comment is associated with.
   * @param commentText The text of the comment which will be appended.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntSaveDataException If the data could not be stored in the database.
   */
  public static Integer appendGlobalEdgeComment(final AbstractSQLProvider provider,
      final INaviEdge edge, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00482: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00483: edge argument can not be null");
    Preconditions.checkNotNull(commentText, "IE00484: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE00485: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String function = "{ ? = call append_global_edge_comment(?, ?, ?, ?, ?, ?) }";

    try {
      final int sourceModuleId = getModuleId(edge.getSource());
      final int destinationModuleId = getModuleId(edge.getTarget());
      final IAddress sourceAddress = CViewNodeHelpers.getAddress(edge.getSource());
      final IAddress destinationAddress = CViewNodeHelpers.getAddress(edge.getTarget());

      try {
        final CallableStatement appendCommentFunction = connection.prepareCall(function);

        try {
          appendCommentFunction.registerOutParameter(1, Types.INTEGER);

          appendCommentFunction.setInt(2, sourceModuleId);
          appendCommentFunction.setInt(3, destinationModuleId);
          appendCommentFunction.setObject(4, sourceAddress.toBigInteger(), Types.BIGINT);
          appendCommentFunction.setObject(5, destinationAddress.toBigInteger(), Types.BIGINT);
          appendCommentFunction.setInt(6, userId);
          appendCommentFunction.setString(7, commentText);

          appendCommentFunction.execute();

          final int commentId = appendCommentFunction.getInt(1);
          if (appendCommentFunction.wasNull()) {
            throw new CouldntSaveDataException(
                "Error: Got an comment id of null from the database");
          }
          return commentId;
        } finally {
          appendCommentFunction.close();
        }
      } catch (final SQLException exception) {
        throw new CouldntSaveDataException(exception);
      }
    } catch (final MaybeNullException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Appends a local edge comment to the list of local edge comments associated with this edge.
   *
   * @param provider The provider to access the database with.
   * @param edge The Edge where to comment is associated with.
   * @param commentText The text of the comment which will be appended.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntSaveDataException If the data could not be stored in the database.
   */
  public static int appendLocalEdgeComment(final AbstractSQLProvider provider, final INaviEdge edge,
      final String commentText, final Integer userId) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00486: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00502: edge argument can not be null");
    Preconditions.checkNotNull(commentText, "IE00503: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE00504: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String function = "{ ? = call append_local_edge_comment( ?, ?, ?) }";

    try {
      final CallableStatement appendCommentFunction = connection.prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);

        appendCommentFunction.setInt(2, edge.getId());
        appendCommentFunction.setInt(3, userId);
        appendCommentFunction.setString(4, commentText);

        appendCommentFunction.execute();

        final int commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("Error: Database returned null for comment id");
        }
        return commentId;
      } finally {
        appendCommentFunction.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * This function deletes a global edge comment from the database.
   *
   * @param provider The provider to access the database.
   * @param edge The edge to which the comment is associated.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteGlobalEdgeComment(final AbstractSQLProvider provider,
      final INaviEdge edge, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE00505: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00506: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE00507: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE00508: userId argument can not be null");

    final String function = " { ? = call delete_global_edge_comment(?, ?, ?, ?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentFunction =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentFunction.registerOutParameter(1, Types.INTEGER);
        deleteCommentFunction.setInt(2, getModuleId(edge.getSource()));
        deleteCommentFunction.setInt(3, getModuleId(edge.getTarget()));
        deleteCommentFunction.setObject(
            4, ((INaviCodeNode) edge.getSource()).getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentFunction.setObject(
            5, ((INaviCodeNode) edge.getTarget()).getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentFunction.setInt(6, commentId);
        deleteCommentFunction.setInt(7, userId);

        deleteCommentFunction.execute();

        deleteCommentFunction.getInt(1);
        if (deleteCommentFunction.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } finally {
        deleteCommentFunction.close();
      }

    } catch (SQLException | MaybeNullException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * This function deletes a local edge comment from the list of edge comments associated with the
   * edge provided as argument.
   *
   * @param provider The provider to access the database.
   * @param edge The edge where the comment will be deleted.
   * @param commentId The comment id of the comment which will be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteLocalEdgeComment(final AbstractSQLProvider provider,
      final INaviEdge edge, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE00509: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00510: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE00511: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE00512: userId argument can not be null");

    final String function = " { ? = call delete_local_edge_comment(?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentFunction =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentFunction.registerOutParameter(1, Types.INTEGER);
        deleteCommentFunction.setInt(2, edge.getId());
        deleteCommentFunction.setInt(3, commentId);
        deleteCommentFunction.setInt(4, userId);

        deleteCommentFunction.execute();

        deleteCommentFunction.getInt(1);
        if (deleteCommentFunction.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } finally {
        deleteCommentFunction.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * This function edits a global edge comment which is associated with the edge provided as
   * argument.
   *
   * @param provider The provider to access the database.
   * @param edge The edge where the comment will be edited.
   * @param commentId The comment id of the comment that will be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new text of the comment.
   *
   * @throws CouldntSaveDataException if the changes to the comment could not be saved in the
   *         database.
   */
  public static void editGlobalEdgeComment(final AbstractSQLProvider provider, final INaviEdge edge,
      final Integer commentId, final Integer userId, final String newComment)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00533: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00560: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE00561: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE00562: userId argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * This function edits a local edge comment which is associated with the edge provided as
   * argument.
   *
   * @param provider The provider to access the database.
   * @param edge The edge where the comment is edited.
   * @param commentId The comment id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new text of the comment.
   *
   * @throws CouldntSaveDataException if the changes to the comment could not be saved to the
   *         database.
   */
  public static void editLocalEdgeComment(final AbstractSQLProvider provider, final INaviEdge edge,
      final Integer commentId, final Integer userId, final String newComment)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00856: provider argument can not be null");
    Preconditions.checkNotNull(edge, "IE00857: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE00858: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE00874: userId argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }
}
