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

import com.google.security.zynamics.zylib.disassembly.IReference;

/**
 * Interface for objects that want to be notified about changes in operand tree nodes.
 */
public interface INaviOperandTreeNodeListener {
  /**
   * Invoked after a reference was added to an operand tree node.
   * 
   * @param operandTreeNode The operand tree node the reference was added to.
   * @param reference The added reference.
   */
  void addedReference(INaviOperandTreeNode operandTreeNode, IReference reference);

  /**
   * Invoked after the display style of an operand tree node was changed.
   * 
   * @param operandTreeNode The operand tree node whose display style was changed.
   * @param style The new display style.
   */
  void changedDisplayStyle(COperandTreeNode operandTreeNode, OperandDisplayStyle style);

  /**
   * Invoked after the operand changed its value.
   * 
   * @param operandTreeNode The operand tree node whose value changed.
   */
  void changedValue(INaviOperandTreeNode operandTreeNode);

  /**
   * Invoked after a reference was removed from an operand tree node.
   * 
   * @param operandTreeNode The operand tree node the reference was removed from.
   * @param reference The reference removed from the operand tree node.
   */
  void removedReference(INaviOperandTreeNode operandTreeNode, IReference reference);
}
