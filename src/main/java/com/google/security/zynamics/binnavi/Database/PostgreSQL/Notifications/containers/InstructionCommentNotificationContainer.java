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
import com.google.security.zynamics.binnavi.disassembly.CommentManager.CommentScope;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;

/**
 * Class to store information for instruction comment notification.
 */
public class InstructionCommentNotificationContainer extends AbstractScopedCommentNotification {

  private final INaviInstruction instruction;
  private final INaviCodeNode node;

  /**
   * Creates a container with the information necessary to inform about changes in a local
   * instruction comment in the database.
   *
   * @param instruction The {@link INaviInstruction instruction} where the change happens.
   * @param node The {@link INaviCodeNode node} where the instruction is located.
   * @param operation The {@link CommentOperation operation} which has been performed.
   * @param commentId The id of the comment that has changed in the database.
   */
  public InstructionCommentNotificationContainer(final INaviInstruction instruction,
      final INaviCodeNode node, final CommentOperation operation, final CommentScope scope,
      final Integer commentId) {
    super(operation, commentId, scope);
    this.instruction =
        Preconditions.checkNotNull(instruction, "Error: Instruction argument can not be null.");
    this.node = node;
  }

  public INaviInstruction getInstruction() {
    return instruction;
  }

  public INaviCodeNode getNode() {
    return node;
  }

  @Override
  void informDelete(CommentManager manager, IComment comment) {
    manager.deleteInstructionComment(instruction, node, getScope(), comment);
  }

  @Override
  void informAppend(CommentManager manager) throws CouldntLoadDataException {
    manager.appendInstructionComment(instruction, node, getScope(), getCommentId());
  }
}
