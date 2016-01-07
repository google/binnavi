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
package com.google.security.zynamics.binnavi.Gui.DragAndDrop;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.dndtree.AbstractTreeTransferHandler;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

/**
 * Provides Drag & Drop management for trees.
 */
public final class CDefaultTransferHandler extends AbstractTreeTransferHandler {
  /**
   * List of handlers that are asked for handling drag & drop events.
   */
  private final List<IDropHandler> m_handlers;

  /**
   * The last handler that agreed to handle a drag & drop event. This is the handler which is used
   * when the drag & drop event needs to be executed.
   */
  private IDropHandler m_activeHandler = null;

  /**
   * Creates a new drag & drop handler for the tree.
   *
   * @param tree The tree to handle drag & drop events for.
   * @param action The supported drag & drop action.
   * @param handlers List of handlers that are asked for handling drag & drop events.
   */
  public CDefaultTransferHandler(
      final DNDTree tree, final int action, final List<IDropHandler> handlers) {
    super(tree, action, true);

    Preconditions.checkNotNull(handlers, "IE01930: Handlers argument can not be null");

    for (final IDropHandler handler : handlers) {
      Preconditions.checkNotNull(handler, "IE01931: Handlers list contains a null-element");
    }

    m_handlers = new ArrayList<IDropHandler>(handlers);
  }

  @Override
  protected boolean canPerformAction(final DNDTree target, final DataFlavor flavor,
      final Transferable transferable, final int action, final Point location) {
    for (final IDropHandler handler : m_handlers) {
      if (handler.canHandle(target, transferable, flavor, location.x, location.y)) {
        m_activeHandler = handler;

        return true;
      }
    }

    return false;
  }

  @Override
  protected boolean executeDrop(final DNDTree tree, final Transferable transferable,
      final DefaultMutableTreeNode newParentNode, final int action) {
    m_activeHandler.drop(transferable, newParentNode);

    return true;
  }

  @Override
  public boolean canPerformAction(final DNDTree target, final DefaultMutableTreeNode draggedNode,
      final int action, final Point location) {
    final TreePath pathTarget = target.getPathForLocation(location.x, location.y);

    if (pathTarget == null) {
      target.setSelectionPath(null);

      return false;
    }

    target.setSelectionPath(pathTarget);

    final DefaultMutableTreeNode parentNode =
        (DefaultMutableTreeNode) pathTarget.getLastPathComponent();

    for (final IDropHandler handler : m_handlers) {
      if (handler.canHandle(parentNode, draggedNode)) {
        m_activeHandler = handler;

        return true;
      }
    }

    return false;
  }

  @Override
  public boolean executeDrop(final DNDTree target, final DefaultMutableTreeNode draggedNode,
      final DefaultMutableTreeNode newParentNode, final int action) {
    m_activeHandler.drop(target, newParentNode, draggedNode);

    return true;
  }

}
