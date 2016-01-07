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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces;

import java.util.List;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;


/**
 * Interface which allows unified access to comments.
 */
public interface ICommentAccessor {

  /**
   * Appends a comment to the list of comments associated.
   * 
   * @param commentText The text of the comment to be appended.
   * @return The appended comment.
   * 
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendComment(final String commentText) throws CouldntSaveDataException,
      CouldntLoadDataException;

  /**
   * Deletes a comment in the list of comments associated.
   * 
   * @param comment The comment to be deleted.
   * 
   * @throws CouldntDeleteException if the comment could not be deleted.
   */
  void deleteComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Edits a comment in the list of comments associated.
   * 
   * @param comment The comment to edit.
   * @param newCommentText The new comment text.
   * 
   * @return The edited comment.
   * 
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  IComment editComment(final IComment comment, String newCommentText)
      throws CouldntSaveDataException;

  /**
   * Returns the lists of comments associated.
   * 
   * @return The List of comments associated.
   */
  List<IComment> getComment();

  /**
   * Determines the current active user for checking if a comment can be edited or deleted by the
   * current user.
   * 
   * @param comment The comment to be checked against the current active user.
   * 
   * @return true if the current active user is the owner of the comment false otherwise.
   */
  boolean isOwner(final IComment comment);
}
