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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTableRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Help.CNativeCallgraphViewsTableHelp;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import javax.swing.JTree;

/**
 * Table class that is used to display the native Call graph views of a module.
 */
public final class CNativeCallgraphViewsTable extends CViewsTable {

  /**
   * Creates a new table to display the native Call graph views of a module.
   * 
   * @param projectTree Project tree of the main window.
   * @param module The module which contains the native Call graph view.
   * @param container The context in which the module views are opened.
   */
  public CNativeCallgraphViewsTable(final JTree projectTree, final INaviModule module,
      final IViewContainer container) {
    super(projectTree, new CNativeCallgraphsViewsModel(module), container,
        new CNativeCallgraphViewsTableHelp());

    setDefaultRenderer(Object.class, new CViewsTableRenderer(this, container));
  }
}
