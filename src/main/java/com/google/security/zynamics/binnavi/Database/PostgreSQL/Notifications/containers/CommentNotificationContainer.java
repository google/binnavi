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
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.CommentNotification;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.CommentManager;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentOperation;
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;

/**
 * Class to store information for {@link IComment comment} notifications.
 */
public class CommentNotificationContainer implements CommentNotification {

  private final IComment currentComment;
  private final IComment newComment;
  private final CommentOperation operation;

  public CommentNotificationContainer(
      final IComment currentComment, final IComment newComment, final CommentOperation operation) {
    this.currentComment = Preconditions.checkNotNull(
        currentComment, "Error: Current comment argument can not be null.");
    this.newComment = newComment;
    this.operation =
        Preconditions.checkNotNull(operation, "Error: Operation argument can not be null.");
  }

  @Override
  public void inform(final CommentManager manager) throws CouldntLoadDataException {
    if (operation == CommentOperation.EDIT) {
      informEdit(manager);
    } else if (operation == CommentOperation.DELETE) {
      informDelete(manager);
    }
  }

  private void informEdit(final CommentManager manager) {
    if (manager.getCommentedFunction(getCurrentComment()) != null) {
      manager.editFunctionComment(manager.getCommentedFunction(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.GLOBAL);
    }
    if (manager.getCommentedFunctionNode(getCurrentComment()) != null) {
      manager.editFunctionNodeComment(manager.getCommentedFunctionNode(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.GLOBAL);
    }
    if (manager.getCommentedGlobalCodeNode(getCurrentComment()) != null) {
      manager.editCodeNodeComment(manager.getCommentedGlobalCodeNode(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.GLOBAL);
    }
    if (manager.getCommentedLocalCodeNode(getCurrentComment()) != null) {
      manager.editCodeNodeComment(manager.getCommentedLocalCodeNode(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.LOCAL);
    }
    if (manager.getCommentedLocalEdge(getCurrentComment()) != null) {
      manager.editEdgeComment(manager.getCommentedLocalEdge(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.LOCAL);
    }
    if (manager.getCommentedGlobalEdge(getCurrentComment()) != null) {
      manager.editEdgeComment(manager.getCommentedGlobalEdge(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.GLOBAL);
    }
    if (manager.getCommentedGlobalInstruction(getCurrentComment()) != null) {
      manager.editInstructionComment(manager.getCommentedGlobalInstruction(getCurrentComment()),
          null, getCurrentComment(), newComment, CommentScope.GLOBAL);
    }
    if (manager.getCommentedLocalInstruction(getCurrentComment()) != null) {
      manager.editInstructionComment(
          manager.getCommentedLocalInstruction(getCurrentComment()).second(),
          manager.getCommentedLocalInstruction(getCurrentComment()).first(), getCurrentComment(),
          newComment, CommentScope.LOCAL);
    }
    if (manager.getCommentedGroupNode(getCurrentComment()) != null) {
      manager.editGroupNodeComment(manager.getCommentedGroupNode(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.LOCAL);
    }
    if (manager.getCommentedTextNode(getCurrentComment()) != null) {
      manager.editTextNodeComment(manager.getCommentedTextNode(getCurrentComment()),
          getCurrentComment(), newComment, CommentScope.LOCAL);
    }
    if (manager.getCommentedTypeInstance(getCurrentComment()) != null) {
      manager.editTypeInstanceComment(
          manager.getCommentedTypeInstance(getCurrentComment()), getCurrentComment(), newComment);
    }
  }

  private void informDelete(final CommentManager manager) {
    if (manager.getCommentedFunction(getCurrentComment()) != null) {
      manager.deleteFunctionCommentInternal(
          manager.getCommentedFunction(getCurrentComment()), getCurrentComment());
    }
    if (manager.getCommentedFunctionNode(getCurrentComment()) != null) {
      manager.deleteFunctionNodeCommentInternal(
          manager.getCommentedFunctionNode(getCurrentComment()), getCurrentComment());
    }
    if (manager.getCommentedGlobalCodeNode(getCurrentComment()) != null) {
      manager.deleteCodeNodeComment(manager.getCommentedGlobalCodeNode(getCurrentComment()),
          getCurrentComment(), CommentScope.GLOBAL);
    }
    if (manager.getCommentedLocalCodeNode(getCurrentComment()) != null) {
      manager.deleteCodeNodeComment(manager.getCommentedLocalCodeNode(getCurrentComment()),
          getCurrentComment(), CommentScope.LOCAL);
    }
    if (manager.getCommentedLocalEdge(getCurrentComment()) != null) {
      manager.deleteEdgeComment(manager.getCommentedLocalEdge(getCurrentComment()),
          getCurrentComment(), CommentScope.LOCAL);
    }
    if (manager.getCommentedGlobalEdge(getCurrentComment()) != null) {
      manager.deleteEdgeComment(manager.getCommentedGlobalEdge(getCurrentComment()),
          getCurrentComment(), CommentScope.GLOBAL);
    }
    if (manager.getCommentedGlobalInstruction(getCurrentComment()) != null) {
      manager.deleteInstructionComment(manager.getCommentedGlobalInstruction(getCurrentComment()),
          null, CommentScope.GLOBAL, getCurrentComment());
    }
    if (manager.getCommentedLocalInstruction(getCurrentComment()) != null) {
      manager.deleteInstructionComment(
          manager.getCommentedLocalInstruction(getCurrentComment()).second(),
          manager.getCommentedLocalInstruction(getCurrentComment()).first(), CommentScope.LOCAL,
          getCurrentComment());
    }
    if (manager.getCommentedGroupNode(getCurrentComment()) != null) {
      manager.deleteGroupNodeCommentInternal(
          manager.getCommentedGroupNode(getCurrentComment()), getCurrentComment());
    }
    if (manager.getCommentedTextNode(getCurrentComment()) != null) {
      manager.deleteTextNodeCommentInternal(
          manager.getCommentedTextNode(getCurrentComment()), getCurrentComment());
    }
    if (manager.getCommentedTypeInstance(getCurrentComment()) != null) {
      manager.deleteTypeInstanceCommentInternal(
          manager.getCommentedTypeInstance(getCurrentComment()), getCurrentComment());
    }
  }

  public IComment getCurrentComment() {
    return currentComment;
  }

  public IComment getNewComment() {
    return newComment;
  }

  @Override
  public CommentOperation getOperation() {
    return operation;
  }

  @Override
  public Integer getCommentId() {
    return currentComment.getId();
  }
}
