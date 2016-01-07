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

import java.util.ArrayList;
import java.util.List;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.disassembly.IInstruction;



/**
 * The com.google.security.zynamics.binnavi-specific instruction interfaces which extends the general instruction interface with
 * methods that are required by com.google.security.zynamics.binnavi.
 */
public interface INaviInstruction extends IInstruction, IDatabaseObject {
  /**
   * Adds a listener that is notified about changes in the instruction.
   * 
   * @param listener The listener object.
   */
  void addListener(IInstructionListener listener);

  /**
   * Appends a comment to the list of global comments of this instruction.
   * 
   * @param commentText The comment text that will be appended.
   * 
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   * @throws CouldntLoadDataException
   */
  List<IComment> appendGlobalComment(final String commentText)
      throws CouldntSaveDataException, CouldntLoadDataException;

  /**
   * Clones an instruction. Note that this clone is not actually a true deep copy. Only the internal
   * operators are cloned as well as the registration with the global comment manager.
   * 
   * @return The cloned instruction.
   */
  INaviInstruction cloneInstruction();

  /**
   * Closes the instruction and frees allocated resources.
   */
  void close();

  /**
   * Deletes a comment from the list of global instruction comments.
   *
   * @param comment The comment that will be deleted.
   *
   * @throws CouldntDeleteException if the comment could not be deleted from the database.
   */
  void deleteGlobalComment(final IComment comment) throws CouldntDeleteException;

  /**
   * Edits a comment in the list of the global instruction comments.
   * 
   * @param oldComment The comment that will be edited.
   * @param commentText The new comment text.
   * 
   * @return The edited comment.
   * 
   * @throws CouldntSaveDataException if the comment could not be saved to the database.
   */
  IComment editGlobalComment(final IComment oldComment, String commentText)
      throws CouldntSaveDataException;

  /**
   * Returns the global comment of the instruction.
   * 
   * @return The global comment of the instruction.
   */
  List<IComment> getGlobalComment();

  /**
   * Returns a printable string that represents the instruction.
   * 
   * @return The instruction string.
   */
  String getInstructionString();

  /**
   * Returns the module the instruction belongs to.
   * 
   * @return The module the instruction belongs to.
   */
  INaviModule getModule();

  /**
   * Returns the zero-based index position of the given operand within this instruction. Returns -1
   * if the operand was not found in the instruction.
   *
   * @param operand The operand to search for.
   * @return The zero-based index of the given operand within this instruction.
   */
  int getOperandPosition(INaviOperandTree operand);

  @Override
  List<COperandTree> getOperands();

  /**
   * Initializes the global comment of the instruction.
   * 
   * @param comment The global comment of the instruction.
   */
  void initializeGlobalComment(final ArrayList<IComment> comment);

  /**
   * Checks if the current active user is the owner of the comment in question.
   * 
   * @param comment The comment to check.
   * 
   * @return True if the current active user is the owner of the comment.
   */
  boolean isOwner(IComment comment);

  /**
   * Returns if the instruction in question has been saved to the Database
   * 
   * @return true if the instruction has been saved false otherwise.
   */
  boolean isStored();

  /**
   * Removes an instruction listener from the list of objects that are notified about changes in the
   * instruction.
   * 
   * @param listener The listener to be removed.
   */
  void removeListener(IInstructionListener listener);

  /**
   * Set the save status in the database for an instruction
   * 
   * @param saved is the save status of the instruction
   */
  void setSaved(boolean saved);
}
