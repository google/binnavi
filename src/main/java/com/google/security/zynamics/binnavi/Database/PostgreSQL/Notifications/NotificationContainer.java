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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;

public class NotificationContainer<NotificationObject> {

  private final NotificationObject notified;
  private final CommentScope scope;
  private final CommentOperation operation;
  private final Integer commentId;
  private final IComment commentText;

  public NotificationContainer(final NotificationObject notifiedObject,
      final CommentScope commentScope, final Integer id, final CommentOperation commentOperation,
      final IComment newComment) {
    notified =
        Preconditions
            .checkNotNull(notifiedObject, "Error: notifiedObject argument can not be null");
    scope = commentScope;
    operation =
        Preconditions.checkNotNull(commentOperation, "IE02519: operation argument can not be null");
    commentId = id;
    commentText = newComment;
  }

  public Integer getCommentId() {
    return commentId;
  }

  public IComment getNewComment() {
    return commentText;
  }

  public NotificationObject getNotified() {
    return notified;
  }

  public CommentOperation getOperation() {
    return operation;
  }

  public CommentScope getScope() {
    return scope;
  }
}
