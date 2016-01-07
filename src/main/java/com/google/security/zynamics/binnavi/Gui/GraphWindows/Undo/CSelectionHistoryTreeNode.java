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

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;



/**
 * Node class for representing selection state snapshots in the selection history chooser tree.
 *
 * TODO: Separate root node from real snapshot nodes.
 */
public final class CSelectionHistoryTreeNode extends IconNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7628040230460801915L;

  /**
   * Icon shown for root nodes.
   */
  private static final ImageIcon ICON_ROOT =
      new ImageIcon(CMain.class.getResource("data/undoselectionchoosericons/root.png"));

  /**
   * Icon shown if none of the nodes of the represented selection state are selected.
   */
  private static final ImageIcon ICON_ALL_UNSELECTED_GRAPHNODES = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/graph_selection_folder_all_unselected.png"));

  /**
   * Icon shown if all nodes of the represented selection state are selected.
   */
  private static final ImageIcon ICON_ALL_SELECTED_GRAPHNODES = new ImageIcon(CMain.class
      .getResource("data/undoselectionchoosericons/graph_selection_folder_open.png"));

  /**
   * Icon shown if all nodes of the represented selection state are invisible.
   */
  private static final
      ImageIcon ICON_ALL_INVISIBLE_GRAPHNODES = new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_all_unselected_gray.png"));

  /**
   * Icon shown if all nodes of the represented selection are unselected but some are invisible.
   */
  private static final ImageIcon ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_all_unselected_halfgray.png"));

  /**
   * Icon shown if all nodes of the represented selection are visible but only some are selected.
   */
  private static final ImageIcon ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_some_unselected.png"));

  /**
   * Icon shown if some nodes of the represented selection are visible and some are selected.
   */
  private static final ImageIcon ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES =
      new ImageIcon(CMain.class.getResource(
          "data/undoselectionchoosericons/graph_selection_folder_some_unselected_halfgray.png"));

  /**
   * Icon shown for selection states without nodes.
   */
  private static final ImageIcon ICON_EMPTY_FOLDER = new ImageIcon(CMain.class.getResource(
      "data/undoselectionchoosericons/graph_selection_folder_empty.png"));

  /**
   * Icon shown if no other icon criterium matches.
   */
  private static final ImageIcon ICON_DUMMY = new ImageIcon(CMain.class.getResource(
      "data/undoselectionchoosericons/graph_selection_folder_closed.png"));

  /**
   * Selection snapshot represented by this node.
   */
  private final CSelectionSnapshot m_snapshot;

  /**
   * True, if the node is a root node. False, otherwise.
   */
  private final boolean m_root;

  /**
   * Creates a new node that represents a snapshot.
   *
   * @param snapshot Snapshot represented by the node.
   * @param stateIndex Index of the snapshot.
   */
  public CSelectionHistoryTreeNode(final CSelectionSnapshot snapshot, final int stateIndex) {
    super(new CSelectionHistoryNodeWrapper(snapshot, stateIndex));

    m_snapshot = snapshot;
    m_root = false;
  }

  /**
   * Creates a new root node.
   *
   * @param name Text shown in the root node.
   */
  public CSelectionHistoryTreeNode(final String name) {
    super(name);

    m_snapshot = new CSelectionSnapshot(new ArrayList<NaviNode>());
    m_root = true;
  }

  /**
   * Returns an icon depending on the selection and visibility state of the nodes in a selection.
   *
   * @param countAll Number of nodes.
   * @param selected Number of selected nodes.
   * @param unselected Number of unselected nodes.
   * @param invisible Number of invisible nodes.
   *
   * @return The appropriate icon.
   */
  private static Icon getIcon(
      final int countAll, final int selected, final int unselected, final int invisible) {
    if (countAll == 0) {
      return ICON_EMPTY_FOLDER;
    } else if (invisible == countAll) {
      return ICON_ALL_INVISIBLE_GRAPHNODES;
    } else if (selected == countAll) {
      return ICON_ALL_SELECTED_GRAPHNODES;
    } else if (unselected == countAll && invisible == 0) {
      return ICON_ALL_UNSELECTED_GRAPHNODES;
    } else if (selected == 0) {
      return ICON_ALL_UNSELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    } else if (invisible == 0) {
      return ICON_ALL_VISIBLE_SOME_SELECTED_SOME_UNSELECTED_GRAPHNODES;
    } else if (invisible != 0 && selected != 0) {
      return ICON_SOME_SELECTED_SOME_VISIBLE_SOME_INVISIBLE_GRAPHNODES;
    }

    return ICON_DUMMY;
  }

  @Override
  public Icon getIcon() {
    if (m_root) {
      return ICON_ROOT;
    }

    final Pair<Integer, Integer> result = CNodeTypeCounter.count(m_snapshot.getSelection());

    final int countAll = m_snapshot.getSelection().size();
    final int selected = result.first();
    final int unselected = countAll - selected;
    final int invisible = result.second();

    return getIcon(countAll, selected, unselected, invisible);
  }

  /**
   * Returns the snapshot represented by the node.
   *
   * @return The snapshot represented by the node.
   */
  public CSelectionSnapshot getSnapshot() {
    return m_snapshot;
  }
}
