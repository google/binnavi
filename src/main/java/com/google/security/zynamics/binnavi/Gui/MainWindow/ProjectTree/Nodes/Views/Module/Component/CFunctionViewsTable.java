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

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTableRenderer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Help.CFunctionViewsTableHelp;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import javax.swing.JTree;

/**
 * This table is used to display Flow graph views in the main window.
 */
public final class CFunctionViewsTable extends CViewsTable {

  /**
   * Creates a new table which is used to display Flow graph views of any type.
   * 
   * @param projectTree The project tree of the main window.
   * @param database The database the module belongs to.
   * @param module The module the Flow graph views are taken from.
   * @param container The context in which the module views are opened.
   */
  public CFunctionViewsTable(final JTree projectTree, final IDatabase database,
      final INaviModule module, final IViewContainer container) {
    super(projectTree, new CFunctionViewsModel(database, module), container,
        new CFunctionViewsTableHelp());

    setDefaultRenderer(Object.class, new CViewsTableRenderer(this, container));
  }
}
