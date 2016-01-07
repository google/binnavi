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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProviderListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.Users.CUserManager;
import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The comment manager is the ultimate source for all comments.
 *
 *  Because comments are global for each database, there is one comment manager for each database
 * connection known to com.google.security.zynamics.binnavi.
 *
 *  Objects that can have comments must listen on the comment manager to make sure that their
 * comments stay synchronized when they are changed by some other object that shares their comment
 * and or by other BinNavi entities using the same database host.
 */
public final class CommentManager {

  /**
   * Keeps track of the global comment managers for the individual databases.
   */
  private static Map<SQLProvider, CommentManager> managers =
      new HashMap<SQLProvider, CommentManager>();

  /**
   * The map which stores comment id to comment object mappings.
   */
  private final Map<Integer, IComment> commentIdToComment = new HashMap<Integer, IComment>();

  /**
   * Objects that want to be notified about changes in global comments.
   */
  private final ListenerProvider<CommentListener> listeners =
      new ListenerProvider<CommentListener>();

  /**
   * Comment container for global instruction comments.
   */
  private final CommentContainer<INaviInstruction, IComment> globalInstructionsCommentContainer =
      new CommentContainer<INaviInstruction, IComment>();

  /**
   * Comment container for local instruction comments.
   */
  private final
      CommentContainer<Pair<INaviCodeNode, INaviInstruction>, IComment>
      localInstructionCommentContainer =
          new CommentContainer<Pair<INaviCodeNode, INaviInstruction>, IComment>();

  /**
   * Comment container for type instances comments.
   */
  private final CommentContainer<TypeInstance, IComment> typeInstanceCommentContainer =
      new CommentContainer<TypeInstance, IComment>();

  /**
   * Comment container for section instances comments.
   */
  private final CommentContainer<Section, IComment> sectionCommentContainer =
      new CommentContainer<Section, IComment>();

  /**
   * Comment container for function comments.
   */
  private final CommentContainer<INaviFunction, IComment> functionCommentContainer =
      new CommentContainer<INaviFunction, IComment>();

  /**
   * Comment container for global edge comments.
   */
  private final CommentContainer<INaviEdge, IComment> globalEdgeCommentContainer =
      new CommentContainer<INaviEdge, IComment>();

  /**
   * Comment container for global code node comments.
   */
  private final CommentContainer<INaviCodeNode, IComment> globalCodeNodeCommentContainer =
      new CommentContainer<INaviCodeNode, IComment>();

  /**
   * Comment container for local edge comments.
   */
  private final CommentContainer<INaviEdge, IComment> localEdgeCommentContainer =
      new CommentContainer<INaviEdge, IComment>();

  /**
   * Comment container for local code node comments.
   */
  private final CommentContainer<INaviCodeNode, IComment> localCodeNodeCommentContainer =
      new CommentContainer<INaviCodeNode, IComment>();

  /**
   * Comment container for function node comments.
   */
  private final CommentContainer<INaviFunctionNode, IComment> functionNodeCommentContainer =
      new CommentContainer<INaviFunctionNode, IComment>();

  /**
   * Comment container for text node comments.
   */
  private final CommentContainer<INaviTextNode, IComment> textNodeCommentContainer =
      new CommentContainer<INaviTextNode, IComment>();

  /**
   * Comment container for group node comments.
   */
  private final CommentContainer<INaviGroupNode, IComment> groupNodeCommentContainer =
      new CommentContainer<INaviGroupNode, IComment>();

  /**
   * Database for which the global comment manager was created.
   */
  private final SQLProvider provider;

  /**
   * Listener to get informed about changes in the {@link SQLProvider provider}.
   */
  private final SQLProviderListener providerListener = new InternalSQLProviderListener();

  /**
   * Creates a new global comment manager object.
   *
   * @param sqlProvider Database for which the global comment manager was created.
   */
  private CommentManager(final SQLProvider sqlProvider) {
    provider =
        Preconditions.checkNotNull(sqlProvider, "IE00089: Provider argument can not be null");
    provider.addListener(providerListener);
  }

  /**
   * Returns the global comment manager for a database.
   *
   * @param provider The SQL provider that is connected to the database.
   *
   * @return The global comment manager for the given database.
   */
  public static synchronized CommentManager get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE01239: Provider argument can not be null");

    if (!managers.containsKey(provider)) {
      managers.put(provider, new CommentManager(provider));
    }

    return managers.get(provider);
  }

  /**
   * Close method which removes the closed provider from the static container where all
   * {@link CommentManager managers} register themselves.
   */
  private void close() {
    managers.remove(provider);
    provider.removeListener(providerListener);
  }

  /**
   * This function is used when database notifications about comments are received that have been
   * added. In this case the comment id is known as the comment has already been stored in the
   * database and can be passed to the function as parameter. The function loads the comment chain
   * from the database starting with the comment id which was provided.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function wrappers.
   * @param commentId The comment id of the comment which was just added.
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  private synchronized void appendComment(
      final CommentingStrategy strategy, final Integer commentId) throws CouldntLoadDataException {
    Preconditions.checkNotNull(strategy, "IE02539: strategy argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02540: commentId argument can not be null");

    final ArrayList<IComment> currentDataBaseComments = provider.loadCommentById(commentId);

    strategy.saveComments(currentDataBaseComments);
    for (final IComment comment : currentDataBaseComments) {
      commentIdToComment.put(comment.getId(), comment);
    }

    IComment notificationComment = null;
    for (final IComment comment : currentDataBaseComments) {
      if (comment.getId().equals(commentId)) {
        notificationComment = comment;
      }
    }

    if (notificationComment != null) {
      for (final CommentListener listener : listeners) {
        try {
          strategy.sendAppendedCommentNotifcation(listener, notificationComment);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }

  /**
   * This function provides the append comment functionality for any given Implementation of a
   * {@link CommentingStrategy}. It uses the methods of the interface to only have one algorithm
   * with different objects that can be commented.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function forwarders.
   * @param commentText The commenting text to append.
   *
   * @return The generated comment.
   *
   * @throws CouldntSaveDataException if the comment could not be stored in the database.
   * @throws CouldntLoadDataException if the list of comments now associated with the commented
   *         object could not be loaded from the database.
   */
  private synchronized List<IComment> appendComment(
      final CommentingStrategy strategy, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {

    final IUser user = CUserManager.get(provider).getCurrentActiveUser();

    final List<IComment> currentComments = new ArrayList<IComment>();

    if (strategy.isStored()) {
      currentComments.addAll(strategy.appendComment(commentText, user.getUserId()));
    } else {
      currentComments.addAll(strategy.getComments() == null ? new ArrayList<IComment>()
          : Lists.newArrayList(strategy.getComments()));
      final IComment parent = currentComments.isEmpty() ? null : Iterables.getLast(currentComments);
      final IComment newComment = new CComment(null, user, parent, commentText);
      currentComments.add(newComment);
    }

    strategy.saveComments(currentComments);
    for (final IComment comment : currentComments) {
      commentIdToComment.put(comment.getId(), comment);
    }

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendAppendedCommentNotifcation(listener, Iterables.getLast(currentComments));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
    return currentComments;
  }

  /**
   * This function provides the delete comment functionality for any given implementation of a
   * {@link CommentingStrategy}. It uses the methods of the interface to only have one algorithm
   * with different objects that can be commented.
   *
   *
   * @param strategy The {@link CommentingStrategy} which holds the function forwarders.
   * @param comment The comment to delete.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  private synchronized void deleteComment(final CommentingStrategy strategy, final IComment comment)
      throws CouldntDeleteException {

    Preconditions.checkNotNull(comment, "IE02541: comment argument can not be null");
    Preconditions.checkArgument(CUserManager.get(provider).isOwner(comment),
        "Error: a comment can be deleted only by its owner.");

    final List<IComment> currentComments = strategy.getComments();
    if (!currentComments.remove(comment)) {
      return;
    }
    strategy.deleteComment(comment.getId(), comment.getUser().getUserId());
    strategy.saveComments(currentComments);
    commentIdToComment.remove(comment.getId());

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendDeletedCommentNotification(listener, comment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * This function is used when notification from the database are received that a comment has been
   * deleted. Therefore the comment only needs to be removed from the data structures in the comment
   * manager as it has already been removed from the database.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function wrappers.
   * @param comment The comment which has been deleted from the database.
   */
  private synchronized void deleteCommentInternal(
      final CommentingStrategy strategy, final IComment comment) {

    Preconditions.checkNotNull(comment, "IE02542: comment argument can not be null");

    final List<IComment> currentComments = strategy.getComments();
    if (!currentComments.remove(comment)) {
      return;
    }
    strategy.saveComments(currentComments);
    commentIdToComment.remove(comment.getId());

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendDeletedCommentNotification(listener, comment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

  }

  /**
   * This function provides the edit comment functionality for any given implementation of a
   * {@link CommentingStrategy}. It uses the methods of the interface to only have one algorithm
   * with different objects that can be commented.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function forwarders.
   * @param editedComment The comment which is getting edited.
   * @param commentText The comment text which will be saved in the edited comment.
   *
   * @return The comment after it has been edited.
   *
   * @throws CouldntSaveDataException if the changes to the comment could not be saved to the
   *         database.
   */
  private synchronized IComment editComment(
      final CommentingStrategy strategy, final IComment editedComment, final String commentText)
      throws CouldntSaveDataException {

    Preconditions.checkNotNull(editedComment, "IE02543: comment argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02544: commentText argument can not be null");
    Preconditions.checkArgument(CUserManager.get(provider).isOwner(editedComment),
        "Error: a comment can only be edited by its owner.");

    final List<IComment> currentComments = strategy.getComments();

    // Abort early if nothing changes.
    if (editedComment.getComment().equals(commentText)) {
      return editedComment;
    }

    final int index = currentComments.indexOf(editedComment);
    if (index == -1) {
      throw new IllegalArgumentException(
          "Error: Can not edit comment as there is no comment to edit.");
    }

    final IComment newComment = new CComment(
        editedComment.getId(), editedComment.getUser(), editedComment.getParent(), commentText);

    strategy.editComment(newComment.getId(), newComment.getUser().getUserId(), commentText);
    currentComments.set(index, newComment);
    strategy.saveComments(currentComments);
    commentIdToComment.put(editedComment.getId(), newComment);

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendEditedCommentNotification(listener, newComment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    return newComment;
  }

  /**
   * This function is used when notification about edited comments are received from the database.
   * As the comment has already been edited the edit only needs to be propagated into the internal
   * data structures of the comment manager.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function wrappers.
   * @param editedComment The comment which has been edited.
   * @param newComment The new comment which will replace the edited comment.
   */
  private synchronized void editCommentInternal(
      final CommentingStrategy strategy, final IComment editedComment, final IComment newComment) {
    Preconditions.checkNotNull(strategy, "IE02545: strategy argument can not be null");
    Preconditions.checkNotNull(editedComment, "IE02546: comment argument can not be null");
    Preconditions.checkNotNull(newComment, "IE02547: commentText argument can not be null");

    final List<IComment> currentComments = strategy.getComments();

    if (editedComment.getComment().equals(newComment)) {
      return;
    }

    final int index = currentComments.indexOf(editedComment);
    if (index == -1) {
      throw new IllegalArgumentException(
          "Error: Can not edit comment as there is no comment to edit.");
    }

    currentComments.set(index, newComment);
    strategy.saveComments(currentComments);
    commentIdToComment.put(editedComment.getId(), newComment);

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendEditedCommentNotification(listener, newComment);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * This function provides the initialize functionality for any given implementation of a
   * {@link CommentingStrategy}. It uses the methods of the interface to only have one algorithm for
   * different objects that can be commented.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function forwarders.
   * @param comments The comment with which the object held by the {@link CommentingStrategy} will
   *        be initialized.
   */
  private synchronized void initializeComment(
      final CommentingStrategy strategy, final List<IComment> comments) {
    Preconditions.checkNotNull(strategy, "IE02548: strategy argument can not be null");
    if (comments == null) {
      return;
    }
    strategy.saveComments(comments);
    for (final IComment comment : comments) {
      commentIdToComment.put(comment.getId(), comment);
    }

    for (final CommentListener listener : listeners) {
      try {
        strategy.sendInitializedCommentNotification(listener, comments);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Unloads a comment and removes the items it references from the internal storage.
   *
   * @param strategy The {@link CommentingStrategy} which holds the function forwarders.
   * @param comments The List of {@link IComment} to be unloaded.
   */
  private synchronized void unloadComment(
      final CommentingStrategy strategy, final List<IComment> comments) {
    Preconditions.checkNotNull(strategy, "Error: strategy argument can not be null");
    if (comments == null) {
      return;
    }
    strategy.removeComments(comments);
    for (final IComment comment : comments) {
      commentIdToComment.remove(comment.getId());
    }
  }

  /**
   * Adds a listener object that is notified about changes in global comments.
   *
   * @param listener The listener object to add.
   */
  public synchronized void addListener(final CommentListener listener) {
    listeners.addListener(listener);
  }


  /**
   * Appends a new code node comment to the list of code node comments associated with the given
   * code node by using the comment id supplied by the used to fetch the comment from the database.
   *
   * @param node The code node to which the comment will be associated.
   * @param commentId The comment id of the comment to fetch from the database.
   * @param scope The scope of the comment (GLOBAL/LOCAL).
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendCodeNodeComment(
      final INaviCodeNode node, final Integer commentId, final CommentScope scope)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02549: node argument can not be null");
    Preconditions.checkNotNull(commentId, "IE02550: commentId argument can not be null");
    Preconditions.checkNotNull(scope, "IE02551: scope argument can not be null");
    appendComment(new CodeNodeCommentingStrategy(node, scope), commentId);
  }

  /**
   * Appends a new edge comment to the list of edge comments associated with the given edge by using
   * the comment id supplied by the caller to load the comment from the database.
   *
   * @param edge The edge to which the comment will be associated.
   * @param commentId The comment id of the comment to fetch from the database.
   * @param scope The scope of the comment (GLOBAL/LOCAL).
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendEdgeComment(
      final INaviEdge edge, final Integer commentId, final CommentScope scope)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(edge, "IE02552: edge argument can not be null");
    appendComment(new EdgeCommentingStrategy(edge, scope), commentId);
  }

  /**
   * Appends a new function comment to the list of function comments associated with the given
   * function by using the comment id supplied the caller to fetch the comment from the database.
   *
   * @param function The function to which the comment will be associated.
   * @param commentId The comment id of the comment to fetch from the database.
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendFunctionComment(
      final INaviFunction function, final Integer commentId) throws CouldntLoadDataException {
    Preconditions.checkNotNull(function, "IE02553: function argument can not be null");
    appendComment(new FunctionCommentingStrategy(function), commentId);
  }

  /**
   * Appends a new function node comment to the list of function node comments associated with the
   * given function node by using the comment id supplied the caller to fetch the comment from the
   * database.
   *
   * @param node The function node to which the comment will be associated.
   * @param commentId The comment id of the comment to fetch from the database.
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendFunctionNodeComment(
      final INaviFunctionNode node, final Integer commentId) throws CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02554: node argument can not be null");
    appendComment(new FunctionNodeCommentingStrategy(node), commentId);
  }

  /**
   * Appends a new comment to the list of function node comments associated with the given function
   * node.
   *
   * @param node The function node to which the appended comment will be associated.
   * @param comment The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendFunctionNodeComment(
      final INaviFunctionNode node, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE01242: FunctionNode argument can not be null");
    return appendComment(new FunctionNodeCommentingStrategy(node), comment);
  }

  /**
   * Appends a new comment to the list of global code node comments associated with the given code
   * node.
   *
   * @param node The code node to which the appended comment will be associated.
   * @param comment The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendGlobalCodeNodeComment(
      final INaviCodeNode node, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02555: codeNode argument can not be null");
    return appendComment(new CodeNodeCommentingStrategy(node, CommentScope.GLOBAL), comment);
  }

  /**
   * Appends a new comment to the list of global edge comment associated with the given edge.
   *
   * @param edge The edge to which the appended comment will be associated.
   * @param commentText The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendGlobalEdgeComment(
      final INaviEdge edge, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(edge, "IE01244: Edge argument can not be null");
    return appendComment(new EdgeCommentingStrategy(edge, CommentScope.GLOBAL), commentText);
  }

  /**
   * Appends a new comment to the list of global function comments associated with the given
   * function.
   *
   * @param function The function to which the appended comment will be associated.
   * @param commentText The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendGlobalFunctionComment(
      final INaviFunction function, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(function, "IE01242: Function argument can not be null");
    return appendComment(new FunctionCommentingStrategy(function), commentText);
  }

  /**
   * Appends a new global comment to the list of global instruction comments associated with the
   * given instruction.
   *
   * @param instruction The instruction to which the appended comment will be associated.
   * @param commentText The comments text.
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendGlobalInstructionComment(
      final INaviInstruction instruction, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(instruction, "IE01246: Instruction argument can not be null");
    return appendComment(
        new InstructionCommentingStrategy(instruction, null, CommentScope.GLOBAL), commentText);
  }

  /**
   * Appends a new group node comment to the list of group node comments associated with the given
   * group node by using the comment id supplied the caller to fetch the comment from the database.
   *
   * @param node The {@link INaviGroupNode} to which the appended comment will be associated.
   * @param commentId The comment id of the comment which has been appended.
   *
   * @throws CouldntLoadDataException if the appended comment could not be loaded from the database.
   */
  public synchronized void appendGroupNodeComment(
      final INaviGroupNode node, final Integer commentId) throws CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02556: node argument can not be null");
    appendComment(new GroupNodeCommentingStrategy(node), commentId);
  }

  /**
   * Appends a new comment to the list of group node comments associated with the given group node.
   *
   * @param node The group node to which the appended comment will be associated.
   * @param comment The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendGroupNodeComment(
      final INaviGroupNode node, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE01242: Text node argument can not be null");
    return appendComment(new GroupNodeCommentingStrategy(node), comment);
  }

  /**
   * Appends a new instruction comment to the list of comments associated with the given instruction
   * to the storage of the comment manager. The comment id supplied is used to load the comment in
   * question from the database.
   *
   * @param instruction The {@link INaviInstruction} to which the comment will be associated.
   * @param node The {@link INaviCodeNode} which needs to be present if the comment is a local
   *        instruction comment.
   * @param scope The {@link CommentScope} to decide whether the comment is GLOBAL or LOCAL.
   * @param commentId The comment id of the comment to be appended from the database.
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendInstructionComment(final INaviInstruction instruction,
      final INaviCodeNode node, final CommentScope scope, final Integer commentId)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(instruction, "IE02557: instruction argument can not be null");
    appendComment(new InstructionCommentingStrategy(instruction, node, scope), commentId);
  }

  /**
   * Appends a new local comment to the list of local code node comments associated with the given
   * code node.
   *
   * @param node The code node where the comment is appended.
   * @param comment The comment text for the new comment.
   *
   * @return The comment which is saved in the database.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendLocalCodeNodeComment(
      final INaviCodeNode node, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02558: group node argument can not be null");
    return appendComment(new CodeNodeCommentingStrategy(node, CommentScope.LOCAL), comment);
  }

  /**
   * Appends a new local comment to the list of local edge comment associated with the given edge.
   *
   * @param edge The edge where the comment is appended.
   * @param comment The comment text for the new comment.
   *
   * @return The comment which is saved in the database.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  public synchronized List<IComment> appendLocalEdgeComment(
      final INaviEdge edge, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(edge, "IE02559: edge argument can not be null");
    return appendComment(new EdgeCommentingStrategy(edge, CommentScope.LOCAL), comment);
  }

  /**
   * Appends a new local comment to the list of local instruction comments associated with the given
   * instruction.
   *
   * @param instruction The instruction to which the appended comment will be associated.
   * @param commentText The comments text.
   * @return The list of local instruction comments associated to the {@link INaviInstruction
   *         instruction}.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   * @throws CouldntLoadDataException if the comment information could not be loaded from the
   *         database.
   */
  public synchronized List<IComment> appendLocalInstructionComment(
      final INaviInstruction instruction, final INaviCodeNode node, final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(instruction, "IE01246: Instruction argument can not be null");
    Preconditions.checkNotNull(node, "IE02560: node argument can not be null");
    Preconditions.checkArgument(Iterables.contains(node.getInstructions(), instruction),
        "Error: instruction does not belong to the specified node");
    return appendComment(
        new InstructionCommentingStrategy(instruction, node, CommentScope.LOCAL), commentText);
  }

  /**
   * Appends a new text node comment to the list of comments associated with the given text node.
   * The comment id is used to load the comment from the database.
   *
   * @param node The {@link INaviTextNode} to associated the comment with.
   * @param commentId The comment id to load from the database.
   *
   * @throws CouldntLoadDataException if the comment could not be loaded from the database.
   */
  public synchronized void appendTextNodeComment(final INaviTextNode node, final Integer commentId)
      throws CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE02561: node argument can not be null");
    appendComment(new TextNodeCommentingStrategy(node), commentId);
  }

  /**
   * Appends a new comment to the list of text node comments associated with the given text node.
   *
   * @param node The text node to which the appended comment will be associated.
   * @param comment The comments text.
   *
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the information could not be saved to the database.
   */
  public synchronized List<IComment> appendTextNodeComment(
      final INaviTextNode node, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(node, "IE01242: Text node argument can not be null");
    return appendComment(new TextNodeCommentingStrategy(node), comment);
  }

  /**
   * Appends a new comment to the given type instance.
   *
   * @param instance The type instance for which to append a comment.
   * @param comment The comment to append.
   * @return The list of comments of the type instance.
   *
   * @throws CouldntSaveDataException Thrown if the comment could not be saved in the database.
   * @throws CouldntLoadDataException Thrown if the comments could not be loaded from the database.
   */
  public synchronized List<IComment> appendTypeInstanceComment(
      final TypeInstance instance, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    return appendComment(new TypeInstanceCommentingStrategy(instance), comment);
  }

  /**
   * Appends a new {@link IComment comment} to the given {@link TypeInstance type instance} by
   * comment id.
   *
   * @param instance The {@link TypeInstance type instance} to append the {@link IComment comment}
   *        to.
   * @param commentId The id of the {@link IComment comment} which is appended.
   */
  public synchronized void appendTypeInstanceComment(
      final TypeInstance instance, final Integer commentId) throws CouldntLoadDataException {
    Preconditions.checkNotNull(instance, "Error: instance argument can not be null");
    appendComment(new TypeInstanceCommentingStrategy(instance), commentId);
  }

  /**
   * Appends a new comment to the given section.
   *
   * @param section The section where the comment is appended.
   * @param comment The comments text.
   *
   * @return The list of comments currently associated to the section.
   *
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException if the comments could not be loaded from the database.
   */
  public synchronized List<IComment> appendSectionComment(
      final Section section, final String comment)
      throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    return appendComment(new SectionCommentingStrategy(section), comment);
  }

  /**
   * Deletes a comment from the list of code node comments it used to be associated with without
   * removing it from the database. This function is used when the database sends a notification
   * about a deleted code node comment.
   *
   * @param node The {@link INaviCodeNode} to which the comment was associated.
   * @param comment The {@link IComment} which has been deleted from the database.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void deleteCodeNodeComment(
      final INaviCodeNode node, final IComment comment, final CommentScope scope) {
    Preconditions.checkNotNull(node, "IE02562: node argument can not be null");
    deleteCommentInternal(new CodeNodeCommentingStrategy(node, scope), comment);
  }

  /**
   * Deletes a comment from the list of comments associated to the given edge without removing it
   * from the database. This function is used when the database sends a notification about a deleted
   * edge comment.
   *
   * @param edge The {@link INaviEdge} to which the comment was associated.
   * @param comment The {@link IComment} which has been deleted from the database.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void deleteEdgeComment(
      final INaviEdge edge, final IComment comment, final CommentScope scope) {
    Preconditions.checkNotNull(edge, "IE02563: edge argument can not be null");
    deleteCommentInternal(new EdgeCommentingStrategy(edge, scope), comment);
  }

  /**
   * Deletes a function comment from the list of comments which is associated to the given function
   * without removing it from the database. This function is used when the database sends
   * notifications about a deleted function comment.
   *
   * @param function The {@link INaviFunction} to which the comment used to be associated to.
   * @param comment The {@link IComment} which has been deleted from the database.
   */
  public synchronized void deleteFunctionCommentInternal(
      final INaviFunction function, final IComment comment) {
    Preconditions.checkNotNull(function, "IE02564: function argument can not be null");
    deleteCommentInternal(new FunctionCommentingStrategy(function), comment);
  }

  /**
   * Deletes a function node comment.
   *
   * @param node The function node where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteFunctionNodeComment(
      final INaviFunctionNode node, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02565: functionNode argument can not be null");
    deleteComment(new FunctionNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes a function node comment from the list of comments associated with the function node
   * given without removing it from the database. The function is used when the database sends
   * notifications about a deleted function node comment.
   *
   * @param node The {@link INaviFunctionNode} to which the comment was associated.
   * @param comment The {@link IComment} which was deleted from the database.
   */
  public synchronized void deleteFunctionNodeCommentInternal(
      final INaviFunctionNode node, final IComment comment) {
    Preconditions.checkNotNull(node, "IE02566: node argument can not be null");
    deleteCommentInternal(new FunctionNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes a global comment from a code nodes global comment list.
   *
   * @param node The code node where the comment is deleted.
   * @param comment The comment which will get deleted.
   *
   * @throws CouldntDeleteException if the changes could not be saved to the database.
   */
  public synchronized void deleteGlobalCodeNodeComment(
      final INaviCodeNode node, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02567: codeNode argument can not be null");
    deleteComment(new CodeNodeCommentingStrategy(node, CommentScope.GLOBAL), comment);
  }

  /**
   * Deletes a global edge comment.
   *
   * @param edge The edge where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteGlobalEdgeComment(final INaviEdge edge, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(edge, "IE02568: edge argument can not be null");
    deleteComment(new EdgeCommentingStrategy(edge, CommentScope.GLOBAL), comment);
  }

  /**
   * Deletes a global function comment.
   *
   * @param function The function where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteGlobalFunctionComment(
      final INaviFunction function, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(function, "IE02569: function argument can not be null");
    deleteComment(new FunctionCommentingStrategy(function), comment);
  }

  /**
   * Deletes a global instruction comment.
   *
   * @param instruction The instruction where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteGlobalInstructionComment(
      final INaviInstruction instruction, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(instruction, "IE02570: instruction argument can not be null");
    deleteComment(
        new InstructionCommentingStrategy(instruction, null, CommentScope.GLOBAL), comment);
  }

  /**
   * Deletes a group node comment.
   *
   * @param node The group node where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteGroupNodeComment(final INaviGroupNode node, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02571: functionNode argument can not be null");
    deleteComment(new GroupNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes a group node comment from the list of comments associated with the given group node
   * without deleting it from the database. This function is used when the database sends out
   * notifications about deleted group node comments.
   *
   * @param node The {@link INaviGroupNode} where the comment was deleted.
   * @param comment The {@link IComment} which was deleted.
   */
  public synchronized void deleteGroupNodeCommentInternal(
      final INaviGroupNode node, final IComment comment) {
    Preconditions.checkNotNull(node, "IE02572: node argument can not be null");
    deleteCommentInternal(new GroupNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes an instruction comment from the list of comments associated with the given instruction
   * without deleting it from the database. This function is used when the database sends out
   * notifications about deleted instruction comments.
   *
   * @param instruction The {@link INaviInstruction} to which the comment was associated.
   * @param node The {@link INaviCodeNode} to pass when the {@link CommentScope} is LOCAL.
   * @param scope The {@link CommentScope} of the comment.
   * @param comment The {@link IComment} which has been deleted.
   */
  public synchronized void deleteInstructionComment(final INaviInstruction instruction,
      final INaviCodeNode node, final CommentScope scope, final IComment comment) {
    Preconditions.checkNotNull(instruction, "IE02573: instruction argument can not be null");
    deleteCommentInternal(new InstructionCommentingStrategy(instruction, node, scope), comment);
  }

  /**
   * Deletes a local comment from the list of comment associated to the given code node.
   *
   * @param node The code node where the local comment is deleted.
   * @param comment The comment to be deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteLocalCodeNodeComment(
      final INaviCodeNode node, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02574: codeNode argument can not be null");
    deleteComment(new CodeNodeCommentingStrategy(node, CommentScope.LOCAL), comment);
  }

  /**
   * Deletes a local edge comment from the lost of comments associated with the given edge.
   *
   * @param edge The edge were the local comment is deleted.
   * @param comment The comment to be deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteLocalEdgeComment(final INaviEdge edge, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(edge, "IE02575: edge argument can not be null");
    deleteComment(new EdgeCommentingStrategy(edge, CommentScope.LOCAL), comment);
  }

  /**
   * Deletes a local instruction comment.
   *
   * @param instruction The instruction where the comment is deleted.
   * @param node The code node were the instruction is located in.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteLocalInstructionComment(
      final INaviInstruction instruction, final INaviCodeNode node, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(instruction, "IE02576: instruction argument can not be null");
    deleteComment(
        new InstructionCommentingStrategy(instruction, node, CommentScope.LOCAL), comment);
  }

  /**
   * Deletes a text node comment.
   *
   * @param node The text node where the comment is deleted.
   * @param comment The comment which is deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  public synchronized void deleteTextNodeComment(final INaviTextNode node, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(node, "IE02577: textnode argument can not be null");
    deleteComment(new TextNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes a text node comment from the list of comments associated with the given text node
   * without deleting it from the database. This function is used when the database sends out
   * notifications about deleted text node comments.
   *
   * @param node The {@link INaviTextNode} to which the comment was associated.
   * @param comment The {@link IComment} which was deleted.
   */
  public synchronized void deleteTextNodeCommentInternal(
      final INaviTextNode node, final IComment comment) {
    Preconditions.checkNotNull(node, "IE02578: node argument can not be null");
    deleteCommentInternal(new TextNodeCommentingStrategy(node), comment);
  }

  /**
   * Deletes a type instance comment.
   *
   * @param typeInstance The {@link TypeInstance} to which the comment is associated.
   * @param comment The {@link IComment} which is deleted.
   *
   * @throws CouldntDeleteException if the {@link IComment} could not be deleted from the database.
   */
  public synchronized void deleteTypeInstanceComment(
      final TypeInstance typeInstance, final IComment comment) throws CouldntDeleteException {
    Preconditions.checkNotNull(typeInstance, "Error: type instance can not be null");
    deleteComment(new TypeInstanceCommentingStrategy(typeInstance), comment);
  }

  /**
   * Deletes a type instance comment, without changing the database.
   *
   * @param typeInstance The {@link TypeInstance} where the comment is deleted.
   * @param comment The {@link IComment} which is deleted.
   */
  public synchronized void deleteTypeInstanceCommentInternal(
      final TypeInstance typeInstance, final IComment comment) {
    Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null");
    deleteCommentInternal(new TypeInstanceCommentingStrategy(typeInstance), comment);
  }

  /**
   * Deletes a section comment.
   *
   * @param section The {@link Section} to which the comment is associated.
   * @param comment The {@link IComment} which is deleted.
   *
   * @throws CouldntDeleteException if the {@link IComment} could not be deleted from the database.
   */
  public synchronized void deleteSectionComment(final Section section, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    deleteComment(new SectionCommentingStrategy(section), comment);
  }

  /**
   * Edits a code node comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited code node comments are send
   * from the database.
   *
   * @param node The {@link INaviCodeNode} where the comment is edited.
   * @param comment The {@link IComment} which will be edited.
   * @param newComment The {@link IComment} which is replacing the old comment.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void editCodeNodeComment(final INaviCodeNode node, final IComment comment,
      final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(node, "IE02579: node argument can not be null");
    editCommentInternal(new CodeNodeCommentingStrategy(node, scope), comment, newComment);
  }

  /**
   * Edits an edge comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited edge comments are send from
   * the database.
   *
   * @param edge The {@link INaviEdge} where the comment is edited.
   * @param comment The {@link IComment} which will be edited.
   * @param newComment The {@link IComment} which is replacing the old comment.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void editEdgeComment(final INaviEdge edge, final IComment comment,
      final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(edge, "IE02580: edge argument can not be null");
    editCommentInternal(new EdgeCommentingStrategy(edge, scope), comment, newComment);
  }

  /**
   * Edits a function comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited function comments are send
   * from the database.
   *
   * @param function The {@link INaviFunction} where the comment is edited.
   * @param comment The {@link IComment} which will be edited.
   * @param newComment The {@link IComment} which will replace the old comment.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void editFunctionComment(final INaviFunction function, final IComment comment,
      final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(function, "IE02581: function argument can not be null");
    editCommentInternal(new FunctionCommentingStrategy(function), comment, newComment);
  }

  /**
   * Edits a function node comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited function node comments are
   * send from the database.
   *
   * @param node The {@link INaviFunctionNode} where the comment is edited.
   * @param comment The {@link IComment} which will be edited.
   * @param newComment The {@link IComment} which will replace the old comment.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void editFunctionNodeComment(final INaviFunctionNode node,
      final IComment comment, final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(node, "IE02582: node argument can not be null");
    editCommentInternal(new FunctionNodeCommentingStrategy(node), comment, newComment);
  }

  /**
   * Edits a function node comment.
   *
   * @param functionNode The function node where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changed could not be saved to the database.
   */
  public synchronized IComment editFunctionNodeComment(
      final INaviFunctionNode functionNode, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(functionNode, "IE02583: function argument can not be null");
    return editComment(new FunctionNodeCommentingStrategy(functionNode), oldComment, commentText);
  }

  /**
   * Edits a global comment from a code node.
   *
   * @param codeNode The code node where the comment is edited.
   * @param commentText The comment which has been edited.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public synchronized IComment editGlobalCodeNodeComment(
      final INaviCodeNode codeNode, final IComment editedComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(codeNode, "IE02584: codeNode argument can not be null");
    return editComment(
        new CodeNodeCommentingStrategy(codeNode, CommentScope.GLOBAL), editedComment, commentText);
  }

  /**
   * Edits a global edge comment.
   *
   * @param edge The edge where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public synchronized IComment editGlobalEdgeComment(
      final INaviEdge edge, final IComment editedComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(edge, "IE02585: edge argument can not be null");
    return editComment(
        new EdgeCommentingStrategy(edge, CommentScope.GLOBAL), editedComment, commentText);
  }

  /**
   * Edits a global function comment.
   *
   * @param function The function where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changed could not be saved to the database.
   */
  public synchronized IComment editGlobalFunctionComment(
      final INaviFunction function, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(function, "IE02586: function argument can not be null");
    return editComment(new FunctionCommentingStrategy(function), oldComment, commentText);
  }

  /**
   * Edits a global instruction comment.
   *
   * @param instruction The instruction where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  public synchronized IComment editGlobalInstructionComment(
      final INaviInstruction instruction, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(instruction, "IE02587: instruction argument can not be null");
    return editComment(new InstructionCommentingStrategy(instruction, null, CommentScope.GLOBAL),
        oldComment, commentText);
  }

  /**
   * Edits a group node comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited group node comments are
   * send from the database.
   *
   * @param node The {@link INaviGroupNode} where the comment is edited.
   * @param comment The {@link IComment} which is edited.
   * @param newComment The {@link IComment} which will replace the old comment.
   * @param scope The {@link CommentScope} of the comment.
   */
  public synchronized void editGroupNodeComment(final INaviGroupNode node, final IComment comment,
      final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(node, "IE02588: node argument can not be null");
    editCommentInternal(new GroupNodeCommentingStrategy(node), comment, newComment);
  }

  /**
   * Edits a group node comment.
   *
   * @param node The group node where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changed could not be saved to the database.
   */
  public synchronized IComment editGroupNodeComment(
      final INaviGroupNode node, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(node, "IE02589: function argument can not be null");
    return editComment(new GroupNodeCommentingStrategy(node), oldComment, commentText);
  }

  /**
   * Edits an instruction comment depending.
   *
   * @param instruction The instruction where the comment is edited.
   * @param node The node where the comment resides in the case a local instruction comment is
   *        edited.
   * @param comment The comment which is edited.
   * @param newComment The new comment text.
   * @param scope The scope of the edit either {@link CommentScope} LOCAL or GLOBAL.
   */
  public synchronized void editInstructionComment(final INaviInstruction instruction,
      final INaviCodeNode node, final IComment comment, final IComment newComment,
      final CommentScope scope) {
    Preconditions.checkNotNull(instruction, "IE02590: instruction argument can not be null");
    editCommentInternal(
        new InstructionCommentingStrategy(instruction, node, scope), comment, newComment);
  }

  /**
   * Edits a local comment from a code node.
   *
   * @param node The code node where the local comment is edited
   * @param comment The comment which is getting edited.
   * @param commentText The text to be inserted into the comment.
   *
   * @return The edited comment.
   *
   * @throws CouldntSaveDataException if the changes could not be saved in the database.
   */
  public synchronized IComment editLocalCodeNodeComment(
      final INaviCodeNode node, final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(node, "IE02591: codeNode argument can not be null");
    return editComment(
        new CodeNodeCommentingStrategy(node, CommentScope.LOCAL), comment, commentText);
  }

  /**
   * Edits a local comment from an edge.
   *
   * @param edge The edge where the local comment is edited.
   * @param editedComment The comment which is edited.
   * @param commentText The text to be inserted into the comment.
   *
   * @return The edited comment.
   *
   * @throws CouldntSaveDataException if the changes could not be saved in the database.
   */
  public synchronized IComment editLocalEdgeComment(
      final INaviEdge edge, final IComment editedComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(edge, "IE02592: edge argument can not be null");
    return editComment(
        new EdgeCommentingStrategy(edge, CommentScope.LOCAL), editedComment, commentText);
  }

  /**
   * Edits a local comment associated to an instruction.
   *
   * @param node The code node where the instruction is located in.
   * @param instruction The instruction whose local comment is edited.
   * @param comment The comment which is edited.
   * @param commentText The comment text to be inserted into the comment.
   *
   * @return The edited comment.
   *
   * @throws CouldntSaveDataException if the changes could not be stored in the database.
   */
  public synchronized IComment editLocalInstructionComment(final INaviCodeNode node,
      final INaviInstruction instruction, final IComment comment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(instruction, "IE00100: Instruction argument can not be null");
    return editComment(new InstructionCommentingStrategy(instruction, node, CommentScope.LOCAL),
        comment, commentText);
  }

  /**
   * Edits a text node comment in the comment manager without propagation of the changes to the
   * database. This function is used when notifications about and edited text node comments are send
   * from the database.
   *
   * @param node The {@link INaviTextNode} where the comment is edited.
   * @param comment The {@link IComment} which will be edited.
   * @param newComment The {@link IComment} which will replace the old comment.
   * @param scope THe {@link CommentScope} of the comment.
   */
  public synchronized void editTextNodeComment(final INaviTextNode node, final IComment comment,
      final IComment newComment, final CommentScope scope) {
    Preconditions.checkNotNull(node, "IE02593: node argument can not be null");
    editCommentInternal(new TextNodeCommentingStrategy(node), comment, newComment);
  }

  /**
   * Edits a text node comment.
   *
   * @param node The text node where the comment is edited.
   * @param commentText The comment which is edited.
   *
   * @throws CouldntSaveDataException if the changed could not be saved to the database.
   */
  public synchronized IComment editTextNodeComment(
      final INaviTextNode node, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(node, "IE02594: function argument can not be null");
    return editComment(new TextNodeCommentingStrategy(node), oldComment, commentText);
  }

  /**
   * Edits a type instance comment.
   *
   * @param typeInstance The {@link TypeInstance} where the {@link IComment} is edited.
   * @param oldComment The {@link IComment} which is edited.
   * @param commentText The new comment {@link String}
   *
   * @return The edited {@link IComment}.
   *
   * @throws CouldntSaveDataException if the {@link IComment} could not be edited in the database.
   */
  public synchronized IComment editTypeInstanceComment(
      final TypeInstance typeInstance, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(typeInstance, "Error: type instance argument can not be null");
    return editComment(new TypeInstanceCommentingStrategy(typeInstance), oldComment, commentText);
  }

  /**
   * Edits a {@link TypeInstance type instance} {@link IComment comment}.
   *
   * @param typeInstance The {@link TypeInstance type instance} where the {@link IComment comment}
   *        is edited.
   * @param currentComment The current {@link IComment comment} of the {@link TypeInstance type
   *        instance}.
   * @param newComment The donor {@link IComment comment} of the {@link TypeInstance type instance}.
   */
  public synchronized void editTypeInstanceComment(
      final TypeInstance typeInstance, final IComment currentComment, final IComment newComment) {
    Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null");
    editCommentInternal(
        new TypeInstanceCommentingStrategy(typeInstance), currentComment, newComment);
  }

  /**
   * Edits a section comment.
   *
   * @param section The {@link Section} where the {@link IComment} is edited.
   * @param oldComment The {@link IComment} which is edited.
   * @param commentText The new comment {@link String}.
   *
   * @return The edited {@link IComment}.
   *
   * @throws CouldntSaveDataException if the {@link IComment} could not be edited in the database.
   */
  public synchronized IComment editSectionComment(
      final Section section, final IComment oldComment, final String commentText)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    return editComment(new SectionCommentingStrategy(section), oldComment, commentText);
  }

  /**
   * This function determines a comment by its id if it is known to the comment manager.
   *
   * @param commentId The comment id to look up.
   * @return The {@link IComment} if it is known to the comment manager.
   */
  public synchronized IComment getCommentById(final Integer commentId) {
    if (commentId == null) {
      return null;
    }
    return commentIdToComment.get(commentId);
  }

  /**
   * This function return a {@link INaviFunction} if the given comment is associated with a function
   * otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviFunction} the comment is associated to if found.
   */
  public synchronized INaviFunction getCommentedFunction(final IComment comment) {
    return functionCommentContainer.getCommented(comment);
  }

  /**
   * This function return a {@link INaviFunctionNode} if the given comment is associated with a
   * function node otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviFunctionNode} the comment is associated to if found.
   */
  public synchronized INaviFunctionNode getCommentedFunctionNode(final IComment comment) {
    return functionNodeCommentContainer.getCommented(comment);
  }

  /**
   * This function return a {@link INaviCodeNode} if the given comment is associated with a function
   * code node null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviCodeNode} the comment is associated to if found.
   */
  public synchronized INaviCodeNode getCommentedGlobalCodeNode(final IComment comment) {
    return globalCodeNodeCommentContainer.getCommented(comment);
  }

  /**
   * This function return a {@link INaviEdge} if the given comment is associated with a function
   * edge null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviEdge} the comment is associated to if found.
   */
  public synchronized INaviEdge getCommentedGlobalEdge(final IComment comment) {
    return globalEdgeCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an {@link INaviInstruction} if the given comment is associated with an
   * instruction otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviInstruction} the comment is associated to if found.
   */
  public synchronized INaviInstruction getCommentedGlobalInstruction(final IComment comment) {
    return globalInstructionsCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an {@link INaviGroupNode} if the given comment is associated with an
   * group node otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviGroupNode} the comment is associated to if found.
   */
  public synchronized INaviGroupNode getCommentedGroupNode(final IComment comment) {
    return groupNodeCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an {@link INaviCodeNode} if the given comment is associated with an code
   * node otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviCodeNode} the comment is associated to if found.
   */
  public synchronized INaviCodeNode getCommentedLocalCodeNode(final IComment comment) {
    return localCodeNodeCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an {@link INaviEdge} if the given comment is associated with an edge
   * otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviEdge} the comment is associated to if found.
   */
  public synchronized INaviEdge getCommentedLocalEdge(final IComment comment) {
    return localEdgeCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an Pair<INaviCodeNode, INaviInstruction> if the given comment is
   * associated with an instruction otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The Pair<INaviCodeNode, INaviInstruction> the comment is associated to if found.
   */
  public synchronized Pair<INaviCodeNode, INaviInstruction> getCommentedLocalInstruction(
      final IComment comment) {
    return localInstructionCommentContainer.getCommented(comment);
  }

  /**
   * This function returns an {@link INaviTextNode} if the given comment is associated with an text
   * node otherwise null.
   *
   *  This function exists as it is necessary to have a fast way to determine the object associated
   * with a comment.
   *
   * @param comment The comment for which we want to know its associated object.
   * @return The {@link INaviTextNode} the comment is associated to if found.
   */
  public synchronized INaviTextNode getCommentedTextNode(final IComment comment) {
    return textNodeCommentContainer.getCommented(comment);
  }

  /**
   * Returns the {@link TypeInstance type instance} to which a specific {@link IComment comment} is
   * associated to.
   *
   * @param comment The {@link IComment comment} to which the {@link TypeInstance type instance} is
   *        determined.
   */
  public synchronized TypeInstance getCommentedTypeInstance(final IComment comment) {
    return typeInstanceCommentContainer.getCommented(comment);
  }

  /**
   * Returns the comment for a given function node.
   *
   * @param functionNode The function node whose comment is determined.
   *
   * @return The comment of the given function node.
   */
  public synchronized List<IComment> getFunctionNodeComment(final INaviFunctionNode functionNode) {
    Preconditions.checkNotNull(functionNode, "IE02595: functionNode argument can not be null");
    return functionNodeCommentContainer.getComments(functionNode);
  }

  /**
   * Returns the global comment for a given code node.
   *
   * @param node The code node whose global comment is determined.
   *
   * @return The global comment associated with the given code node.
   */
  public synchronized List<IComment> getGlobalCodeNodeComment(final INaviCodeNode node) {
    Preconditions.checkNotNull(node, "IE00090: Code node argument can not be null");
    return globalCodeNodeCommentContainer.getComments(node);
  }

  /**
   * Returns the global comment for a given edge.
   *
   * @param edge The code node whose global comment is determined.
   *
   * @return The global comment associated with the given edge.
   */
  public synchronized List<IComment> getGlobalEdgeComment(final INaviEdge edge) {
    Preconditions.checkNotNull(edge, "IE00092: Edge argument can not be null");
    return globalEdgeCommentContainer.getComments(edge);
  }


  /**
   * Returns the global comment for a given function.
   *
   * @param function The function whose global comment is determined.
   *
   * @return The global comment associated with the given function.
   */
  public synchronized List<IComment> getGlobalFunctionComment(final INaviFunction function) {
    Preconditions.checkNotNull(function, "IE00091: Function argument can not be null");
    return functionCommentContainer.getComments(function);
  }

  /**
   * Returns the global comment for a given instruction.
   *
   * @param instruction The instruction whose global comment is determined.
   *
   * @return The global comment associated with the given instruction.
   */
  public synchronized List<IComment> getGlobalInstructionComment(
      final INaviInstruction instruction) {
    Preconditions.checkNotNull(instruction, "IE00093: Instruction argument can not be null");
    return globalInstructionsCommentContainer.getComments(instruction);
  }

  /**
   * Returns the comment for a given group node.
   *
   * @param node The group node whose local comment is determined.
   *
   * @return The comment associated with the given group node.
   */
  public synchronized List<IComment> getGroupNodeComment(final INaviGroupNode node) {
    Preconditions.checkNotNull(node, "IE02596: node argument can not be null");
    return groupNodeCommentContainer.getComments(node);
  }

  /**
   * Returns the local comment for a given code node.
   *
   * @param node The code node whose local comment is determined.
   *
   * @return The local comment associated with the given code node.
   */
  public synchronized List<IComment> getLocalCodeNodeComment(final INaviCodeNode node) {
    Preconditions.checkNotNull(node, "IE02597: node argument can not be null");
    return localCodeNodeCommentContainer.getComments(node);
  }

  /**
   * Returns the local comment for a given edge.
   *
   * @param edge The edge whose local comment is determined.
   *
   * @return The local comment associated with the edge.
   */
  public synchronized List<IComment> getLocalEdgeComment(final INaviEdge edge) {
    Preconditions.checkNotNull(edge, "IE02598: edge argument can not be null");
    return localEdgeCommentContainer.getComments(edge);
  }

  /**
   * Returns the local comment for a given instruction.
   *
   * @param instruction The instruction whose global comment is determined.
   * @param codeNode The code node where the instruction is located.
   *
   * @return The local comment associated with the given instruction.
   */
  public synchronized List<IComment> getLocalInstructionComment(
      final INaviInstruction instruction, final INaviCodeNode codeNode) {
    Preconditions.checkNotNull(instruction, "IE02599: instruction argument can not be null");
    Preconditions.checkNotNull(codeNode, "IE02600: codeNode argument can not be null");
    return localInstructionCommentContainer.getComments(
        new Pair<INaviCodeNode, INaviInstruction>(codeNode, instruction));
  }

  /**
   * Returns the comment for a given section.
   *
   * @param section The section whose comment is determined.
   *
   * @return The comment associated with the given section.
   */
  public synchronized List<IComment> getSectionComments(final Section section) {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    return sectionCommentContainer.getComments(section);
  }

  /**
   * Returns the comment for a given text node.
   *
   * @param node The text node whose local comment is determined.
   *
   * @return The comment associated with the given text node.
   */
  public synchronized List<IComment> getTextNodeComment(final INaviTextNode node) {
    Preconditions.checkNotNull(node, "IE02601: node argument can not be null");
    return textNodeCommentContainer.getComments(node);
  }

  /**
   * Returns the comment for a given type instance.
   *
   * @param instance The type instance whose comment is determined.
   *
   * @return The comment associated with the given type instance.
   */
  public synchronized List<IComment> getTypeInstanceComments(final TypeInstance instance) {
    Preconditions.checkNotNull(instance, "Error: type instance argument can not be null");
    return typeInstanceCommentContainer.getComments(instance);
  }

  /**
   * Initializes the comment of a function node. When this function is called, , the given comments
   * are not stored in the database again.
   *
   * @param functionNode The function node whose global comment is initialized.
   * @param comments The initial global comment of the function.
   */
  public synchronized void initializeFunctionNodeComment(
      final INaviFunctionNode functionNode, final List<IComment> comments) {
    Preconditions.checkNotNull(functionNode, "IE00096: Function argument can not be null");
    initializeComment(new FunctionNodeCommentingStrategy(functionNode), comments);
  }

  /**
   * Unloads the List of {@link IComment comments} from a {@link INaviFunctionNode function node}.
   *
   * @param functionNode The {@link INaviFunctionNode} where the {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} to be unloaded.
   */
  public synchronized void unloadFunctionNodeComment(
      final INaviFunctionNode functionNode, final List<IComment> comments) {
    Preconditions.checkNotNull(functionNode, "Error: functionNode argument can not be null");
    unloadComment(new FunctionNodeCommentingStrategy(functionNode), comments);
  }

  /**
   * Initializes the global comment of a code node. When this function is called, , the given
   * comments are not stored in the database again.
   *
   * @param codeNode The code node whose global comment is initialized.
   * @param comments The initial global comment of the code node.
   */
  public synchronized void initializeGlobalCodeNodeComment(
      final INaviCodeNode codeNode, final List<IComment> comments) {
    Preconditions.checkNotNull(codeNode, "IE00094: Code node argument can not be null");
    initializeComment(new CodeNodeCommentingStrategy(codeNode, CommentScope.GLOBAL), comments);
  }

  /**
   * Unload {@link CommentScope global} {@link INaviCodeNode code node} {@link IComment comments}.
   *
   * @param codeNode The {@link INaviCodeNode code node} where the {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} which are unloaded.
   */
  public synchronized void unloadGlobalCodeNodeComment(
      final INaviCodeNode codeNode, final List<IComment> comments) {
    Preconditions.checkNotNull(codeNode, "Error: codeNode argument can not be null");
    unloadComment(new CodeNodeCommentingStrategy(codeNode, CommentScope.GLOBAL), comments);
  }

  /**
   * Initializes the global comment of an edge. When this function is called, , the given comments
   * are not stored in the database again.
   *
   * @param edge The edge whose global comment is initialized.
   * @param comments The initial global comment of the edge.
   */
  public synchronized void initializeGlobalEdgeComment(
      final INaviEdge edge, final List<IComment> comments) {
    Preconditions.checkNotNull(edge, "IE00098: Edge argument can not be null");
    initializeComment(new EdgeCommentingStrategy(edge, CommentScope.GLOBAL), comments);
  }

  /**
   * Unload {@link CommentScope global} {@link INaviEdge edge} {@link IComment comments}.
   *
   * @param edge The {@link INaviEdge edge} where the {@link IComment comments} are unloaded.
   * @param comments The {@link IComment comments} which are unloaded.
   */
  public synchronized void unloadGlobalEdgeComment(
      final INaviEdge edge, final List<IComment> comments) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
    unloadComment(new EdgeCommentingStrategy(edge, CommentScope.GLOBAL), comments);
  }

  /**
   * Initializes the global comment of a function. When this function is called, , the given
   * comments are not stored in the database again.
   *
   * @param function The function whose global comment is initialized.
   * @param comments The initial global comment of the function.
   */
  public synchronized void initializeGlobalFunctionComment(
      final INaviFunction function, final List<IComment> comments) {
    Preconditions.checkNotNull(function, "IE00096: Function argument can not be null");
    initializeComment(new FunctionCommentingStrategy(function), comments);
  }

  /**
   * Unload {@link INaviFunction function} {@link IComment comments}.
   *
   * @param function The {@link INaviFunction function} where the {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} which are unloaded.
   */
  public synchronized void unloadGlobalFunctionComment(
      final INaviFunction function, final List<IComment> comments) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    unloadComment(new FunctionCommentingStrategy(function), comments);
  }

  /**
   * Initializes the global comment of an instruction. When this function is called, , the given
   * comments are not stored in the database again.
   *
   * @param instruction The instruction whose global comment is initialized.
   * @param comments The initial global comment of the instruction.
   */
  public synchronized void initializeGlobalInstructionComment(
      final INaviInstruction instruction, final List<IComment> comments) {
    Preconditions.checkNotNull(instruction, "IE00100: Instruction argument can not be null");
    initializeComment(
        new InstructionCommentingStrategy(instruction, null, CommentScope.GLOBAL), comments);
  }

  /**
   * Unload {@link CommentScope global} {@link INaviInstruction instruction} {@link IComment
   * comments}
   *
   * @param instruction The {@link INaviInstruction instruction} where the {@link IComment comments}
   *        are unloaded.
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadGlobalInstructionComment(
      final INaviInstruction instruction, final List<IComment> comments) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    unloadComment(
        new InstructionCommentingStrategy(instruction, null, CommentScope.GLOBAL), comments);
  }

  /**
   * Initializes the comment of a group node. When this function is called, , the given comments are
   * not stored in the database again.
   *
   * @param groupNode The function node whose global comment is initialized.
   * @param comments The initial global comment of the function.
   */
  public synchronized void initializeGroupNodeComment(
      final INaviGroupNode groupNode, final List<IComment> comments) {
    Preconditions.checkNotNull(groupNode, "IE00096: Function argument can not be null");
    initializeComment(new GroupNodeCommentingStrategy(groupNode), comments);
  }

  /**
   * Unload {@link INaviGroupNode group node} {@link IComment comments}.
   *
   * @param groupNode The {@link INaviGroupNode group node} where the {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadGroupNodeComment(
      final INaviGroupNode groupNode, final List<IComment> comments) {
    Preconditions.checkNotNull(groupNode, "Error: groupNode argument can not be null");
    unloadComment(new GroupNodeCommentingStrategy(groupNode), comments);
  }

  /**
   * Initializes the local comment of a code node. When this function is called, , the given
   * comments are not stored in the database again.
   *
   * @param codeNode The code node whose local comment is initialized.
   * @param comments The initial local comment of the code node.
   */
  public synchronized void initializeLocalCodeNodeComment(
      final INaviCodeNode codeNode, final List<IComment> comments) {
    Preconditions.checkNotNull(codeNode, "IE00094: Code node argument can not be null");
    initializeComment(new CodeNodeCommentingStrategy(codeNode, CommentScope.LOCAL), comments);
  }

  /**
   * Unload {@link CommentScope local} {@link INaviCodeNode code node} {@link IComment comments}
   *
   * @param codeNode The {@link INaviCodeNode code node} where the {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadLocalCodeNodeComment(
      final INaviCodeNode codeNode, final List<IComment> comments) {
    Preconditions.checkNotNull(codeNode, "Error: codeNode argument can not be null");
    unloadComment(new CodeNodeCommentingStrategy(codeNode, CommentScope.LOCAL), comments);
  }

  /**
   * Initializes the local comment of an edge. When this function is called, , the given comments
   * are not stored in the database again.
   *
   * @param edge The edge whose local comment is initialized.
   * @param comments The local comments to initialize the edge with.
   */
  public synchronized void initializeLocalEdgeComment(
      final INaviEdge edge, final List<IComment> comments) {
    Preconditions.checkNotNull(edge, "IE02602: edge argument can not be null");
    initializeComment(new EdgeCommentingStrategy(edge, CommentScope.LOCAL), comments);
  }

  /**
   * Unload {@link CommentScope local} {@link INaviEdge edge} {@link IComment comments}.
   *
   * @param edge The {@link INaviEdge edge} where the {@link IComment comments} are unloaded
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadLocalEdgeComment(
      final INaviEdge edge, final List<IComment> comments) {
    Preconditions.checkNotNull(edge, "Error: edge argument can not be null");
    unloadComment(new EdgeCommentingStrategy(edge, CommentScope.LOCAL), comments);
  }

  /**
   * Initializes the local comment of an instruction. When this function is called, , the given
   * comments are not stored in the database again.
   *
   * @param instruction The instruction whose global comment is initialized.
   * @param node The code node the instruction is located in.
   * @param comments The initial {@link List list} of local {@link IComment comments} of the
   *        instruction.
   */
  public synchronized void initializeLocalInstructionComment(
      final INaviCodeNode node, final INaviInstruction instruction, final List<IComment> comments) {
    Preconditions.checkNotNull(instruction, "IE00100: Instruction argument can not be null");
    initializeComment(
        new InstructionCommentingStrategy(instruction, node, CommentScope.LOCAL), comments);
  }

  /**
   * Unload {@link CommentScope local} {@link INaviInstruction instruction} {@link IComment
   * comments}.
   *
   * @param node The {@link INaviCodeNode code node} where the {@link INaviInstruction instruction}
   *        is located in.
   * @param instruction The {@link INaviInstruction instruction} where the {@link IComment comments}
   *        are unloaded.
   * @param comments The {@link IComment comments} to be unloaded.
   */
  public synchronized void unloadLocalnstructionComment(
      final INaviCodeNode node, final INaviInstruction instruction, final List<IComment> comments) {
    Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null");
    unloadComment(
        new InstructionCommentingStrategy(instruction, node, CommentScope.LOCAL), comments);
  }

  /**
   * Initializes the comment of a text node. When this function is called, , the given comments are
   * not stored in the database again.
   *
   * @param textnode The text node whose global comment is initialized.
   * @param comments The initial global comment of the function.
   */
  public synchronized void initializeTextNodeComment(
      final INaviTextNode textnode, final List<IComment> comments) {
    Preconditions.checkNotNull(textnode, "IE00096: text node argument can not be null");
    initializeComment(new TextNodeCommentingStrategy(textnode), comments);
  }

  /**
   * Unloads the {@link IComment comments} of a {@link INaviTextNode text node}.
   *
   * @param textnode The {@link INaviTextNode text node} whose {@link IComment comments} are
   *        unloaded.
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadTextNodeComment(
      final INaviTextNode textnode, final List<IComment> comments) {
    Preconditions.checkNotNull(textnode, "Error: textnode argument can not be null");
    unloadComment(new TextNodeCommentingStrategy(textnode), comments);
  }

  /**
   * Initializes the comment of a type instance. When this function is called, the given comments
   * are not stored in the database again.
   *
   * @param typeInstance The {@link TypeInstance type instance} whose {@link IComment comments} are
   *        initialized.
   * @param comments The {@link IComment comments} to initialize.
   */
  public synchronized void initializeTypeInstanceComment(
      final TypeInstance typeInstance, final List<IComment> comments) {
    Preconditions.checkNotNull(typeInstance, "Error: type instance argument can not be null");
    initializeComment(new TypeInstanceCommentingStrategy(typeInstance), comments);
  }

  /**
   * Unloads the {@link IComment comments} of a {@link TypeInstance type instance}.
   *
   * @param typeInstance The {@link TypeInstance} whose {@link IComment comments} are unloaded.
   * @param comments The {@link IComment comments} that are unloaded.
   */
  public synchronized void unloadTypeInstanceComment(
      final TypeInstance typeInstance, final List<IComment> comments) {
    Preconditions.checkNotNull(typeInstance, "Error: typeInstance argument can not be null");
    unloadComment(new TypeInstanceCommentingStrategy(typeInstance), comments);
  }

  /**
   * Initializes the comment of a section. When this function is called, the given comments are not
   * stored in the database again.
   *
   * @param section The section whose comment is initialized.
   * @param comments The initial comments of the section.
   */
  public synchronized void initializeSectionComment(
      final Section section, final List<IComment> comments) {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    initializeComment(new SectionCommentingStrategy(section), comments);
  }

  /**
   * Unloads the {@link IComment comments} of a {@link Section section}.
   *
   * @param section The {@link Section} where the {@link IComment comments} are unloaded
   * @param comments The {@link IComment} that are unloaded
   */
  public synchronized void unloadSectionComment(
      final Section section, final List<IComment> comments) {
    Preconditions.checkNotNull(section, "Error: section argument can not be null");
    unloadComment(new SectionCommentingStrategy(section), comments);
  }

  /**
   * Removes a listener object from the global comment dialog.
   *
   * @param listener The listener object to remove.
   */
  public synchronized void removeListener(final CommentListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Internal listener class to be informed about provider changes.
   */
  private class InternalSQLProviderListener implements SQLProviderListener {

    @Override
    public void providerClosing(SQLProvider provider) {
      if (CommentManager.this.provider.equals(provider)) {
        CommentManager.this.close();
      }
    }
  }

  /**
   * This class abstracts the access to commenting functions on edges.
   */
  private class CodeNodeCommentingStrategy implements CommentingStrategy {

    private final INaviCodeNode codeNode;
    private final CommentScope scope;

    public CodeNodeCommentingStrategy(final INaviCodeNode node, final CommentScope currentScope) {
      codeNode = Preconditions.checkNotNull(node, "IE02603: node argument can not be null");
      scope = Preconditions.checkNotNull(
          currentScope, "IE02604: currentScope argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId =
          scope.equals(CommentScope.GLOBAL) ? provider.appendGlobalCodeNodeComment(
              codeNode, commentText, userId)
              : provider.appendLocalCodeNodeComment(codeNode, commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.deleteGlobalCodeNodeComment(codeNode, commentId, userId);
      } else {
        provider.deleteLocalCodeNodeComment(codeNode, commentId, userId);
      }
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.editGlobalCodeNodeComment(codeNode, id, userId, commentText);
      } else {
        provider.editLocalCodeNodeComment(codeNode, id, userId, commentText);
      }
    }

    @Override
    public List<IComment> getComments() {
      return scope.equals(CommentScope.GLOBAL) ? getGlobalCodeNodeComment(codeNode)
          : getLocalCodeNodeComment(codeNode);
    }

    @Override
    public boolean isStored() {
      return codeNode.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalCodeNodeCommentContainer.setComments(codeNode, comments);
      } else {
        localCodeNodeCommentContainer.setComments(codeNode, comments);
      }
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.appendedGlobalCodeNodeComment(codeNode, comment);
      } else {
        listener.appendedLocalCodeNodeComment(codeNode, comment);
      }
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.deletedGlobalCodeNodeComment(codeNode, comment);
      } else {
        listener.deletedLocalCodeNodeComment(codeNode, comment);
      }
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.editedGLobalCodeNodeComment(codeNode, comment);
      } else {
        listener.editedLocalCodeNodeComment(codeNode, comment);
      }
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.initializedGlobalCodeNodeComments(codeNode, comments);
      } else {
        listener.initializedLocalCodeNodeComments(codeNode, comments);
      }
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalCodeNodeCommentContainer.unsetComments(codeNode, comments);
      } else {
        localCodeNodeCommentContainer.unsetComments(codeNode, comments);
      }
    }
  }

  /**
   * This class provides a container for comments internally in the comment manager.
   *
   * It provides methods to set the comments of a new {@link CommentContainer} and for the lookup of
   * either the list of CommentType associated to a CommentableType object or to lookup the
   * CommentableType by CommentType.
   *
   * @param <CommentableType> The object type to which a comment can be associated.
   * @param <CommentType> The object type of a comment which can be associated.
   */
  private final class CommentContainer<CommentableType, CommentType> {

    /**
     * Hashmap for the lookup of comments which are associated to an object which can be commented.
     */
    private final Map<CommentableType, List<CommentType>> commentableToComment = Maps.newHashMap();

    /**
     * Hashmap for the lookup of objects which can be commented by comments.
     */
    private final Map<CommentType, CommentableType> commentToCommentable = Maps.newHashMap();

    /**
     * Retrieve the commented object to which the comment is associated.
     *
     * @param comment The CommentType object to reverse lookup.
     *
     * @return The CommentableType object to which the comment is associated.
     */
    public CommentableType getCommented(final CommentType comment) {
      return commentToCommentable.get(comment);
    }

    /**
     * Retrieve the list of CommentType objects which are associated to an CommentableType.
     *
     * @param commentable The CommentableType to get the list of CommentType for.
     *
     * @return The list of CommentType associated to the CommentableType.
     */
    public List<CommentType> getComments(final CommentableType commentable) {
      return commentableToComment.get(commentable);
    }

    /**
     * Set the comments for a CommentableType with a list of CommentType objects.
     *
     * @param commentable The CommentableType to associate with the list of CommentType objects.
     * @param comments The list of CommentType objects to associate with the CommentableType.
     */
    public void setComments(final CommentableType commentable, final List<CommentType> comments) {
      commentableToComment.put(commentable, comments);
      for (final CommentType commentType : comments) {
        commentToCommentable.put(commentType, commentable);
      }
    }

    /**
     * Unset the comments for a CommentableType.
     *
     * @param commentable The CommentableType which will be purged from the internal storage.
     * @param comments The CommentType objects to be removed from the internal storage.
     */
    public void unsetComments(final CommentableType commentable, final List<CommentType> comments) {
      commentableToComment.remove(commentable);
      for (final CommentType commentType : comments) {
        commentToCommentable.remove(commentType);
      }
    }
  }

  /**
   * This interface delegates all methods for commenting to the appropriate functions of the
   * contained object.
   */
  private interface CommentingStrategy {

    /**
     * Function which forwards the comment appending to the appropriate function of the contained
     * object.
     *
     * @param commentText The text of the comment to append.
     * @param userId The id of the currently active user.
     * @return The generated comment id from the database.
     *
     * @throws CouldntSaveDataException if the comment could not be stored to the database.
     * @throws CouldntLoadDataException if the list of comments now associated with the commented
     *         object could not be loaded from the database.
     */
    List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException;

    void removeComments(List<IComment> comments);

    /**
     * Function which forwards the comment deletion to the appropriate function of the contained
     * object.
     *
     * @param commentId The id of the comment to delete.
     * @param userId The id of the currently active user.
     *
     * @throws CouldntDeleteException if the comment could not be deleted from the database.
     */
    void deleteComment(final Integer commentId, final Integer userId) throws CouldntDeleteException;

    /**
     * Function which forwards the comment editing to the appropriate function of the contained
     * object.
     *
     * @param commentId The id of the comment which is getting edited.
     * @param userId The user id of the currently active user.
     * @param commentText The text which will be stored in the edited comment.
     *
     * @throws CouldntSaveDataException if the comment changes could not be stored in the database.
     */
    void editComment(final Integer commentId, final int userId, final String commentText)
        throws CouldntSaveDataException;

    /**
     * Function which forwards the comment getter to the appropriate function of the contained
     * object.
     *
     * @return The list of comments currently associated to the object contained.
     */
    List<IComment> getComments();

    /**
     * Determines if the object in question is stored in the database or not.
     *
     * @return true if the object is stored in the database.
     */
    boolean isStored();

    /**
     * Function which forwards the comment saving to the appropriate function of the contained
     * object.
     *
     * @param comments The list of comments to associate with the contained object.
     */
    void saveComments(final List<IComment> comments);

    /**
     * Function which which forwards the notification of appending to the appropriate function of
     * the contained object
     *
     * @param listener The listener to call the notification function in.
     * @param comment The comment which has been appended.
     */
    void sendAppendedCommentNotifcation(CommentListener listener, IComment comment);

    /**
     * Function which forwards the notification of deletion to the appropriate function of the
     * contained object
     *
     * @param listener The listener to call the notification function in.
     * @param comment The comment which was deleted.
     */
    void sendDeletedCommentNotification(final CommentListener listener, final IComment comment);

    /**
     * Function which forwards the notification of editing to the appropriate function of the
     * contained object
     *
     * @param listener The listener to call the notification function in.
     * @param comment The comment which was edited.
     */
    void sendEditedCommentNotification(CommentListener listener, IComment comment);

    /**
     * Function which forwards the notification of initializing to the appropriate function of the
     * contained object.
     *
     * @param listener The listener to call the notification function in.
     * @param comments The comment which was edited.
     */
    void sendInitializedCommentNotification(CommentListener listener, List<IComment> comments);
  }

  /**
   * This class abstracts the access to commenting functions on edges.
   */
  private class EdgeCommentingStrategy implements CommentingStrategy {

    private final INaviEdge edge;
    private final CommentScope scope;

    public EdgeCommentingStrategy(final INaviEdge currentEdge, final CommentScope currentScope) {
      edge = Preconditions.checkNotNull(currentEdge, "IE02605: edge argument can not be null");
      scope = Preconditions.checkNotNull(
          currentScope, "IE02606: currentScope argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId =
          scope.equals(CommentScope.GLOBAL) ? provider.appendGlobalEdgeComment(
              edge, commentText, userId)
              : provider.appendLocalEdgeComment(edge, commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.deleteGlobalEdgeComment(edge, commentId, userId);
      } else {
        provider.deleteLocalEdgeComment(edge, commentId, userId);
      }
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.editGlobalEdgeComment(edge, id, userId, commentText);
      } else {
        provider.editLocalEdgeComment(edge, id, userId, commentText);
      }
    }

    @Override
    public List<IComment> getComments() {
      return scope.equals(CommentScope.GLOBAL) ? getGlobalEdgeComment(edge)
          : getLocalEdgeComment(edge);
    }

    @Override
    public boolean isStored() {
      return edge.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalEdgeCommentContainer.setComments(edge, comments);
      } else {
        localEdgeCommentContainer.setComments(edge, comments);
      }
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.appendedGlobalEdgeComment(edge, comment);
      } else {
        listener.appendedLocalEdgeComment(edge, comment);
      }
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.deletedGlobalEdgeComment(edge, comment);
      } else {
        listener.deletedLocalEdgeComment(edge, comment);
      }
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.editedGlobalEdgeComment(edge, comment);
      } else {
        listener.editedLocalEdgeComment(edge, comment);
      }
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.initializedGlobalEdgeComments(edge, comments);
      } else {
        listener.initializedLocalEdgeComments(edge, comments);
      }
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalEdgeCommentContainer.unsetComments(edge, comments);
      } else {
        localEdgeCommentContainer.unsetComments(edge, comments);
      }
    }
  }

  /**
   * This class abstracts the access to commenting functions on functions.
   */
  private class FunctionCommentingStrategy implements CommentingStrategy {

    private final INaviFunction function;

    public FunctionCommentingStrategy(final INaviFunction currentFunction) {
      function =
          Preconditions.checkNotNull(currentFunction, "IE02607: node argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId = provider.appendFunctionComment(function, commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      provider.deleteFunctionComment(function, commentId, userId);
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      provider.editFunctionComment(function, id, userId, commentText);
    }

    @Override
    public List<IComment> getComments() {
      return getGlobalFunctionComment(function);
    }

    @Override
    public boolean isStored() {
      return true;
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      functionCommentContainer.setComments(function, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedGlobalFunctionComment(function, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedGlobalFunctionComment(function, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedGlobalFunctionComment(function, comment);
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedGlobalFunctionComments(function, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      functionCommentContainer.unsetComments(function, comments);
    }
  }

  /**
   * This class abstracts the access to commenting functions of sections.
   */
  private class SectionCommentingStrategy implements CommentingStrategy {

    private final Section section;

    public SectionCommentingStrategy(final Section section) {
      this.section = Preconditions.checkNotNull(section, "Error: section argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId = provider.appendSectionComment(
          section.getModule().getConfiguration().getId(), section.getId(), commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      provider.deleteSectionComment(
          section.getModule().getConfiguration().getId(), section.getId(), commentId, userId);
    }

    @Override
    public void editComment(final Integer commentId, final int userId, final String commentText)
        throws CouldntSaveDataException {
      provider.editSectionComment(section.getModule().getConfiguration().getId(), section.getId(),
          commentId, userId, commentText);
    }

    @Override
    public List<IComment> getComments() {
      return sectionCommentContainer.getComments(section);
    }

    @Override
    public boolean isStored() {
      return true;
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      sectionCommentContainer.setComments(section, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedSectionComment(section, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedSectionComment(section, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedSectionComment(section, comment);

    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedSectionComments(section, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      sectionCommentContainer.unsetComments(section, comments);
    }
  }

  /**
   * This class abstracts the access to commenting functions on functions.
   */
  private class FunctionNodeCommentingStrategy implements CommentingStrategy {

    private final INaviFunctionNode functionNode;

    public FunctionNodeCommentingStrategy(final INaviFunctionNode node) {
      functionNode = Preconditions.checkNotNull(node, "IE02608: node argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId = functionNode.isStored() ? provider.appendFunctionNodeComment(
          functionNode, commentText, userId)
          : null;
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      if (functionNode.isStored()) {
        provider.deleteFunctionNodeComment(functionNode, commentId, userId);
      }
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      if (functionNode.isStored()) {
        provider.editFunctionNodeComment(functionNode, id, userId, commentText);
      }
    }

    @Override
    public List<IComment> getComments() {
      return getFunctionNodeComment(functionNode);
    }

    @Override
    public boolean isStored() {
      return functionNode.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      functionNodeCommentContainer.setComments(functionNode, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedFunctionNodeComment(functionNode, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedFunctionNodeComment(functionNode, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedFunctionNodeComment(functionNode, comment);
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedFunctionNodeComments(functionNode, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      functionNodeCommentContainer.unsetComments(functionNode, comments);
    }
  }

  /**
   * This class abstracts the access to commenting functions on functions.
   */
  private class GroupNodeCommentingStrategy implements CommentingStrategy {

    private final INaviGroupNode groupNode;

    public GroupNodeCommentingStrategy(final INaviGroupNode node) {
      groupNode = Preconditions.checkNotNull(node, "IE02609: node argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId =
          groupNode.isStored() ? provider.appendGroupNodeComment(groupNode, commentText, userId)
              : null;
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      if (groupNode.isStored()) {
        provider.deleteGroupNodeComment(groupNode, commentId, userId);
      }
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      if (groupNode.isStored()) {
        provider.editGroupNodeComment(groupNode, id, userId, commentText);
      }
    }

    @Override
    public List<IComment> getComments() {
      return getGroupNodeComment(groupNode);
    }

    @Override
    public boolean isStored() {
      return groupNode.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      groupNodeCommentContainer.setComments(groupNode, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedGroupNodeComment(groupNode, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedGroupNodeComment(groupNode, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedGroupNodeComment(groupNode, comment);
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedGroupNodeComments(groupNode, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      groupNodeCommentContainer.unsetComments(groupNode, comments);
    }
  }

  /**
   * This class abstracts the access to commenting functions on instructions.
   */
  private class InstructionCommentingStrategy implements CommentingStrategy {

    private final INaviInstruction instruction;
    private final INaviCodeNode codeNode;
    private final CommentScope scope;

    public InstructionCommentingStrategy(final INaviInstruction currentInstruction,
        final INaviCodeNode node, final CommentScope currentScope) {
      instruction = Preconditions.checkNotNull(
          currentInstruction, "Error: currentInstruction argument can not be null");
      scope = Preconditions.checkNotNull(
          currentScope, "IE02610: currentScope argument can not be null");
      Preconditions.checkArgument(currentScope.equals(CommentScope.GLOBAL) || (node != null),
          "Error: a local comment scope requires a valid code node argument");
      codeNode = node;
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId =
          scope.equals(CommentScope.GLOBAL) ? provider.appendGlobalInstructionComment(
              instruction, commentText, userId)
              : provider.appendLocalInstructionComment(codeNode, instruction, commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.deleteGlobalInstructionComment(instruction, commentId, userId);
      } else {
        provider.deleteLocalInstructionComment(codeNode, instruction, commentId, userId);
      }
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      if (scope.equals(CommentScope.GLOBAL)) {
        provider.editGlobalInstructionComment(instruction, id, userId, commentText);
      } else {
        provider.editLocalInstructionComment(codeNode, instruction, id, userId, commentText);
      }
    }

    @Override
    public List<IComment> getComments() {
      return scope.equals(CommentScope.GLOBAL) ? getGlobalInstructionComment(instruction)
          : getLocalInstructionComment(instruction, codeNode);
    }

    @Override
    public boolean isStored() {
      return instruction.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalInstructionsCommentContainer.setComments(instruction, comments);
      } else {
        localInstructionCommentContainer.setComments(
            new Pair<INaviCodeNode, INaviInstruction>(codeNode, instruction), comments);
      }
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.appendedGlobalInstructionComment(instruction, comment);
      } else {
        listener.appendedLocalInstructionComment(codeNode, instruction, comment);
      }
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.deletedGlobalInstructionComment(instruction, comment);
      } else {
        listener.deletedLocalInstructionComment(codeNode, instruction, comment);
      }
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.editedGlobalInstructionComment(instruction, comment);
      } else {
        listener.editedLocalInstructionComment(codeNode, instruction, comment);
      }
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        listener.initializedGlobalInstructionComments(instruction, comments);
      } else {
        listener.initializedLocalInstructionComments(codeNode, instruction, comments);
      }
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      if (scope.equals(CommentScope.GLOBAL)) {
        globalInstructionsCommentContainer.unsetComments(instruction, comments);
      } else {
        localInstructionCommentContainer.unsetComments(
            new Pair<INaviCodeNode, INaviInstruction>(codeNode, instruction), comments);
      }
    }
  }

  /**
   * This class abstracts the access to commenting functions on functions.
   */
  private class TextNodeCommentingStrategy implements CommentingStrategy {

    private final INaviTextNode textNode;

    public TextNodeCommentingStrategy(final INaviTextNode node) {
      textNode = Preconditions.checkNotNull(node, "IE02611: node argument can not be null");
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId = provider.appendTextNodeComment(textNode, commentText, userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      provider.deleteTextNodeComment(textNode, commentId, userId);
    }

    @Override
    public void editComment(final Integer id, final int userId, final String commentText)
        throws CouldntSaveDataException {
      provider.editTextNodeComment(textNode, id, userId, commentText);
    }

    @Override
    public List<IComment> getComments() {
      return getTextNodeComment(textNode);
    }

    @Override
    public boolean isStored() {
      return textNode.isStored();
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      textNodeCommentContainer.setComments(textNode, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedTextNodeComment(textNode, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedTextNodeComment(textNode, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedTextNodeComment(textNode, comment);
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedTextNodeComments(textNode, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      textNodeCommentContainer.unsetComments(textNode, comments);
    }
  }

  /**
   * Implements the strategy for type instance comments.
   */
  private class TypeInstanceCommentingStrategy implements CommentingStrategy {

    private final TypeInstance typeInstance;

    public TypeInstanceCommentingStrategy(final TypeInstance instance) {
      this.typeInstance = instance;
    }

    @Override
    public List<IComment> appendComment(final String commentText, final Integer userId)
        throws CouldntSaveDataException, CouldntLoadDataException {
      final Integer commentId = provider.appendTypeInstanceComment(
          typeInstance.getModule().getConfiguration().getId(), typeInstance.getId(), commentText,
          userId);
      return provider.loadCommentById(commentId);
    }

    @Override
    public void deleteComment(final Integer commentId, final Integer userId)
        throws CouldntDeleteException {
      provider.deleteTypeInstanceComment(typeInstance.getModule().getConfiguration().getId(),
          typeInstance.getId(), commentId, userId);
    }

    @Override
    public void editComment(final Integer commentId, final int userId, final String commentText)
        throws CouldntSaveDataException {
      provider.editTypeInstanceComment(
          typeInstance.getModule().getConfiguration().getId(), commentId, userId, commentText);
    }

    @Override
    public List<IComment> getComments() {
      return typeInstanceCommentContainer.getComments(typeInstance);
    }

    @Override
    public boolean isStored() {
      return true;
    }

    @Override
    public void saveComments(final List<IComment> comments) {
      typeInstanceCommentContainer.setComments(typeInstance, comments);
    }

    @Override
    public void sendAppendedCommentNotifcation(
        final CommentListener listener, final IComment comment) {
      listener.appendedTypeInstanceComment(typeInstance, comment);
    }

    @Override
    public void sendDeletedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.deletedTypeInstanceComment(typeInstance, comment);
    }

    @Override
    public void sendEditedCommentNotification(
        final CommentListener listener, final IComment comment) {
      listener.editedTypeInstanceComment(typeInstance, comment);
    }

    @Override
    public void sendInitializedCommentNotification(
        final CommentListener listener, final List<IComment> comments) {
      listener.initializedTypeInstanceComment(typeInstance, comments);
    }

    @Override
    public void removeComments(final List<IComment> comments) {
      typeInstanceCommentContainer.unsetComments(typeInstance, comments);
    }
  }

  /**
   * Enumeration which kind of comment operation is currently performed.
   */
  public enum CommentOperation {
    APPEND, EDIT, DELETE;
  }

  /**
   * Enumeration to flag whether a comment is local or global.
   */
  public enum CommentScope {
    GLOBAL, LOCAL;
  }
}
