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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphInliner;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action used for inlining all function calls of a view.
 */
public final class CActionInlineAll extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1532759745844022259L;

  /**
   * Parent window used for dialogs.
   */
  private final CGraphWindow m_parent;

  /**
   * Graph where the inline operation takes place.
   */
  private final ZyGraph m_graph;

  /**
   * Contains the functions to be inlined.
   */
  private final IViewContainer m_container;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used for dialogs.
   * @param container Contains the functions to be inlined.
   * @param graph Graph where the inline operation takes place.
   */
  public CActionInlineAll(
      final CGraphWindow parent, final IViewContainer container, final ZyGraph graph) {
    super("Inline all function calls");

    Preconditions.checkNotNull(parent, "IE01644: Parent argument can not be null");
    Preconditions.checkNotNull(container, "IE02276: Container argument can not be null");
    Preconditions.checkNotNull(graph, "IE01645: Graph argument can not be null");

    m_parent = parent;
    m_container = container;
    m_graph = graph;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphInliner.inlineAll(m_parent, m_container, m_graph);
    m_graph.doLayout();
  }
}
