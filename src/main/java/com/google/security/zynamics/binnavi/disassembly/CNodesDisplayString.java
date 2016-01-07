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

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IFunctionNode;



/**
 * Generates a displayable string that represents a given node.
 */
public final class CNodesDisplayString {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodesDisplayString() {
  }

  /**
   * Generates a displayable string that represents a given node.
   * 
   * @param node The node for which the string is generated.
   * 
   * @return The generated identification string.
   */
  public static String getDisplayString(final NaviNode node) {
    if ((node.getRawNode() instanceof INaviCodeNode)
        || (node.getRawNode() instanceof IFunctionNode)) {
      return getDisplayString(node.getRawNode());
    }
    return node.toString();
  }

  public static String getDisplayString(final INaviViewNode node) {
    if (node instanceof INaviCodeNode) {
      return ((INaviCodeNode) node).getAddress().toHexString();
    } else if (node instanceof IFunctionNode) {
      return ((IFunctionNode<?, ?>) node).getFunction().getName();
    } else {
      return node.toString();
    }
  }
}
