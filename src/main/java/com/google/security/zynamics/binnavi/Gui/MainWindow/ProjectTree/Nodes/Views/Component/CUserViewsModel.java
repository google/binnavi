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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.Timer;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CStaredItemFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainerListener;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;



/**
 * Model for displaying user views.
 */
public final class CUserViewsModel extends CAbstractTreeViewsTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6101658647465016518L;

  /**
   * Names of the columns shown by the table model.
   */
  private static final String[] COLUMN_NAMES = {"Name", "Description", "Nodes", "Edges",
      "Creation Date", "Modification Date"};

  /**
   * Index of the column that shows the view names.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column that shows the view descriptions.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column that shows the number of nodes in the views.
   */
  private static final int NODE_COUNT_COLUMN = 2;

  /**
   * Index of the column that shows the number of edges in the views.
   */
  private static final int EDGE_COUNT_COLUMN = 3;

  /**
   * Index of the column that shows the view creation dates.
   */
  private static final int CREATION_DATE_COLUMN = 4;

  /**
   * Index of the column that shows the view modification dates.
   */
  private static final int MODIFICATION_DATE_COLUMN = 5;

  /**
   * Container the views belong to.
   */
  private final IViewContainer m_originContainer;

  /**
   * Keeps the table model synchronized with the underlying raw data model.
   */
  private final CViewTableListener m_listener;

  /**
   * Views are cached for speed reasons.
   */
  private List<INaviView> m_cachedViews = null;

  /**
   * Creates a new table model object.
   * 
   * @param originContainer Container the views belong to.
   */
  public CUserViewsModel(final IViewContainer originContainer) {
    m_originContainer = originContainer;
    m_listener = new CViewTableListener(); // ATTENTION: Do not move this up; requires initialized
                                           // m_originContainer
  }

  @Override
  public void delete() {
    m_listener.dispose();
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
    return getViews().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(NODE_COUNT_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(EDGE_COUNT_COLUMN, new IntComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final List<INaviView> views = m_cachedViews == null ? getViews() : m_cachedViews;

    final INaviView view = views.get(rowIndex);

    switch (columnIndex) {
      case NAME_COLUMN:
        return view.getName();
      case DESCRIPTION_COLUMN:
        return view.getConfiguration().getDescription();
      case NODE_COUNT_COLUMN:
        return view.getNodeCount();
      case EDGE_COUNT_COLUMN:
        return view.getEdgeCount();
      case CREATION_DATE_COLUMN:
        return view.getConfiguration().getCreationDate();
      case MODIFICATION_DATE_COLUMN:
        return view.getConfiguration().getModificationDate();
      default:
        throw new IllegalStateException("IE02244: Unknown column");
    }
  }

  @Override
  public List<INaviView> getViews() {
    // A local copy is needed here for thread safety, because the module loader thread repeatedly
    // uses the
    // addedView function in the InternalContainerListener to set the m_cachedViews to null which
    // then
    // causes a null pointer exception if it is triggered after declaration and use.
    // Synchronized was not used due to speed issues.
    List<INaviView> localCachedViews = m_cachedViews;

    if (localCachedViews == null) {
      final IFilter<INaviView> filter = getFilter();

      if (m_originContainer.isLoaded()) {
        localCachedViews =
            filter == null ? m_originContainer.getUserViews() : filter.get(m_originContainer
                .getUserViews());
      } else {
        localCachedViews = new ArrayList<INaviView>();
      }
    }

    CStaredItemFunctions.sort(localCachedViews);

    m_cachedViews = localCachedViews;
    return new ArrayList<INaviView>(localCachedViews);
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return (NAME_COLUMN == column) || (DESCRIPTION_COLUMN == column);
  }

  @Override
  public void setFilter(final IFilter<INaviView> filter) {
    m_cachedViews = null;

    super.setFilter(filter);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int column) {
    final INaviView view = getViews().get(row);

    if (column == NAME_COLUMN) {
      try {
        view.getConfiguration().setName((String) value);

        fireTableDataChanged();
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00187: " + "View name could not be changed";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view name of view '%s' could not be changed.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view was not updated and the new view name is lost."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (column == DESCRIPTION_COLUMN) {
      try {
        view.getConfiguration().setDescription((String) value);

        fireTableDataChanged();
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00186: " + "View description could not be changed";
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
   * Keeps the table model synchronized with the underlying raw data model.
   */
  private class CViewTableListener {
    /**
     * Updates the model on relevant changes in the view container.
     */
    private final InternalContainerListener m_containerListener = new InternalContainerListener();

    /**
     * Updates the model on relevant changes in individual views.
     */
    private final InternalViewListener m_viewListener = new InternalViewListener();

    /**
     * Creates a new table listener object.
     */
    public CViewTableListener() {
      m_originContainer.addListener(m_containerListener);
    }

    /**
     * Frees allocated resources.
     */
    public void dispose() {
      m_originContainer.removeListener(m_containerListener);
    }

    /**
     * Updates the table model on changes in the view container.
     */
    private class InternalContainerListener implements IViewContainerListener {
      @Override
      public void addedView(final IViewContainer container, final INaviView view) {
        m_cachedViews = null;
        view.addListener(m_viewListener);
        fireTableDataChanged();
      }

      @Override
      public void closedContainer(final IViewContainer container, final List<INaviView> views) {
        m_cachedViews = null;
      }

      @Override
      public void deletedView(final IViewContainer container, final INaviView view) {
        m_cachedViews = null;
        view.removeListener(m_viewListener);
        fireTableDataChanged();
      }

      @Override
      public void loaded(final IViewContainer container) {
        m_cachedViews = null;
        for (final INaviView view : container.getViews()) {
          view.addListener(m_viewListener);
        }
      }
    }

    /**
     * Updates the model on relevant changes in individual views.
     */
    private class InternalViewListener extends CViewListenerAdapter {
      /**
       * To make sure that the table is not updated all the time, we buffer individual updating
       * reasons to update the table only once every 100ms.
       */
      private final Timer m_updateTimer = new Timer(100, new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent event) {
          fireTableDataChanged();
          m_updateTimer.stop();
        }
      });

      @Override
      public void addedEdge(final INaviView view, final INaviEdge node) {
        m_cachedViews = null;
        m_updateTimer.restart();
      }

      @Override
      public void addedNode(final INaviView view, final INaviViewNode node) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void changedDescription(final INaviView view, final String description) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void changedGraphType(final INaviView view, final GraphType type,
          final GraphType oldType) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void changedModificationDate(final INaviView view, final Date modificationDate) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void changedName(final INaviView view, final String name) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void changedStarState(final INaviView view, final boolean isStared) {
        m_cachedViews = null;
        m_updateTimer.restart();
      }

      @Override
      public void closedView(final INaviView view,
          final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void deletedEdge(final INaviView view, final INaviEdge edge) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void deletedNode(final INaviView view, final INaviViewNode node) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
        m_cachedViews = null;
        fireTableDataChanged();
      }

      @Override
      public void savedView(final INaviView view) {
        m_cachedViews = null;
        fireTableDataChanged();
      }
    }
  }
}
