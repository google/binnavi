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
package com.google.security.zynamics.binnavi.ZyGraph.Builders.Wrappers;

import com.google.security.zynamics.binnavi.disassembly.INaviEdge;

/**
 * Wrapper class to associate the local comment of an edge with a visible edge.
 *
 *  TODO: This class is not actually used yet. The functionality must be implemented for it.
 */
public class CLocalEdgeCommentWrapper {
  /**
   * The edge whose local comment is wrapped.
   */
  private final INaviEdge m_edge;

  /**
   * Creates a new wrapper object.
   *
   * @param edge The edge whose local comment is wrapped.
   */
  public CLocalEdgeCommentWrapper(final INaviEdge edge) {
    m_edge = edge;
  }

  /**
   * Returns the wrapped edge.
   *
   * @return The wrapped edge.
   */
  public INaviEdge getEdge() {
    return m_edge;
  }
}
