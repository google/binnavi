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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyEdgeBuilder;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IEdgeRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers.ZyEdgeRealizer;

/**
 * Responsible for updating edges when the underlying data changes.
 */
public final class CEdgeUpdater implements IEdgeRealizerUpdater<NaviEdge> {
  /**
   * The edge that is updated.
   */
  private final INaviEdge m_edge;

  /**
   * Creates a new edge updater object.
   *
   * @param edge The edge that is updated.
   */
  public CEdgeUpdater(final INaviEdge edge) {
    Preconditions.checkNotNull(edge, "IE00987: Edge argument can't be null");

    m_edge = edge;
  }

  @Override
  public ZyLabelContent generateContent(final ZyEdgeRealizer<NaviEdge> realizer) {
    return ZyEdgeBuilder.buildContent(m_edge);
  }

  @Override
  public void setRealizer(final ZyEdgeRealizer<NaviEdge> realizer) {
  }
}
