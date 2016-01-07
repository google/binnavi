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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Implementations.CBreakpointFunctions;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class used for zooming to a given breakpoint.
 */
public class CZoomBreakpointAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3470953151562055911L;

  /**
   * Parent window for dialogs.
   */
  private final Window m_parent;

  /**
   * Graph shown in the window of the breakpoint table.
   */
  private final ZyGraph m_graph;

  /**
   * View container of the graph.
   */
  private final IViewContainer m_container;

  /**
   * Address to zoom to.
   */
  private final BreakpointAddress m_address;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window for dialogs.
   * @param graph Graph shown in the window of the breakpoint table.
   * @param container View container of the graph.
   * @param address Address to zoom to.
   */
  public CZoomBreakpointAction(final Window parent, final ZyGraph graph,
      final IViewContainer container, final BreakpointAddress address) {
    super(String.format("Zoom to address %s", address.getAddress().getAddress().toHexString()));

    m_parent = parent;
    m_graph = graph;
    m_container = container;
    m_address = address;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CBreakpointFunctions.zoomToBreakpoint(m_parent, m_graph, m_container, m_address);
  }
}
