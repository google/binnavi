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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.InstructionComments;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EmptyComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IInstructionCommentAccessor;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to access the global comments of instructions in a generic way.
 */
public final class CGlobalInstructionCommentAccessor implements IInstructionCommentAccessor {

  private final INaviCodeNode m_codeNode;

  public CGlobalInstructionCommentAccessor(final INaviCodeNode codeNode) {
    m_codeNode = Preconditions.checkNotNull(codeNode, "IE02684: codenode argument can not be null");
  }

  @Override
  public List<IComment> appendInstructionComment(final INaviInstruction instruction,
      final String commentText) throws CouldntSaveDataException, CouldntLoadDataException {
    Preconditions.checkNotNull(instruction, "IE02685: instruction argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02686: comment argument can not be null");

    return instruction.appendGlobalComment(commentText);
  }

  @Override
  public void deleteInstructionComment(final INaviInstruction instruction, final IComment comment)
      throws CouldntDeleteException {
    Preconditions.checkNotNull(instruction, "IE02687: instruction argument can not be null");
    Preconditions.checkNotNull(comment, "IE02688: comment argument can not be null");

    instruction.deleteGlobalComment(comment);
  }

  @Override
  public IComment editInstructionComment(final INaviInstruction instruction,
      final IComment comment, final String commentText) throws CouldntSaveDataException {
    Preconditions.checkNotNull(instruction, "IE02689: instruction argument can not be null");
    Preconditions.checkNotNull(comment, "IE02690: comment argument can not be null");
    Preconditions.checkNotNull(commentText, "IE02691: commentText argument can not be null");

    return instruction.editGlobalComment(comment, commentText);
  }

  @Override
  public ArrayList<Pair<INaviInstruction, IComment>> getAllComments() {
    final ArrayList<Pair<INaviInstruction, IComment>> values =
        new ArrayList<Pair<INaviInstruction, IComment>>();

    for (final INaviInstruction instruction : m_codeNode.getInstructions()) {
      final List<IComment> comments = instruction.getGlobalComment();

      if ((comments == null) || comments.isEmpty()) {
        values.add(new Pair<INaviInstruction, IComment>(instruction, new EmptyComment()));
      } else {
        for (final IComment comment : comments) {
          values.add(new Pair<INaviInstruction, IComment>(instruction, comment));
        }
        values.add(new Pair<INaviInstruction, IComment>(instruction, new EmptyComment()));
      }
    }

    return values;
  }

  @Override
  public List<IComment> getInstructionComments(final INaviInstruction instruction) {
    return instruction.getGlobalComment();
  }

  @Override
  public boolean isOwner(final IComment comment) {
    return m_codeNode.isOwner(comment);
  }
}
