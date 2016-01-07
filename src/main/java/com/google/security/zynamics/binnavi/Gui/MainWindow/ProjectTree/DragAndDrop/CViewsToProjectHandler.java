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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CProjectFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Project.CProjectViewsContainerNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

/**
 * Drag & Drop handler for dragging views into projects.
 */
public final class CViewsToProjectHandler extends CAbstractDropHandler {
  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates a new handler object.
   * 
   * @param parent Parent window used for dialogs.
   */
  public CViewsToProjectHandler(final JFrame parent) {
    super(CViewTransferable.VIEW_FLAVOR);

    Preconditions.checkNotNull(parent, "IE01934: Parent argument can not be null");

    m_parent = parent;
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    // TODO Auto-generated method stub
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode, final Object data) {
    if (parentNode instanceof CProjectViewsContainerNode) {
      final List<INaviView> views = (List<INaviView>) data;

      if (views.isEmpty()) {
        return false;
      }

      return views.get(0).inSameDatabase(((CProjectViewsContainerNode) parentNode).getObject());
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void drop(final DefaultMutableTreeNode parentNode, final Object data) {
    final CProjectViewsContainerNode projectNode = (CProjectViewsContainerNode) parentNode;

    final INaviProject project = projectNode.getObject();

    final List<INaviView> views = (List<INaviView>) data;

    for (final INaviView view : views) {
      CProjectFunctions.copyView(m_parent, project, view);
    }
  }

  @Override
  public void drop(final DNDTree target, final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    // Views can not be dragged from nodes
  }
}
