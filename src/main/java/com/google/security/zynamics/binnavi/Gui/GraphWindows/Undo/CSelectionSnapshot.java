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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Undo;

import com.google.security.zynamics.binnavi.disassembly.CNodesDisplayString;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A selection snapshot that stores the selection state of a graph at one point in time.
 */
public final class CSelectionSnapshot {
  /**
   * Selected nodes of the graph.
   */
  private final List<NaviNode> m_selectedNodes;

  /**
   * Creates a new selection snapshot.
   *
   * @param selectedNodes Selected nodes in the graph.
   */
  public CSelectionSnapshot(final Collection<NaviNode> selectedNodes) {
    m_selectedNodes = new ArrayList<NaviNode>(selectedNodes);
  }

  @Override
  public boolean equals(final Object rhs) {
    return rhs instanceof CSelectionSnapshot
        && ((CSelectionSnapshot) rhs).m_selectedNodes.equals(m_selectedNodes);
  }

  /**
   * Returns the description string of the snapshot.
   *
   * @return The description string of the snapshot.
   */
  public String getDescription() {
    return String.format("Selection %s", m_selectedNodes.isEmpty() || m_selectedNodes.size() > 1
        ? "Group" : CNodesDisplayString.getDisplayString(m_selectedNodes.get(0)));
  }

  /**
   * Returns the selected nodes of the snapshot.
   *
   * @return The selected nodes of the snapshot.
   */
  public List<NaviNode> getSelection() {
    return m_selectedNodes;
  }

  @Override
  public int hashCode() {
    return m_selectedNodes.hashCode();
  }

  /**
   * Returns the number of nodes in the selection snapshot.
   *
   * @return The number of nodes in the selection snapshot.
   */
  public int size() {
    return m_selectedNodes.size();
  }
}
