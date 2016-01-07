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
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ITextNode;

import java.util.List;



/**
 * Interface to be implemented by classes that want to be used as text nodes. Text nodes are nodes
 * that only display a text but do not have any other special characteristics.
 */
public interface INaviTextNode extends INaviViewNode, ITextNode {
  /**
   * Adds a listener that is notified about changes in the text node.
   * 
   * @param listener The listener to be added.
   */
  void addListener(INaviTextNodeListener listener);

  /**
   * Appends a comment to the text node.
   * 
   * @param comment The comment which gets appended to the text node.
   * 
   * @return The newly generated comment.
   * @throws CouldntSaveDataException
   * @throws CouldntLoadDataException
   */
  List<IComment> appendComment(String comment) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Deletes a comment from a text node.
   * 
   * @param comment The comment to be deleted from the text node.
   * @throws CouldntDeleteException
   */
  void deleteComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Edits a comment in a text node.
   * 
   * @param comment The comment which is edited.
   * @param commentText The comment text to store in the comment.
   * 
   * @return The new comment with the new comment text.
   */
  IComment editComment(final IComment comment, final String commentText)
      throws CouldntSaveDataException;

  /**
   * Returns the comments stored in te text node.
   * 
   * @return The comments that are displayed in the text node.
   */
  List<IComment> getComments();

  /**
   * initializes the comments of a text node without any database access.
   * 
   * @param comments The comment which are used to initialize the text node comments with.
   */
  void initializeComment(final List<IComment> comments);

  /**
   * Determines if the current active user is the owner of the comment.
   * 
   * @param comment The comment.
   * 
   * @return true if the current active user owns the comment.
   */
  boolean isOwner(IComment comment);

  /**
   * Determines if this text node is currently stored in the database.
   * 
   * @return true if the text node is stored in the database.
   */
  boolean isStored();

  /**
   * Removes a listener object from the text node.
   * 
   * @param listener The listener object to remove.
   */
  void removeListener(INaviTextNodeListener listener);
}
