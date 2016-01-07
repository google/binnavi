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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.standardplugins.utils.IconNodeRenderer;
import com.google.security.zynamics.binnavi.standardplugins.utils.TreeHelpers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JTree;

/**
 * This class is used to show all functions and basic blocks of a module in a tree structure.
 */
public final class FunctionTree extends JTree {

  /**
   * Creates a new function tree object.
   * 
   * @param parent The function dialog where the tree is shown.
   * @param module The module that provides the functions and basic blocks.
   */
  public FunctionTree(final JDialog parent, final Module module) {
    setModel(new FilteredTreeModel(parent, module));
    setRootVisible(false);
    setCellRenderer(new IconNodeRenderer()); // ATTENTION: UNDER NO CIRCUMSTANCES MOVE THIS LINE
                                             // ABOVE THE SETROOT LINE
    addMouseListener(new InternalMouseListener());
  }

  /**
   * Passes double-click events to the nodes to handle them.
   */
  private void handleDoubleClick(final MouseEvent event) {
    final IFunctionTreeNode selectedNode =
        (IFunctionTreeNode) TreeHelpers.getNodeAt(this, event.getX(), event.getY());

    if (selectedNode == null) {
      return;
    }

    selectedNode.doubleClicked();
  }

  public void dispose() {
    ((FunctionTreeRootNode) getModel().getRoot()).dispose();
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
  }
}
