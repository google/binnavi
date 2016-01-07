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
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;

/**
 * This class can be used to switch over all known types of INaviViewNode objects. This is preferred
 * to using switch/case because using this class makes sure that all known subclasses of
 * INaviViewNode are handled.
 */
public final class CNodeTypeSwitcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeTypeSwitcher() {
  }

  /**
   * Switches over an INaviViewNode
   * 
   * @param <T> Type of the return value of the function.
   * 
   * @param node The node to switch over.
   * @param callback The callback object that is invoked once the specific type of the node is
   *        determined.
   * 
   * @return The return value of the invoked callback method.
   */
  public static <T> T switchNode(final INaviViewNode node, final INodeTypeCallback<T> callback) {
    Preconditions.checkNotNull(node, "IE00121: Node argument can not be null");
    Preconditions.checkNotNull(callback, "IE00970: Callback argument can not be null");

    if (node instanceof INaviCodeNode) {
      return callback.handle((INaviCodeNode) node);
    } else if (node instanceof INaviFunctionNode) {
      return callback.handle((INaviFunctionNode) node);
    } else if (node instanceof INaviGroupNode) {
      return callback.handle((INaviGroupNode) node);
    } else if (node instanceof INaviTextNode) {
      return callback.handle((INaviTextNode) node);
    } else {
      throw new IllegalStateException("IE00021: Unknown node type " + node.getClass());
    }
  }
}
