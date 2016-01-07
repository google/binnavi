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

import java.util.ArrayList;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * Model that keeps track of the selection history of a graph.
 *
 * TODO: The selection history should be separated from the Undo/Redo handling.
 */
public final class CSelectionHistory {
  /**
   * Graph whose selection history is tracked.
   */
  private final ZyGraph m_graph;

  /**
   * Maximum number of tracked selection snapshots.
   */
  private final int m_maxSnapshots;

  /**
   * List of recorded selection states.
   */
  private final List<CSelectionSnapshot> m_snapshotList = new ArrayList<CSelectionSnapshot>();

  /**
   * Listeners that are notified about changes in the selection history.
   */
  private final ListenerProvider<ISelectionHistoryListener> m_listeners =
      new ListenerProvider<ISelectionHistoryListener>();

  /**
   * Current snapshot index for undo/redo handling.
   */
  private int m_undoIndex = -1;

  /**
   * Creates a new selection history object.
   *
   * @param graph Graph whose selection history is tracked.
   * @param maxSnapshots Maximum number of tracked selection snapshots.
   */
  public CSelectionHistory(final ZyGraph graph, final int maxSnapshots) {
    m_graph = Preconditions.checkNotNull(graph, "IE01813: Graph argument can not be null");
    Preconditions.checkArgument(maxSnapshots > 0, "IE01814: Invalid maximum snapshot number");
    m_maxSnapshots = maxSnapshots;
  }

  /**
   * Adds a new listener object that is notified about changes in the selection history.
   *
   * @param listener The listener object to add.
   */
  public void addHistoryListener(final ISelectionHistoryListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Adds a new selection snapshot to the selection history.
   *
   * @param snapshot The snapshot to add.
   */
  public void addSnapshot(final CSelectionSnapshot snapshot) {
    m_snapshotList.add(snapshot);

    if (m_snapshotList.size() > m_maxSnapshots) {
      m_snapshotList.remove(0);

      for (final ISelectionHistoryListener listener : m_listeners) {
        listener.snapshotRemoved();
      }
    }

    m_undoIndex = size() - 1;

    for (final ISelectionHistoryListener listener : m_listeners) {
      listener.snapshotAdded(snapshot);
    }
  }

  /**
   * Determines whether a redo operation is possible.
   *
   * @return True, if redo is possible. False, otherwise.
   */
  public boolean canRedo() {
    return m_undoIndex <= (size() - 1);
  }

  /**
   * Determines whether an undo operation is possible.
   *
   * @return True, if undo is possible. False, otherwise.
   */
  public boolean canUndo() {
    return m_undoIndex >= 0;
  }

  /**
   * Returns the snapshot with the given index.
   *
   * @param index The index of the snapshot.
   *
   * @return The snapshot with the given index.
   */
  public CSelectionSnapshot getSnapshot(final int index) {
    return m_snapshotList.get(index);
  }

  /**
   * Returns the current undo/redo snapshot and advances the undo/redo index.
   *
   * @param undo True, to undo a selection state. False, to redo it.
   *
   * @return The snapshot at the updated undo/redo index.
   */
  public CSelectionSnapshot getUndoSnapshot(final boolean undo) {
    if (undo) {
      if (m_undoIndex != 0) {
        m_undoIndex--;
      }

      return getSnapshot(m_undoIndex);
    } else {
      if (m_undoIndex != (size() - 1)) {
        m_undoIndex++;
      }

      return getSnapshot(m_undoIndex);
    }
  }

  /**
   * Executes a redo operation.
   */
  public void redo() {
    for (final ISelectionHistoryListener listener : m_listeners) {
      try {
        listener.startedRedo();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_graph.selectNodes(m_graph.getSelectedNodes(), false);
    m_graph.selectNodes(getUndoSnapshot(false).getSelection(), true);

    for (final ISelectionHistoryListener listener : m_listeners) {
      try {
        listener.finishedRedo();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Removes a listener object that was previously notified about changes in the selection history.
   *
   * @param listener The listener object to remove.
   */
  public void removeHistoryListener(final ISelectionHistoryListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Returns the number of tracked snapshots.
   *
   * @return The number of tracked snapshots.
   */
  public int size() {
    return m_snapshotList.size();
  }

  /**
   * Executes an undo operation.
   */
  public void undo() {
    for (final ISelectionHistoryListener listener : m_listeners) {
      try {
        listener.startedUndo();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    m_graph.selectNodes(m_graph.getSelectedNodes(), false);
    m_graph.selectNodes(getUndoSnapshot(true).getSelection(), true);

    for (final ISelectionHistoryListener listener : m_listeners) {
      try {
        listener.finishedUndo();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
