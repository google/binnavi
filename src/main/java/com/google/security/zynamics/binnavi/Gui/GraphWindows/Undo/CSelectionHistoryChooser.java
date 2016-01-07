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
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.types.common.CollectionHelpers;
import com.google.security.zynamics.zylib.types.common.ICollectionFilter;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Panel where the user can select from the last graph selection states.
 */
public final class CSelectionHistoryChooser extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7667801745468915909L;

  /**
   * Graph whose selection history is shown.
   */
  private final ZyGraph m_graph;

  /**
   * Selection history model that backs this component.
   */
  private final CSelectionHistory m_selectionHistory;

  /**
   * Tree that shows the previous selection states.
   */
  private final JTree m_tree;

  /**
   * Model of the selection tree.
   */
  private DefaultTreeModel m_model;

  /**
   * Keeps track of the number of previous selection states.
   */
  private int m_stateIndex = 0;

  /**
   * Updates the history chooser on graph selection changes.
   */
  private final InternalGraphSelectionListener m_graphSelectionListener =
      new InternalGraphSelectionListener();

  /**
   * Restores previous selection states when the user clicks on them.
   */
  private final InternalTreeSelectionListener m_treeSelectionListener =
      new InternalTreeSelectionListener();

  /**
   * Handles clicks on the history chooser.
   */
  private final InternalTreeMouseListener m_treeMouseListener = new InternalTreeMouseListener();

  /**
   * Keeps the history chooser synchronized with the selection history model.
   */
  private final InternalSelectionHistoryListener m_selectionHistoryListener =
      new InternalSelectionHistoryListener();

  /**
   * Creates a new selection history object.
   * 
   * @param graph Graph whose selection history is shown.
   * @param selectionHistory Selection history model that backs this component.
   */
  public CSelectionHistoryChooser(final ZyGraph graph, final CSelectionHistory selectionHistory) {
    super(new GridLayout(1, 0));

    m_graph = graph;
    m_selectionHistory = selectionHistory;

    selectionHistory.addHistoryListener(m_selectionHistoryListener);

    m_tree = createUndoTree();

    add(new JScrollPane(m_tree));

    m_graph.addListener(m_graphSelectionListener);

    // Listen for when the selection changes.
    m_tree.addTreeSelectionListener(m_treeSelectionListener);
    m_tree.addMouseListener(m_treeMouseListener);

    ToolTipManager.sharedInstance().registerComponent(m_tree);

    setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Selection History"));
  }

  /**
   * Creates the selection history tree where the previous selection states are shown.
   * 
   * @return The created tree component.
   */
  private JTree createUndoTree() {
    final CSelectionHistoryTreeNode rootNode = new CSelectionHistoryTreeNode("Selection History");

    final JTree tree = new CUndoTree(rootNode);
    m_model = (DefaultTreeModel) tree.getModel();
    // m_model = new DefaultTreeModel(rootNode);
    // m_model.nodeStructureChanged(rootNode);

    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setRootVisible(true);
    tree.setCellRenderer(new CSelectionTreeCellRenderer());

    return tree;
  }

  /**
   * Adds a new selection state to the history chooser.
   * 
   * @param snapshot Provides information about the selection state to add.
   */
  private void insertSnapshot(final CSelectionSnapshot snapshot) {
    // New Group node
    final CSelectionHistoryTreeNode selection =
        new CSelectionHistoryTreeNode(snapshot, m_stateIndex);

    // Add the selected node addresses to the group node
    for (final NaviNode node : snapshot.getSelection()) {
      selection.add(new CNodeNode(node));
    }

    m_stateIndex++;

    // Add the new group node to the
    m_model.insertNodeInto(selection, (CSelectionHistoryTreeNode) m_model.getRoot(), 0);
  }

  /**
   * Changes the selection state of the graph by unselecting all nodes but the given nodes.
   * 
   * @param toSelect The nodes to select.
   */
  private void selectNodes(final List<NaviNode> toSelect) {
    m_graph.removeListener(m_graphSelectionListener);
    m_graph.selectNodes(m_graph.getSelectedNodes(), false);
    m_graph.selectNodes(toSelect, true);
    m_graph.addListener(m_graphSelectionListener);
  }

  /**
   * Changes the selection state of the graph by unselecting the given nodes.
   * 
   * @param toUnselect The nodes to unselect.
   */
  private void unselectNodes(final List<NaviNode> toUnselect) {
    m_graph.removeListener(m_graphSelectionListener);
    m_graph.selectNodes(toUnselect, false);
    m_graph.addListener(m_graphSelectionListener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_selectionHistory.removeHistoryListener(m_selectionHistoryListener);

    m_graph.removeListener(m_graphSelectionListener);
    m_tree.removeMouseListener(m_treeMouseListener);
    m_tree.removeTreeSelectionListener(m_treeSelectionListener);

    m_tree.setSelectionModel(new DefaultTreeSelectionModel());
  }

  /**
   * Updates the history chooser on graph selection changes.
   */
  private class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    /**
     * The last previously recorded selection snapshot.
     */
    private CSelectionSnapshot lastSnapshot;

    @Override
    public void selectionChanged() {
      // Graph selection changed => Add a new selection snapshot

      final CSelectionSnapshot snapshot = new CSelectionSnapshot(m_graph.getSelectedNodes());

      if (snapshot.size() != 0 && !snapshot.equals(lastSnapshot)) {
        m_selectionHistory.addSnapshot(snapshot);

        lastSnapshot = snapshot;
      }
    }

  }

  /**
   * Keeps the history chooser synchronized with the selection history model.
   */
  private class InternalSelectionHistoryListener implements ISelectionHistoryListener {
    @Override
    public void finishedRedo() {
      m_graph.addListener(m_graphSelectionListener);
    }

    @Override
    public void finishedUndo() {
      m_graph.addListener(m_graphSelectionListener);
    }

    @Override
    public void snapshotAdded(final CSelectionSnapshot undoSelection) {
      // Snapshot added to history => Add a new node to the tree

      insertSnapshot(undoSelection);
    }

    @Override
    public void snapshotRemoved() {
      // A snapshot was removed from the history => Remove the corresponding node
      // from the tree.

      final CSelectionHistoryTreeNode root = (CSelectionHistoryTreeNode) m_model.getRoot();

      final int children = root.getChildCount();

      final CSelectionHistoryTreeNode nodeToDelete =
          (CSelectionHistoryTreeNode) m_model.getChild(root, children - 1);

      m_model.removeNodeFromParent(nodeToDelete);
    }

    @Override
    public void startedRedo() {
      m_graph.removeListener(m_graphSelectionListener);
    }

    @Override
    public void startedUndo() {
      m_graph.removeListener(m_graphSelectionListener);
    }
  }

  /**
   * Handles clicks on the history chooser.
   */
  private class InternalTreeMouseListener extends MouseAdapter {
    /**
     * Returns the tree path for a mouse click event.
     * 
     * @param event The mouse click event.
     * 
     * @return The tree path to the location of the mouse click event.
     */
    private TreePath getTreePath(final MouseEvent event) {
      return m_tree.getPathForLocation(event.getX(), event.getY());
    }

    /**
     * Handles right-clicks on the history chooser.
     * 
     * @param clickCount Click count of the click event.
     * @param node Node to be zoomed to or centered.
     */
    private void handleRightClick(final int clickCount, final NaviNode node) {
      if (clickCount == 1) {
        MoveFunctions.centerNode(m_graph, node);
      } else if (clickCount == 2) {
        ZoomFunctions.zoomToNode(m_graph, node);
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      final TreePath path = getTreePath(event);

      if (path == null) {
        return;
      }

      if (event.getButton() == 1) {
        final Object obj = path.getLastPathComponent();

        // Select all nodes of the snapshot. If all nodes
        // are already selected, then deselect them.

        if (obj instanceof CSelectionHistoryTreeNode) {
          final CSelectionHistoryTreeNode treenode = (CSelectionHistoryTreeNode) obj;

          final List<NaviNode> selection = treenode.getSnapshot().getSelection();

          if (new HashSet<NaviNode>(selection).equals(m_graph.getSelectedNodes())) {
            unselectNodes(selection);
          } else {
            selectNodes(selection);
          }
        } else if (obj instanceof CNodeNode) {
          final CNodeNode treenode = (CNodeNode) obj;

          final NaviNode graphNode = treenode.getNode();

          final boolean graphNodeSelected = graphNode.getRawNode().isSelected();

          m_graph.removeListener(m_graphSelectionListener);
          m_graph.selectNode(graphNode, !graphNodeSelected);
          m_graph.addListener(m_graphSelectionListener);
        }
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      final TreePath path = getTreePath(event);

      if (path == null) {
        return;
      }

      if (event.getButton() == 3) {
        final Object obj = path.getLastPathComponent();

        if (obj instanceof CSelectionHistoryTreeNode) {
          final CSelectionHistoryTreeNode treenode = (CSelectionHistoryTreeNode) obj;

          final CSelectionSnapshot snapshot = treenode.getSnapshot();

          final List<NaviNode> nodes =
              CollectionHelpers.filter(snapshot.getSelection(), new ICollectionFilter<NaviNode>() {
                @Override
                public boolean qualifies(final NaviNode item) {
                  return item.isSelected() && item.isVisible();
                }
              });

          if (nodes.size() > 1) {
            ZoomFunctions.zoomToNodes(m_graph, nodes);
          } else if (nodes.size() == 1) {
            handleRightClick(event.getClickCount(), nodes.get(0));
          }
        } else if (obj instanceof CNodeNode) {
          final CNodeNode treenode = (CNodeNode) obj;

          final NaviNode graphNode = treenode.getNode();

          if (graphNode.isVisible()) {
            handleRightClick(event.getClickCount(), graphNode);
          }
        }
      }
    }
  }

  /**
   * Restores previous selection states when the user clicks on them.
   */
  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      // Every time a group node was selected, the corresponding
      // snapshot must be reloaded.

      final DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) m_tree.getLastSelectedPathComponent();

      if (node == null || node.isLeaf()) {
        return;
      }

      final int childIndex =
          m_model.getIndexOfChild(m_model.getRoot(), node) == -1 ? 0 : m_model.getIndexOfChild(
              m_model.getRoot(), node);

      final int index = m_selectionHistory.size() - childIndex - 1;

      final CSelectionSnapshot snapshot = m_selectionHistory.getSnapshot(index);

      // Listener must be removed; otherwise the snapshot restoration would
      // cause new entries to be added to the snapshot history.
      selectNodes(snapshot.getSelection());
    }
  }
}
