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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.Component;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * The project model is used to fill the projects table with information about the projects.
 */
public final class CProjectsModel extends CAbstractTreeTableModel<INaviProject> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6723444733980985791L;

  /**
   * Index of the column where project names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where project descriptions are shown.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column where project creation dates are shown.
   */
  private static final int CREATION_DATE_COLUMN = 2;

  /**
   * Index of the column where project modification dates are shown.
   */
  private static final int MODIFICATION_DATE_COLUMN = 3;

  /**
   * Names of the columns shown by this model.
   */
  private final String[] COLUMNS = {"Name", "Description", "Creation Date", "Modification Date"};

  /**
   * The database that contains the projects.
   */
  private final IDatabase m_database;

  /**
   * Keeps track of relevant changes in the database.
   */
  private final InternalDatabaseListener m_databaseListener = new InternalDatabaseListener();

  /**
   * Keeps track of relevant changes in the projects.
   */
  private final InternalProjectListener m_projectListener = new InternalProjectListener();

  /**
   * The displayed projects are cached for performance reasons.
   */
  private List<INaviProject> m_cachedValues = null;

  /**
   * Creates a new table model to display the projects of a database.
   * 
   * @param database The database that contains the projects.
   */
  public CProjectsModel(final IDatabase database) {
    Preconditions.checkNotNull(database, "IE01989: Database argument can't be null");

    m_database = database;

    database.addListener(m_databaseListener);

    if (database.isLoaded()) {
      final List<INaviProject> projects = database.getContent().getProjects();

      for (final INaviProject project : projects) {
        project.addListener(m_projectListener);
      }
    }
  }

  @Override
  public void delete() {
    m_database.removeListener(m_databaseListener);

    if (m_database.isLoaded()) {
      final List<INaviProject> projects = m_database.getContent().getProjects();

      for (final INaviProject project : projects) {
        project.removeListener(m_projectListener);
      }
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  /**
   * Returns the currently displayed projects.
   * 
   * @return The currently displayed projects.
   */
  public ArrayList<INaviProject> getProjects() {
    List<INaviProject> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      if (m_database.isConnected()) {
        final IFilter<INaviProject> filter = getFilter();

        localCachedValues =
            filter == null ? m_database.getContent().getProjects() : filter.get(m_database
                .getContent().getProjects());
      } else {
        localCachedValues = new ArrayList<INaviProject>();
      }
    }

    m_cachedValues = localCachedValues;
    return new ArrayList<INaviProject>(localCachedValues);
  }

  @Override
  public int getRowCount() {
    return getProjects().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return new ArrayList<Pair<Integer, Comparator<?>>>();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final INaviProject project = getProjects().get(row);

    switch (col) {
      case NAME_COLUMN:
        return project.getConfiguration().getName();
      case DESCRIPTION_COLUMN:
        return project.getConfiguration().getDescription();
      case CREATION_DATE_COLUMN:
        return project.getConfiguration().getCreationDate();
      case MODIFICATION_DATE_COLUMN:
        return project.getConfiguration().getModificationDate();
      default:
        throw new IllegalStateException("IE01171: Invalid column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return (col == NAME_COLUMN) || (col == DESCRIPTION_COLUMN);
  }

  @Override
  public void setFilter(final IFilter<INaviProject> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    if ((col != NAME_COLUMN) && (col != DESCRIPTION_COLUMN)) {
      throw new IllegalStateException("IE01172: Column can not be edited");
    }

    final INaviProject project = getProjects().get(row);

    if (col == NAME_COLUMN) {
      try {
        project.getConfiguration().setName((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00174: " + "Could not save project name";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new name of the project '%s' could not be saved.", project.getConfiguration()
                    .getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The project keeps its old name."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (col == DESCRIPTION_COLUMN) {
      try {
        project.getConfiguration().setDescription((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00175: " + "Could not save project description";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new description of the project '%s' could not be saved.", project
                    .getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The project keeps its old description."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Keeps the table model up to date when the database changes.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedProject(final IDatabase connection, final INaviProject newProject) {
      m_cachedValues = null;

      newProject.addListener(m_projectListener);

      fireTableDataChanged();
    }

    @Override
    public void deletedProject(final IDatabase database, final INaviProject project) {
      m_cachedValues = null;

      project.removeListener(m_projectListener);

      fireTableDataChanged();
    }
  }

  /**
   * Keeps the table model up to date when the project information changes.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void changedDescription(final INaviProject project, final String description) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedModificationDate(final INaviProject project, final Date date) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedName(final INaviProject project, final String name) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void loadedProject(final CProject project) {
      m_cachedValues = null;

      fireTableDataChanged();
    }
  }
}
