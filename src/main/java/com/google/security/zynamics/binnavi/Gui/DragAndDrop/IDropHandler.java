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

import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Interface to be used by all drag & drop handlers.
 */
public interface IDropHandler {
  /**
   * Asks whether the handler can handle the drag & drop event with the given arguments.
   *
   * @param parentNode The parent node the dragged node is dragged on.
   * @param draggedNode The node that is being dragged by the user.
   *
   * @return True, if the handler can handle the event. False, otherwise.
   */
  boolean canHandle(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode draggedNode);

  /**
   * Asks whether the handler can handle the drag & drop event with the given arguments.
   *
   * @param target Tree where the drag & drop operation happens.
   * @param transferable Transferable object created by the drag & drop event.
   * @param flavor Flavor to check for.
   * @param x X-location of the drag & drop event.
   * @param y Y-location of the drag & drop event.
   *
   * @return True, if the handler can handle the event. False, otherwise.
   */
  boolean canHandle(DNDTree target, Transferable transferable, DataFlavor flavor, int x, int y);

  /**
   * Tells the handler to execute a drag & drop event.
   *
   * @param target Tree where the drag & drop operation happens.
   * @param parentNode The parent node the dragged node is dragged on.
   * @param draggedNode The node that is being dragged by the user.
   */
  void drop(DNDTree target, DefaultMutableTreeNode parentNode, DefaultMutableTreeNode draggedNode);

  /**
   * Tells the handler to execute a drag & drop event.
   *
   * @param transferable Transferable object created by the drag & drop event.
   * @param parentNode The parent node the object is dragged on.
   */
  void drop(Transferable transferable, DefaultMutableTreeNode parentNode);
}
