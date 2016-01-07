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
import com.google.security.zynamics.zylib.gui.zygraph.nodes.ICodeNodeListener;

import java.awt.Color;



/**
 * Interface for listeners that want to be notified about changes in code nodes.
 */
public interface INaviCodeNodeListener extends INaviViewNodeListener,
    ICodeNodeListener<INaviCodeNode, INaviInstruction, IComment> {
  /**
   * Invoked after an instruction was added to a code node.
   * 
   * @param codeNode The code node where the instruction was added.
   * @param instruction The instruction that was added to the code node.
   */
  void addedInstruction(INaviCodeNode codeNode, INaviInstruction instruction);

  /**
   * Invoked after the background color of an instruction changed.
   * 
   * @param codeNode The code node where the background color changed.
   * @param instruction The instruction whose background color changed.
   * @param level The priority level of the background color.
   * @param color The new background color of the instruction.
   */
  void changedInstructionColor(CCodeNode codeNode, INaviInstruction instruction, int level,
      Color color);

  /**
   * Invoked after an instruction was removed from a code node.
   * 
   * @param codeNode The code node from which the instruction was removed.
   * @param instruction The instruction removed from the code node.
   */
  void removedInstruction(INaviCodeNode codeNode, INaviInstruction instruction);
}
