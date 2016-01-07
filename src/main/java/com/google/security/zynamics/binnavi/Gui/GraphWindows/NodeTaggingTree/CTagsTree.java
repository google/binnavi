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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.DragAndDrop.CDefaultTransferHandler;
import com.google.security.zynamics.binnavi.Gui.DragAndDrop.IDropHandler;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Actions.CAddRootTagNodeAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.DragDrop.CTagSortingHandler;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Implementations.CTagFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTagTreeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodeNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.CTaggedGraphNodesContainerNode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes.ITagTreeNode;
import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.NodeTaggingTree.Nodes.CRootTagTreeNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphSelectionListener;
import com.google.security.zynamics.zylib.gui.zygraph.IZyGraphVisibilityListener;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.MoveFunctions;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Tree where the available node tags are shown in graph windows.
 */
public final class CTagsTree extends DNDTree implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1074808285623025354L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph shown in the window.
   */
  private final ZyGraph m_graph;

  /**
   * Provides tag information.
   */
  private final ITagManager m_tagManager;

  /**
   * Model of the tags tree.
   */
  private final CTagsTreeModel m_model;

  /**
   * Root node of the tags tree. This is an invisible dummy node.
   */
  private final CRootTagTreeNode m_rootNode;

  /**
   * Handles clicks on the tree.
   */
  private final InternalMouseListener m_mouseListener = new InternalMouseListener();

  /**
   * Updates the tree GUI on selection changes in the graph.
   */
  private final InternalGraphSelectionListener m_graphSelectionListener =
      new InternalGraphSelectionListener();

  /**
   * Updates the tree GUI on visibility changes in the graph.
   */
  private final InternalGraphVisibilityListener m_graphVisibilityListener =
      new InternalGraphVisibilityListener();

  /**
   * The last selection path that can actually be selected.
   */
  private TreePath m_lastValidSelectionPath = null;

  /**
   * The last selected node that can actually be selected.
   */
  private CTagTreeNode m_lastValidSelectedNode = null;

  /**
   * Creates a new tree object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph shown in the window.
   * @param manager Provides tag information.
   */
  public CTagsTree(final JFrame parent, final ZyGraph graph, final ITagManager manager) {
    m_parent = Preconditions.checkNotNull(parent, "IE02308: Perent argument can not be null");
    m_graph = Preconditions.checkNotNull(graph, "IE01776: Graph can not be null");
    m_tagManager = Preconditions.checkNotNull(manager, "IE01777: Manager argument can not be null");

    m_model = new CTagsTreeModel(this);
    setModel(m_model);
    getModel().addTreeModelListener(new InternalModelListener());
    addMouseListener(m_mouseListener);
    m_graph.addListener(m_graphSelectionListener);
    m_graph.addListener(m_graphVisibilityListener);

    setRootVisible(false);

    m_rootNode = new CRootTagTreeNode(parent, this, graph, m_tagManager);
    m_model.setRoot(m_rootNode);

    setCellRenderer(new CTagTreeCellRenderer()); // ATTENTION: UNDER NO CIRCUMSTANCES MOVE THIS LINE
                                                 // ABOVE THE SETROOT LINE

    m_model.nodeStructureChanged(m_rootNode);

    final List<IDropHandler> handlers = new ArrayList<IDropHandler>();

    handlers.add(new CTagSortingHandler());

    new CDefaultTransferHandler(this, DnDConstants.ACTION_COPY_OR_MOVE, handlers);

    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(selectionModel);

    final InputMap windowImap = getInputMap(JComponent.WHEN_FOCUSED);

    windowImap.put(HotKeys.DELETE_HK.getKeyStroke(), "DELETE");
    getActionMap().put("DELETE", CActionProxy.proxy(new DeleteAction()));
  }

  /**
   * Shows the context menu for a given mouse event.
   *
   * @param event The mouse event that triggered the popup menu.
   */
  private void showPopupMenu(final MouseEvent event) {
    final ITagTreeNode selectedNode =
        (ITagTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      // Show the default menu
      final JPopupMenu popupMenu = new JPopupMenu();

      popupMenu.add(CActionProxy.proxy(new CAddRootTagNodeAction(m_parent, m_tagManager, m_rootNode
          .getTag())));

      popupMenu.show(this, event.getX(), event.getY());
    } else {
      final JPopupMenu menu = selectedNode.getPopupMenu();

      if (menu != null) {
        menu.show(this, event.getX(), event.getY());
      }
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_rootNode.dispose();

    removeMouseListener(m_mouseListener);
    m_graph.removeListener(m_graphSelectionListener);
    m_graph.removeListener(m_graphVisibilityListener);
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "This tree is used to configure and assign node tags. "
            + "Once you have assigned tags to nodes you can use this "
            + "tree to quickly select all nodes tagged with given tags.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }

  @Override
  public CTagsTreeModel getModel() {
    return m_model;
  }

  /**
   * Action class used to delete the selected tag.
   */
  private class DeleteAction extends AbstractAction {
    @Override
    public void actionPerformed(final ActionEvent event) {
      final Object component = getLastSelectedPathComponent();

      if (component instanceof CTagTreeNode) {
        CTagFunctions.deleteTag(m_parent, m_tagManager, ((CTagTreeNode) component).getTag());
      }
    }
  }

  /**
   * Updates the tree GUI on selection changes in the graph.
   */
  private class InternalGraphSelectionListener implements IZyGraphSelectionListener {
    @Override
    public void selectionChanged() {
    }
  }

  /**
   * Updates the tree GUI on visibility changes in the graph.
   */
  private class InternalGraphVisibilityListener implements IZyGraphVisibilityListener {
    @Override
    public void nodeDeleted() {
    }

    @Override
    public void visibilityChanged() {
    }
  }

  /**
   * Makes sure to update the tree if the model changed.
   */
  private class InternalModelListener implements TreeModelListener {
    /**
     * Updates the tree.
     */
    private void update() {
      if (m_lastValidSelectedNode == null) {
        validate();
      } else {
        final Integer tagId = (Integer) m_lastValidSelectedNode.getUserObject();

        final Enumeration<?> nodes = m_rootNode.breadthFirstEnumeration();

        while (nodes.hasMoreElements()) {
          final DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
          if (Objects.equals(node.getUserObject(), tagId)) {

            new SwingInvoker() {

              @Override
              protected void operation() {
                m_lastValidSelectionPath = new TreePath(getModel().getPathToRoot(node));
                getSelectionModel().setSelectionPath(m_lastValidSelectionPath);

              }
            };
            return;
          }
        }
      }
    }

    @Override
    public void treeNodesChanged(final TreeModelEvent event) {
      update();
    }

    @Override
    public void treeNodesInserted(final TreeModelEvent event) {
      update();
    }

    @Override
    public void treeNodesRemoved(final TreeModelEvent event) {
      update();
    }

    @Override
    public void treeStructureChanged(final TreeModelEvent event) {
      update();
    }
  }

  /**
   * Handles clicks on the tree.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      } else {
        final int y = event.getY();
        final int x = event.getX();
        final TreePath path = getPathForLocation(x, y);

        if (path == null) {
          return;
        }

        final Object treenode = path.getLastPathComponent();

        if (event.getButton() == 1) {
          if (treenode instanceof CTagTreeNode) {
            m_lastValidSelectedNode = (CTagTreeNode) treenode;
            m_lastValidSelectionPath = path;

            // avoids flickering
            ((CTagTreeCellRenderer) getCellRenderer()).setSelectedNode(m_lastValidSelectedNode);
          } else if (treenode instanceof CTaggedGraphNodesContainerNode) {
            final CTaggedGraphNodesContainerNode containerNode =
                (CTaggedGraphNodesContainerNode) treenode;

            final Collection<NaviNode> nodes = containerNode.getGraphNodes();

            boolean select = false;
            int countunselected = 0;
            int countinvisible = 0;
            for (final NaviNode nn : nodes) {
              if (!nn.getRawNode().isSelected()) {
                select = true;
                countunselected++;
              }

              if (!nn.getRawNode().isVisible()) {
                countinvisible++;
              }
            }

            if (((countinvisible == countunselected) || !select)
                && !m_graph.getSettings().getProximitySettings().getProximityBrowsingFrozen()) {

              m_graph.selectNodes(nodes, select);
            } else {
              final Collection<NaviNode> visiblenodes = new ArrayList<NaviNode>();
              for (final NaviNode nn : nodes) {
                if (nn.isVisible()) {
                  visiblenodes.add(nn);
                }
              }
              m_graph.selectNodes(visiblenodes, select);

            }
          } else if (treenode instanceof CTaggedGraphNodeNode) {
            final CTaggedGraphNodeNode graphNode = (CTaggedGraphNodeNode) treenode;

            final boolean graphNodeSelected = graphNode.getGraphNode().getRawNode().isSelected();

            if (!(m_graph.getSettings().getProximitySettings().getProximityBrowsingFrozen() && !graphNode
                .getGraphNode().isVisible())) {
              m_graph.selectNode(graphNode.getGraphNode(), !graphNodeSelected);
            }
          }

          new SwingInvoker() {
            @Override
            protected void operation() {
              getSelectionModel().setSelectionPath(m_lastValidSelectionPath);
            }
          }.invokeLater();
        }
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }

      final int y = event.getY();
      final int x = event.getX();
      final TreePath path = getPathForLocation(x, y);

      if (path == null) {
        return;
      }

      final Object treenode = path.getLastPathComponent();

      if ((event.getButton() == 3) && (treenode instanceof CTaggedGraphNodeNode)) {
        final CTaggedGraphNodeNode treeNode = (CTaggedGraphNodeNode) treenode;
        final NaviNode graphNode = treeNode.getGraphNode();
        if (graphNode.isVisible()) {
          if (event.getClickCount() == 1) {
            MoveFunctions.centerNode(m_graph, graphNode);
          } else if (event.getClickCount() == 2) {
            ZoomFunctions.zoomToNode(m_graph, graphNode);
          }
        }
      }
    }
  }
}
