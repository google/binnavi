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
package com.google.security.zynamics.binnavi.debug.helpers;

import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.gui.zygraph.helpers.INodeCallback;
import com.google.security.zynamics.zylib.types.common.IterationMode;

/**
 * Helper class for iterating over all breakpointable nodes of a graph.
 */
public final class NodeBreakpointDecider implements INodeCallback<NaviNode> {
  /**
   * Callback object invoked for each breakpointable node.
   */
  private final INodeCallback<NaviNode> breakpointNodeCallback;

  /**
   * Creates a new decider object.
   *
   * @param callback Callback object invoked for each breakpointable node.
   */
  public NodeBreakpointDecider(final INodeCallback<NaviNode> callback) {
    breakpointNodeCallback = callback;
  }

  @Override
  public IterationMode next(final NaviNode node) {
    if (node.getRawNode() instanceof INaviFunctionNode) {
      final INaviFunctionNode functionNode = (INaviFunctionNode) node.getRawNode();
      if (functionNode.getFunction().getType() == FunctionType.IMPORT) {
        return IterationMode.CONTINUE;
      }
    }
    breakpointNodeCallback.next(node);
    return IterationMode.CONTINUE;
  }
}
