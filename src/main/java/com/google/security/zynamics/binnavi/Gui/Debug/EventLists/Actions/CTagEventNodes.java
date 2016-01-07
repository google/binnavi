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
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Implementations.CTraceFunctions;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Action class that can be used to tag all nodes of a graph that were hit by a given event.
 */
public final class CTagEventNodes extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3068475334186948864L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph where the trace is tagged.
   */
  private final ZyGraph m_graph;

  /**
   * The trace list that provides the individual events.
   */
  private final TraceList m_list;

  /**
   * The tag used for tagging the nodes.
   */
  private final CTag m_tag;

  /**
   * Creates a new action to tag all nodes in a graph that belong to a trace list.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph where the trace is selected.
   * @param list The trace list that provides the individual events.
   * @param tag The tag used for tagging the nodes.
   */
  public CTagEventNodes(
      final JFrame parent, final ZyGraph graph, final TraceList list, final CTag tag) {
    super("Tag event nodes with selected tag");

    Preconditions.checkNotNull(parent, "IE01282: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE01379: Graph argument can not be null");

    Preconditions.checkNotNull(list, "IE01380: Trace list argument can't be null");

    Preconditions.checkNotNull(tag, "IE01283: Tag argument can not be null");

    m_parent = parent;
    m_graph = graph;
    m_list = list;
    m_tag = tag;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTraceFunctions.tagList(m_parent, m_graph, m_list, m_tag);
  }
}
