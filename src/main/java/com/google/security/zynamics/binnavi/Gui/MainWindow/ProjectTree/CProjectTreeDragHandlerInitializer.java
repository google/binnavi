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

import java.awt.dnd.DnDConstants;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.DragAndDrop.CDefaultTransferHandler;
import com.google.security.zynamics.binnavi.Gui.DragAndDrop.IDropHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CDatabaseSortingHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CModulesToAddressSpaceHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CTagSortingHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CViewsToProjectHandler;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop.CViewsToTagHandler;


/**
 * Contains code for initializing all drag & drop handlers for the project tree.
 */
public final class CProjectTreeDragHandlerInitializer {
  /**
   * You are not supposed to instantiate this.
   */
  private CProjectTreeDragHandlerInitializer() {
  }

  /**
   * Initializes the drag & drop handlers for the project tree.
   * 
   * @param parent Parent window of the tree.
   * @param tree Tree whose drag & drop handlers are initialized.
   * @param databaseManager Provides the database information that is shown in the tree.
   */
  public static void initialize(final JFrame parent, final CProjectTree tree,
      final CDatabaseManager databaseManager) {
    final List<IDropHandler> handlers = new ArrayList<IDropHandler>();

    handlers.add(new CViewsToProjectHandler(parent));
    handlers.add(new CViewsToTagHandler(parent));
    handlers.add(new CModulesToAddressSpaceHandler(parent));
    handlers.add(new CDatabaseSortingHandler(databaseManager));
    handlers.add(new CTagSortingHandler());

    // Initialize the Drag & Drop handler
    new CDefaultTransferHandler(tree, DnDConstants.ACTION_COPY_OR_MOVE, handlers);
  }
}
