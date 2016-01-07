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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Provides functions for deleting nodes and edges from a graph.
 */
public final class CEdgeDeleter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CEdgeDeleter() {
  }

  /**
   * Removes an edge from a graph.
   *
   * @param view The view from which the edge is removed.
   * @param edge The edge to remove from the graph.
   */
  public static void deleteEdge(final INaviView view, final INaviEdge edge) {
    Preconditions.checkNotNull(view, "IE01727: View argument can not be null");
    Preconditions.checkNotNull(edge, "IE01728: Edge argument can not be null");

    view.getContent().deleteEdge(edge);
  }
}
