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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Interface that must be implemented by objects that want to update graph nodes.
 */
public interface INodeUpdater {
  /**
   * Asks an updater to consider attaching itself to a given node.
   *
   * @param model The model the node belongs to.
   * @param node The node that requires an updater.
   */
  void visit(CGraphModel model, NaviNode node);
}
