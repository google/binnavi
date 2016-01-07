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

import com.google.security.zynamics.binnavi.Gui.Users.Interfaces.IUser;

public interface IComment {
  /**
   * Returns the comment in this specific comment.
   *
   * @return The actual comment as a String,
   */
  String getComment();

  /**
   * Returns a particular line in the comment (or null).
   *
   * @return The actual comment as a String,
   */
  String getCommentLine(int index);

  /**
   * Returns the array of lines in the comment (or null).
   *
   * @return The actual comment as a String,
   */
  String[] getCommentLines();

  /**
   * Returns the unique id of the comment.
   *
   * @return The id of the comment.
   */
  Integer getId();

  /**
   * Returns the parent id of this comment. If this comment has no parent a negative number is
   * returned.
   *
   * @return The parent id of the comment.
   */
  IComment getParent();

  /**
   * Returns the user which owns a specific comment.
   *
   * @return The user of the current comment.
   */
  IUser getUser();

  /**
   * Returns if the current comment has a parent or not.
   *
   * @return true if comment has parent comment.
   */
  boolean hasParent();

  /**
   * Returns if the current comment is stored in the database.
   *
   * @return true if comment is stored in the database.
   */
  boolean isStored();

  /**
   * Returns the number of lines the comment is long.
   *
   * @return The number of lines the comment is long
   */
  int getNumberOfCommentLines();
}
