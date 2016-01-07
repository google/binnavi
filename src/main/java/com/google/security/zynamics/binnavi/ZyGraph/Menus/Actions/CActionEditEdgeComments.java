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

import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CEdgeFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Action class used for editing the comments of an edge.
 */
public final class CActionEditEdgeComments extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5977033249090274914L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * The edge whose comments are edited.
   */
  private final NaviEdge m_edge;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param edge The edge whose comments are edited.
   */
  public CActionEditEdgeComments(final JFrame parent, final NaviEdge edge) {
    super("Edit Edge Comments");

    m_parent = parent;
    m_edge = edge;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CEdgeFunctions.editEdgeComments(m_parent, m_edge);
  }
}
