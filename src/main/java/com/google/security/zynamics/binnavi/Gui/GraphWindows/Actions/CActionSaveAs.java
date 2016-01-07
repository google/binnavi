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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSaver;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action class for "saving as" views
 */
public final class CActionSaveAs extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5182263060599434690L;

  /**
   * Parent window used to display dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph to be written to the database.
   */
  private final ZyGraph m_graph;

  /**
   * View container the graph is written to.
   */
  private final IViewContainer m_container;

  /**
   * Creates a new action object.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph Graph to be written to the database.
   * @param container View container the graph is written to.
   */
  public CActionSaveAs(final JFrame parent, final ZyGraph graph, final IViewContainer container) {
    super("Save View As");

    m_parent = parent;
    m_graph = graph;
    m_container = container;
  }

  @Override
  public void actionPerformed(final ActionEvent Event) {
    CGraphSaver.saveAs(m_parent, m_graph, m_container);
  }
}
