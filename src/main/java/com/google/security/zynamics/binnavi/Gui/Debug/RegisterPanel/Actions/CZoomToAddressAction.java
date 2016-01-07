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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions;

import com.google.security.zynamics.binnavi.ZyGraph.Implementations.ZyZoomHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;



/**
 * Action class that can be used to zoom to an address in the graph.
 */
public final class CZoomToAddressAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5406123528498038624L;

  /**
   * The graph to be zoomed.
   */
  private final ZyGraph m_graph;

  /**
   * The offset to go to.
   */
  private final IAddress m_offset;

  /**
   * The module in which the offset is located in.
   */
  private final INaviModule m_module;

  /**
   * Creates a new action object.
   *
   * @param graph The graph to be zoomed.
   * @param offset The offset to go to.
   */
  public CZoomToAddressAction(
      final ZyGraph graph, final IAddress offset, final INaviModule module) {
    super(String.format("Zoom to instruction %s", offset.toHexString()));

    m_graph = graph;
    m_offset = offset;
    m_module = module;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    ZyZoomHelpers.zoomToAddress(m_graph, m_offset, m_module, true);
  }
}
