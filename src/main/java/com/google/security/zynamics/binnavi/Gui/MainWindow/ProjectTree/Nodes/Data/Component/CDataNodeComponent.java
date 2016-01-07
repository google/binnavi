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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;



import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation.CNavigationPanel;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.jidesoft.swing.JideSplitPane;

/**
 * This is the component that is shown when the data node of a module is selected in the project
 * tree.
 */
public final class CDataNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8854928455082131440L;

  private final DataSectionComponent dataSectionComponent;

  /**
   * Creates a new data node component.
   * 
   * @param module Module whose data is shown in the component.
   * @param originContainer
   */
  public CDataNodeComponent(final INaviModule module, final IViewContainer originContainer) {
    super(new BorderLayout());

    Preconditions.checkNotNull(module, "IE01960: Module argument can not be null");

    final JideSplitPane splitPane = new JideSplitPane(JideSplitPane.VERTICAL_SPLIT);

    splitPane.setDoubleBuffered(true);
    splitPane.setOneTouchExpandable(true);
    splitPane.setMinimumSize(new Dimension(0, 0));
    splitPane.setProportionalLayout(true);
    splitPane.setInitiallyEven(true);

    final JPanel panel = new JPanel(new BorderLayout());
    dataSectionComponent = new DataSectionComponent(module, originContainer);
    panel.add(dataSectionComponent);

    final JTabbedPane pane = new JTabbedPane();
    pane.addTab("Navigation", new CNavigationPanel(dataSectionComponent.getHexView()));

    splitPane.addPane(panel);
    splitPane.addPane(pane);
    add(splitPane);
  }

  public DataSectionComponent getDataSectionComponent() {
    return dataSectionComponent;
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
  }
}
