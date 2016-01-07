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
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class PostgreSQLCommentFunctions {

  private PostgreSQLCommentFunctions() {
    // You are not supposed to instantiate this class.
  }

  /**
   * Generic function which edits a comment object in the database.
   *
   * @param provider The provider used to access the database server.
   * @param commentId The id of the comment which is edited.
   * @param userId The id of the current active user.
   * @param newComment The comment which is stored in the database.
   *
   * @throws CouldntSaveDataException If the data could not be stored in the database.
   */
  public static void editComment(final SQLProvider provider, final Integer commentId,
      final Integer userId, final String newComment) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00436: provider argument can not be null");
    Preconditions.checkNotNull(commentId, "IE00437: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE00438: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE00439: newComment argument can not be null");

    final String function = "{ call edit_comment(?, ?, ?) }";

    try (CallableStatement editCommentFunction =
          provider.getConnection().getConnection().prepareCall(function)) {
    
        editCommentFunction.setInt(1, commentId);
        editCommentFunction.setInt(2, userId);
        editCommentFunction.setString(3, newComment);

        editCommentFunction.execute();

    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Loads comments by a comment root id it returns all ancestors of the comment.
   *
   * @param provider The provider used to access the database.
   * @param commentRootId The comment root id where to start the recursive query.
   * @return An array of comments from and including the comment referenced by the comment root id.
   *
   * @throws CouldntLoadDataException if the data could not be loaded from the database.
   */
  public static ArrayList<IComment> loadCommentByCommentId(final SQLProvider provider,
      final int commentRootId) throws CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE00440: connection argument can not be null");

    final HashMap<Integer, IComment> commentIdToComment = new HashMap<>();
    final ArrayList<IComment> comments = new ArrayList<>();

    final String commentQuery = "SELECT * FROM get_all_comment_ancestors(" + commentRootId + ");";

    try (ResultSet resultSet = provider.getConnection().executeQuery(commentQuery, true)) {
    
        while (resultSet.next()) {
          resultSet.getInt("level");
          final int commentId = resultSet.getInt("id");
          Integer parentId = resultSet.getInt("parent_id");
          if (resultSet.wasNull()) {
            parentId = null;
          }
          final int userId = resultSet.getInt("user_id");
          final String commentText = resultSet.getString("comment");
          final CComment comment = new CComment(commentId,
              CUserManager.get(provider).getUserById(userId), commentIdToComment.get(parentId),
              commentText);
          commentIdToComment.put(commentId, comment);
          comments.add(comment);
        }
      
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    return comments;
  }

  /**
   * Loads multiple comments from the database at once. The function uses the passed array list of
   * integer values to create a PostgreSQL array and retrieves all of those comments and their
   * respective ancestors.
   *
   * @param provider The provider to access the database.
   * @param commentIds The list of comments ids which will be fetched from the database.
   * @return A Map from comment id to list of comments.
   *
   * @throws CouldntLoadDataException
   */
  public static HashMap<Integer, ArrayList<IComment>> loadMultipleCommentsById(
      final SQLProvider provider, final Collection<Integer> commentIds)
      throws CouldntLoadDataException {

    Preconditions.checkNotNull(provider, "IE00480: provider argument can not be null");
    Preconditions.checkNotNull(commentIds, "IE00481: commentIds argument can not be null");

    final String query = "SELECT * FROM get_all_comment_ancestors_multiple(?)";
    final HashMap<Integer, IComment> commentIdToComment = new HashMap<>();
    final Object[] commentIdsArray = commentIds.toArray();
    final HashMap<Integer, ArrayList<IComment>> commentIdToComments = new HashMap<>();

    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareCall(query)) {

        statement.setArray(1,
          provider.getConnection().getConnection().createArrayOf("int4", commentIdsArray));
        final ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
          final int rootComment = resultSet.getInt("commentid");
          resultSet.getInt("level");
          final int commentId = resultSet.getInt("id");
          final int parentId = resultSet.getInt("parent_id");
          final int userId = resultSet.getInt("user_id");
          final String commentText = resultSet.getString("comment");
          final IUser user = CUserManager.get(provider).getUserById(userId);

          final CComment comment =
              new CComment(commentId, user, commentIdToComment.get(parentId), commentText);

          commentIdToComment.put(commentId, comment);
          if (commentIdToComments.containsKey(rootComment)) {
            commentIdToComments.get(rootComment).add(comment);
          } else {
            commentIdToComments.put(rootComment, Lists.<IComment>newArrayList(comment));
          }
        }
     
    } catch (final SQLException exception) {
      throw new CouldntLoadDataException(exception);
    }

    return commentIdToComments;
  }
}
