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

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import java.util.List;


/**
 * Wrapper class for showing nodes in the selection history tree.
 */
public final class CSelectionHistoryNodeWrapper {
  /**
   * The wrapped selection state.
   */
  private final CSelectionSnapshot m_operation;

  /**
   * Index of the snapshot.
   */
  private final int m_stateIndex;

  /**
   * Creates a new wrapper object.
   *
   * @param snapshot The wrapped selection state.
   * @param stateIndex Index of the snapshot.
   */
  public CSelectionHistoryNodeWrapper(final CSelectionSnapshot snapshot, final int stateIndex) {
    m_operation = snapshot;
    m_stateIndex = stateIndex;
  }

  @Override
  public String toString() {
    int visible = 0;
    int invisible = 0;
    int selected = 0;
    int all = 0;

    final List<NaviNode> selection = m_operation.getSelection();

    for (final NaviNode graphnode : selection) {
      all++;

      if (graphnode.getRawNode().isVisible()) {
        if (graphnode.getRawNode().isSelected()) {
          selected++;
        }

        visible++;
      } else {
        invisible++;
      }
    }

    return String.format("%d-%s (%d/%d/%d/%d)",
        m_stateIndex,
        m_operation.getDescription(),
        selected,
        visible,
        invisible,
        all);
  }
}
