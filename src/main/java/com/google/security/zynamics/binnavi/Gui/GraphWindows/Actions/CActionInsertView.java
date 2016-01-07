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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CViewInserter;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class for inserting a view.
 */
public final class CActionInsertView extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1064965959187849542L;

  /**
   * Parent window used to display dialogs.
   */
  private final JFrame m_parent;

  /**
   * The graph where the view is inserted.
   */
  private final ZyGraph m_graph;

  /**
   * The container that provides the views that can be inserted.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph The graph where the view is inserted.
   * @param viewContainer The container that provides the views that can be inserted.
   */
  public CActionInsertView(
      final JFrame parent, final ZyGraph graph, final IViewContainer viewContainer) {
    super("Insert View");

    m_parent = parent;
    m_graph = graph;
    m_viewContainer = viewContainer;
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CViewInserter.insertView(m_parent, m_graph, m_viewContainer);
  }
}
