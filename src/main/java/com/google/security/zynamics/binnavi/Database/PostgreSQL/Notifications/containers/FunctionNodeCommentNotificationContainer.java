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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;

/**
 * Class to store information for {@link INaviFunctionNode function node} comment notification.
 */
public class FunctionNodeCommentNotificationContainer extends AbstractCommentNotification {

  private final INaviFunctionNode node;

  public FunctionNodeCommentNotificationContainer(
      final INaviFunctionNode node, final CommentOperation operation, final Integer commentId) {
    super(operation, commentId);
    this.node = Preconditions.checkNotNull(node, "Error: Node argument can not be null.");
  }

  public INaviFunctionNode getNode() {
    return node;
  }

  @Override
  void informDelete(CommentManager manager, IComment comment) {
    manager.deleteFunctionNodeCommentInternal(node, comment);
  }

  @Override
  void informAppend(CommentManager manager) throws CouldntLoadDataException {
    manager.appendFunctionNodeComment(node, getCommentId());
  }
}
