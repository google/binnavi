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

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.AbstractSQLProvider;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLInstructionFunctions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Functions.PostgreSQLNodeFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CCodeNode;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.CTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.general.Pair;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class PostgreSQLNodeSaver {
  private static String CODE = "code";
  private static String FUNCTION = "function";
  private static String GROUP = "group";
  private static String TEXT = "text";

  /**
   * Class is only used to provide state less methods and thus should not be instantiated.
   */
  private PostgreSQLNodeSaver() {
  }

  /**
   * Saves the nodes to the nodes table. As a side effect, this function also fills index lists that
   * store the indices into the nodes list for all node types. TODO: This method should probably be
   * split into two methods.
   *
   * @param provider Provides the connection to the database.
   * @param newViewId ID of the new view that is being saved.
   * @param nodes The nodes to save.
   * @param functionNodeIndices Index into the nodes list that identifies the function nodes.
   * @param codeNodeIndices Index into the nodes list that identifies the code nodes.
   * @param textNodeIndices Index into the nodes list that identifies the text nodes.
   * @param groupNodeIndices Index into the nodes list that identifies the group nodes.
   * @param groupNodeMap Maps between node IDs and group node objects.
   * @return The ID of the first node saved to the database.
   * @throws SQLException Thrown if saving the nodes failed.
   */
  private static int saveNodes(final AbstractSQLProvider provider, final int newViewId,
      final List<INaviViewNode> nodes, final List<Integer> functionNodeIndices,
      final List<Integer> codeNodeIndices, final List<Integer> textNodeIndices,
      final List<Integer> groupNodeIndices, final BiMap<Integer, INaviGroupNode> groupNodeMap)
      throws SQLException {

    final String query =
        "INSERT INTO " + CTableNames.NODES_TABLE
            + "( view_id, parent_id, type, x, y, width, height, color, bordercolor, "
            + " selected, visible) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    final PreparedStatement preparedStatement =
        provider.getConnection().getConnection()
            .prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);

    int counter = 0;
    for (final INaviViewNode node : nodes) {
      String nodeType = null;

      if (node instanceof CCodeNode) {
        nodeType = CODE;
        codeNodeIndices.add(counter);
      } else if (node instanceof CFunctionNode) {
        nodeType = FUNCTION;
        functionNodeIndices.add(counter);
      } else if (node instanceof INaviGroupNode) {
        nodeType = GROUP;
        groupNodeIndices.add(counter);
        groupNodeMap.put(counter, (INaviGroupNode) node);
      } else if (node instanceof CTextNode) {
        nodeType = TEXT;
        textNodeIndices.add(counter);
      }

      counter++;

      preparedStatement.setInt(1, newViewId);
      preparedStatement.setNull(2, Types.INTEGER);
      preparedStatement.setObject(3, nodeType, Types.OTHER);
      preparedStatement.setDouble(4, node.getX());
      preparedStatement.setDouble(5, node.getY());
      preparedStatement.setDouble(6, node.getWidth());
      preparedStatement.setDouble(7, node.getHeight());
      preparedStatement.setInt(8, node.getColor().getRGB());
      preparedStatement.setInt(9, node.getBorderColor().getRGB());
      preparedStatement.setBoolean(10, node.isSelected());
      preparedStatement.setBoolean(11, node.isVisible());

      preparedStatement.addBatch();

    }

    preparedStatement.executeBatch();

    final ResultSet resultSet = preparedStatement.getGeneratedKeys();

    int lastId = 0;
    try {
      while (resultSet.next()) {
        if (resultSet.isFirst()) {
          lastId = resultSet.getInt(1);
          break;
        }
      }
    } finally {
      preparedStatement.close();
      resultSet.close();
    }

    return lastId;
  }

  protected static void checkArguments(final AbstractSQLProvider provider, final int newViewId,
      final List<INaviViewNode> nodes) {
    Preconditions.checkNotNull(provider, "IE01992: Provider argument can not be null");
    Preconditions.checkArgument(newViewId > 0,
        "IE01993: New View ID argument must be greater then zero");
    Preconditions.checkNotNull(nodes, "IE01994: Nodes argument can not be null");
  }

  /**
   * Saves the mapping between code nodes and their instructions to the database.
   *
   * @param provider The provider used to access the database.
   * @param nodes The nodes to save.
   * @param firstNode The database index of the first node.
   * @param codeNodeIndices Index into the nodes list that identifies the code nodes.
   *
   * @throws SQLException Thrown if saving the code node instructions failed.
   */
  protected static ArrayList<Pair<INaviCodeNode, INaviInstruction>> saveCodeNodeInstructions(
      final SQLProvider provider, final List<INaviViewNode> nodes, final int firstNode,
      final List<Integer> codeNodeIndices) throws SQLException {
    if (!nodes.isEmpty()) {
      final Set<INaviInstruction> unsavedInstructions = new HashSet<INaviInstruction>();

      for (final int index : codeNodeIndices) {
        final CCodeNode node = (CCodeNode) nodes.get(index);

        final Iterable<INaviInstruction> instructions = node.getInstructions();

        for (final INaviInstruction instruction : instructions) {
          if (!(instruction.isStored())) {
            unsavedInstructions.add(instruction);
          }
        }
      }

      PostgreSQLInstructionFunctions.createInstructions(provider, unsavedInstructions);

      final String query =
          "INSERT INTO " + CTableNames.CODENODE_INSTRUCTIONS_TABLE
              + " (module_id, node_id, position, address, comment_id) VALUES (?, ?, ?, ?, ?)";

      final PreparedStatement preparedStatement =
          provider.getConnection().getConnection().prepareStatement(query);

      final ArrayList<Pair<INaviCodeNode, INaviInstruction>> instructionsWithUnsavedLocalComments =
          new ArrayList<Pair<INaviCodeNode, INaviInstruction>>();

      try {
        for (final Integer index : codeNodeIndices) {
          final INaviCodeNode codeNode = (INaviCodeNode) nodes.get(index);
          int position = 0;
          for (final INaviInstruction instruction : codeNode.getInstructions()) {
            final List<IComment> comments =
                codeNode.getComments().getLocalInstructionComment(instruction);
            final Integer commentId =
                comments == null ? null : comments.size() == 0 ? null : Iterables.getLast(comments)
                    .getId();

            if ((comments != null) && (comments.size() != 0) && (commentId == null)) {
              instructionsWithUnsavedLocalComments.add(new Pair<INaviCodeNode, INaviInstruction>(
                  codeNode, instruction));
            }

            final int moduleId = instruction.getModule().getConfiguration().getId();

            preparedStatement.setInt(1, moduleId);
            preparedStatement.setInt(2, firstNode + index);
            preparedStatement.setInt(3, position);
            preparedStatement.setObject(4, instruction.getAddress().toBigInteger(), Types.BIGINT);
            if (commentId == null) {
              preparedStatement.setNull(5, Types.INTEGER);
            } else {
              preparedStatement.setInt(5, commentId);
            }

            position++;
            preparedStatement.addBatch();
          }
        }
        preparedStatement.executeBatch();
      } finally {
        preparedStatement.close();
      }
      return instructionsWithUnsavedLocalComments;
    }
    return null;
  }

  /**
   * Saves the code nodes to the database.
   *
   * @param provider The connection to the database.
   * @param nodes The nodes to save.
   * @param firstNode The database index of the first node.
   * @param codeNodeIndices Index into the nodes list that identifies the code nodes.
   *
   * @throws SQLException Thrown if saving the code node instructions failed.
   */
  protected static void saveCodeNodes(final SQLProvider provider, final List<INaviViewNode> nodes,
      final int firstNode, final List<Integer> codeNodeIndices) throws SQLException {

    if (!codeNodeIndices.isEmpty()) {
      final List<Pair<INaviCodeNode, INaviInstruction>> instructionsWithUnsavedLocalComments =
          PostgreSQLNodeSaver.saveCodeNodeInstructions(provider, nodes, firstNode, codeNodeIndices);

      final String query =
          "INSERT INTO " + CTableNames.CODE_NODES_TABLE
              + "(module_id, node_id, parent_function, comment_id) VALUES (?, ?, ?, ?)";

      final ArrayList<INaviCodeNode> codeNodesWithUnsavedComments = new ArrayList<INaviCodeNode>();

      final PreparedStatement preparedStatement =
          provider.getConnection().getConnection().prepareStatement(query);

      try {
        for (final int index : codeNodeIndices) {
          final INaviCodeNode codeNode = (INaviCodeNode) nodes.get(index);
          codeNode.setId(firstNode + index);
          INaviFunction function = null;
          try {
            function = codeNode.getParentFunction();
          } catch (final MaybeNullException e) {
          }
          final int moduleId =
              Iterables.getLast(codeNode.getInstructions()).getModule().getConfiguration().getId();
          final List<IComment> comment = codeNode.getComments().getLocalCodeNodeComment();
          final Integer commentId =
              comment == null ? null : comment.size() == 0 ? null : Iterables.getLast(comment)
                  .getId();

          if ((comment != null) && (comment.size() != 0) && (commentId == null)) {
            codeNodesWithUnsavedComments.add(codeNode);
          }
          preparedStatement.setInt(1, moduleId);
          preparedStatement.setInt(2, firstNode + index);
          if (function == null) {
            preparedStatement.setNull(3, Types.BIGINT);
          } else {
            preparedStatement.setObject(3, function.getAddress().toBigInteger(), Types.BIGINT);
          }
          if (commentId == null) {
            preparedStatement.setNull(4, Types.INTEGER);
          } else {
            preparedStatement.setInt(4, commentId);
          }
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
      } finally {
        preparedStatement.close();
      }

      // TODO (timkornau): this is not the best solution and is more a test then a full fledged
      // implementation.
      for (final INaviCodeNode codeNode : codeNodesWithUnsavedComments) {
        final ArrayList<IComment> codeNodecomments = new ArrayList<IComment>();
        for (final IComment comment : codeNode.getComments().getLocalCodeNodeComment()) {
          try {
            final Integer commentId =
                PostgreSQLNodeFunctions.appendLocalCodeNodeComment(provider, codeNode,
                    comment.getComment(), comment.getUser().getUserId());
            final IComment newComment =
                new CComment(commentId, comment.getUser(), comment.getParent(),
                    comment.getComment());
            codeNodecomments.add(newComment);
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
        codeNode.getComments().initializeLocalCodeNodeComment(codeNodecomments);
      }

      // TODO (timkornau): this is not the best solution and is more a test then a full fledged
      // implementation.
      for (final Pair<INaviCodeNode, INaviInstruction> pair : instructionsWithUnsavedLocalComments) {
        final ArrayList<IComment> localInstructionComments = new ArrayList<IComment>();
        for (final IComment comment : pair.first().getComments()
            .getLocalInstructionComment(pair.second())) {
          try {
            final int commentId =
                PostgreSQLInstructionFunctions.appendLocalInstructionComment(provider,
                    pair.first(), pair.second(), comment.getComment(), comment.getUser()
                        .getUserId());
            final IComment newComment =
                new CComment(commentId, comment.getUser(), comment.getParent(),
                    comment.getComment());
            localInstructionComments.add(newComment);
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
        pair.first().getComments()
            .initializeLocalInstructionComment(pair.second(), localInstructionComments);
      }
    }
  }

  /**
   * Saves the function nodes to the database.
   *
   * @param provider The connection to the database.
   * @param nodes The nodes to save.
   * @param firstNode The database index of the first node.
   * @param functionNodeIndices Index into the nodes list that identifies the function nodes.
   *
   * @throws SQLException Thrown if saving the function nodes failed.
   */
  protected static void saveFunctionNodes(final SQLProvider provider,
      final List<INaviViewNode> nodes, final int firstNode, final List<Integer> functionNodeIndices)
      throws SQLException {

    if (functionNodeIndices.isEmpty()) {
      return;
    }

    final String query =
        "INSERT INTO " + CTableNames.FUNCTION_NODES_TABLE
            + "(module_id, node_id, function, comment_id) VALUES (?, ?, ?, ?)";

    final ArrayList<INaviFunctionNode> functionNodesWithUnsavedComments =
        new ArrayList<INaviFunctionNode>();

    final PreparedStatement preparedStatement =
        provider.getConnection().getConnection().prepareStatement(query);

    try {
      for (final int index : functionNodeIndices) {
        final CFunctionNode node = (CFunctionNode) nodes.get(index);
        final INaviFunction function = node.getFunction();
        final List<IComment> comments = node.getLocalFunctionComment();
        final Integer commentId =
            comments == null ? null : comments.size() == 0 ? null : Iterables.getLast(comments)
                .getId();

        if ((comments != null) && (comments.size() != 0) && (commentId == null)) {
          functionNodesWithUnsavedComments.add(node);
        }

        preparedStatement.setInt(1, function.getModule().getConfiguration().getId());
        preparedStatement.setInt(2, firstNode + index);
        preparedStatement.setObject(3, function.getAddress().toBigInteger(), Types.BIGINT);
        if (commentId == null) {
          preparedStatement.setNull(4, Types.INTEGER);
        } else {
          preparedStatement.setInt(4, commentId);
        }
        preparedStatement.addBatch();
      }
      preparedStatement.executeBatch();
    } finally {
      preparedStatement.close();
    }

    for (final INaviFunctionNode functionNode : functionNodesWithUnsavedComments) {
      final ArrayList<IComment> functionNodeComments = new ArrayList<IComment>();
      for (final IComment comment : functionNode.getLocalFunctionComment()) {
        try {
          final Integer commentId =
              provider.appendFunctionNodeComment(functionNode, comment.getComment(), comment
                  .getUser().getUserId());
          final IComment newComment =
              new CComment(commentId, comment.getUser(), comment.getParent(), comment.getComment());
          functionNodeComments.add(newComment);
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);
        }
      }
      functionNode.initializeLocalFunctionComment(functionNodeComments);
    }
  }

  /**
   * Saves the group nodes to the database.
   *
   * @param provider The connection to the database.
   * @param nodes The nodes to save.
   * @param firstNode The database index of the first node.
   * @param groupNodeIndices Index into the nodes list that identifies the group nodes.
   *
   * @throws SQLException Thrown if saving the group nodes failed.
   */
  protected static void saveGroupNodes(final SQLProvider provider, final List<INaviViewNode> nodes,
      final int firstNode, final List<Integer> groupNodeIndices) throws SQLException {

    Preconditions.checkNotNull(provider, "IE02525: connection argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02526: nodes argument can not be null");
    Preconditions
        .checkNotNull(groupNodeIndices, "Error: groupNodeIndices argument can not be null");

    if (!groupNodeIndices.isEmpty()) {

      final String query =
          "INSERT INTO " + CTableNames.GROUP_NODES_TABLE
              + "(node_id, collapsed, comment_id) VALUES (?, ?, ?)";

      final PreparedStatement preparedStatement =
          provider.getConnection().getConnection().prepareStatement(query);

      final List<INaviGroupNode> groupNodesWithUnsavedComments = new ArrayList<INaviGroupNode>();

      try {
        for (final Integer index : groupNodeIndices) {
          final INaviGroupNode node = (INaviGroupNode) nodes.get(index);
          preparedStatement.setInt(1, firstNode + index);
          preparedStatement.setBoolean(2, node.isCollapsed());


          final List<IComment> comment = node.getComments();
          final Integer commentId =
              comment == null ? null : comment.size() == 0 ? null : Iterables.getLast(comment)
                  .getId();

          if ((comment != null) && (comment.size() != 0) && (commentId == null)) {
            groupNodesWithUnsavedComments.add(node);
          }

          if (commentId == null) {
            preparedStatement.setNull(3, Types.INTEGER);
          } else {
            preparedStatement.setInt(3, commentId);
          }
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
      } finally {
        preparedStatement.close();
      }

      // TODO (timkornau): this can work better.
      for (final INaviGroupNode groupNode : groupNodesWithUnsavedComments) {
        final ArrayList<IComment> groupNodeComments = new ArrayList<IComment>();
        for (final IComment comment : groupNode.getComments()) {
          try {
            final Integer commentId =
                provider.appendGroupNodeComment(groupNode, comment.getComment(), comment.getUser()
                    .getUserId());
            final IComment newComment =
                new CComment(commentId, comment.getUser(), comment.getParent(),
                    comment.getComment());
            groupNodeComments.add(newComment);
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
        groupNode.initializeComment(groupNodeComments);
      }
    }
  }

  /**
   * Stores parent groups for all nodes that have an assigned parent group.
   *
   * @param connection Provides the connection to the database.
   * @param nodes All nodes that were saved.
   * @param firstNode The database index of the first node.
   * @param groupNodeMap Maps between node IDs and parent group node objects.
   *
   * @throws SQLException Thrown if the parent groups could not be assigned.
   */
  protected static void saveParentGroups(final CConnection connection,
      final List<INaviViewNode> nodes, final int firstNode,
      final BiMap<Integer, INaviGroupNode> groupNodeMap) throws SQLException {
    int counter = 0;

    for (final INaviViewNode node : nodes) {
      if (node.getParentGroup() != null) {
        final int parentId = firstNode + groupNodeMap.inverse().get(node.getParentGroup());
        final int childId = firstNode + counter;

        connection.executeUpdate(String.format("UPDATE " + CTableNames.NODES_TABLE
            + " set parent_id = %d WHERE id = %d", parentId, childId), true);
      }

      counter++;
    }
  }

  /**
   *
   * TODO (timkornau): this code here has serious issues and is in no way anything that we want to
   * keep.
   *
   * Saves the node tags to the database.
   *
   * @param connection The connection to the database.
   * @param nodes The nodes to save.
   * @param firstNode Database index of the first node.
   *
   * @throws SQLException Thrown if saving the tags failed.
   */
  protected static void saveTags(final CConnection connection, final List<INaviViewNode> nodes,
      final int firstNode) throws SQLException {
    int counter = firstNode;

    final String deleteStatement =
        "DELETE FROM " + CTableNames.TAGGED_NODES_TABLE + " WHERE node_id IN (%s)";
    final String insertStatement = "INSERT INTO " + CTableNames.TAGGED_NODES_TABLE + " VALUES %s ";

    boolean isFirst = true;
    final StringBuilder range = new StringBuilder();

    for (int i = 0; i < nodes.size(); i++) {
      if (isFirst) {
        range.append(counter);
        isFirst = false;
        continue;
      }

      range.append(", ");
      range.append(counter);

      ++counter;
    }
    if (range.length() != 0) {
      connection.executeUpdate(String.format(deleteStatement, range.toString()), true);
    }

    counter = firstNode;

    final StringBuilder insert = new StringBuilder();
    isFirst = true;
    for (final INaviViewNode node : nodes) {
      final Iterator<CTag> it = node.getTagsIterator();
      while (it.hasNext()) {
        final CTag tag = it.next();
        insert.append(isFirst ? "" : ",");
        insert.append('(');
        insert.append(counter);
        insert.append(", ");
        insert.append(tag.getId());
        insert.append(')');

        isFirst = false;
      }

      ++counter;
    }
    if (insert.length() != 0) {
      connection.executeUpdate(String.format(insertStatement, insert.toString()), true);
    }
  }

  /**
   * Saves the text nodes to the database.
   *
   * @param provider The connection to the database.
   * @param nodes The nodes to save.
   * @param firstNode The database index of the first node.
   * @param textNodeIndices Index into the nodes list that identifies the text nodes.
   *
   * @throws SQLException Thrown if saving the text nodes failed.
   */
  protected static void saveTextNodes(final SQLProvider provider, final List<INaviViewNode> nodes,
      final int firstNode, final List<Integer> textNodeIndices) throws SQLException {

    Preconditions.checkNotNull(provider, "IE02527: provider argument can not be null");
    Preconditions.checkNotNull(nodes, "IE02528: nodes argument can not be null");
    Preconditions
        .checkNotNull(textNodeIndices, "IE02529: textNodeIndices argument can not be null");

    if (!textNodeIndices.isEmpty()) {

      final String query =
          "INSERT INTO " + CTableNames.TEXT_NODES_TABLE + "(node_id, comment_id) VALUES (?, ?)";

      final PreparedStatement preparedStatement =
          provider.getConnection().getConnection().prepareStatement(query);

      final List<INaviTextNode> textNodesWithUnsavedComments = new ArrayList<INaviTextNode>();

      try {
        for (final Integer index : textNodeIndices) {
          final INaviTextNode node = (INaviTextNode) nodes.get(index);
          final List<IComment> comment = node.getComments();
          final Integer commentId =
              comment == null ? null : comment.size() == 0 ? null : Iterables.getLast(comment)
                  .getId();

          if ((comment != null) && (comment.size() != 0) && (commentId == null)) {
            textNodesWithUnsavedComments.add(node);
          }

          preparedStatement.setInt(1, firstNode + index);
          if (commentId == null) {
            preparedStatement.setNull(2, Types.INTEGER);
          } else {
            preparedStatement.setInt(2, commentId);
          }
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
      } finally {
        preparedStatement.close();
      }

      // TODO (timkornau): this needs to be reworked once I have thought of a better idea for the
      // unsaved comments to be stored. Possibly one can handle all of those in one query.
      for (final INaviTextNode textNode : textNodesWithUnsavedComments) {
        final ArrayList<IComment> textNodeComments = new ArrayList<IComment>();
        for (final IComment comment : textNode.getComments()) {
          try {
            final Integer commentId =
                provider.appendTextNodeComment(textNode, comment.getComment(), comment.getUser()
                    .getUserId());
            final IComment newComment =
                new CComment(commentId, comment.getUser(), comment.getParent(),
                    comment.getComment());
            textNodeComments.add(newComment);
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);
          }
        }
        textNode.initializeComment(textNodeComments);
      }
    }
  }

  /**
   * Sorts the group nodes of a view in a way that makes sure that group nodes inside other group
   * nodes come later in the list.
   *
   * @param groupNodeIndices Database indices of the group nodes to sort.
   * @param groupNodeMap Maps between group node database indices and objects.
   *
   * @return The sorted list of group node indices.
   */
  protected static List<Integer> sortGroupNodes(final List<Integer> groupNodeIndices,
      final BiMap<Integer, INaviGroupNode> groupNodeMap) {
    final List<Integer> sortedList = new ArrayList<Integer>();
    final List<Integer> clonedList = new ArrayList<Integer>(groupNodeIndices);
    final Set<INaviGroupNode> addedNodes = new HashSet<INaviGroupNode>();

    while (!clonedList.isEmpty()) {
      for (final Integer id : clonedList) {
        final INaviGroupNode node = groupNodeMap.get(id);

        if ((node.getParentGroup() == null) || addedNodes.contains(node.getParentGroup())) {
          addedNodes.add(node);
          sortedList.add(id);
          clonedList.remove(id);
          break;
        }
      }
    }

    return sortedList;
  }

  /**
   * Updates the node IDs of the nodes that were saved to the database.
   *
   * @param nodes The nodes whose IDs are updated.
   * @param firstNode The new ID of the first node.
   */
  protected static void updateNodeIds(final List<INaviViewNode> nodes, final int firstNode) {
    int newIdCounter = firstNode;

    for (final INaviViewNode node : nodes) {
      node.setId(newIdCounter);

      newIdCounter++;
    }
  }

  /**
   * Writes the nodes of a view to the database.
   *
   * @param provider The connection to the database.
   * @param newViewId The ID of the view the nodes belong to.
   * @param nodes The nodes to save.
   * @throws SQLException Thrown if saving the nodes failed.
   */
  public static void writeNodes(final AbstractSQLProvider provider, final int newViewId,
      final List<INaviViewNode> nodes) throws SQLException {
    Preconditions.checkNotNull(provider, "IE01992: Provider argument can not be null");
    Preconditions.checkArgument(newViewId > 0,
        "IE01993: New View ID argument must be greater then zero");
    Preconditions.checkNotNull(nodes, "IE01994: Nodes argument can not be null");

    if (nodes.isEmpty()) {
      return;
    }

    final ArrayList<Integer> functionNodeIndices = new ArrayList<Integer>();
    final ArrayList<Integer> codeNodeIndices = new ArrayList<Integer>();
    final ArrayList<Integer> textNodeIndices = new ArrayList<Integer>();
    final ArrayList<Integer> groupNodeIndices = new ArrayList<Integer>();

    final BiMap<Integer, INaviGroupNode> groupNodeMap = HashBiMap.create();

    final int firstNode =
        saveNodes(provider, newViewId, nodes, functionNodeIndices, codeNodeIndices,
            textNodeIndices, groupNodeIndices, groupNodeMap);

    // After this point, the nodes table has been filled
    // After each saving, the node IDs have to be updated
    PostgreSQLNodeSaver.updateNodeIds(nodes, firstNode);


    // Now, the individual node type tables can be saved
    PostgreSQLNodeSaver.saveGroupNodes(provider, nodes, firstNode,
        PostgreSQLNodeSaver.sortGroupNodes(groupNodeIndices, groupNodeMap));
    PostgreSQLNodeSaver.saveFunctionNodes(provider, nodes, firstNode, functionNodeIndices);
    PostgreSQLNodeSaver.saveCodeNodes(provider, nodes, firstNode, codeNodeIndices);
    PostgreSQLNodeSaver.saveTextNodes(provider, nodes, firstNode, textNodeIndices);


    // Once all nodes are saved, the parent nodes can be saved too
    final CConnection connection = provider.getConnection();
    PostgreSQLNodeSaver.saveParentGroups(connection, nodes, firstNode, groupNodeMap);

    // And finally, we can save the tags associated with the nodes
    PostgreSQLNodeSaver.saveTags(connection, nodes, firstNode);
  }
}
