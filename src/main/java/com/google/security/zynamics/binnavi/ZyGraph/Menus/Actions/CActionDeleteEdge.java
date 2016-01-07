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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CEdgeDeleter;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Action class that can be used to delete an edge from a graph.
 */
public final class CActionDeleteEdge extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3954116361504927648L;

  /**
   * View from where the edge is deleted.
   */
  private final INaviView m_view;

  /**
   * The edge to delete.
   */
  private final INaviEdge m_edge;

  /**
   * Creates a new action object.
   *
   * @param view View from where the edge is deleted.
   * @param edge The edge to delete.
   */
  public CActionDeleteEdge(final INaviView view, final INaviEdge edge) {
    super("Delete Edge");

    m_view = Preconditions.checkNotNull(view, "IE00924: View argument can't be null");
    m_edge = Preconditions.checkNotNull(edge, "IE00925: Edge argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CEdgeDeleter.deleteEdge(m_view, m_edge);
  }
}
