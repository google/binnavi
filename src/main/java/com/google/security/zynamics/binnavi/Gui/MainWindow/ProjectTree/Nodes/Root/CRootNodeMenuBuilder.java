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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Root;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;


/**
 * Context menu builder for the project tree root node.
 */
public final class CRootNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Creates a new menu builder object.
   * 
   * @param projectTree Project tree of the main window.
   */
  public CRootNodeMenuBuilder(final JTree projectTree) {
    super(projectTree);
  }

  @Override
  protected void createMenu(final JComponent menu) {
    // The root node is invisible and has no menu
  }

  @Override
  protected JMenu getMenu() {
    return null;
  }

  @Override
  public JPopupMenu getPopupMenu() {
    return null;
  }
}
