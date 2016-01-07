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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.util.List;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;


// / Used to listen on instructions.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * Instruction objects.
 */
public interface IInstructionListener {
  // ! Signals an appended instruction comment.
  /**
   * Invoked after a comment has been added to the list of instruction comments.
   * 
   * @param instruction The instruction where the comment was added.
   * @param comment The comment which was added.
   */
  void appendedComment(Instruction instruction, IComment comment);

  // ! Signals the deletion of a comment in the list of instruction comments.
  /**
   * Invoked after a comment has been deleted from the list of instruction comments.
   * 
   * @param instruction The instruction where the comment was deleted.
   * @param comment The comment which was deleted.
   */
  void deletedComment(Instruction instruction, IComment comment);

  // ! Signals an edited instruction comment.
  /**
   * Invoked after the comment of the instruction was edited.
   * 
   * @param instruction The instruction whose comment changed.
   * @param comment The new comment of the instruction.
   */
  void editedComment(Instruction instruction, IComment comment);

  // ! Signals the initialization of the instruction comments.
  /**
   * Invoked after the comments of an instruction have been initialized.
   * 
   * @param instruction The instructions whose comments are initialized.
   * @param comments The comments with which the instruction is initialized.
   */
  void initializedComment(Instruction instruction, List<IComment> comments);
}
