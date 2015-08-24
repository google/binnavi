/*
Copyright 2015 Google Inc. All Rights Reserved.

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.Breakpoint;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointStatus;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.BreakpointManagerListener;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CStaredItemFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IFunction;
import com.google.security.zynamics.zylib.disassembly.IFunctionListener;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.HexStringComparator;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * This is the model used in the table that displays the native functions of a module on the right
 * side of the main window.
 */
public class CFunctionViewsModel extends CAbstractTreeViewsTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6047574847073142955L;

  /**
   * Names of all columns shown in the table.
   */
  private static final String[] COLUMN_NAMES = {"Address", "Name", "Description", "Module",
      "Forwarded to", "Basic Blocks", "Edges", "In", "Out"};

  /**
   * Index of the column that shows function addresses.
   */
  private static final int ADDRESS_COLUM = 0;

  /**
   * Index of the column that shows function names.
   */
  private static final int FUNCTIONNAME_COLUM = 1;

  /**
   * Index of the column that shows function descriptions.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Index of the column that shows what module a function belongs to.
   */
  private static final int MODULE_COLUMN = 3;

  /**
   * Index of the column that shows forwarding information.
   */
  private static final int FORWARDER_COLUMN = 4;

  /**
   * Index of the colum that shows the number of basic blocks in the function.
   */
  private static final int BASICBLOCK_COUNT_COLUMN = 5;

  /**
   * Index of the column that shows the numbre of edges in the function.
   */
  private static final int EDGE_COUNT_COLUMN = 6;

  /**
   * Index of the column that shows the number of calls leading into the function.
   */
  private static final int INCOMING_CALLS_COUNT_COLUMN = 7;

  /**
   * Index of the column that shows the number of calls leaving the function.
   */
  private static final int OUTGOING_CALLS_COUNT_COLUMN = 8;

  /**
   * Database the functions belong to.
   */
  private final IDatabase m_database;

  /**
   * The module object from where the information about the native functions is taken.
   */
  private final INaviModule m_module;

  /**
   * This listener is attached to the module and makes sure to update the table model once the
   * module is loaded or closed.
   */
  private final InternalModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * This listener is added to all native functions of the module. Changes in the names,
   * descriptions, ... of native functions need to displayed in the table immediately.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Views are cached so that they do not always have to be re-calculated. For this reason, this
   * attribute is protected too (subclasses want to use caching too).
   */
  private IFilledList<INaviView> m_cachedValues = null;

  /**
   * Listens on changes in the breakpoint manager to update the table model.
   */
  private final BreakpointManagerListener m_breakpointManagerListener =
      new InternalBreakpointManagerListener();

  /**
   * Active module debugger.
   */
  private IDebugger m_activeDebugger;

  /**
   * Creates a new table model object.
   * 
   * @param database Database the functions belong to.
   * @param module The module object from where the information about the native functions is taken.
   */
  public CFunctionViewsModel(final IDatabase database, final INaviModule module) {
    m_module = Preconditions.checkNotNull(module, "IE01215: Module can't be null");

    m_database = database;

    module.addListener(m_moduleListener);

    m_activeDebugger = module.getConfiguration().getDebugger();

    if (m_activeDebugger != null) {
      final BreakpointManager manager = m_activeDebugger.getBreakpointManager();

      manager.addListener(m_breakpointManagerListener);
    }

    if (module.isLoaded()) {
      for (final IFlowgraphView view : m_module.getContent().getViewContainer()
          .getNativeFlowgraphViews()) {
        view.addListener(m_viewListener);
      }

      for (final INaviFunction function : m_module.getContent().getFunctionContainer()
          .getFunctions()) {
        function.addListener(m_viewListener);
      }
    }
  }

  /**
   * Converts a list of flow graph views into normal views.
   * 
   * @param nativeFlowgraphViews The flow graph views to convert.
   * 
   * @return The converted views.
   */
  private IFilledList<INaviView> convert(final ImmutableList<IFlowgraphView> nativeFlowgraphViews) {
    final IFilledList<INaviView> views = new FilledList<INaviView>();

    for (final IFlowgraphView flowgraphView : nativeFlowgraphViews) {
      views.add(flowgraphView);
    }
    return views;
  }

  /**
   * Calculates the number of edges in a view.
   * 
   * @param view The view to calculate.
   * @param function The function represented by the view.
   * 
   * @return The number of edges in the view.
   */
  private int getEdgeCount(final INaviView view, final INaviFunction function) {
    if (view.isLoaded() || (function.getForwardedFunctionAddress() == null)) {
      // View is loaded or not forwarded => Get the active edge count

      return view.getEdgeCount();
    } else {
      final INaviModule forwardedModule =
          m_database.getContent().getModule(function.getForwardedFunctionModuleId());

      Preconditions.checkNotNull(forwardedModule, "IE01186: Unknown forwarded module");
      if (forwardedModule.isLoaded()) {
        final INaviFunction forwardedFunction =
            forwardedModule.getContent().getFunctionContainer()
                .getFunction(function.getForwardedFunctionAddress());
        Preconditions.checkNotNull(forwardedFunction, "IE01185: Unknown forwarded function");
        return forwardedFunction.getEdgeCount();
      } else {
        // If the module is not loaded we can not know how many nodes
        // there are in the function. By default we display 0 here to
        // make the column sorter happy which requires an integer.

        return 0;
      }
    }
  }

  /**
   * Calculates the content of the forwarder column for a given function.
   * 
   * @param function The function to check.
   * 
   * @return The text shown in the table for the forwarder column of the given function.
   */
  private String getForwarderColumnText(final INaviFunction function) {
    final IAddress address = function.getForwardedFunctionAddress();

    if (address == null) {
      return "-";
    }

    final int moduleId = function.getForwardedFunctionModuleId();

    final INaviModule forwardedModule = m_database.getContent().getModule(moduleId);

    if (forwardedModule == null) {
      return "INVALID";
    } else if (!forwardedModule.isLoaded()) {
      return String.format("%s:%s", forwardedModule.getConfiguration().getName(),
          address.toHexString());
    }

    final INaviFunction forwardedFunction =
        forwardedModule.getContent().getFunctionContainer().getFunction(address);

    if (forwardedFunction == null) {
      return "INVALID";
    } else {
      return String.format("%s:%s", forwardedModule.getConfiguration().getName(),
          forwardedFunction.getName());
    }
  }

  /**
   * Calculates the number of nodes in a view.
   * 
   * @param view The view to calculate.
   * @param function The function represented by the view.
   * 
   * @return The number of nodes in the view.
   */
  private int getNodeCount(final INaviView view, final INaviFunction function) {
    if (view.isLoaded() || (function.getForwardedFunctionAddress() == null)) {
      // View is loaded or not forwarded => Get the active node count
      return view.getNodeCount();
    } else {
      final INaviModule forwardedModule =
          m_database.getContent().getModule(function.getForwardedFunctionModuleId());

      Preconditions.checkNotNull(forwardedModule, "IE01188: Unknown forwarded module");
      if (forwardedModule.isLoaded()) {
        final INaviFunction forwardedFunction =
            forwardedModule.getContent().getFunctionContainer()
                .getFunction(function.getForwardedFunctionAddress());

        Preconditions.checkNotNull(forwardedFunction, "IE01187: Unknown forwarded function");
        return forwardedFunction.getBasicBlockCount();
      } else {
        // If the module is not loaded we can not know how many nodes
        // there are in the function. By default we display 0 here to
        // make the column sorter happy which requires an integer.

        return 0;
      }
    }
  }

  @Override
  public void delete() {
    m_module.removeListener(m_moduleListener);

    if (m_activeDebugger != null) {
      final BreakpointManager manager = m_activeDebugger.getBreakpointManager();

      manager.removeListener(m_breakpointManagerListener);
    }

    if (m_module.isLoaded()) {
      for (final IFlowgraphView view : m_module.getContent().getViewContainer()
          .getNativeFlowgraphViews()) {
        view.removeListener(m_viewListener);
      }

      for (final INaviFunction function : m_module.getContent().getFunctionContainer()
          .getFunctions()) {
        function.removeListener(m_viewListener);
      }
    }
  }

  @Override
  public final int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public final String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public final int getRowCount() {
    return getViews().size();
  }

  @Override
  public final List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(ADDRESS_COLUM, new HexStringComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(FUNCTIONNAME_COLUM, new FunctionNameComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(BASICBLOCK_COUNT_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(EDGE_COUNT_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(INCOMING_CALLS_COUNT_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(OUTGOING_CALLS_COUNT_COLUMN, new IntComparator()));

    return sorters;
  }

  @Override
  public final Object getValueAt(final int row, final int col) {
    final List<INaviView> views = m_cachedValues == null ? getViews() : m_cachedValues;

    final INaviView view = views.get(row);
    final INaviFunction function = m_module.getContent().getViewContainer().getFunction(view);

    Preconditions.checkNotNull(function, "IE01189: View without known function");

    switch (col) {
      case ADDRESS_COLUM:
        return function.getAddress().toHexString();
      case FUNCTIONNAME_COLUM:
        return new CFunctionNameTypePair(view.getName(), function.getType());
      case DESCRIPTION_COLUMN:
        return view.getConfiguration().getDescription();
      case MODULE_COLUMN:
        return function.getOriginalModulename();
      case FORWARDER_COLUMN:
        return getForwarderColumnText(function);
      case BASICBLOCK_COUNT_COLUMN:
        return getNodeCount(view, function);
      case EDGE_COUNT_COLUMN:
        return getEdgeCount(view, function);
      case INCOMING_CALLS_COUNT_COLUMN:
        return function.getIndegree();
      case OUTGOING_CALLS_COUNT_COLUMN:
        return function.getOutdegree();
      default:
        throw new IllegalStateException("IE02245: Unknown column");
    }
  }

  @Override
  public synchronized List<INaviView> getViews() {
    IFilledList<INaviView> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      final IFilter<INaviView> filter = getFilter();

      if (m_module.isLoaded()) {
        localCachedValues =
            filter == null ? convert(m_module.getContent().getViewContainer()
                .getNativeFlowgraphViews()) : filter.get(convert(m_module.getContent()
                .getViewContainer().getNativeFlowgraphViews()));
      } else {
        localCachedValues = new FilledList<INaviView>();
      }

      CStaredItemFunctions.sort(localCachedValues);
    }

    m_cachedValues = localCachedValues;
    return new ArrayList<INaviView>(localCachedValues);
  }

  @Override
  public final boolean isCellEditable(final int row, final int column) {
    return (FUNCTIONNAME_COLUM == column) || (DESCRIPTION_COLUMN == column);
  }

  @Override
  public void setFilter(final IFilter<INaviView> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }

  @Override
  public final void setValueAt(final Object value, final int row, final int column) {
    final INaviView view = getViews().get(row);

    if (column == DESCRIPTION_COLUMN) {
      try {
        view.getConfiguration().setDescription((String) value);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00188: " + "View name could not be changed";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view name of view '%s' could not be changed.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view was not updated and the new view name is lost."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (column == FUNCTIONNAME_COLUM) {
      try {
        view.getConfiguration().setName((String) value);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00189: " + "View description could not be changed";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view description of view '%s' could not be changed.",
                    view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view was not updated and the new view description is lost."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Listens on changes in the breakpoint manager to update the table model.
   */
  private class InternalBreakpointManagerListener extends BreakpointManagerListenerAdapter {
    @Override
    public void breakpointsAdded(final List<Breakpoint> breakpoints) {
      fireTableDataChanged();
    }

    @Override
    public void breakpointsRemoved(final Set<Breakpoint> breakpoints) {
      fireTableDataChanged();
    }

    @Override
    public void breakpointsStatusChanged(
        final Map<Breakpoint, BreakpointStatus> breakpointsToOldStatus,
        final BreakpointStatus newStatus) {
      fireTableDataChanged();
    }
  }

  /**
   * This listener is attached to the module and makes sure to update the table model once the
   * module is loaded or closed.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedDebugger(final INaviModule module, final IDebugger debugger) {
      if (m_activeDebugger != null) {
        m_activeDebugger.getBreakpointManager().removeListener(m_breakpointManagerListener);
      }

      m_activeDebugger = debugger;

      if (debugger != null) {
        debugger.getBreakpointManager().addListener(m_breakpointManagerListener);
      }

      fireTableDataChanged();
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void closedModule(final CModule module, final ICallgraphView callgraphView,
        final List<IFlowgraphView> flowgraphs) {
      m_cachedValues = null;
    }

    @Override
    public void loadedModule(final INaviModule module) {
      m_cachedValues = null;

      for (final IFlowgraphView view : m_module.getContent().getViewContainer()
          .getNativeFlowgraphViews()) {
        view.addListener(m_viewListener);
      }

      for (final INaviFunction function : m_module.getContent().getFunctionContainer()
          .getFunctions()) {
        function.addListener(m_viewListener);
      }

      fireTableDataChanged();
    }
  }

  /**
   * This listener is added to all native functions of the module. Changes in the names,
   * descriptions, ... of native functions need to displayed in the table immediately.
   */
  private class InternalViewListener extends CViewListenerAdapter implements
      IFunctionListener<IComment> {
    @Override
    public void addedEdge(final INaviView view, final INaviEdge node) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void appendedComment(final IFunction function, final IComment comment) {
      // The table does not display function comments.
    }

    @Override
    public void changedDescription(final IFunction function, final String description) {
      m_cachedValues = null;
    }

    @Override
    public void changedDescription(final INaviView view, final String description) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void changedGraphType(final INaviView view, final GraphType type, final GraphType oldType) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void changedModificationDate(final INaviView view, final Date modificationDate) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void changedName(final IFunction function, final String name) {
      m_cachedValues = null;
    }

    @Override
    public void changedName(final INaviView view, final String name) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void changedForwardedFunction(final IFunction function) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void changedStarState(final INaviView view, final boolean isStared) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void closed(final IFunction function) {
      // The table does not display whether a function is loaded or not.
    }

    @Override
    public void closedView(final INaviView view,
        final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void deletedComment(final IFunction function, final IComment comment) {
      // The table does not display function comments.
    }

    @Override
    public void deletedEdge(final INaviView view, final INaviEdge edge) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      m_cachedValues = null;
      fireTableDataChanged();
    }

    @Override
    public void editedComment(final IFunction function, final IComment comment) {
      // The table does not display function comments.
    }

    @Override
    public void initializedComment(final IFunction function, final List<IComment> comment) {
      // The table does not display function comments.
    }

    @Override
    public void loadedFunction(final IFunction function) {
      m_cachedValues = null;
    }

    @Override
    public void savedView(final INaviView view) {
      m_cachedValues = null;
      fireTableDataChanged();
    }
  }
}
