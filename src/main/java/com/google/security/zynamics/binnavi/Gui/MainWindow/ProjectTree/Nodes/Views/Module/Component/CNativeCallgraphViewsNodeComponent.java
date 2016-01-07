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



import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Component that displays information about the native callgraphs of a module.
 */
public final class CNativeCallgraphViewsNodeComponent extends CTablePanel<INaviView> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2100968846028908365L;

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param module Module that contains the views to be displayed.
   * @param container Context in which the module is displayed in the tree.
   */
  public CNativeCallgraphViewsNodeComponent(final JTree projectTree, final INaviModule module,
      final IViewContainer container) {
    super(new CNativeCallgraphViewsTable(projectTree, module, container), null, null);

    updateBorderText("Native Callgraph");
  }
}
