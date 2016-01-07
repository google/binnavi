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

import com.google.security.zynamics.binnavi.Database.NodeParser.IInstructionContainer;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.disassembly.ICodeContainer;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ICodeNode;

import java.awt.Color;
import java.util.List;



/**
 * Interface for code nodes.
 */
public interface INaviCodeNode extends INaviViewNode,
    ICodeNode<INaviEdge, INaviInstruction, INaviCodeNodeListener>, IAddressNode,
    ICodeContainer<INaviInstruction>, IInstructionContainer {
  /**
   * Adds an instruction and its local comment to the code node.
   * 
   * @param instruction The instruction to add.
   * @param localComment The local comment of the instruction.
   */
  void addInstruction(INaviInstruction instruction, List<IComment> localComment);

  /**
   * Returns the Code node comments container for the current code node.
   * 
   * @return The code node comment container.
   */
  CCodeNodeComments getComments();

  /**
   * Returns the parent function of the code node.
   * 
   * @return The parent function of the code node.
   * 
   * @throws MaybeNullException Thrown if the code node does not have a parent function.
   */
  INaviFunction getParentFunction() throws MaybeNullException;

  /**
   * Checks if the current active user is the owner of the comment in question.
   * 
   * @param comment The comment to check.
   * 
   * @return True if the current active user is the owner of the comment.
   */
  boolean isOwner(IComment comment);

  boolean isStored();

  /**
   * Removes an instruction from the code node.
   * 
   * @param instruction The instruction to remove.
   */
  void removeInstruction(INaviInstruction instruction);

  /**
   * Removes a listener object from the code node.
   * 
   * @param listener The listener to remove.
   */
  void removeListener(INaviCodeNodeListener listener);

  /**
   * Changes the background color of an instruction of the code node.
   * 
   * @param instruction The instruction whose background color is changed.
   * @param level The priority level of the background color.
   * @param color The new background color of the instruction.
   */
  void setInstructionColor(INaviInstruction instruction, int level, Color color);
}
