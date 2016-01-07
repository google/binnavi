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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree;



import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CAddDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.IProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Root.CRootNode;
import com.google.security.zynamics.binnavi.Help.CHelpFunctions;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;

/**
 * The project tree is the tree in the main window where all databases, projects, address spaces,
 * modules, views, and so on are displayed.
 * 
 */
public final class CProjectTree extends DNDTree implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1615290227300451827L;

  /**
   * There is a default popup associated with the tree that is shown when the user right-clicks on
   * the tree but doesn't hit a node.
   */
  private final JPopupMenu m_popup = new JPopupMenu();

  /**
   * The root node of the project tree is a hidden dummy node.
   */
  private final CRootNode m_rootNode;

  /**
   * Tree model of the project tree.
   */
  private final CProjectTreeModel m_treeModel;

  /**
   * Creates a new project tree component.
   * 
   * @param parent Parent window used for dialogs.
   * @param databaseManager Provides the database information that is shown in the tree.
   */
  public CProjectTree(final JFrame parent, final CDatabaseManager databaseManager) {
    // ATTENTION: DO NOT USE SETPREFERREDSIZE IN THIS TREE. TREES THAT HAVE A PREFERRED
    // SIZE ARE NOT CORRECTLY SCROLLED IF THEY ARE PLACED IN JSPLITPANES.

    Preconditions.checkNotNull(databaseManager, "IE01840: Database manager object can't be null");

    m_treeModel = new CProjectTreeModel(this);
    setModel(m_treeModel);

    CProjectTreeDragHandlerInitializer.initialize(parent, this, databaseManager);

    // Initialize the handler for showing popup context menus
    addMouseListener(new InternalMouseListener());

    setScrollsOnExpand(false);
    setRootVisible(false);

    // Set the root node of the tree.
    m_rootNode = new CRootNode(this, databaseManager);
    m_treeModel.setRoot(m_rootNode);

    // Each node has its own custom icon.
    setCellRenderer(new IconNodeRenderer()); // ATTENTION: UNDER NO CIRCUMSTANCES MOVE THIS LINE
                                             // ABOVE THE SETROOT LINE

    m_popup.add(CActionProxy.proxy(new CAddDatabaseAction(this)));
  }

  /**
   * Passes double-click events to the nodes to handle them.
   * 
   * @param event The event to handle.
   */
  private void handleDoubleClick(final MouseEvent event) {
    final IProjectTreeNode selectedNode =
        (IProjectTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.doubleClicked();
  }

  /**
   * Shows a popup menu that depends on the node that was clicked.
   * 
   * @param event The event to handle.
   */
  private void showPopupMenu(final MouseEvent event) {
    final IProjectTreeNode selectedNode =
        (IProjectTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      // Show the default menu
      m_popup.show(this, event.getX(), event.getY());
    } else {
      setSelectionPath(new TreePath(((DefaultMutableTreeNode) selectedNode).getPath()));

      final JPopupMenu menu = selectedNode.getPopupMenu();

      if (menu != null) {
        menu.show(this, event.getX(), event.getY());
      }
    }
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return new IHelpInformation() {
      @Override
      public String getText() {
        return "The Project Tree gives a quick overview of the BinNavi databases you have configured and the data stored in the databases.";
      }

      @Override
      public URL getUrl() {
        return CHelpFunctions.urlify(CHelpFunctions.MAIN_WINDOW_FILE);
      }
    };
  }

  @Override
  public CProjectTreeModel getModel() {
    return m_treeModel;
  }

  /**
   * Returns the root node of the project tree.
   * 
   * @return The root node of the project tree.
   */
  public CRootNode getRootNode() {
    return m_rootNode;
  }

  /**
   * Handles double-clicks and right-clicks on nodes.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if ((event.getClickCount() == 2) && (event.getButton() == MouseEvent.BUTTON1)) {
        handleDoubleClick(event);
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
