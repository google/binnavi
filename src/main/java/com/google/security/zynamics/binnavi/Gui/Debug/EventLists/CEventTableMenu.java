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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.Actions.CSetBreakpointsAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions.COpenInLastWindowAction;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.tables.CopySelectionAction;

import java.awt.Window;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Context menu of the trace events table menu.
 */
public final class CEventTableMenu extends JPopupMenu {
  /**
   * The corresponding graph model for the events of this table.
   */
  private IGraphModel graphModel;

  /**
   * Creates a new menu object.
   *
   * @param table The table for which the menu is provided.
   * @param model Provides debugger information selected by the user.
   * @param traces Selected traces in the table.
   */
  public CEventTableMenu(final JTable table, final CDebugPerspectiveModel model,
      final List<ITraceEvent> traces) {
    graphModel = model.getGraphModel();
    addOpenFunction(SwingUtilities.getWindowAncestor(table), traces);
    add(new JMenuItem(CActionProxy.proxy(new CSetBreakpointsAction(model, traces))));
    addSeparator();
    add(new JMenuItem(CActionProxy.proxy(
        new CSearchTableAction(SwingUtilities.getWindowAncestor(table), table))));
    add(new CopySelectionAction(table));
  }

  /**
   * Creates a new menu object.
   *
   * @param table The table for which the menu is created.
   * @param traces The traces to be shown in the table.
   */
  public CEventTableMenu(final JTable table, final List<ITraceEvent> traces) {
    addOpenFunction(SwingUtilities.getWindowAncestor(table), traces);
    add(new JMenuItem(CActionProxy.proxy(
        new CSearchTableAction(SwingUtilities.getWindowAncestor(table), table))));
    add(new CopySelectionAction(table));
  }

  /**
   * Adds a menu item to open the selected trace.
   *
   * @param parent Parent window of the menu.
   * @param traces The selected traces.
   */
  private void addOpenFunction(final Window parent, final List<ITraceEvent> traces) {
    if (traces.size() == 1) {
      final ITraceEvent trace = traces.get(0);

      final INaviModule module = trace.getOffset().getModule();

      if (module.isLoaded()) {
        final INaviFunction function = module.getContent().getFunctionContainer()
            .getFunction(trace.getOffset().getAddress().getAddress());

        if (function != null) {
          final IViewContainer container = graphModel.getViewContainer();
          final INaviView view = container.getView(function);

          if (view != null) {
            add(new JMenuItem(CActionProxy.proxy(
                new COpenInLastWindowAction(parent, container, new INaviView[] {view}))));

            addSeparator();
          }
        }
      }
    }
  }
}
