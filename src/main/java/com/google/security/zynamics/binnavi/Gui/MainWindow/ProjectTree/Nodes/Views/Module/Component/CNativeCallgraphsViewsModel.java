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



import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

/**
 * Table model class that is used to display the native Call graph of a module in a JTable.
 */
public final class CNativeCallgraphsViewsModel extends CAbstractTreeViewsTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1992422414886514519L;

  /**
   * Names of the all table columns.
   */
  private static final String[] COLUMN_NAMES = {"Name", "Description", "Functions", "Edges"};

  /**
   * Index of the column where the view name is shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where the view description is shown.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column that shows the number of functions in the call graph views.
   */
  private static final int FUNCTION_COUNT_COLUMN = 2;

  /**
   * Index of the column that shows the number of edges in the call graph views.
   */
  private static final int EDGE_COUNT_COLUMN = 3;

  /**
   * Module that contains the native Call graph view.
   */
  private final INaviModule m_module;

  /**
   * Listener that keeps track of changes in the native Call graph view.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Listener that keeps track of relevant changes in the module.
   */
  private final InternalModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * Creates a new native Call graph views table model.
   * 
   * @param module The module which contains the native Call graph view.
   */
  public CNativeCallgraphsViewsModel(final INaviModule module) {
    Preconditions.checkNotNull(module, "IE02049: Module argument can't be null");

    m_module = module;

    module.addListener(m_moduleListener);

    initListeners();
  }

  /**
   * Initializes the necessary listeners if a module is loaded.
   */
  private void initListeners() {
    if (m_module.isLoaded()) {
      m_module.getContent().getViewContainer().getNativeCallgraphView().addListener(m_viewListener);
    }
  }

  @Override
  public void delete() {
    m_module.removeListener(m_moduleListener);

    if (m_module.isLoaded()) {
      m_module.getContent().getViewContainer().getNativeCallgraphView()
          .removeListener(m_viewListener);
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  @Override
  public int getRowCount() {
    // There is always exactly 1 native Call graph in a module.

    return m_module.isLoaded() ? 1 : 0;
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(FUNCTION_COUNT_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(EDGE_COUNT_COLUMN, new IntComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final INaviView view = m_module.getContent().getViewContainer().getNativeCallgraphView();

    switch (col) {
      case NAME_COLUMN:
        return view.getName();
      case DESCRIPTION_COLUMN:
        return view.getConfiguration().getDescription();
      case FUNCTION_COUNT_COLUMN:
        return view.getNodeCount();
      case EDGE_COUNT_COLUMN:
        return view.getEdgeCount();
      default:
        throw new IllegalStateException("IE02246: Unknown column");
    }
  }

  @Override
  public List<INaviView> getViews() {
    final List<INaviView> views = new ArrayList<INaviView>();

    views.add(m_module.getContent().getViewContainer().getNativeCallgraphView());

    return views;
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return DESCRIPTION_COLUMN == column;
  }

  @Override
  public void setValueAt(final Object value, final int row, final int column) {
    if (column == DESCRIPTION_COLUMN) {
      final INaviView view = m_module.getContent().getViewContainer().getNativeCallgraphView();

      try {
        view.getConfiguration().setDescription((String) value);

        fireTableDataChanged();
      } catch (final CouldntSaveDataException e) {
        // TODO: Improve this
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Keeps track of relevant changes in the module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void loadedModule(final INaviModule module) {
      initListeners();

      fireTableDataChanged();
    }
  }

  /**
   * Keeps track of relevant changes in the native Call graph view.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void addedEdge(final INaviView view, final INaviEdge node) {
      fireTableDataChanged();
    }

    @Override
    public void addedNode(final INaviView view, final INaviViewNode node) {
      fireTableDataChanged();
    }

    @Override
    public void changedDescription(final INaviView view, final String description) {
      fireTableDataChanged();
    }

    @Override
    public void changedModificationDate(final INaviView view, final Date modificationDate) {
      fireTableDataChanged();
    }

    @Override
    public void changedName(final INaviView view, final String name) {
      fireTableDataChanged();
    }

    @Override
    public void closedView(final INaviView view,
        final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
      fireTableDataChanged();
    }

    @Override
    public void deletedEdge(final INaviView view, final INaviEdge edge) {
      fireTableDataChanged();
    }

    @Override
    public void deletedNode(final INaviView view, final INaviViewNode node) {
      fireTableDataChanged();
    }

    @Override
    public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
      fireTableDataChanged();
    }

    @Override
    public void loadedView(final INaviView view) {
      fireTableDataChanged();
    }

    @Override
    public void savedView(final INaviView view) {
      fireTableDataChanged();
    }
  }
}
