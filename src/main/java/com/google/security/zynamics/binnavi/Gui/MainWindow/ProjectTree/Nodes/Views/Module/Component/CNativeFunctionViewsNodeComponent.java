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



import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilterFieldListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Help.CViewFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CFunctionFilterCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Component that displays information about the native flowgraphs of a module.
 */
public final class CNativeFunctionViewsNodeComponent extends CTablePanel<INaviView> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8062344779729139495L;

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private final IFilterFieldListener m_filterFieldListener = new InternalFilterFieldListener();

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database the module belongs to.
   * @param module Module that contains the views to be displayed.
   * @param container Context in which the module is displayed in the tree.
   */
  public CNativeFunctionViewsNodeComponent(final JTree projectTree, final IDatabase database,
      final INaviModule module, final IViewContainer container) {
    super(new CFunctionViewsTable(projectTree, database, module, container),
        new CFunctionFilterCreator(container), new CViewFilterHelp());

    updateBorderText(String.format("%d Native Functions", module.getFunctionCount()));

    addListener(m_filterFieldListener);
  }

  /**
   * Shows a context menu when the filter field is clicked.
   */
  private class InternalFilterFieldListener implements IFilterFieldListener {
    /**
     * Shows a context menu depending on the mouse event.
     * 
     * @param event The mouse event the context menu depends on.
     */
    private void showPopupMenu(final MouseEvent event) {
      final JPopupMenu menu = new CNativeFunctionViewFilterFieldMenu(getFilterField());

      menu.show(event.getComponent(), event.getX(), event.getY());
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }
}
