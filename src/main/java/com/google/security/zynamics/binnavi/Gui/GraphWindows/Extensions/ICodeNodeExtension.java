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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions;

import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;


/**
 * Interface for classes that want to extend code nodes in graphs.
 */
public interface ICodeNodeExtension {
  /**
   * In this function, implementing classes can extend the context menu of code nodes where incoming
   * registers are shown.
   *
   * @param menu The menu to extend.
   * @param node The code node for which the menu is created.
   * @param instruction The instruction for which the menu is created.
   * @param register An incoming register.
   */
  void extendIncomingRegistersMenu(
      JMenu menu, INaviCodeNode node, INaviInstruction instruction, String register);

  /**
   * In this function, implementing classes can extend the context menu of code nodes where
   * instructions are shown.
   *
   * @param menu The menu to extend.
   * @param node The code node for which the menu is created.
   * @param instruction The instruction for which the menu is created.
   */
  void extendInstruction(JMenu menu, INaviCodeNode node, INaviInstruction instruction);

  /**
   * In this function, implementing classes can extend the context menu of code nodes where outgoing
   * registers are shown.
   *
   * @param menu The menu to extend.
   * @param node The code node for which the menu is created.
   * @param instruction The instruction for which the menu is created.
   * @param register An outgoing register.
   */
  void extendOutgoingRegistersMenu(
      JMenu menu, INaviCodeNode node, INaviInstruction instruction, String register);
}
