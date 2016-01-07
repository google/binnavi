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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component;

import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CViewContainerFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Help.CUserViewsTableHelp;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import javax.swing.JTree;

/**
 * Table class that can display custom views.
 */
public final class CUserViewsTable extends CViewsTable {

  /**
   * Context in which the views are opened.
   */
  private final IViewContainer m_contextContainer;

  /**
   * Creates a new custom views table for module views of a given type.
   * 
   * @param projectTree Project tree of the main window.
   * @param originContainer Container the views belong to.
   * @param contextContainer Context in which the views are opened.
   */
  public CUserViewsTable(final JTree projectTree, final IViewContainer originContainer,
      final IViewContainer contextContainer) {
    super(projectTree, new CUserViewsModel(originContainer), contextContainer,
        new CUserViewsTableHelp());

    m_contextContainer = contextContainer;

    setDefaultRenderer(Object.class, new CViewsTableRenderer(this, originContainer));
  }

  @Override
  protected void deleteRows() {
    CViewContainerFunctions.deleteViews(getParentWindow(), m_contextContainer,
        getViews(getSortSelectedRows()));
  }
}
