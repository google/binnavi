/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.DragAndDrop.IDropHandler;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

/**
 * Abstract base class for drag & drop handlers.
 */
public abstract class CAbstractDropHandler implements IDropHandler {
  /**
   * The data flavor supported by this handler.
   */
  private final DataFlavor m_flavor;

  /**
   * Creates a new drag & drop handler.
   * 
   * @param flavor The data flavor supported by this handler.
   */
  protected CAbstractDropHandler(final DataFlavor flavor) {
    m_flavor = Preconditions.checkNotNull(flavor, "IE01925: Flavor argument can not be null");
  }

  /**
   * Retrieves the dragged object from a Transferable object.
   * 
   * @param transferable The transferable object.
   * 
   * @return The dragged object or null if the dragged object could not be retrieved.
   */
  private Object getData(final Transferable transferable) {
    try {
      return transferable.getTransferData(m_flavor);
    } catch (UnsupportedFlavorException | IOException exception) {
      CUtilityFunctions.logException(exception);
      return null;
    }
  }

  /**
   * Asks the implementing subclasses whether they can handle drag & drop events with the given
   * parent node and the given dragged object.
   * 
   * @param parentNode The parent node the object is dragged on.
   * @param data The object that is dragged onto the node.
   * 
   * @return True, to signal that the handler can handle the event. False, otherwise.
   */
  protected abstract boolean canHandle(DefaultMutableTreeNode parentNode, Object data);

  /**
   * Asks the implementing subclass to execute a drag & drop event. It is guaranteed that this
   * method is only called if the canHandle method returned true for the given arguments previously.
   * 
   * @param parentNode The parent node the object is dragged on.
   * @param data The object that is dragged onto the node.
   */
  protected abstract void drop(DefaultMutableTreeNode parentNode, Object data);

  @Override
  public boolean canHandle(final DNDTree target, final Transferable transferable,
      final DataFlavor flavor, final int x, final int y) {
    if (!transferable.isDataFlavorSupported(m_flavor)) {
      return false;
    }

    final Object data = getData(transferable);

    if (data == null) {
      return false;
    }

    final TreePath pathTarget = target.getPathForLocation(x, y);

    if (pathTarget == null) {
      target.setSelectionPath(null);

      return false;
    }

    return canHandle((DefaultMutableTreeNode) pathTarget.getLastPathComponent(), data);
  }

  @Override
  public void drop(final Transferable transferable, final DefaultMutableTreeNode parentNode) {
    if (!transferable.isDataFlavorSupported(m_flavor)) {
      return;
    }

    final Object data = getData(transferable);

    if (data == null) {
      return;
    }

    drop(parentNode, data);
  }
}
