// Copyright 2011-2016 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.ZyGraph.Updaters.CNodeUpdaterInitializer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Loader.CGraphBuilder;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.gui.GuiHelper;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.CRegisterHotKeys;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.functions.ZoomFunctions;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;

/**
 * Thread class for loading a view while a progress dialog is shown.
 */
public final class CViewLoader {
  /**
   * The graph window in which the graph is shown.
   */
  private final CGraphWindow window;

  /**
   * The context in which the view is opened.
   */
  private final IViewContainer container;

  /**
   * The view to be loaded.
   */
  private final INaviView view;

  /**
   * Model generated during view loading.
   */
  private CGraphModel graphModel;

  /**
   * Creates a new thread object.
   *
   * @param window The graph window in which the graph is shown.
   * @param container The context in which the view is opened.
   * @param view The view to be loaded.
   */
  public CViewLoader(final CGraphWindow window, final IViewContainer container,
      final INaviView view) {
    this.window = window;
    this.container =
        Preconditions.checkNotNull(container, "Error: Container argument can not be null.");
    this.view = Preconditions.checkNotNull(view, "Error: View argument can not be null.");
  }

  /**
   * Silly workaround function (nobody seems to remember what this function was a workaround
   * for).
   *
   * @param graph The graph to initialize.
   */
  // TODO(thomasdullien): Have a look and try to recover the reasons for this.
  private void workaroundCase874(final ZyGraph graph) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (view.getType() == ViewType.Native) {
          ZoomFunctions.zoomToScreen(graph);
        } else {
          graph.workAroundCase874();
        }
      }
    });

  }

  /**
   * Silly workaround function.
   *
   * @param panel The panel to initialize.
   */
  private void workArounds(final CGraphPanel panel) {
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        panel.updateSplitters();

        workaroundCase874(panel.getModel().getGraph());
      }
    });
  }

  /**
   * Returns the graph model generated during view loading.
   *
   * @return The graph model generated during view loading.
   */
  public CGraphModel getModel() {
    return graphModel;
  }

  /**
   * Loads a view.
   *
   * @throws LoadCancelledException Thrown if the user canceled the load operation.
   * @throws CPartialLoadException Thrown if not all required modules are loaded.
   * @throws CouldntLoadDataException Thrown if the view data could not be loaded from the database.
   */
  public void load() throws CouldntLoadDataException, CPartialLoadException,
      LoadCancelledException {
    if (!view.isLoaded()) {
      view.load();
    }

    // Convert the data from the raw view into a graph that can be displayed.
    final ZyGraph graph = container instanceof CModuleContainer ? CGraphBuilder.buildDnDGraph(view,
        container.getModules().get(0).getTypeManager())
        : CGraphBuilder.buildGraph(view);
    CRegisterHotKeys.register(graph);

    if (window == null) {
      // If the parent window of the graph is null, a new graph window is created.
      final CGraphWindow navi = new CGraphWindow();
      final CGraphModel model = new CGraphModel(navi, container.getDatabase(), container, graph);
      CNodeUpdaterInitializer.addUpdaters(model);
      final CGraphPanel panel = new CGraphPanel(model);
      navi.addGraph(panel);
      CWindowManager.instance().register(navi);

      // Part of the workaround you can find in WorkaroundListener in CGraphWindow
      navi.setSize(Toolkit.getDefaultToolkit().getScreenSize());
      navi.setVisible(true);
      GuiHelper.applyWindowFix(navi);
      navi.setExtendedState(Frame.MAXIMIZED_BOTH);
      workArounds(panel);
      graphModel = model;
    } else {
      final CGraphModel model = new CGraphModel(window, container.getDatabase(), container, graph);

      CNodeUpdaterInitializer.addUpdaters(model);
      final CGraphPanel panel = new CGraphPanel(model);

      // If a parent window is given, the graph is added to this window.
      window.addGraph(panel);
      workArounds(panel);
      window.toFront();
      graphModel = model;
    }
  }
}
