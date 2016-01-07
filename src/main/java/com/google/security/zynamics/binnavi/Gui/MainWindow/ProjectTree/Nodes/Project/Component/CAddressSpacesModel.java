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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * This model is used in the address space table of the main window that displays all address spaces
 * of a project.
 */
public final class CAddressSpacesModel extends CAbstractTreeTableModel<INaviAddressSpace> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1003200350051243176L;

  /**
   * Index of the column where address space names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where address space descriptions are shown.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column where the number of modules in the address spaces are shown.
   */
  private static final int MODULES_COLUMN = 2;

  /**
   * Index of the column where the address space creation dates are shown.
   */
  private static final int CREATION_DATE_COLUMN = 3;

  /**
   * Index of the column where the address space modification dates are shown.
   */
  private static final int MODIFICATION_DATE_COLUMN = 4;

  /**
   * Names of the columns shown by this table.
   */
  private final String[] COLUMNS = {"Name", "Description", "Modules", "Creation Date",
      "Modification Date"};

  /**
   * The project that contains the address spaces that are shown by the model.
   */
  private final INaviProject m_project;

  /**
   * Keeps track of changes in the project.
   */
  private final InternalProjectListener m_projectListener = new InternalProjectListener();

  /**
   * Keeps track of changes in the address spaces.
   */
  private final InternalAddressSpaceListener m_addressSpaceListener =
      new InternalAddressSpaceListener();

  private final InternalAddressSpaceConfigurationListener m_addressSpaceConfigurationListener =
      new InternalAddressSpaceConfigurationListener();

  /**
   * The displayed address spaces are cached for performance reasons.
   */
  private List<INaviAddressSpace> m_cachedValues = null;

  /**
   * Creates an address spaces model that displays information about the address spaces of a given
   * project.
   * 
   * @param project The project that contains the information about the address spaces.
   */
  public CAddressSpacesModel(final INaviProject project) {
    m_project = Preconditions.checkNotNull(project, "IE01984: Project argument can't be null");

    project.addListener(m_projectListener);

    if (project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : project.getContent().getAddressSpaces()) {
        addressSpace.addListener(m_addressSpaceListener);
        addressSpace.getConfiguration().addListener(m_addressSpaceConfigurationListener);
      }
    }
  }

  @Override
  public void delete() {
    m_project.removeListener(m_projectListener);

    if (m_project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        addressSpace.removeListener(m_addressSpaceListener);
        addressSpace.getConfiguration().removeListener(m_addressSpaceConfigurationListener);
      }
    }
  }

  /**
   * Returns the currently displayed address spaces.
   * 
   * @return The currently displayed address spaces.
   */
  public List<INaviAddressSpace> getAddressSpaces() {
    List<INaviAddressSpace> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      final IFilter<INaviAddressSpace> filter = getFilter();

      if (m_project.isLoaded()) {
        localCachedValues =
            filter == null ? m_project.getContent().getAddressSpaces() : filter.get(m_project
                .getContent().getAddressSpaces());
      } else {
        localCachedValues = new ArrayList<INaviAddressSpace>();
      }
    }

    m_cachedValues = localCachedValues;
    return new ArrayList<INaviAddressSpace>(localCachedValues);
  }

  @Override
  public int getColumnCount() {
    return COLUMNS.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMNS[index];
  }

  @Override
  public int getRowCount() {
    return getAddressSpaces().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    return new ArrayList<Pair<Integer, Comparator<?>>>();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final INaviAddressSpace addressSpace = getAddressSpaces().get(row);

    switch (col) {
      case NAME_COLUMN:
        return addressSpace.getConfiguration().getName();
      case DESCRIPTION_COLUMN:
        return addressSpace.getConfiguration().getDescription();
      case MODULES_COLUMN:
        return addressSpace.isLoaded() ? addressSpace.getContent().getModules().size() : "?";
      case CREATION_DATE_COLUMN:
        return addressSpace.getConfiguration().getCreationDate();
      case MODIFICATION_DATE_COLUMN:
        return addressSpace.getConfiguration().getModificationDate();
      default:
        throw new IllegalStateException("IE01169: Invalid column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return (col == NAME_COLUMN) || (col == DESCRIPTION_COLUMN);
  }

  @Override
  public void setFilter(final IFilter<INaviAddressSpace> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    if ((col != NAME_COLUMN) && (col != DESCRIPTION_COLUMN)) {
      throw new IllegalStateException("IE01170: Column can not be edited");
    }

    final INaviAddressSpace addressSpace = getAddressSpaces().get(row);

    if (col == NAME_COLUMN) {
      try {
        addressSpace.getConfiguration().setName((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00169: " + "Could not save address space name";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new name of the address space '%s' could not be saved.", addressSpace
                    .getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The address space keeps its old name."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (col == DESCRIPTION_COLUMN) {
      try {
        addressSpace.getConfiguration().setDescription((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00170: " + "Could not save address space description";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new description of the address space '%s' could not be saved.", addressSpace
                    .getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The address space keeps its old description."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  private class InternalAddressSpaceConfigurationListener extends
      CAddressSpaceConfigurationListenerAdapter {
    @Override
    public void changedDescription(final INaviAddressSpace addressSpace, final String description) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedName(final INaviAddressSpace addressSpace, final String name) {
      m_cachedValues = null;

      fireTableDataChanged();
    }
  }

  /**
   * Keeps the table up to date when information inside the address spaces changes.
   */
  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      m_cachedValues = null;

      fireTableDataChanged();
    }
  }

  /**
   * Keeps the table model up to date when projects change.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
      m_cachedValues = null;

      space.addListener(m_addressSpaceListener);
      space.getConfiguration().addListener(m_addressSpaceConfigurationListener);

      fireTableDataChanged();
    }

    @Override
    public void loadedProject(final CProject project) {
      m_cachedValues = null;

      for (final INaviAddressSpace addressSpace : project.getContent().getAddressSpaces()) {
        addressSpace.addListener(m_addressSpaceListener);
        addressSpace.getConfiguration().addListener(m_addressSpaceConfigurationListener);
      }

      fireTableDataChanged();
    }

    @Override
    public void removedAddressSpace(final INaviProject project, final INaviAddressSpace space) {
      m_cachedValues = null;

      space.removeListener(m_addressSpaceListener);
      space.getConfiguration().removeListener(m_addressSpaceConfigurationListener);

      fireTableDataChanged();
    }
  }
}
