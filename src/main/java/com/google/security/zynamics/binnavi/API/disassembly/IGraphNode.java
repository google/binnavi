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

import java.util.List;

// / Base class for all graph nodes.
/**
 * Interface that is implemented by nodes that are part of graphs.
 *
 * @param <T> Type of the node.
 */
public interface IGraphNode<T> extends com.google.security.zynamics.zylib.types.graphs.IGraphNode<T> {
  // ! Children of the graph node.
  /**
   * Returns a list of all children of the graph node.
   *
   * @return List of all children of the graph node.
   */
  @Override
  List<T> getChildren();

  // ! Parents of the graph node.
  /**
   * Returns a list of all parents of the graph node.
   *
   * @return List of all parents of the graph node.
   */
  @Override
  List<T> getParents();
}
