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
package com.google.security.zynamics.binnavi.disassembly;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.List;

/**
 * Interface that must be implemented by all classes that want to be notified about changes in an
 * instruction.
 */
public interface IInstructionListener {
  /**
   * Invoked whenever a comment is appended to the list of instruction comments.
   *
   * @param instruction The instruction where the comment is added.
   * @param comment The comment which is added.
   */
  void appendedComment(IInstruction instruction, IComment comment);

  /**
   * Invoked whenever a comment is deleted from the list of instruction comments.
   *
   * @param instruction The instruction where the comment is deleted.
   * @param comment The comment which is deleted.
   */
  void deletedComment(IInstruction instruction, IComment comment);

  /**
   * Called whenever the comment of an instruction was edited.
   *
   * @param instruction The instruction whose comment changed.
   * @param comment The changed comment of the instruction.
   */
  void editedComment(IInstruction instruction, IComment comment);

  /**
   * Invoked whenever the comments of an instruction are initialized.
   *
   * @param instruction The instruction whose comments are initialized.
   * @param comment The comments.
   */
  void initializedComment(IInstruction instruction, List<IComment> comment);
}
