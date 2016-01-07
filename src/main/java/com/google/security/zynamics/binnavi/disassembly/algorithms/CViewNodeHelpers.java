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
package com.google.security.zynamics.binnavi.disassembly.algorithms;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.IAddressNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.disassembly.IAddress;

/**
 * Contains helper functions for view nodes.
 */
public final class CViewNodeHelpers {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewNodeHelpers() {
  }

  /**
   * Determines the start address of a view node.
   * 
   * @param node The node whose address is determined.
   * 
   * @return The start address of the node.
   */
  public static IAddress getAddress(final INaviViewNode node) {
    Preconditions.checkArgument(node instanceof IAddressNode,
        "IE00431: Node is not a code node or a function node");
    return ((IAddressNode) node).getAddress();
  }
}
