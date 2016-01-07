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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;

/**
 * This interface is the base interface for all comment notifications. It provides the methods all
 * comment notifications need to implement.
 */
public interface CommentNotification {

  /**
   * Informs the given notified object about the performed comment changes in the database.
   *
   * @param manager The {@link CommentManager manager} to use for the change.
   * @throws CouldntLoadDataException If the {@link CommentManager manager} could not load the
   *         changed comment from the database.
   */
  void inform(final CommentManager manager) throws CouldntLoadDataException;

  /**
   * Returns the {@link CommentOperation operation} in the {@link CommentNotification notification}.
   * This method is here for testing reasons be sure to change the comment if you use it elsewhere.
   *
   * @return {@link CommentOperation operation} stored in the {@link CommentNotification
   *         notification}.
   */
  CommentOperation getOperation();

  /**
   * Returns the comment id in the {@link CommentNotification notification}. This method id here for
   * testing reasons be sure to change the comment if you use it elsewhere.
   *
   * @return comment id stored in the {@link CommentNotification notification}.
   */
  Integer getCommentId();
}
