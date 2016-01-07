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
package com.google.security.zynamics.binnavi.ZyGraph;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Silly helper class that exposes graph internals that are required by graph synchronizers.
 *
 * TODO: Get rid of this class
 */
public interface IZyGraphInternals {
  /**
   * Notifies all listeners that a node was deleted.
   */
  void notifyNodeDeleted();

  /**
   * Removes an edge from the graph.
   *
   * @param edge The edge to remove.
   */
  void removeEdge(NaviEdge edge);

  /**
   * Removes a node from the graph.
   *
   * @param node The node to remove.
   */
  void removeNode(NaviNode node);
}
