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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CFunctionHelpers;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CShowViewFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CViewContainerFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CRemoveFunctionBreakpointsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSearchTableAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CSetFunctionBreakpointsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CStarViewsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CToggleStarViewsAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CUnstarViewAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions.COpenInLastWindowAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component.Actions.CRenameBackAction;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CStaredItemFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.tables.CopyCellAction;
import com.google.security.zynamics.zylib.gui.tables.CopySelectionAction;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;

/**
 * The popup menu that is shown when the user right-clicks on a view in a table of the main window.
 */
public class CViewPopupMenu extends JPopupMenu {
  /**
   * Creates a new view table popup menu.
   *
   * @param parent Parent window used for dialogs.
   * @param table Table that was clicked.
   * @param container Container the views belong to.
   * @param views Selected views in the table.
   * @param x X-coordinate of the mouse click.
   * @param y Y-coordinate of the mouse click.
   */
  public CViewPopupMenu(final Window parent,
      final JTable table,
      final IViewContainer container,
      final INaviView[] views,
      final int x,
      final int y) {
    Preconditions.checkNotNull(container, "IE02029: Container can't be null");
    Preconditions.checkNotNull(views, "IE02030: View argument can't be null");

    final List<CGraphWindow> windows = CWindowManager.instance().getOpenWindows();

    if (!windows.isEmpty()) {
      add(CActionProxy.proxy(new COpenInLastWindowAction(parent, container, views)));
    }

    add(CActionProxy.proxy(new OpenInNewWindowAction(parent, container, views)));

    if (!windows.isEmpty()) {
      final JMenu openInItem = new JMenu("Open in window ...");

      for (final CGraphWindow graphContainer : windows) {
        openInItem.add(
            CActionProxy.proxy(new OpenInWindowAction(parent, container, views, graphContainer)));
      }

      add(openInItem);
    }

    addSeparator();

    if (CStaredItemFunctions.allStared(views)) {
      add(CActionProxy.proxy(new CUnstarViewAction(parent, views)));
    } else if (CStaredItemFunctions.allNotStared(views)) {
      add(CActionProxy.proxy(new CStarViewsAction(parent, views)));
    } else {
      add(CActionProxy.proxy(new CToggleStarViewsAction(parent, views)));
    }

    if (canDelete(views)) {
      addSeparator();
      add(CActionProxy.proxy(new DeleteViewAction(parent, container, views)));
    }

    final boolean singleSelection = views.length == 1;

    if (singleSelection) {
      final INaviView view = views[0];

      final INaviFunction function = container.getFunction(view);

      if ((function != null) && CFunctionHelpers.isForwardableFunction(function)) {
        addSeparator();

        if (function.getForwardedFunctionAddress() == null) {
          add(CActionProxy.proxy(
              new CConnectFunctionAction(parent, container.getDatabase(), function)));
        } else {
          add(CActionProxy.proxy(new CRemoveConnectedFunctionAction(parent, function)));
        }
      }

      if ((function != null) && !function.getName().equals(function.getOriginalName())) {
        addSeparator();

        add(CActionProxy.proxy(new CRenameBackAction(parent, view, function.getOriginalName())));
      }
    }

    addSeparator();

    final List<INaviFunction> functions = new ArrayList<INaviFunction>();
    boolean anyNull = false;
    for (final INaviView view : views) {
      final INaviFunction func = container.getFunction(view);
      anyNull = func == null;
      if (anyNull) {
        break;
      }
      functions.add(func);
    }

    if (!anyNull) {
      final List<IDebugger> debuggers = new ArrayList<IDebugger>();
      anyNull = false;
      for (final INaviFunction func : functions) {
        final IDebugger dbg = container.getDebuggerProvider().getDebugger(func.getModule());
        anyNull = dbg == null;
        if (anyNull) {
          break;
        }
        debuggers.add(dbg);
      }

      // Only functions selected
      if (!anyNull) {
        final IFilledList<Pair<IDebugger, INaviFunction>> pairs =
            new FilledList<Pair<IDebugger, INaviFunction>>();
        for (int i = 0; i < debuggers.size(); i++) {
          pairs.add(new Pair<IDebugger, INaviFunction>(debuggers.get(i), functions.get(i)));
        }

        if (singleSelection) {
          if (pairs.get(0).second().getType() != FunctionType.IMPORT) {
            // Don't allow breakpoints on imported functions
            if (CGraphDebugger.hasBreakpoint(pairs.get(0).first().getBreakpointManager(),
                pairs.get(0).second().getModule(),
                new UnrelocatedAddress(pairs.get(0).second().getAddress()))) {
              add(CActionProxy.proxy(new CRemoveFunctionBreakpointsAction(pairs)));
            } else {
              add(CActionProxy.proxy(new CSetFunctionBreakpointsAction(pairs)));
            }

            addSeparator();
          }
        } else {
          add(CActionProxy.proxy(new CSetFunctionBreakpointsAction(pairs)));
          add(CActionProxy.proxy(new CRemoveFunctionBreakpointsAction(pairs)));

          addSeparator();
        }
      }
    }

    add(CActionProxy.proxy(new CopySelectionAction(table)));
    add(CActionProxy.proxy(new CopyCellAction(table, x, y)));

    add(new JSeparator());

    add(new JMenuItem(CActionProxy.proxy(new CSearchTableAction(parent, table))));
  }

  /**
   * Determines the derived views for the given views.
   *
   * @param views The base views.
   *
   * @return The derived views.
   *
   * @throws CouldntLoadDataException Thrown if the derived views could not be determined.
   */
  private static List<Pair<INaviView, List<INaviView>>> getDerivedViews(final INaviView[] views)
      throws CouldntLoadDataException {
    final List<Pair<INaviView, List<INaviView>>> derivedViews =
        new ArrayList<Pair<INaviView, List<INaviView>>>();

    for (final INaviView view : views) {
      derivedViews.add(new Pair<INaviView, List<INaviView>>(view, view.getDerivedViews()));
    }

    return derivedViews;
  }

  /**
   * Tests whether all of the selected views can be deleted.
   *
   * @param views The views to check.
   *
   * @return True, if all of the selected views can be deleted. False, otherwise.
   */
  private boolean canDelete(final INaviView[] views) {
    for (final INaviView view : views) {
      if (view.getType() != ViewType.NonNative) {
        return false;
      }
    }

    return true;
  }

  /**
   * Action class for resolving imported functions.
   */
  private static class CConnectFunctionAction extends AbstractAction {
    /**
     * Parent window used for dialogs.
     */
    private final Window parentWindow;

    /**
     * Database the passed function belongs to.
     */
    private final IDatabase database;

    /**
     * Function to be resolved.
     */
    private final INaviFunction function;

    /**
     * Creates a new action object.
     *
     * @param parent Parent window used for dialogs.
     * @param database Database the passed function belongs to.
     * @param function Function to be resolved.
     */
    public CConnectFunctionAction(final Window parent, final IDatabase database,
        final INaviFunction function) {
      super("Resolve Imported Function");

      parentWindow = parent;
      this.database = database;
      this.function = function;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CFunctionHelpers.resolveFunction(parentWindow, database, function);
    }
  }

  /**
   * Action class for removing function forwarding from one function.
   */
  private static class CRemoveConnectedFunctionAction extends AbstractAction {
    /**
     * Parent window used for dialogs.
     */
    private final Window parentWindow;

    /**
     * Function whose forwarding information is removed.
     */
    private final INaviFunction function;

    /**
     * Creates a new action object.
     *
     * @param parent Parent window used for dialogs.
     * @param function Function whose forwarding information is removed.
     */
    public CRemoveConnectedFunctionAction(final Window parent, final INaviFunction function) {
      super("Remove function forwarding");

      parentWindow = parent;
      this.function = function;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CFunctionHelpers.removeResolvedFunction(parentWindow, function);
    }
  }

  /**
   * Action class for deleting views.
   */
  private static class DeleteViewAction extends AbstractAction {
    /**
     * Parent window used for dialogs.
     */
    private final Window parentWindow;

    /**
     * Container the views belong to.
     */
    private final IViewContainer viewContainer;

    /**
     * Views to delete.
     */
    private final INaviView[] views;

    /**
     * Creates a new action object.
     *
     * @param parent Parent window used for dialogs.
     * @param viewContainer Container the views belong to.
     * @param views Views to delete.
     */
    public DeleteViewAction(final Window parent, final IViewContainer viewContainer,
        final INaviView[] views) {
      super("Delete View");

      parentWindow = Preconditions.checkNotNull(parent, "IE02260: parent argument can not be null");
      this.views = Preconditions.checkNotNull(views.clone(),
          "IE03413: views.clone() argument can not be null");
      this.viewContainer = Preconditions.checkNotNull(viewContainer,
          "IE02297: viewContainer argument can not be null");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CViewContainerFunctions.deleteViews(parentWindow, viewContainer, views);
    }
  }

  /**
   * Action object for opening views in a new window.
   */
  private static class OpenInNewWindowAction extends AbstractAction {
    /**
     * Parent window used for dialogs.
     */
    private final Window parentWindow;

    /**
     * Container the views belong to.
     */
    private final IViewContainer viewContainer;

    /**
     * Views to opened in a new window.
     */
    private final INaviView[] views;

    /**
     * Creates a new action object.
     *
     * @param parent Parent window used for dialogs.
     * @param container Container the views belong to.
     * @param views Views to opened in a new window.
     */
    public OpenInNewWindowAction(final Window parent, final IViewContainer container,
        final INaviView[] views) {
      super("Open in new window");

      parentWindow = Preconditions.checkNotNull(parent, "IE02378: parent argument can not be null");
      viewContainer =
          Preconditions.checkNotNull(container, "IE02379: container argument can not be null");
      this.views =
          Preconditions.checkNotNull(views, "IE02875: views argument can not be null").clone();

      putValue(ACCELERATOR_KEY, HotKeys.LOAD_NEW_WINDOW_HK.getKeyStroke());
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CShowViewFunctions.showViewInNewWindow(parentWindow, viewContainer, views);
    }
  }

  /**
   * Action class for opening views in a given window.
   */
  private static class OpenInWindowAction extends AbstractAction {
    /**
     * Parent window used for dialogs.
     */
    private final Window parentWindow;

    /**
     * Container the views belong to.
     */
    private final IViewContainer viewContainer;

    /**
     * Views to be opened.
     */
    private final INaviView[] views;

    /**
     * Window in which the views are opened.
     */
    private final CGraphWindow graphWindow;

    /**
     * Creates a new action object.
     *
     * @param parent Parent window used for dialogs.
     * @param container Container the views belong to.
     * @param views Views to be opened.
     * @param graphWindow Window in which the views are opened.
     */
    public OpenInWindowAction(final Window parent, final IViewContainer container,
        final INaviView[] views, final CGraphWindow graphWindow) {
      super(createTitle(graphWindow));

      parentWindow = Preconditions.checkNotNull(parent, "IE02380: parent argument can not be null");
      viewContainer =
          Preconditions.checkNotNull(container, "IE02381: container argument can not be null");
      this.views = Preconditions.checkNotNull(views.clone(),
          "Error: views.clone() argument can not be null");
      this.graphWindow =
          Preconditions.checkNotNull(graphWindow, "IE02382: graphWindow argument can not be null");
    }

    /**
     * Creates the text shown in the menu.
     *
     * @param graphWindow Window in which the graphs are opened.
     *
     * @return The menu text generated from the state of the given window.
     */
    private static String createTitle(final IGraphContainerWindow graphWindow) {
      int counter = 0;

      final StringBuffer ret = new StringBuffer();

      boolean addSlash = false;

      for (final IGraphPanel window : graphWindow) {
        if (addSlash) {
          ret.append('/');
        }

        addSlash = true;

        final INaviView view = window.getModel().getGraph().getRawView();

        final String viewName = view.getName();

        ret.append(viewName);

        counter++;

        if ((counter == 3) && (graphWindow.getOpenGraphCount() != 3)) {
          ret.append("/...");

          break;
        }
      }

      return ret.toString();
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      CShowViewFunctions.showViews(parentWindow, viewContainer, views, graphWindow);
    }
  }
}
