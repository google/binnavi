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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.CDataNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component.CTracesNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.CModuleViewsContainerComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.CNativeFunctionViewsNodeComponent;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

/**
 * Component that is shown when module nodes are selected in the project tree.
 */
public final class CModuleNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7941100284678927190L;

  /**
   * Icon shown in the Overview tab.
   */
  private static final Icon OVERVIEW_ICON =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module.png"));

  /**
   * Icon shown in the Functions tab.
   */
  private static final Icon FUNCTIONS_ICON = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/native_flowgraph_views_container.png"));

  /**
   * Icon shown in the Views tab.
   */
  private static final Icon VIEWS_ICON =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/view4.png"));

  /**
   * Icon shown in the Traces tab.
   */
  private static final Icon TRACES_ICON =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/debug_traces.png"));

  /**
   * Icon shown in the Data tab.
   */
  private static final Icon DATA_ICON =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/chart_curve.png"));

  /**
   * Module whose information is shown in the panel.
   */
  private final INaviModule module;

  /**
   * Shows the individual tabs for the module.
   */
  private final JTabbedPane tabbedPane = new JTabbedPane();

  /**
   * Shows Overview information.
   */
  private final CModuleOverviewPanel overviewPanel;

  /**
   * Shows Functions information.
   */
  private final CNativeFunctionViewsNodeComponent functionsPanel;

  /**
   * Shows Views information.
   */
  private final CModuleViewsContainerComponent viewsPanel;

  /**
   * Shows Traces information.
   */
  private final CTracesNodeComponent tracesPanel;

  /**
   * Shows Data information.
   */
  private final CDataNodeComponent dataPanel;

  /**
   * Updates the pane on changes in the module.
   */
  private final IModuleListener internalModuleListener = new InternalModuleListener();

  private static final Map<INaviModule, CModuleNodeComponent> lookup =
      new HashMap<INaviModule, CModuleNodeComponent>();

  /**
   * Creates a new component object.
   *
   * @param projectTree Project tree that is updated when certain events happen.
   * @param database Database where the module is stored.
   * @param addressSpace Address space the module belongs to (this argument can be null in case of a
   *        global module).
   * @param module Module that provides the information displayed in the component.
   * @param container Container that represents the context in which the module is displayed.
   */
  public CModuleNodeComponent(final JTree projectTree, final IDatabase database,
      final INaviAddressSpace addressSpace, final INaviModule module,
      final IViewContainer container) {
    super(new BorderLayout());

    Preconditions.checkNotNull(database, "IE01977: Database argument can't be null");
    this.module = Preconditions.checkNotNull(module, "IE01978: Module argument can't be null");

    // Note: the order of creating tabs must not be changed since, e.g., focusSectionAddress relies
    // upon a fixed index.
    tabbedPane.addTab("Overview", OVERVIEW_ICON, overviewPanel =
        new CModuleOverviewPanel(projectTree, database, addressSpace, module, container));
    tabbedPane.addTab("Functions" + String.format(" (%d)", module.getFunctionCount()),
        FUNCTIONS_ICON, functionsPanel =
            new CNativeFunctionViewsNodeComponent(projectTree, database, module, container));
    tabbedPane.addTab("Views" + String.format(" (%d)", module.getCustomViewCount()), VIEWS_ICON,
        viewsPanel = new CModuleViewsContainerComponent(projectTree, container));
    tabbedPane.addTab("Debug Traces", TRACES_ICON,
        tracesPanel = new CTracesNodeComponent(projectTree, container));
    tabbedPane.addTab(
        "Sections and Types", DATA_ICON, dataPanel = new CDataNodeComponent(module, container));

    tabbedPane.setEnabledAt(1, module.isLoaded());
    tabbedPane.setEnabledAt(2, module.isLoaded());
    tabbedPane.setEnabledAt(3, module.isLoaded());
    tabbedPane.setEnabledAt(4, module.isLoaded());

    module.addListener(internalModuleListener);

    add(tabbedPane);
    lookup.put(module, this);
  }

  public static void focusSectionAddress(
      final INaviModule module, final Section section, final long address) {
    final CModuleNodeComponent component = lookup.get(module);
    final JTabbedPane tabbedPane = component.tabbedPane;
    final Window window = SwingUtilities.getWindowAncestor(tabbedPane);
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        window.toFront();
        window.repaint();
        tabbedPane.setSelectedIndex(4 /* index of the binary data component */);
        component.dataPanel.getDataSectionComponent().scrollToSectionAddress(section, address);
      }
    });
  }

  public static void focusTypeInstance(final INaviModule module, final TypeInstance instance) {
    final CModuleNodeComponent component = 
        Preconditions.checkNotNull(lookup.get(module),
            "Attempting to give focus to the type window, but the CModuleNodeComponent is null.");
    final JTabbedPane tabbedPane = Preconditions.checkNotNull(component.tabbedPane,
        "Attempting to give focus to the type window, but the corresponding tabbed pane is null.");
    final Window window = Preconditions.checkNotNull(SwingUtilities.getWindowAncestor(tabbedPane),
        "Attempting to give focus to the type window, but the tabbedPane has no Window ancestor.");
    
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        window.toFront();
        window.repaint();
        tabbedPane.setSelectedIndex(4 /* index of the binary data component */);
        component.dataPanel.getDataSectionComponent().scrollToInstance(instance);
      }
    });
  }

  @Override
  public void dispose() {
    module.removeListener(internalModuleListener);
    overviewPanel.dispose();
    functionsPanel.dispose();
    viewsPanel.dispose();
    tracesPanel.dispose();
    dataPanel.dispose();
    lookup.remove(module);
  }

  /**
   * Updates the pane on changes in the module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      tabbedPane.setTitleAt(2, "Views" + String.format(" (%d)", module.getCustomViewCount()));
    }

    @Override
    public void deletedView(final INaviModule module, final INaviView view) {
      tabbedPane.setTitleAt(2, "Views" + String.format(" (%d)", module.getCustomViewCount()));
    }

    @Override
    public void loadedModule(final INaviModule module) {
      tabbedPane.setEnabledAt(1, module.isLoaded());
      tabbedPane.setEnabledAt(2, module.isLoaded());
      tabbedPane.setEnabledAt(3, module.isLoaded());
      tabbedPane.setEnabledAt(4, module.isLoaded());
    }
  }
}
