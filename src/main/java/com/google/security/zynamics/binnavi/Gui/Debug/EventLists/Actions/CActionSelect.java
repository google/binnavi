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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class that can be used to select all nodes of a graph that triggered events.
 */
public final class CActionSelect extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5857262480227781295L;

  /**
   * Graph where the trace is selected.
   */
  private final ZyGraph m_graph;

  /**
   * The trace list that provides the individual events.
   */
  private final TraceList m_list;

  /**
   * Creates a new action to select all nodes in a graph depending on the events of a trace list.
   *
   * @param graph Graph where the trace is selected.
   * @param list The trace list that provides the individual events.
   */
  public CActionSelect(final ZyGraph graph, final TraceList list) {
    super("Select");

    m_graph = Preconditions.checkNotNull(graph, "IE01287: Graph argument can not be null");
    m_list = Preconditions.checkNotNull(list, "IE01288: Trace list argument can't be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTraceFunctions.selectList(m_graph, m_list);
  }
}
