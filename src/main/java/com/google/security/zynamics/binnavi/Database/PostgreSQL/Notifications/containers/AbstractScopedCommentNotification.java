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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.CommentNotification;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;

/**
 * This class implements the common methods for {@link CommentNotification comment notifications}.
 */
public abstract class AbstractScopedCommentNotification extends AbstractCommentNotification {

  private final CommentScope scope;

  public AbstractScopedCommentNotification(
      final CommentOperation operation, final Integer commentId, final CommentScope scope) {
    super(operation, commentId);
    this.scope = Preconditions.checkNotNull(scope, "Error: Scope argument can not be null.");
  }

  public CommentScope getScope() {
    return scope;
  }
}
