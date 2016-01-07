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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.Component;



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeViewsTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Views model that is used to display an arbitrary list of views.
 */
public final class CArbitraryViewsModel extends CAbstractTreeViewsTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7352980312308189539L;

  /**
   * Names of the columns shown by the model.
   */
  private static final String COLUMN_NAMES[] =
      {"Type", "Name", "Description", "Nodes", "Edges", "Creation Date", "Modification Date"};

  /**
   * Index of the column that shows the view types.
   */
  private static final int TYPE_COLUMN = 0;

  /**
   * Index of the column that shows the view names.
   */
  private static final int NAME_COLUMN = 1;

  /**
   * Index of the column that shows the view descriptions.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Index of the column that shows the view node counts.
   */
  private static final int NODE_COUNT_COLUMN = 3;

  /**
   * Index of the column that shows the view edge counts.
   */
  private static final int EDGE_COUNT_COLUMN = 4;

  /**
   * Index of the column that shows the view creation dates.
   */
  private static final int CREATION_DATE_COLUMN = 5;

  /**
   * Index of the column that shows the view modification dates.
   */
  private static final int MODIFICATION_DATE_COLUMN = 6;

  /**
   * Views shown in the table.
   */
  private final IFilledList<INaviView> m_views = new FilledList<INaviView>();

  @Override
  public void delete() {
    // No listeners to delete
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
    return m_views.size();
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
    final INaviView view = m_views.get(rowIndex);

    switch (columnIndex) {
      case TYPE_COLUMN:
        return view.getType() == ViewType.Native ? "Native" : "Not Native";
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
        throw new IllegalStateException("IE02242: Unknown column");
    }
  }

  @Override
  public List<INaviView> getViews() {
    return m_views;
  }

  @Override
  public boolean isCellEditable(final int row, final int column) {
    return (NAME_COLUMN == column) || (DESCRIPTION_COLUMN == column);
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

        final String innerMessage = "E00184: " + "View name could not be changed";
        final String innerDescription = CUtilityFunctions.createDescription(
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

        final String innerMessage = "E00185: " + "View description could not be changed";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The view description of view '%s' could not be changed.", view.getName()),
            new String[] {"There was a problem with the database connection."},
            new String[] {"The view was not updated and the new view description is lost."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Updates the views shown in the table.
   *
   * @param views The views shown in the table.
   */
  public void setViews(final List<INaviView> views) {
    m_views.clear();
    m_views.addAll(views);
    fireTableDataChanged();
  }
}
