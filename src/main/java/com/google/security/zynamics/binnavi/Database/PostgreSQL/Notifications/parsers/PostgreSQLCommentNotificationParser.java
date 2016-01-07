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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.CodeNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.CommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.EdgeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.GroupNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.InstructionCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TextNodeCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypeInstanceCommentNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.CommentNotification;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationParser;
import com.google.security.zynamics.binnavi.Database.cache.EdgeCache;
import com.google.security.zynamics.binnavi.Database.cache.InstructionCache;
import com.google.security.zynamics.binnavi.Database.cache.NodeCache;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.postgresql.PGNotification;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses the incoming {@link PGNotification notifications} with regular expressions and
 * then informs the appropriate classes.
 */
public class PostgreSQLCommentNotificationParser implements
    PostgreSQLNotificationParser<CommentNotification> {

  /**
   * This regular expression matched the following strings:
   *
   * <pre>
   *bn_comments UPDATE 6572 6571 1 edit comment which is last comment
   *bn_comments UPDATE 6572 null 1 edit comment which is last comment
   *bn_comments DELETE 6572 null 1 edit comment which is last comment
   *</pre>
   */
  private static final String COMMENTS_NOTIFICATION = "^(" + CTableNames.COMMENTS_TABLE + ")"
      + "\\s(UPDATE|DELETE)" + "\\s(\\d*)" + "\\s((null)|(\\d*))" + "\\s(\\d*)" + "($|\\s(.*)$)";
  private static final Pattern COMMENTS_PATTERN;

  /**
   * This regular expression matches the following strings.
   *
   * <pre>
   *bn_type_instances UPDATE 1 94 363365
   *bn_type_instances UPDATE 1 94 null
   *</pre>
   */
  private static final String TYPE_INSTANCE_NOTIFICATION = "^(" + CTableNames.TYPE_INSTANCE_TABLE
      + ")" + "\\s(UPDATE)" + "\\s(\\d*)" + "\\s(\\d*)" + "\\s((null)$|(\\d*)$)";
  private static final Pattern TYPE_INSTANCE_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_edges UPDATE 13 6583 // append any comment
   *bn_edges UPDATE 13 null // delete any last comment
   *</pre>
   */
  private static final String EDGE_LOCAL_NOTIFICATION =
      "^(" + CTableNames.EDGES_TABLE + ")\\s(UPDATE)" + "\\s(\\d*)" + "\\s((\\d*)|(null))$";
  private static final Pattern EDGE_LOCAL_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_global_edge_comments INSERT 1 1 16783988 16784085 1852 // append comment 1
   *bn_global_edge_comments UPDATE 1 1 16783988 16784085 1853 // append comment 2
   *bn_global_edge_comments DELETE 1 1 16783988 16784085 // delete comment 1
   *</pre>
   */
  private static final String EDGE_GLOBAL_NOTIFICATION = "^("
      + CTableNames.GLOBAL_EDGE_COMMENTS_TABLE
      + ")\\s(INSERT|DELETE|UPDATE)\\s(\\d*)\\s(\\d*)\\s(\\d*)\\s(\\d*)($|\\s(\\d*)$)";
  private static final Pattern EDGE_GLOBAL_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_code_nodes UPDATE 2 10 2088769061 6577 // append any comment
   *bn_code_nodes UPDATE 2 10 2088769061 null // delete any last comment
   *</pre>
   */
  private static final String NODE_LOCAL_NOTIFICATION = "^(" + CTableNames.CODE_NODES_TABLE
      + ")\\s(UPDATE)\\s(\\d*)\\s(\\d*)\\s(\\d*)\\s((null)$|(\\d*)$)";
  private static final Pattern NODE_LOCAL_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_global_node_comments INSERT 1 16783793 1854 // append comment 1
   *bn_global_node_comments UPDATE 1 16783793 1855 // append comment 2
   *bn_global_node_comments DELETE 1 16783793 // delete comment 1
   *</pre>
   */
  private static final String NODE_GLOBAL_NOTIFICATION = "^("
      + CTableNames.GLOBAL_NODE_COMMENTS_TABLE
      + ")\\s(INSERT|DELETE|UPDATE)\\s(\\d*)\\s(\\d*)($|\\s(\\d*)$)";
  private static final Pattern NODE_GLOBAL_PATTERN;


  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_codenode_instructions UPDATE 2 10 0 2088769191 6571 // append any comment
   *bn_codenode_instructions UPDATE 2 10 0 2088769191 null // delete any last comment
   *</pre>
   */
  private static final String INSTRUCTION_LOCAL_NOTIFICATION = "^("
      + CTableNames.CODENODE_INSTRUCTIONS_TABLE
      + ")\\s(UPDATE)\\s(\\d*)\\s(\\d*)\\s(\\d*)\\s(\\d*)\\s((null)$|(\\d*)$)";
  private static final Pattern INSTRUCTION_LOCAL_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_instructions UPDATE 2 2088769191 6573 // append any comment
   *bn_instructions UPDATE 2 2088769191 null // delete any last comment
   *</pre>
   */
  private static final String INSTRUCTION_GLOBAL_NOTIFICATION =
      "^(" + CTableNames.INSTRUCTIONS_TABLE + ")\\s(UPDATE)\\s(\\d*)\\s(\\d*)\\s((null)$|(\\d*)$)";
  private static final Pattern INSTRUCTION_GLOBAL_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_function_nodes UPDATE 1 1405 16791949 1860 // append any comment
   *bn_function_nodes UPDATE 1 1405 16791949 null // delete any last comment
   *</pre>
   */
  private static final String FUNCTION_NODE_NOTIFICATION = "^(" + CTableNames.FUNCTION_NODES_TABLE
      + ")\\s(UPDATE)\\s(\\d*)\\s(\\d*)\\s(\\d*)\\s((null)$|(\\d*)$)";
  private static final Pattern FUNCTION_NODE_PATTERN;

  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_functions UPDATE 2 2088769061 6579 // append any comment
   *bn_functions UPDATE 2 2088769061 null // delete any last comment
   *</pre>
   */
  private static final String FUNCTION_NOTIFICATION =
      "^(" + CTableNames.FUNCTIONS_TABLE + ")\\s(UPDATE)\\s(\\d*)\\s(\\d*)\\s((null)$|(\\d*)$)";
  private static final Pattern FUNCTION_PATTERN;


  /**
   * This regular expression matches the following strings:
   *
   * <pre>
   *bn_group_nodes UPDATE 2450 1051 // append any comment
   *bn_group_nodes DELETE 2450 null // delete any last comment.
   *</pre>
   */
  private static final String GROUP_NODE_NOTIFICATION =
      "^(" + CTableNames.GROUP_NODES_TABLE + ")\\s(UPDATE)" + "\\s(\\d*)" + "\\s((\\d*)|(null))$";
  private static final Pattern GROUP_NODE_PATTERN;

  /**
   * This regular expression matched the following string:
   *
   * <pre>
   *bn_text_nodes UPDATE 2450 1051 // append any comment
   *bn_text_nodes DELETE 2450 null // delete any last comment.
   *</pre>
   */
  private static final String TEXT_NODE_NOTIFICATION =
      "^(" + CTableNames.TEXT_NODES_TABLE + ")\\s(UPDATE)" + "\\s(\\d*)" + "\\s((\\d*)|(null))$";
  private static final Pattern TEXT_NODE_PATTERN;

  /**
   * Flags for the regular expression matching.
   */
  private static final int FLAGS = Pattern.MULTILINE | Pattern.DOTALL;
  /**
   * Static initializer to only compile the used patterns in the class once.
   */
  static {
    COMMENTS_PATTERN = Pattern.compile(COMMENTS_NOTIFICATION, FLAGS);
    EDGE_GLOBAL_PATTERN = Pattern.compile(EDGE_GLOBAL_NOTIFICATION, FLAGS);
    TYPE_INSTANCE_PATTERN = Pattern.compile(TYPE_INSTANCE_NOTIFICATION, FLAGS);
    EDGE_LOCAL_PATTERN = Pattern.compile(EDGE_LOCAL_NOTIFICATION, FLAGS);
    TEXT_NODE_PATTERN = Pattern.compile(TEXT_NODE_NOTIFICATION, FLAGS);
    GROUP_NODE_PATTERN = Pattern.compile(GROUP_NODE_NOTIFICATION, FLAGS);
    FUNCTION_PATTERN = Pattern.compile(FUNCTION_NOTIFICATION, FLAGS);
    FUNCTION_NODE_PATTERN = Pattern.compile(FUNCTION_NODE_NOTIFICATION, FLAGS);
    INSTRUCTION_GLOBAL_PATTERN = Pattern.compile(INSTRUCTION_GLOBAL_NOTIFICATION, FLAGS);
    INSTRUCTION_LOCAL_PATTERN = Pattern.compile(INSTRUCTION_LOCAL_NOTIFICATION, FLAGS);
    NODE_LOCAL_PATTERN = Pattern.compile(NODE_LOCAL_NOTIFICATION, FLAGS);
    NODE_GLOBAL_PATTERN = Pattern.compile(NODE_GLOBAL_NOTIFICATION, FLAGS);
  }

  /**
   * Parses a {@link PGNotification} notification from the database back end for comment table
   * changes. These changes can not directly be mapped to any of the commentable objects as the
   * relation to which commentable object they belong is not in the notification message. This
   * notification is generated for the following situations for any commentable object:
   *
   * <pre>
   *
   *Delete a comment:
   *
   *[1] Comment 1    [1] Comment 1
   *[2] Comment 2 ->
   *[3] Comment 3    [3] Comment 3
   *
   *Edit a comment:
   *
   *[1] Comment 1    [1] Comment 1
   *[2] Comment 2 -> [2] Edited Comment 2
   *[3] Comment 3    [3] Comment 3
   *
   *</pre>
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processCommentNotification(final PGNotification notification,
      final SQLProvider provider) {
    final Matcher matcher = COMMENTS_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    Integer commentId = null;
    try {
      commentId = Integer.parseInt(matcher.group(3));
    } catch (final NumberFormatException exception) {
      throw new IllegalStateException(exception);
    }

    final IComment comment = CommentManager.get(provider).getCommentById(commentId);
    if (comment == null) {
      return null;
    }

    final String databaseOperation = matcher.group(2);
    Integer parentId = null;
    try {
      parentId =
          matcher.group(4).equalsIgnoreCase("null") ? null : Integer.parseInt(matcher.group(4));
    } catch (final NumberFormatException exception) {
      throw new IllegalStateException(exception);
    }

    // in the case of a delete the notified comments parent must be identical to the stored comments
    // parent.
    if (databaseOperation.equals("DELETE")) {
      if (((parentId == null) && (comment.getParent() != null)) || ((parentId != null)
          && (comment.getParent() != null) && (!parentId.equals(comment.getParent().getId())))) {

        final Integer localCommentParentId = parentId;
        final Integer notificationCommentParentId =
            comment.getParent() != null ? comment.getParent().getId() : null;

        throw new IllegalStateException("IE02521: The parent comment of the localy stored comment: "
            + localCommentParentId + " is not equal to the "
            + "notification comments parent comment: " + notificationCommentParentId);
      }
    }

    final String commentContent = matcher.group(9);

    if (!commentContent.equals(comment.getComment()) && databaseOperation.equals("DELETE")) {
      throw new IllegalStateException("IE02522: The local comments comment: " + comment.getComment()
          + "is not equal to the notification comments content: " + commentContent);
    }

    Integer commentUserId = null;
    try {
      commentUserId = Integer.parseInt(matcher.group(7));
    } catch (final NumberFormatException exception) {
      throw new IllegalStateException(exception);
    }

    if (!commentUserId.equals(comment.getUser().getUserId())) {
      throw new IllegalStateException("IE02523: The user of the localy stored comment: "
          + commentUserId + " is not equal to the " + "notifications comments user: "
          + comment.getUser().getUserId());
    }

    final IComment parentComment = CommentManager.get(provider).getCommentById(parentId);

    final IComment newComment =
        new CComment(comment.getId(), comment.getUser(), parentComment, commentContent);

    final CommentOperation operation =
        databaseOperation.equalsIgnoreCase("UPDATE") ? CommentOperation.EDIT
            : CommentOperation.DELETE;

    return new CommentNotificationContainer(comment, newComment, operation);
  }

  /**
   * Parses the notifications from the database back end for global edge comments by using a regular
   * expression. If the regular expression matches the supplied notification it tries to figure out
   * if the edge which was commented is loaded in BinNavi at this point in time. If the edge is
   * loaded determine the operation which was performed by the database and then return a
   * {@link CommentNotificationContainer} with the gathered results.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static Collection<CommentNotification> processEdgeGlobalCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = EDGE_GLOBAL_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return new ArrayList<>();
    }

    final String databaseOperation = matcher.group(2);
    final Integer notificationSourceModuleId = Integer.parseInt(matcher.group(3));
    final Integer notificationDestinationModuleId = Integer.parseInt(matcher.group(4));
    final IAddress notificationEdgeSourceAddress = new CAddress(new BigInteger(matcher.group(5)));
    final IAddress notificationEdgeDestinationAddress =
        new CAddress(new BigInteger(matcher.group(6)));
    final Integer commentId = matcher.group(8) == null ? null : Integer.parseInt(matcher.group(8));

    final INaviModule notificationSourceModule = provider.findModule(notificationSourceModuleId);
    if ((notificationSourceModule == null) || !notificationSourceModule.isLoaded()) {
      return new ArrayList<>();
    }

    final INaviModule notificationDestinationModule =
        provider.findModule(notificationDestinationModuleId);
    if ((notificationDestinationModule == null) || !notificationDestinationModule.isLoaded()) {
      return new ArrayList<>();
    }

    final CommentOperation operation =
        databaseOperation.equalsIgnoreCase("DELETE") ? CommentOperation.DELETE
            : CommentOperation.APPEND;

    Collection<CommentNotification> notifications = new ArrayList<>();
    final ImmutableCollection<INaviEdge> edges = EdgeCache.get(provider).getEdgeBySourceAndTarget(
        notificationEdgeSourceAddress, notificationSourceModuleId,
        notificationEdgeDestinationAddress, notificationDestinationModuleId);
    for (INaviEdge edge : edges) {
      notifications.add(
          new EdgeCommentNotificationContainer(edge, operation, CommentScope.GLOBAL, commentId));
    }
    return notifications;
  }

  /**
   * Parses the notifications from the database back end for local edge comments by using a regular
   * expression. If the regular expression matches the supplied {@link PGNotification} notification,
   * it is determined if the edge in question is loaded, and if a
   * {@link CommentNotificationContainer} is build with the data from the notification.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processEdgeLocalCommentNotification(final PGNotification notification,
      final SQLProvider provider) {
    final Matcher matcher = EDGE_LOCAL_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer edgeId = Integer.parseInt(matcher.group(3));
    final Integer commentId =
        matcher.group(4).equals("null") ? null : Integer.parseInt(matcher.group(4));

    final INaviEdge edge = EdgeCache.get(provider).getEdgeById(edgeId);
    if (edge == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new EdgeCommentNotificationContainer(edge, operation, CommentScope.LOCAL, commentId);
  }

  /**
   * Parses the notifications from the database back end for function comments by using a regular
   * expression. If the regular expression matches the supplied {@link PGNotification} notification,
   * a {@link CommentNotificationContainer} is build with the data from the notification.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processFunctionCommentNotification(final PGNotification notification,
      final SQLProvider provider) {
    final Matcher matcher = FUNCTION_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer notificationModuleId = Integer.parseInt(matcher.group(3));
    final IAddress notificationFunctionAddress = new CAddress(new BigInteger(matcher.group(4)));
    final Integer commentId =
        matcher.group(5).equals("null") ? null : Integer.parseInt(matcher.group(5));

    final INaviModule module = provider.findModule(notificationModuleId);
    if ((module == null) || !module.isLoaded()) {
      return null;
    }
    final INaviFunction function =
        module.getContent().getFunctionContainer().getFunction(notificationFunctionAddress);
    if (function == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new FunctionCommentNotificationContainer(function, operation, commentId);
  }

  /**
   * Parses the notifications from the database back end for function node comments by using a
   * regular expression. If the regular expression matches the supplied {@link PGNotification}
   * notification, it is determined if the function node in question is currently loaded, and if a
   * {@link CommentNotificationContainer} with the gathered data is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processFunctionNodeCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = FUNCTION_NODE_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer nodeId = Integer.parseInt(matcher.group(4));
    final Integer commentId =
        matcher.group(6).equals("null") ? null : Integer.parseInt(matcher.group(6));

    final INaviModule module = provider.findModule(moduleId);
    if ((module == null) || !module.isLoaded()) {
      return null;
    }
    final INaviFunctionNode functionNode =
        (INaviFunctionNode) NodeCache.get(provider).getNodeById(nodeId);
    if (functionNode == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new FunctionNodeCommentNotificationContainer(functionNode, operation, commentId);
  }

  /**
   * Parses the notifications from the database back end for group node comments by using a regular
   * expression. If the regular expression matches the supplied {@link PGNotification} notification,
   * it is determined if the group node in the notification is currently loaded, and if a
   * {@link CommentNotificationContainer} with the gathered data from the notification is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processGroupNodeCommentNotification(final PGNotification notification,
      final SQLProvider provider) {
    final Matcher matcher = GROUP_NODE_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer nodeId = Integer.parseInt(matcher.group(3));
    final Integer commentId =
        matcher.group(4).equals("null") ? null : Integer.parseInt(matcher.group(4));

    final INaviGroupNode groupNode = (INaviGroupNode) NodeCache.get(provider).getNodeById(nodeId);
    if (groupNode == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new GroupNodeCommentNotificationContainer(groupNode, operation, commentId);
  }

  /**
   * Parses the notifications from the database back end for global instruction comments by using a
   * regular expression. If the regular expression matched the supplied {@link PGNotification}
   * notification, it is determined if the {@link INaviInstruction} instruction in the notification
   * is currently loaded, and if a {@link CommentNotificationContainer} with the gathered data is
   * returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processInstructionGlobalCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = INSTRUCTION_GLOBAL_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final IAddress address = new CAddress(new BigInteger(matcher.group(4)));
    final Integer commentId = matcher.group(7) == null ? null : Integer.parseInt(matcher.group(7));

    final INaviModule module = provider.findModule(moduleId);
    if ((module == null) || !module.isLoaded()) {
      return null;
    }

    final INaviInstruction instruction =
        InstructionCache.get(provider).getInstructionByAddress(address, moduleId);
    if (instruction == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new InstructionCommentNotificationContainer(instruction, null, operation,
        CommentScope.GLOBAL, commentId);
  }

  /**
   * Parses the notifications from the database back end for global code node comments by using a
   * regular expression. If the regular expression matches the supplied {@link PGNotification}
   * notification, it is determined if the code node in the notification is currently loaded, and if
   * a {@link CommentNotificationContainer} with the data from the notification is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static Collection<CommentNotification> processNodeGlobalCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = NODE_GLOBAL_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return new ArrayList<>();
    }

    final String databaseOperation = matcher.group(2);
    final int moduleId = Integer.parseInt(matcher.group(3));
    final IAddress nodeAddress = new CAddress(new BigInteger(matcher.group(4)));
    final Integer commentId = matcher.group(6) == null ? null : Integer.parseInt(matcher.group(6));

    final INaviModule notificationModule = provider.findModule(moduleId);
    if ((notificationModule == null) || !notificationModule.isLoaded()) {
      return new ArrayList<>();
    }

    final ImmutableCollection<INaviViewNode> nodes =
        NodeCache.get(provider).getNodeByAddress(nodeAddress, moduleId);
    if (nodes == null) {
      return new ArrayList<>();
    }
    final CommentOperation operation =
        databaseOperation.equalsIgnoreCase("DELETE") ? CommentOperation.DELETE
            : CommentOperation.APPEND;

    Collection<CommentNotification> notifications = new ArrayList<>();
    for (INaviViewNode node : nodes) {
      notifications.add(new CodeNodeCommentNotificationContainer((INaviCodeNode) node, operation,
          CommentScope.GLOBAL, commentId));
    }
    return notifications;
  }

  /**
   * Parses the {@link PGNotification} notifications from the database back end for local
   * instruction comments by using a regular expression. If the regular expression matches the
   * supplied {@link PGNotification} notification, it is determined if the instruction in the
   * notification is currently loaded, and if a {@link CommentNotificationContainer} with the data
   * from the notification is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processNodeLocalInstructionCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher instructionMatcher =
        INSTRUCTION_LOCAL_PATTERN.matcher(notification.getParameter());
    final boolean instructionMatchFound = instructionMatcher.find();

    if (!instructionMatchFound) {
      return null;
    }

    final Integer moduleId = Integer.parseInt(instructionMatcher.group(3));
    final Integer nodeId = Integer.parseInt(instructionMatcher.group(4));
    final BigInteger notificationInstructionAddress = new BigInteger(instructionMatcher.group(6));
    final Integer commentId = instructionMatcher.group(7).equals("null") ? null
        : Integer.parseInt(instructionMatcher.group(7));

    final INaviModule module = provider.findModule(moduleId);
    if ((module == null) || !module.isLoaded()) {
      return null;
    }
    final IAddress address = new CAddress(notificationInstructionAddress);
    final INaviInstruction instruction = InstructionCache.get(provider).getInstructionByAddress(
        address, module.getConfiguration().getId());
    if (instruction == null) {
      return null;
    }
    final INaviCodeNode codeNode = (INaviCodeNode) NodeCache.get(provider).getNodeById(nodeId);
    if (codeNode == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new InstructionCommentNotificationContainer(instruction, codeNode, operation,
        CommentScope.LOCAL, commentId);
  }

  /**
   * Parses the {@link PGNotification} notifications from the PostgreSQL database back end for local
   * code node comments by using a regular expression. If the regular expression matches the
   * supplied {@link PGNotification} notification, it is determined if the code node in the
   * notification is currently loaded, and if a {@link CommentNotificationContainer} with the
   * gathered data from the notification is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processNodeLocalNodeCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = NODE_LOCAL_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer nodeId = Integer.parseInt(matcher.group(4));
    final Integer commentId =
        matcher.group(6).equals("null") ? null : Integer.parseInt(matcher.group(6));

    final INaviModule module = provider.findModule(moduleId);
    if (!module.isLoaded()) {
      return null;
    }
    final INaviCodeNode codeNode = (INaviCodeNode) NodeCache.get(provider).getNodeById(nodeId);
    if (codeNode == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new CodeNodeCommentNotificationContainer(codeNode, operation, CommentScope.LOCAL,
        commentId);
  }

  /**
   * Parses the {@link PGNotification} notifications from the PostgreSQL database back end for text
   * node comments by using a regular expression. If the regular expression matches the supplied
   * {@link PGNotification} notification, it is determined if the text node in the notification is
   * currently loaded, and if a {@link CommentNotificationContainer} with the gathered data from the
   * notification is returned.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processTextNodeCommentNotification(final PGNotification notification,
      final SQLProvider provider) {
    final Matcher matcher = TEXT_NODE_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer nodeId = Integer.parseInt(matcher.group(3));
    final Integer commentId =
        matcher.group(4).equals("null") ? null : Integer.parseInt(matcher.group(4));

    final INaviTextNode node = (INaviTextNode) NodeCache.get(provider).getNodeById(nodeId);
    if (node == null) {
      return null;
    }

    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new TextNodeCommentNotificationContainer(node, operation, commentId);
  }

  /**
   * Parses the {@link PGNotification notifications} from the PostgreSQL database back end for
   * {@link TypeInstance type instance} comments by using a regular expression.
   *
   * @param notification The {@link PGNotification} from the PostgreSQL database server.
   * @param provider The {@link SQLProvider} which is used to communicate with the database.
   */
  static CommentNotification processTypeInstanceCommentNotification(
      final PGNotification notification, final SQLProvider provider) {
    final Matcher matcher = TYPE_INSTANCE_PATTERN.matcher(notification.getParameter());
    if (!matcher.find()) {
      return null;
    }

    final Integer moduleId = Integer.parseInt(matcher.group(3));
    final Integer typeInstanceId = Integer.parseInt(matcher.group(4));
    final Integer commentId =
        matcher.group(5).equals("null") ? null : Integer.parseInt(matcher.group(5));

    final INaviModule module = provider.findModule(moduleId);
    if (module == null || !module.isLoaded()) {
      return null;
    }
    final TypeInstance instance =
        module.getContent().getTypeInstanceContainer().getTypeInstanceById(typeInstanceId);
    if (instance == null) {
      return null;
    }
    final CommentOperation operation =
        commentId == null ? CommentOperation.DELETE : CommentOperation.APPEND;

    return new TypeInstanceCommentNotificationContainer(instance, operation, commentId);
  }

  @Override
  public Collection<CommentNotification> parse(
      final Collection<PGNotification> commentNotifications, final SQLProvider provider) {

    Preconditions.checkNotNull(commentNotifications,
        "Error: commentNotifications argument can not be null");
    Preconditions.checkNotNull(provider, "IE02524: provider argument can not be null");

    for (final PGNotification notification : commentNotifications) {
      final String notificationParameter = notification.getParameter();
      final String tableName = notificationParameter.split("\\s")[0];

      try {
        switch (tableName) {
          case CTableNames.CODENODE_INSTRUCTIONS_TABLE:
            informNotification(
                processNodeLocalInstructionCommentNotification(notification, provider), provider);
            break;
          case CTableNames.INSTRUCTIONS_TABLE:
            informNotification(processInstructionGlobalCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.CODE_NODES_TABLE:
            informNotification(processNodeLocalNodeCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.GLOBAL_NODE_COMMENTS_TABLE:
            informNotification(processNodeGlobalCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.EDGES_TABLE:
            informNotification(processEdgeLocalCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.GLOBAL_EDGE_COMMENTS_TABLE:
            informNotification(processEdgeGlobalCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.FUNCTION_NODES_TABLE:
            informNotification(processFunctionNodeCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.FUNCTIONS_TABLE:
            informNotification(processFunctionCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.TEXT_NODES_TABLE:
            informNotification(processTextNodeCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.GROUP_NODES_TABLE:
            informNotification(processGroupNodeCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.TYPE_INSTANCE_TABLE:
            informNotification(processTypeInstanceCommentNotification(notification, provider),
                provider);
            break;
          case CTableNames.COMMENTS_TABLE:
            informNotification(processCommentNotification(notification, provider), provider);
            break;
          default:
            NaviLogger.warning("Table name %s not known", tableName);
        }
      } catch (CouldntLoadDataException exception) {
        NaviLogger.severe(
            "Error: Could not successfully parse the database comment notification: %s",
            notification.toString());
      }
    }
    return new ArrayList<>(); // TODO(timkornau): change the interface to not return anything here.
  }

  private static void informNotification(final CommentNotification notification,
      final SQLProvider provider) throws CouldntLoadDataException {
    if (notification != null) {
      notification.inform(CommentManager.get(provider));
    }
  }

  private static void informNotification(final Collection<CommentNotification> notifications,
      final SQLProvider provider) throws CouldntLoadDataException {
    for (CommentNotification notification : notifications) {
      informNotification(notification, provider);
    }
  }

  @Override
  public void inform(final Collection<CommentNotification> commentNotifications,
      final SQLProvider provider) {/* not used */}
}
