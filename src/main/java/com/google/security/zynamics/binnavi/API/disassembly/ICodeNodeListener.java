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

// / Used to listen on code nodes.
/**
 * Listener interface that must be implemented by all objects that want to be notified about changes
 * in a code node.
 */
public interface ICodeNodeListener {
  // ! Signals that an instruction was added to the code node.
  /**
   * Invoked after an instruction was added to a code node.
   *
   * @param codeNode The code node the instruction was added to.
   * @param instruction The instruction that was added to the code node.
   */
  void addedInstruction(CodeNode codeNode, Instruction instruction);

  // ! Signals that an instruction was removed from the code node.
  /**
   * Invoked after an instruction was removed from a code node.
   *
   * @param codeNode The code node the instruction was removed from.
   * @param instruction The instruction that was removed from the code node.
   */
  void removedInstruction(CodeNode codeNode, Instruction instruction);
}
