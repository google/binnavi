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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;


/**
 * Contains helper functions for updating the project tree after certain operations are executed.
 */
public final class CNodeExpander {
  /**
   * You are not supposed to instantiate this class.
   */
  private CNodeExpander() {
  }

  /**
   * Expands the project tree node that represents the given object.
   * 
   * @param tree The project tree.
   * @param object Object whose node is expanded.
   */
  public static void expandNode(final JTree tree, final Object object) {
    tree.expandPath(new TreePath(findNode(tree, object).getPath()));

    tree.validate();
  }

  /**
   * Finds a project tree node that represents a given object.
   * 
   * @param tree The project tree.
   * @param object Object to search for.
   * 
   * @return The project tree node represents the given object.
   */
  public static CProjectTreeNode<?> findNode(final JTree tree, final Object object) {
    final CProjectTreeNode<?> root = (CProjectTreeNode<?>) tree.getModel().getRoot();
    final Enumeration<?> nodes = root.breadthFirstEnumeration();

    while (nodes.hasMoreElements()) {
      final CProjectTreeNode<?> node = (CProjectTreeNode<?>) nodes.nextElement();

      if (node.getObject() == object) {
        return node;
      }
    }

    return null;
  }

  /**
   * Selects the project tree node that represents the given object.
   * 
   * @param tree The project tree.
   * @param object Object whose node is selected.
   */
  public static void setSelectionPath(final JTree tree, final Object object) {
    tree.setSelectionPath(new TreePath(new Object[] {tree.getModel().getRoot(),
        findNode(tree, object)}));

    tree.validate();
  }

}
