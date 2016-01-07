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
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class provides PostgreSQL queries for working with nodes.
 */
public final class PostgreSQLNodeFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLNodeFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Appends a comment as global code node comment to a code node.
   *
   * @param provider The SQL provider used to access the database.
   * @param codeNode The code node to which the global comment will be appended.
   * @param commentText The text of the comment which will be appended.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntSaveDataException Thrown if the global code node comment could not be saved to
   *         the database.
   */
  public static int appendGlobalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02445: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02446: codeNode argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02447: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE02448: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    Integer moduleId = null;
    final int nodeId = codeNode.getId();
    final BigInteger nodeAddress = codeNode.getAddress().toBigInteger();

    try {
      moduleId = codeNode.getParentFunction().getModule().getConfiguration().getId();
    } catch (final MaybeNullException exception) {
      throw new CouldntSaveDataException(
          "Error: Can not append global code node comments for nodes without a parent function");
    }

    final String function = "{ ? = call append_global_code_node_comment( ?, ?, ?, ?, ?) }";

    try {
      final CallableStatement appendCommentFunction = connection.prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);

        appendCommentFunction.setInt(2, moduleId);
        appendCommentFunction.setInt(3, nodeId);
        appendCommentFunction.setObject(4, nodeAddress, java.sql.Types.BIGINT);
        appendCommentFunction.setInt(5, userId);
        appendCommentFunction.setString(6, commentText);

        appendCommentFunction.execute();

        final int commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("Error: Got an comment id of null from the database");
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
   * Appends a comment to the list of comments associated with the group node in the database.
   *
   * @param provider The provider to access the database.
   * @param groupNode The group node to which the comment is associated.
   * @param commentText The comment text of the comment.
   * @param userId The user id of the currently active user.
   *
   * @return The generated comment id from the database.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   */
  public static Integer appendGroupNodeComment(final SQLProvider provider,
      final INaviGroupNode groupNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02449: provider argument can not be null");
    Preconditions.checkNotNull(groupNode, "IE02450: groupNode argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02451: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE02452: userId argument can not be null");
    Preconditions.checkArgument(groupNode.getId() > 0, "Error: group node is not saved.");


    final String function = " { ? = call append_group_node_comment(?, ?, ?) } ";

    try {
      final CallableStatement appendCommentFunction =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);
        appendCommentFunction.setInt(2, groupNode.getId());
        appendCommentFunction.setInt(3, userId);
        appendCommentFunction.setString(4, commentText);

        appendCommentFunction.execute();
        final Integer commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("Error: Got an comment id of null from the database");
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
   * Appends a comment as local code node comment to a code node.
   *
   * @param provider The provider used to access the database.
   * @param codeNode The code node where the comment will be appended.
   * @param commentText The text of the comment which will be appended.
   * @param userId the user id of the currently active user.
   *
   * @throws CouldntSaveDataException
   */
  public static int appendLocalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02453: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02454: codeNode argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02455: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE02456: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final String function = "{ ? = call append_local_code_node_comment( ?, ?, ?, ?) }";

    Integer moduleId = null;
    final int nodeId = codeNode.getId();

    try {
      moduleId = codeNode.getParentFunction().getModule().getConfiguration().getId();
    } catch (final MaybeNullException exception) {
      throw new CouldntSaveDataException(exception);
    }

    try {
      final CallableStatement appendCommentFunction = connection.prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);
        appendCommentFunction.setInt(2, moduleId);
        appendCommentFunction.setInt(3, nodeId);
        appendCommentFunction.setInt(4, userId);
        appendCommentFunction.setString(5, commentText);

        appendCommentFunction.execute();

        final int commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("E00037: ");
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
   * Appends a local function node comment.
   *
   * @param provider The provider used to access the database.
   * @param functionNode The function node where to comment will be appended.
   * @param commentText The text of the comment which will be appended.
   * @param userId The user id of the currently active user.
   *
   * @return The id of the newly generated comment.
   *
   * @throws CouldntSaveDataException if the comment could not be stored to the database.
   */
  public static int appendLocalFunctionNodeComment(final SQLProvider provider,
      final INaviFunctionNode functionNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02457: provider argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE02458: functionNode argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02459: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02460: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final int moduleId = functionNode.getFunction().getModule().getConfiguration().getId();

    final String function = " { ? = call append_function_node_comment(?, ?, ?, ?) } ";

    try {
      final CallableStatement appendCommentFunction = connection.prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);

        appendCommentFunction.setInt(2, moduleId);
        appendCommentFunction.setInt(3, functionNode.getId());
        appendCommentFunction.setInt(4, userId);
        appendCommentFunction.setString(5, commentText);

        appendCommentFunction.execute();

        final int commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("Error: Got an comment id of null from the database");
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
   * Appends a local comment to a text node.
   *
   * @param provider the provider to access the database.
   * @param textNode The text node where to add the comment.
   * @param commentText The comment text of the comment.
   * @param userId the user id of the currently active user.
   *
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   */
  public static Integer appendTextNodeComment(final SQLProvider provider,
      final INaviTextNode textNode, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02461: provider argument can not be null");
    Preconditions.checkNotNull(textNode, "IE02462: textNode argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02463: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE02464: userId argument can not be null");

    final String function = " { ? = call append_text_node_comment(?, ?, ?) } ";

    try {
      final CallableStatement appendCommentFunction =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);
        appendCommentFunction.setInt(2, textNode.getId());
        appendCommentFunction.setInt(3, userId);
        appendCommentFunction.setString(4, commentText);

        appendCommentFunction.execute();
        final Integer commentId = appendCommentFunction.getInt(1);
        if (appendCommentFunction.wasNull()) {
          throw new CouldntSaveDataException("Error: Got an comment id of null from the database");
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
   * Deletes a global node comment from the list of global node comments associated with this code
   * node.
   *
   * @param provider The provider used to access the database.
   * @param codeNode The code node where the comment will be deleted.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  public static void deleteGlobalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02465: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02466: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02467: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02468: userId argument can not be null");

    final String function = " { ? = call delete_global_code_node_comment(?, ?, ?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);
        deleteCommentStatement.setInt(2,
            codeNode.getParentFunction().getModule().getConfiguration().getId());
        deleteCommentStatement.setInt(3, codeNode.getId());
        deleteCommentStatement.setObject(4, codeNode.getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentStatement.setInt(5, commentId);
        deleteCommentStatement.setInt(6, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned by the database was null");
        }

      } catch (final MaybeNullException exception) {
        throw new CouldntDeleteException(exception);
      } finally {
        deleteCommentStatement.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Deletes a group node comment from the list of comments associated with the group node given as
   * argument.
   *
   * @param provider The provider to access the database.
   * @param groupNode The group node to which the comment is associated.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteGroupNodeComment(final SQLProvider provider,
      final INaviGroupNode groupNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02469: provider argument can not be null");
    Preconditions.checkNotNull(groupNode, "IE02470: groupNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02471: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02472: userId argument can not be null");

    final String function = " { ? = call delete_group_node_comment(?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);

        deleteCommentStatement.setInt(2, groupNode.getId());
        deleteCommentStatement.setInt(3, commentId);
        deleteCommentStatement.setInt(4, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } finally {
        deleteCommentStatement.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Deletes a local code node comment from the list of local code node comments associated with the
   * given code node.
   *
   * @param provider The provider to access the database-
   * @param codeNode The code node where the comment will be deletes.
   * @param commentId The id of the comment which will be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException Thrown if the comment could not be deleted from the database.
   */
  public static void deleteLocalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02473: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02474: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02475: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02476: userId argument can not be null");

    final String function = " { ? = call delete_local_code_node_comment(?, ?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);

        deleteCommentStatement.setInt(2,
            codeNode.getParentFunction().getModule().getConfiguration().getId());
        deleteCommentStatement.setInt(3, codeNode.getId());
        deleteCommentStatement.setInt(4, commentId);
        deleteCommentStatement.setInt(5, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } catch (final MaybeNullException exception) {
        throw new CouldntDeleteException(exception);
      } finally {
        deleteCommentStatement.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Deletes a local function node comment from the list of local function node comments associated
   * with the given function node.
   *
   * @param provider THe provider to access the database.
   * @param functionNode The function node to which the comment is associated.
   * @param commentId The id of the comment which will be deleted.
   * @param userId The id of the user of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteLocalFunctionNodeComment(final SQLProvider provider,
      final INaviFunctionNode functionNode, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02477: provider argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE02478: functionNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02479: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02480: userId argument can not be null");

    final String function = " { ? = call delete_function_node_comment(?, ?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);

        deleteCommentStatement.setInt(2,
            functionNode.getFunction().getModule().getConfiguration().getId());
        deleteCommentStatement.setInt(3, functionNode.getId());
        deleteCommentStatement.setInt(4, commentId);
        deleteCommentStatement.setInt(5, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } finally {
        deleteCommentStatement.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Deletes a text node comment from the list of comments associated with the given text node.
   *
   * @param provider The provider to access the database.
   * @param textNode The text node to which the comment is associated.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteTextNodeComment(final SQLProvider provider, final INaviTextNode textNode,
      final Integer commentId, final Integer userId) throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02481: provider argument can not be null");
    Preconditions.checkNotNull(textNode, "IE02482: textNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02483: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02484: userId argument can not be null");

    final String function = " { ? = call delete_text_node_comment(?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);

        deleteCommentStatement.setInt(2, textNode.getId());
        deleteCommentStatement.setInt(3, commentId);
        deleteCommentStatement.setInt(4, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: the comment id returned from the database was null");
        }
      } finally {
        deleteCommentStatement.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Edits a global code node comment
   *
   * @param provider The provider to access the database.
   * @param codeNode The code node where the global comment is edited.
   * @param commentId The comment id of the comment to be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment text.
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  public static void editGlobalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId,
      final String newComment) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02485: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02486: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02487: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02488: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02489: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Edits a group node comment.
   *
   * @param provider The provider to access the database.
   * @param groupNode The code node where the global comment is edited.
   * @param commentId The comment id of the comment to be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment text.
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  public static void editGroupNodeComment(final SQLProvider provider,
      final INaviGroupNode groupNode, final Integer commentId, final Integer userId,
      final String newComment) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02490: provider argument can not be null");
    Preconditions.checkNotNull(groupNode, "IE02491: groupNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02492: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02493: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02494: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Edits a local code node comment
   *
   * @param provider The provider to access the database.
   * @param codeNode The code node where the local comment is edited.
   * @param commentId The comment id of the comment to be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment text.
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  public static void editLocalCodeNodeComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final Integer commentId, final Integer userId,
      final String newComment) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02495: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02496: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02497: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02498: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02499: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Edits a local function node comment.
   *
   * @param provider The provider to access the database.
   * @param functionNode The function node where the local comment is edited.
   * @param commentId The comment id of the comment to be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment text.
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  public static void editLocalFunctionNodeComment(final SQLProvider provider,
      final INaviFunctionNode functionNode, final Integer commentId, final Integer userId,
      final String newComment) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02500: provider argument can not be null");
    Preconditions.checkNotNull(functionNode, "IE02501: functionNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02502: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02503: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02504: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Edits a text node comment.
   *
   * @param provider The provider to access the database.
   * @param textNode The text node where the comment is edited.
   * @param commentId The comment id of the comment to be edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new comment string which will be saved to the database.
   *
   * @throws CouldntSaveDataException if the comment changes could not be saved to the database.
   */
  public static void editTextNodeComment(final SQLProvider provider, final INaviTextNode textNode,
      final Integer commentId, final Integer userId, final String newComment)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02505: provider argument can not be null");
    Preconditions.checkNotNull(textNode, "IE02506: groupNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02507: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02508: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02509: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Tags a node with a given tag.
   *
   * The node must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param node The node to be tagged.
   * @param tagId The ID of the tag that is assigned to the node.
   *
   * @throws CouldntSaveDataException Thrown if the node could not be tagged.
   */
  public static void tagNode(final SQLProvider provider, final INaviViewNode node, final int tagId)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01309: provider argument can not be null");
    Preconditions.checkNotNull(node, "IE01663: node argument can not be null");

    final CConnection connection = provider.getConnection();

    untagNode(provider, node, tagId);

    final String tagQuery = String.format(
        "INSERT INTO %s " + "(node_id, tag_id) " + "VALUES (%d, %d)",
        CTableNames.TAGGED_NODES_TABLE, node.getId(), tagId);

    try {
      connection.executeUpdate(tagQuery, true);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Removes a tag from a node.
   *
   * The node must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param node The node to be untagged.
   * @param tagId The ID of the tag to be removed from the node.
   *
   * @throws CouldntSaveDataException Thrown if the node could not be untagged.
   */
  public static void untagNode(final SQLProvider provider, final INaviViewNode node,
      final int tagId) throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01990: Provider argument can not be null");
    Preconditions.checkNotNull(node, "IE01991: Node argument can not be null");

    final CConnection connection = provider.getConnection();

    try {
      connection.executeUpdate(String.format(
          "DELETE FROM %s " + " WHERE node_id = %d AND tag_id = %d", CTableNames.TAGGED_NODES_TABLE,
          node.getId(), tagId), true);
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }
}
