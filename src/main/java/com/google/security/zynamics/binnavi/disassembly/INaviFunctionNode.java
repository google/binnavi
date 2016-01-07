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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IFunctionNode;

import java.util.List;

/**
 * Interface that represents function nodes in views.
 */
public interface INaviFunctionNode
    extends INaviViewNode, IFunctionNode<INaviEdge, INaviFunctionNodeListener>, IAddressNode {

  /**
   * Appends a local function node comment to the list of comments associated with this function
   * node.
   *
   * @param commentText The text of the comment.
   * @return The newly generated comment.
   *
   * @throws CouldntSaveDataException if the new comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendLocalFunctionComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException;

  /**
   * Deletes a comment from the list of comments associated with this function node.
   *
   * @param comment The comment the be deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  void deleteLocalFunctionComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Edits a local comment which is associated to this function node.
   *
   * @param oldComment The comment which is edited.
   * @param commentText The new comment text.
   *
   * @return The new comment object.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  IComment editLocalFunctionComment(final IComment oldComment, final String commentText)
      throws CouldntSaveDataException;

  /**
   * Returns the local function node comments of this function node.
   *
   * @return The List of comments.
   */
  List<IComment> getLocalFunctionComment();

  /**
   * {@inheritDoc}
   */
  @Override
  INaviFunction getFunction();

  /**
   * Initializes the local function node comments.
   *
   * @param comments The list of comments to associate to this function node.
   */
  void initializeLocalFunctionComment(final List<IComment> comments);

  /**
   * Determines if the current active user owns the comment.
   *
   * @param comment The comment to be checked for ownership.
   * @return true if the current active user owns the comment.
   */
  boolean isOwner(IComment comment);

  // TODO (timkornau): document this function.
  boolean isStored();
}
