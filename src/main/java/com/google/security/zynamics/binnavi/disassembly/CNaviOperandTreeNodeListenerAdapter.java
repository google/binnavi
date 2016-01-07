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
 * Adapter class for operand tree node listeners.
 */
public class CNaviOperandTreeNodeListenerAdapter implements INaviOperandTreeNodeListener {
  @Override
  public void addedReference(final INaviOperandTreeNode operandTreeNode, final IReference reference) {
    // Empty default implementation
  }

  @Override
  public void changedDisplayStyle(final COperandTreeNode operandTreeNode,
      final OperandDisplayStyle style) {
    // Empty default implementation
  }

  @Override
  public void changedValue(final INaviOperandTreeNode operandTreeNode) {
    // Empty default implementation
  }

  @Override
  public void removedReference(final INaviOperandTreeNode operandTreeNode,
      final IReference reference) {
    // Empty default implementation
  }
}
