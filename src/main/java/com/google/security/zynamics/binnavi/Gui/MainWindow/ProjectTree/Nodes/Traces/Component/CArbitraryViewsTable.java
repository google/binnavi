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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component;

import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.CViewsTable;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Views table that can be used to display an arbitrary list of views.
 */
public final class CArbitraryViewsTable extends CViewsTable {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7916066249143784323L;

  /**
   * Creates a new table object.
   * 
   * @param projectTree Project tree of the main window.
   * @param model Table model of the table.
   * @param container Container that owns the views shown in the table.
   * @param helpInfo Provides context-sensitive information for the table.
   */
  public CArbitraryViewsTable(final JTree projectTree, final CAbstractTreeViewsTableModel model,
      final IViewContainer container, final IHelpInformation helpInfo) {
    super(projectTree, model, container, helpInfo);
  }
}
