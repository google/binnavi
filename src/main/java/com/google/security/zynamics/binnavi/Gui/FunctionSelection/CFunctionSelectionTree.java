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
package com.google.security.zynamics.binnavi.Gui.FunctionSelection;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.zylib.gui.jtree.IconNodeRenderer;
import com.google.security.zynamics.zylib.gui.jtree.TreeHelpers;

/**
 * Tree where the available functions to select from are displayed.
 */
public final class CFunctionSelectionTree extends JTree {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5835857188599185473L;

  /**
   * Creates a new function selection tree.
   *
   * @param database The database where the functions come from.
   * @param actionProvider Provides the implementations of the available actions.
   */
  public CFunctionSelectionTree(final IDatabase database, final IActionProvider actionProvider) {
    Preconditions.checkNotNull(database, "IE01575: Database argument can not be null");

    Preconditions.checkNotNull(actionProvider, "IE01576: Action provider argument can not be null");

    final DefaultTreeModel model = new DefaultTreeModel(null);
    setModel(model);

    setRootVisible(false);

    // Set the root node of the tree.
    model.setRoot(new CRootNode(database, model, actionProvider));

    // Each node has its own custom icon.
    setCellRenderer(new IconNodeRenderer());

    addMouseListener(new InternalMouseListener());
  }

  /**
   * Passes double-click events to the nodes to handle them.
   *
   * @param event The mouse event to handle.
   */
  private void handleDoubleClick(final MouseEvent event) {
    final IFunctionTreeNode selectedNode = (IFunctionTreeNode) TreeHelpers.getNodeAt(
        this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.doubleClicked();
  }

  /**
   * Handles double-clicks on nodes.
   */
  private class InternalMouseListener extends MouseAdapter {
    @Override
    public void mouseClicked(final MouseEvent event) {
      if (event.getClickCount() == 2 && event.getButton() == MouseEvent.BUTTON1) {
        handleDoubleClick(event);
      }
    }
  }
}
