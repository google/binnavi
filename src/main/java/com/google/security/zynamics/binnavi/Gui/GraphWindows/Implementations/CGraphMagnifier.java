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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Contains functions to toggle the magnifying mode of a graph.
 */
public final class CGraphMagnifier {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphMagnifier() {
  }

  /**
   * Toggles the magnifying mode of a graph.
   *
   * @param graph The graph where the magnifying mode is toggled.
   */
  public static void toogleMagnifyingGlass(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01749: Graph argument can not be null");

    graph.getSettings().getDisplaySettings()
        .setMagnifyingGlassMode(!graph.getSettings().getDisplaySettings().getMagnifyingGlassMode());
  }
}
