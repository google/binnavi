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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface used to access comments in a generic way.
 */
public interface IInstructionCommentAccessor {
  /**
   * Appends a comment to the list of instruction comments.
   *
   * @param instruction The instruction the comment belongs to.
   * @param commentText The comment text which is being appended.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendInstructionComment(INaviInstruction instruction, String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException;

  /**
   * Deletes a comment from the list of instruction comments.
   *
   * @param instruction The instruction the comment belongs to.
   * @param comment the comment which is being deleted.
   *
   * @throws CouldntDeleteException if the changes could not be saved to the database.
   */
  void deleteInstructionComment(INaviInstruction instruction, IComment comment)
      throws CouldntDeleteException;

  /**
   * Edits a comment in the list of instruction comments.
   *
   * @param instruction The instruction the comment belongs to.
   * @param oldComment The comment which is being edited.
   * @param commentText The comment text to be saved in the comment.
   *
   * @throws CouldntSaveDataException if the changes could not be saved to the database.
   */
  IComment editInstructionComment(INaviInstruction instruction, IComment oldComment,
      String commentText) throws CouldntSaveDataException;

  ArrayList<Pair<INaviInstruction, IComment>> getAllComments();

  /**
   * Returns the comment of an instruction.
   *
   * @param instruction The instruction whose comment is returned.
   *
   * @return The instruction comment.
   */
  List<IComment> getInstructionComments(INaviInstruction instruction);

  /**
   * Returns if the current active user is the owner of the comment passed as argument.
   *
   * @param comment The comment to be checked for ownership
   * @return True if the comment is owned by the currently active user.
   */
  boolean isOwner(final IComment comment);
}
