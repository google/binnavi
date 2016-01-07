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
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Provides SQL queries for working with functions.
 */
public final class PostgreSQLFunctionFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLFunctionFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Appends a global function comment.
   *
   * @param provider The provider used to access the database.
   * @param function The function where the comment is appended.
   * @param commentText The text of the comment which will be appended.
   * @param userId The user id of the currently active user.
   *
   * @return The id of the newly generated comment.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   */
  public static Integer appendGlobalFunctionComment(final AbstractSQLProvider provider,
      final INaviFunction function, final String commentText, final Integer userId)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(function, "IE01042: function argument can not be null");
    Preconditions.checkNotNull(commentText, "IE01240: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE01241: userId argument can not be null");

    final Connection connection = provider.getConnection().getConnection();

    final Integer moduleId = function.getModule().getConfiguration().getId();

    final String storedProcedure = " { ? = call append_function_comment( ?, ?, ?, ?) } ";

    try (CallableStatement appendCommentProcedure = connection.prepareCall(storedProcedure)) {

        appendCommentProcedure.registerOutParameter(1, Types.INTEGER);
        appendCommentProcedure.setInt(2, moduleId);
        appendCommentProcedure.setObject(3, function.getAddress().toBigInteger(), Types.BIGINT);
        appendCommentProcedure.setInt(4, userId);
        appendCommentProcedure.setString(5, commentText);
        appendCommentProcedure.execute();

        final int commentId = appendCommentProcedure.getInt(1);
        if (appendCommentProcedure.wasNull()) {
          throw new CouldntSaveDataException("Error: Got an comment id of null from the database");
        }
        return commentId;
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * This function deletes a global function comment from the list of comments associated with the
   * function given as argument from the database.
   *
   * @param provider The provider to access the database.
   * @param function The function where the comment is deleted.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public static void deleteGlobalFunctionComment(final AbstractSQLProvider provider,
      final INaviFunction function, final Integer commentId, final Integer userId)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE01243: provider argument can not be null");
    Preconditions.checkNotNull(function, "IE01245: codeNode argument can not be null");
    Preconditions.checkNotNull(commentId, "IE01247: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE01308: userId argument can not be null");

    final String deleteFunction = " { ? = call delete_function_comment(?, ?, ?, ?) } ";

    try (CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(deleteFunction)) {

        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);
        deleteCommentStatement.setInt(2, function.getModule().getConfiguration().getId());
        deleteCommentStatement.setObject(3, function.getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentStatement.setInt(4, commentId);
        deleteCommentStatement.setInt(5, userId);
        deleteCommentStatement.execute();
        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: The comment id returned by the database was null");
        }
    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * Edits a global function comment.
   *
   * @param provider The provider used to access the database.
   * @param function The function where the comment is edited.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new text for the comment.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public static void editGlobalFunctionComment(final AbstractSQLProvider provider,
      final INaviFunction function, final Integer commentId, final Integer userId,
      final String newComment) throws CouldntSaveDataException {
    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Forwards a function to another function.
   *
   *  The source function and the target function must be stored in the database connected to by the
   * provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param source The source function that is forwarded.
   * @param target The target function of the forwarding or null if formerly set forwarding should
   *        be removed.
   *
   * @throws CouldntSaveDataException Thrown if the function could not be forwarded.
   */
  public static void resolveFunction(
      final AbstractSQLProvider provider, final INaviFunction source, final INaviFunction target)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00444: Provider argument can not be null");
    Preconditions.checkNotNull(source, "IE00445: Source argument can not be null");
    Preconditions.checkArgument(
        source.inSameDatabase(provider), "IE00446: Source is not part of this database");

    if ((target != null) && !target.inSameDatabase(provider)) {
      throw new IllegalArgumentException("IE00447: Target is not part of this database");
    }

    final Integer parentModuleId =
        target == null ? null : target.getModule().getConfiguration().getId();
    final BigInteger parentModuleAddress =
        target == null ? null : target.getAddress().toBigInteger();

    final String query = "UPDATE " + CTableNames.FUNCTIONS_TABLE + " SET parent_module_id = ?, "
        + " parent_module_function = ? WHERE module_id = ? AND address = ?";

    try (PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query)) {

        if (parentModuleId != null) {
          statement.setInt(1, parentModuleId);
        } else {
          statement.setNull(1, Types.INTEGER);
        }
        if (parentModuleAddress != null) {
          statement.setObject(2, parentModuleAddress, Types.BIGINT);
        } else {
          statement.setNull(2, Types.BIGINT);
        }
        statement.setInt(3, source.getModule().getConfiguration().getId());
        statement.setObject(4, source.getAddress().toBigInteger().toString(), Types.BIGINT);
        statement.execute();

    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * Changes the description of a function.
   *
   * The function must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param function The function whose description is changed.
   * @param description The new description of the function.
   *
   * @throws CouldntSaveDataException Thrown if the new description could not be saved to the
   *         database.
   */
  public static void setDescription(
      final AbstractSQLProvider provider, final INaviFunction function, final String description)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00448: Provider argument can not be null");
    Preconditions.checkNotNull(function, "IE00449: Function argument can not be null");
    Preconditions.checkNotNull(description, "IE00450: Comment argument can not be null");
    Preconditions.checkArgument(
        function.inSameDatabase(provider), "IE00451: Function is not part of this database");

    final CConnection connection = provider.getConnection();

    final int module = function.getModule().getConfiguration().getId();
    final IAddress address = function.getAddress();

    final String query = "UPDATE " + CTableNames.FUNCTIONS_TABLE
        + " SET description = ? WHERE module_id = ? AND address = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {

        statement.setString(1, description);
        statement.setInt(2, module);
        statement.setObject(3, address.toBigInteger(), Types.BIGINT);
        statement.executeUpdate();
        
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Changes the name of a function.
   *
   * The function must be stored in the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param function The function whose name is changed.
   * @param name The new name of the function.
   *
   * @throws CouldntSaveDataException Thrown if storing the new name to the database failed.
   */
  public static void setName(
      final AbstractSQLProvider provider, final INaviFunction function, final String name)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE00456: Provider argument can not be null");
    Preconditions.checkNotNull(function, "IE00457: Function argument can not be null");
    Preconditions.checkNotNull(name, "IE00458: Name argument can not be null");
    Preconditions.checkArgument(
        function.inSameDatabase(provider), "IE00459: Function is not part of this database");

    final CConnection connection = provider.getConnection();

    final int module = function.getModule().getConfiguration().getId();
    final IAddress address = function.getAddress();

    final String query = "UPDATE " + CTableNames.FUNCTIONS_TABLE
        + " SET name = ? WHERE module_id = ? and address = ?";

    try (PreparedStatement statement = connection.getConnection().prepareStatement(query)) {

        statement.setString(1, name);
        statement.setInt(2, module);
        statement.setObject(3, address.toBigInteger(), java.sql.Types.BIGINT);
        statement.executeUpdate();
      
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }
}
