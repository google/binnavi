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
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNode;

import java.util.List;



/**
 * Interface that represents group nodes in views.
 */
public interface INaviGroupNode extends INaviViewNode, IGroupNode<INaviViewNode, INaviEdge> {
  /**
   * Adds a listener that is notified about changes in the group.
   * 
   * @param listener The listener to add.
   */
  void addGroupListener(INaviGroupNodeListener listener);

  /**
   * Appends a comment to the list of group node comments.
   * 
   * @param comment The comment which will be appended.
   * @throws CouldntSaveDataException
   * @throws CouldntLoadDataException
   */
  List<IComment> appendComment(final String comment) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Deletes a comment from the list of comments associated with this group node.
   * 
   * @param comment The comment which will be deleted.
   * @throws CouldntDeleteException if the comment could not be deleted from the group node.
   */
  void deleteComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Edits a comment of the group node.
   * 
   * @param comment The comment which is edited.
   * @param newComment The comment text to save in the comment.
   * @return The edited comment
   * @throws CouldntSaveDataException
   */
  IComment editComment(final IComment comment, String newComment) throws CouldntSaveDataException;

  /**
   * Returns the comment shown when the group is collapsed.
   * 
   * @return The comment shown when the group is collapsed.
   */
  List<IComment> getComments();

  @Override
  List<INaviViewNode> getElements();

  /**
   * Returns the number of elements in the group.
   * 
   * @return The number of elements in the group.
   */
  int getNumberOfElements();

  /**
   * Initializes the comments of a group node.
   * 
   * @param comments The comments with which the group node comments will be initialized.
   */
  void initializeComment(final List<IComment> comments);

  /**
   * Checks if the current active user is the owner of the comment in question.
   * 
   * @param comment The comment to check.
   * 
   * @return True if the current active user is the owner of the comment.
   */
  boolean isOwner(IComment comment);

  boolean isStored();

  /**
   * Removes an element from the group.
   * 
   * @param node The element to remove.
   */
  void removeElement(INaviViewNode node);

  /**
   * Removes a listener object that was previously notified about changes in the group node.
   * 
   * @param listener The listener to remove.
   */
  void removeGroupListener(INaviGroupNodeListener listener);
}
