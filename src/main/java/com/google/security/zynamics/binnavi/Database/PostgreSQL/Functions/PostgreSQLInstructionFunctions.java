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
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTypeConverter;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import com.google.security.zynamics.zylib.general.Convert;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PostgreSQLInstructionFunctions {
  /**
   * Do not instantiate this class.
   */
  private PostgreSQLInstructionFunctions() {
    // You are not supposed to instantiate this class
  }

  /**
   * Creates a new expression tree in the database.
   *
   * @param provider The connection to the database.
   * @param nodes The nodes of the tree to be saved.
   *
   * @return The ID of the created tree in the database.
   *
   * @throws SQLException Thrown if the tree could not be created.
   */
  private static int createExpressionTree(
      final SQLProvider provider, final Set<INaviOperandTreeNode> nodes, final INaviModule module)
          throws SQLException {
    final int moduleId = module.getConfiguration().getId();

    final ResultSet resultSet = provider.getConnection().executeQuery(
        "select max(id) + 1 AS id from " + CTableNames.EXPRESSION_TREE_IDS_TABLE
        + " where module_id = " + moduleId, true);

    int expressionTreeId = -1;

    try {
      if (resultSet.next()) {
        expressionTreeId = resultSet.getInt("id");
      }

    } finally {
      resultSet.close();
    }

    provider.getConnection().executeUpdate(String.format(
        "insert into " + CTableNames.EXPRESSION_TREE_IDS_TABLE + " values(%d , %d)", moduleId,
        expressionTreeId), true);

    for (final INaviOperandTreeNode node : nodes) {
      provider.getConnection().executeUpdate(String.format(
          "insert into " + CTableNames.EXPRESSION_TREE_MAPPING_TABLE + " values(%d, %d, %d)",
          moduleId, expressionTreeId, node.getId()), true);
    }

    return expressionTreeId;
  }

  /**
   * Creates a new operand in the database.
   *
   * @param provider The connection to the database.
   * @param position The position of the operand in the instruction.
   * @param expressionTreeId ID of the expression tree the operand belongs to.
   *
   * @throws SQLException Thrown if the creation of the operand failed.
   */
  private static void createOperand(final SQLProvider provider, final int moduleId,
      final BigInteger address, final int position, final int expressionTreeId)
          throws SQLException {
    final String query = String.format("insert into " + CTableNames.OPERANDS_TABLE
        + "(module_id, address, position, expression_tree_id) values(%d, %d, %d, %d)", moduleId,
        address, position, expressionTreeId);

    provider.getConnection().executeUpdate(query, true);
  }

  /**
   * Creates a new operand expression in the database.
   *
   * @param provider The connection to the database.
   * @param node The node to create in the database.
   * @param parent The ID of the parent node.
   *
   * @throws SQLException Thrown if the operand expression could not be created.
   */
  private static void createOperandExpression(
      final SQLProvider provider, final INaviOperandTreeNode node, final int parent)
          throws SQLException {
    if (node.getId() != -1) {
      return;
    }

    final ExpressionType type = node.getType();
    final String value = getValue(type, node.getValue());

    final int moduleId = node.getOperand().getInstruction().getModule().getConfiguration().getId();
    final int typeId = COperandTypeConverter.convert(type);
    final String immediate = Convert.isDecString(value) ? value : "null";
    final String symbol = Convert.isDecString(value) ? "null" : value;
    final String parentString = parent == 0 ? "null" : String.valueOf(parent);

    final ResultSet resultSet = provider.getConnection().executeQuery("select max(id)+1 AS id from "
        + CTableNames.EXPRESSION_TREE_TABLE + " where module_id = " + moduleId, true);

    try {
      if (resultSet.next()) {
        final int id = resultSet.getInt("id");

        final String query = String.format("insert into " + CTableNames.EXPRESSION_TREE_TABLE
            + "(module_id, id, type, symbol, immediate, position, parent_id) "
            + " values(%d, %d , %d, ?, %s, 0, %s)", moduleId, id, typeId, immediate, parentString);

        final PreparedStatement statement =
            provider.getConnection().getConnection().prepareStatement(query);

        try {
          statement.setString(1, symbol);

          statement.executeUpdate();
        } finally {
          statement.close();
        }

        node.setId(id);
      }
    } finally {
      resultSet.close();
    }

  }

  /**
   * Creates an operand expression tree in the database.
   *
   * @param provider The connection to the database.
   * @param rootNode The root node of the operand tree to crate.
   * @param parent ID of the parent node of the root node.
   * @param nodes Output operand for all created nodes.
   *
   * @throws SQLException Thrown if the creation of the operand expression tree
   *         failed.
   */
  private static void createOperandExpression(final SQLProvider provider,
      final INaviOperandTreeNode rootNode, final int parent, final Set<INaviOperandTreeNode> nodes)
          throws SQLException {
    createOperandExpression(provider, rootNode, parent);

    nodes.add(rootNode);

    for (final INaviOperandTreeNode child : rootNode.getChildren()) {
      createOperandExpression(provider, child, rootNode.getId(), nodes);
    }
  }

  /**
   * Creates an operand expression tree in the database.
   *
   * @param provider The provider used to access the database.
   * @param operand The operand to create.
   * @param position The position of the operand in the instruction.
   *
   * @throws SQLException Thrown if the creation of the operand failed.
   */
  private static void createOperandTree(
      final SQLProvider provider, final COperandTree operand, final int position)
          throws SQLException {
    final Set<INaviOperandTreeNode> nodes = new HashSet<INaviOperandTreeNode>();

    createOperandExpression(provider, operand.getRootNode(), 0, nodes);

    final int expressionTreeId =
        createExpressionTree(provider, nodes, operand.getInstruction().getModule());
    final BigInteger address = operand.getInstruction().getAddress().toBigInteger();
    final int moduleId = operand.getInstruction().getModule().getConfiguration().getId();

    createOperand(provider, moduleId, address, position, expressionTreeId);
  }

  /**
   * Turns an expression value into a string that can be stored in the database.
   *
   * @param type The type of the expression.
   * @param value The value of the expression.
   *
   * @return The expression string to store in the database.
   */
  private static String getValue(final ExpressionType type, final String value) {
    if (type == ExpressionType.SIZE_PREFIX) {
      if ("byte".equals(value)) {
        return "b1";
      } else if ("word".equals(value)) {
        return "b2";
      } else if ("dword".equals(value)) {
        return "b4";
      } else if ("fword".equals(value)) {
        return "b6";
      } else if ("qword".equals(value)) {
        return "b8";
      } else if ("oword".equals(value)) {
        return "b16";
      } else if ("b_var".equals(value)) {
        return "b_var";
      } else {
        throw new IllegalStateException("IE01104: Unknown size " + value);
      }
    } else {
      return value;
    }
  }

  /**
   * Adds a new outgoing reference to an operand expression.
   *
   *  The node for which the reference is added must be stored in the database
   * connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param node The operand expression the reference is added to.
   * @param targetAddress The target address of the reference.
   * @param type The type of the reference.
   *
   * @throws CouldntSaveDataException Thrown if the reference could not be
   *         created.
   */
  public static void addReference(final SQLProvider provider, final INaviOperandTreeNode node,
      final IAddress targetAddress, final ReferenceType type) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE00473: Provider argument can not be null");
    Preconditions.checkNotNull(node, "IE00474: Node argument can not be null");
    Preconditions.checkNotNull(targetAddress, "IE01548: Address argument can not be null");
    Preconditions.checkNotNull(type, "IE00475: Type argument can not be null");

    final CConnection connection = provider.getConnection();
    final int moduleId = node.getOperand().getInstruction().getModule().getConfiguration().getId();
    final BigInteger address = node.getInstructionAddress().toBigInteger();
    final int position = node.getOperandPosition();
    final int expressionId = node.getId();
    final String query = String.format("INSERT INTO " + CTableNames.ADDRESS_REFERENCES_TABLE
        + "(module_id, address, position, expression_id, type, target) "
        + "VALUES(%d, %d, %d, %d, '%s', %s)", moduleId, address, position, expressionId,
        type.toString().toLowerCase(), targetAddress.toBigInteger().toString());

    try {
      connection.executeUpdate(query, true);
    } catch (final SQLException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  /**
   * This function appends a global instruction comment to the list of global
   * instruction comments associated with this instruction in the database.
   *
   * @param provider The provider to access the database.
   * @param instruction The instruction to which the comment is associated.
   * @param commentText The comment text of the comment.
   * @param userId The user id of the currently active user.
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException if the comment could not be stored in the
   *         database.
   */
  public static int appendGlobalInstructionComment(final SQLProvider provider,
      final INaviInstruction instruction, final String commentText, final Integer userId)
          throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE01648: provider argument can not be null");
    Preconditions.checkNotNull(instruction, "IE02040: instruction argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02041: commentText argument can not be null");
    Preconditions.checkNotNull(userId, "IE02142: userId argument can not be null");

    final CConnection connection = provider.getConnection();

    final String function = "{ ? = call append_global_instruction_comment(?, ?, ?, ?) }";

    try {
      final CallableStatement appendCommentFunction =
          connection.getConnection().prepareCall(function);

      try {
        appendCommentFunction.registerOutParameter(1, Types.INTEGER);
        appendCommentFunction.setInt(2, instruction.getModule().getConfiguration().getId());
        appendCommentFunction.setObject(3, instruction.getAddress().toBigInteger(), Types.BIGINT);
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
   * This function appends a local instruction comment to the list of local
   * instruction comments associated with the given instruction residing in the
   * given code node to the database.
   *
   * @param provider The provider to access the database.
   * @param codeNode The code node in which the instruction resides.
   * @param instruction The instruction to which the comment is associated.
   * @param commentText The text of the comment to be appended.
   * @param userId The user id of the currently active user.
   * @return The id of the comment generated by the database.
   *
   * @throws CouldntSaveDataException if the comment could not be stored in the
   *         database.
   */
  public static int appendLocalInstructionComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final INaviInstruction instruction, final String commentText,
      final Integer userId) throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02423: provider argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02424: codeNode argument can not be null");
    Preconditions.checkNotNull(instruction, "IE02425: instruction argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02426: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02427: userId argument can not be null");

    final CConnection connection = provider.getConnection();

    final String function = "{ ? = call append_local_instruction_comment( ?, ?, ?, ?, ?) }";

    try {
      final CallableStatement appendCommentFunction =
          connection.getConnection().prepareCall(function);

      try {

        appendCommentFunction.registerOutParameter(1, Types.INTEGER);
        appendCommentFunction.setInt(2, instruction.getModule().getConfiguration().getId());
        appendCommentFunction.setInt(3, codeNode.getId());
        appendCommentFunction.setObject(4, instruction.getAddress().toBigInteger(), Types.BIGINT);
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
   * Saves an instruction to the database.
   *
   * @param provider The provider used to access the database.
   * @param instructions The instruction to save.
   *
   * @throws SQLException Thrown if the instruction could not be created.
   */
  public static void createInstructions(
      final SQLProvider provider, final Iterable<INaviInstruction> instructions)
          throws SQLException {

    Preconditions.checkNotNull(provider, "IE01550: Provider argument can not be null");
    Preconditions.checkNotNull(instructions, "IE01554: Instruction argument can not be null");

    final String query = "INSERT INTO " + CTableNames.INSTRUCTIONS_TABLE
        + "(module_id, address, mnemonic, data, native, architecture, comment_id) "
        + "VALUES(?, ?, ?, ?, ?, ?, ?)";

    final PreparedStatement insertStatement =
        provider.getConnection().getConnection().prepareStatement(query);

    final ArrayList<INaviInstruction> instructionsWithUnsavedComments =
        new ArrayList<INaviInstruction>();

    final List<List<COperandTree>> operands = new ArrayList<List<COperandTree>>();

    for (final INaviInstruction instruction : instructions) {

      final String mnemonic = instruction.getMnemonic();
      final byte[] data = instruction.getData();
      operands.add(instruction.getOperands());
      final INaviModule module = instruction.getModule();
      final IAddress address = instruction.getAddress();
      final int moduleID = module.getConfiguration().getId();
      final List<IComment> comments = instruction.getGlobalComment();
      final Integer commentId = comments == null ? null
          : comments.size() == 0 ? null : Iterables.getLast(comments).getId();

      if ((comments != null) && (comments.size() != 0) && (commentId == null)) {
        instructionsWithUnsavedComments.add(instruction);
      }

      try {
        insertStatement.setInt(1, moduleID);
        insertStatement.setObject(2, address.toBigInteger(), Types.BIGINT);
        insertStatement.setString(3, mnemonic);
        insertStatement.setBytes(4, data);
        insertStatement.setBoolean(5, false);
        insertStatement.setObject(6, instruction.getArchitecture(), Types.OTHER);
        if (commentId == null) {
          insertStatement.setNull(7, Types.INTEGER);
        } else {
          insertStatement.setInt(7, commentId);
        }

        insertStatement.execute();
      } finally {
        insertStatement.close();
      }
    }

    // TODO(timkornau): it should be possible to store all comments at once and
    // not need to have so
    // many round trips which will be really frustrating if there are a lot of
    // unsaved comments.
    for (final INaviInstruction instruction : instructionsWithUnsavedComments) {
      final ArrayList<IComment> instructionComments = new ArrayList<IComment>();
      for (final IComment comment : instruction.getGlobalComment()) {
        try {
          final Integer commentId = PostgreSQLInstructionFunctions.appendGlobalInstructionComment(
              provider, instruction, comment.getComment(), comment.getUser().getUserId());
          final IComment newComment =
              new CComment(commentId, comment.getUser(), comment.getParent(), comment.getComment());
          instructionComments.add(newComment);
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);
        }
      }
      instruction.initializeGlobalComment(instructionComments);
    }

    for (final List<COperandTree> operand : operands) {
      int position = 0;
      for (final COperandTree operandTree : operand) {
        createOperandTree(provider, operandTree, position);
        position++;
      }
    }
  }

  /**
   * This function deletes a global instruction comment associated with the
   * given instruction from the database.
   *
   * @param provider The provider used to access the database.
   * @param instruction The instruction to which the comment is associated.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the
   *         database.
   */
  public static void deleteGlobalInstructionComment(final SQLProvider provider,
      final INaviInstruction instruction, final Integer commentId, final Integer userId)
          throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE02428: provider argument can not be null");
    Preconditions.checkNotNull(instruction, "IE02429: instruction argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02430: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02431: userId argument can not be null");

    final String function = " { ? = call delete_global_instruction_comment(?, ?, ?, ?) } ";

    try {

      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);
        deleteCommentStatement.setInt(2, instruction.getModule().getConfiguration().getId());
        deleteCommentStatement.setObject(3, instruction.getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentStatement.setInt(4, commentId);
        deleteCommentStatement.setInt(5, userId);

        deleteCommentStatement.execute();

        deleteCommentStatement.getInt(1);
        if (deleteCommentStatement.wasNull()) {
          throw new IllegalArgumentException(
              "Error: The comment id returned from the database was null.");
        }

      } finally {
        deleteCommentStatement.close();
      }

    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * This function deletes a local instruction comment associated with the given
   * instruction in the given code node from the database.
   *
   * @param provider The provider to access the database.
   * @param codeNode The code node where the instruction is located.
   * @param instruction The instruction where the comment is deleted.
   * @param commentId The comment id of the comment to be deleted.
   * @param userId The user id of the currently active user.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the
   *         database.
   */
  public static void deleteLocalInstructionComment(final SQLProvider provider,
      final INaviCodeNode codeNode, final INaviInstruction instruction, final Integer commentId,
      final Integer userId) throws CouldntDeleteException {

    Preconditions.checkNotNull(codeNode, "IE02432: codeNode argument can not be null");
    Preconditions.checkNotNull(provider, "IE02433: provider argument can not be null");
    Preconditions.checkNotNull(instruction, "IE02434: instruction argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02435: comment argument can not be null");
    Preconditions.checkNotNull(userId, "IE02436: userId argument can not be null");

    final String function = " { ? = call delete_local_instruction_comment(?, ?, ?, ?, ?) } ";

    try {
      final CallableStatement deleteCommentStatement =
          provider.getConnection().getConnection().prepareCall(function);

      try {
        deleteCommentStatement.registerOutParameter(1, Types.INTEGER);
        deleteCommentStatement.setInt(2, instruction.getModule().getConfiguration().getId());
        deleteCommentStatement.setInt(3, codeNode.getId());
        deleteCommentStatement.setObject(4, instruction.getAddress().toBigInteger(), Types.BIGINT);
        deleteCommentStatement.setInt(5, commentId);
        deleteCommentStatement.setInt(6, userId);

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
   * Deletes a reference from an operand expression.
   *
   *  The node from which the reference is removed must be stored in the
   * database connected to by the provider argument.
   *
   * @param provider The connection to the database.
   * @param node The operand expression from which the reference is deleted.
   * @param address The target address of the reference to delete.
   * @param type The type of the reference to delete.
   *
   * @throws CouldntDeleteException Thrown if the reference could not be
   *         deleted.
   */
  public static void deleteReference(final SQLProvider provider, final INaviOperandTreeNode node,
      final IAddress address, final ReferenceType type) throws CouldntDeleteException {

    Preconditions.checkNotNull(provider, "IE00476: Provider argument can not be null");
    Preconditions.checkNotNull(node, "IE00477: Node argument can not be null");
    Preconditions.checkNotNull(address, "IE01619: Address argument can not be null");
    Preconditions.checkNotNull(type, "IE00478: Type argument can not be null");

    final CConnection connection = provider.getConnection();

    final BigInteger instructionAddress = node.getInstructionAddress().toBigInteger();
    final int position = node.getOperandPosition();
    final int expressionId = node.getId();
    final BigInteger targetAddress = address.toBigInteger();

    final String deleteQuery = "DELETE FROM " + CTableNames.ADDRESS_REFERENCES_TABLE
        + " WHERE address = ? AND position = ? AND expression_id = ? AND type = '"
        + type.toString().toLowerCase() + "' AND target = ?";

    try {
      final PreparedStatement deleteStatement =
          connection.getConnection().prepareStatement(deleteQuery);
      try {
        deleteStatement.setObject(1, instructionAddress, java.sql.Types.BIGINT);
        deleteStatement.setInt(2, position);
        deleteStatement.setInt(3, expressionId);
        deleteStatement.setObject(4, targetAddress, java.sql.Types.BIGINT);
        deleteStatement.execute();
      } catch (final SQLException exception) {
        throw new CouldntDeleteException(exception);
      } finally {
        deleteStatement.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntDeleteException(exception);
    }
  }

  /**
   * This function edits a global instruction comment in the database.
   *
   * @param provider The provider to access the database.
   * @param commentId The comment id of the comment in the database.
   * @param userId The id of the currently active user.
   * @param newComment The new text which is stored in the comment.
   *
   * @throws CouldntSaveDataException if the edit of the comment in the database
   *         failed.
   */
  public static void editGlobalInstructionComment(final SQLProvider provider,
      final Integer commentId, final Integer userId, final String newComment)
          throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02437: provider argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02438: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02439: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02440: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * This function edits a local instruction comment in the database.
   *
   * @param provider The provider to access the database.
   * @param commentId The id of the comment which is edited.
   * @param userId The user id of the currently active user.
   * @param newComment The new text which is stored in the comment.
   *
   * @throws CouldntSaveDataException if the edit of the comment in the database
   *         failed.
   */
  public static void editLocalInstructionComment(final SQLProvider provider,
      final Integer commentId, final Integer userId, final String newComment)
          throws CouldntSaveDataException {

    Preconditions.checkNotNull(provider, "IE02441: provider argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02442: commentId argument can not be null");
    Preconditions.checkNotNull(userId, "IE02443: userId argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02444: newComment argument can not be null");

    PostgreSQLCommentFunctions.editComment(provider, commentId, userId, newComment);
  }

  /**
   * Changes the replacement string of an operand tree node that represents a
   * global variable.
   *
   *  The operand tree for which the global replacement is set must be stored in
   * the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param operandTreeNode The node whose replacement string is changed.
   * @param replacement The new replacement string.
   *
   * @throws CouldntSaveDataException Thrown if the replacement string could not
   *         be updated.
   */
  public static void setGlobalReplacement(final SQLProvider provider,
      final INaviOperandTreeNode operandTreeNode, final String replacement)
          throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01627: Provider argument can not be null");
    Preconditions.checkNotNull(
        operandTreeNode, "IE01629: operandTreeNode argument can not be null");
    Preconditions.checkNotNull(replacement, "IE01635: Replacement argument can not be null");

    final String query = "UPDATE " + CTableNames.EXPRESSION_SUBSTITUTIONS_TABLE
        + " SET replacement = ? WHERE expression_id = ?";

    try {
      final PreparedStatement statement =
          provider.getConnection().getConnection().prepareStatement(query);

      try {
        statement.setString(1, replacement);
        statement.setInt(2, operandTreeNode.getId());

        statement.executeUpdate();
      } finally {
        statement.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }

  /**
   * Changes the replacement string of an operand tree node.
   *
   *  The operand tree node for which the replacement is set must be stored in
   * the database connected to by the provider argument.
   *
   * @param provider The SQL provider that provides the connection.
   * @param operandTreeNode The node whose replacement string is changed.
   * @param replacement The new replacement string.
   *
   * @throws CouldntSaveDataException Thrown if the replacement string could not
   *         be updated.
   */
  public static void setReplacement(final SQLProvider provider,
      final INaviOperandTreeNode operandTreeNode, final String replacement)
          throws CouldntSaveDataException {
    Preconditions.checkNotNull(provider, "IE01651: Provider argument can not be null");
    Preconditions.checkNotNull(
        operandTreeNode, "IE01699: Operand tree node argument cannot be null");
    Preconditions.checkNotNull(replacement, "IE01778: Replacement argument can not be null");


    try {
      final PreparedStatement statement = provider.getConnection().getConnection().prepareStatement(
          "UPDATE " + CTableNames.EXPRESSION_SUBSTITUTIONS_TABLE + " SET replacement = ? "
              + "WHERE module_id = ? AND address = ? AND position = ? AND expression_id = ?");

      try {
        statement.setString(1, replacement);
        statement.setInt(2,
            operandTreeNode.getOperand().getInstruction().getModule().getConfiguration().getId());
        statement.setObject(3,
            operandTreeNode.getOperand().getInstruction().getAddress().toBigInteger(),
            java.sql.Types.BIGINT);
        statement.setInt(4, operandTreeNode.getOperandPosition());
        statement.setInt(5, operandTreeNode.getId());

        statement.executeUpdate();
      } finally {
        statement.close();
      }
    } catch (final SQLException exception) {
      throw new CouldntSaveDataException(exception);
    }
  }
}
