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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.DateComparator;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;

/**
 * Table model that is used to display all modules inside an address space.
 */
public final class CProjectModulesModel extends CAbstractTreeTableModel<INaviModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6447001409299309718L;

  /**
   * Index of the column showing module names.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column showing module descriptions.
   */
  private static final int DESCRIPTION_COLUMN = 1;

  /**
   * Index of the column showing the number of views in the module.
   */
  private static final int VIEWS_COLUMN = 2;

  /**
   * Index of the column showing module creation dates.
   */
  private static final int CREATION_DATE_COLUMN = 3;

  /**
   * Index of the column showing module modification dates.
   */
  private static final int MODIFICATION_DATE_COLUMN = 4;

  /**
   * Names of the columns.
   */
  private final String[] COLUMNS = {"Name", "Description", "Views", "Creation Date",
      "Modification Date"};

  /**
   * Keeps track of relevant changes in the address space.
   */
  private final InternalAddressSpaceListener m_addressSpaceListener =
      new InternalAddressSpaceListener();

  /**
   * Keeps track of relevant changes in the modules.
   */
  private final InternalModulesListener m_modulesListener = new InternalModulesListener();

  /**
   * The address space that contains the modules.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Displayed modules are cached for performance reasons.
   */
  private List<INaviModule> m_cachedValues = null;

  private final IAddressSpaceContentListener m_contentListener =
      new InternalAddressSpaceContentListener();

  /**
   * Creates a new model object.
   * 
   * @param addressSpace The address space that provides the modules.
   */
  public CProjectModulesModel(final INaviAddressSpace addressSpace) {
    Preconditions.checkNotNull(addressSpace, "IE01955: Address space argument can't be null");

    m_addressSpace = addressSpace;

    addressSpace.addListener(m_addressSpaceListener);

    if (addressSpace.isLoaded()) {
      addressSpace.getContent().addListener(m_contentListener);

      for (final INaviModule module : addressSpace.getContent().getModules()) {
        module.addListener(m_modulesListener);
      }
    }
  }

  @Override
  public void delete() {
    m_addressSpace.removeListener(m_addressSpaceListener);

    if (m_addressSpace.isLoaded()) {
      m_addressSpace.getContent().removeListener(m_contentListener);

      for (final INaviModule module : m_addressSpace.getContent().getModules()) {
        module.removeListener(m_modulesListener);
      }
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    switch (columnIndex) {
      case NAME_COLUMN:
        return String.class;
      case DESCRIPTION_COLUMN:
        return String.class;
      case VIEWS_COLUMN:
        return Integer.class;
      case CREATION_DATE_COLUMN:
        return Date.class;
      case MODIFICATION_DATE_COLUMN:
        return Date.class;
      default:
        throw new IllegalArgumentException(String.format(
            "Unexpected column index retrieved while determining column classes: %d", columnIndex));
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
   * Returns the currently displayed modules.
   * 
   * @return The currently displayed modules.
   */
  public List<INaviModule> getModules() {
    List<INaviModule> localCachedValues = m_cachedValues;

    if (localCachedValues == null) {
      final IFilter<INaviModule> filter = getFilter();

      if (m_addressSpace.isLoaded()) {
        localCachedValues =
            filter == null ? m_addressSpace.getContent().getModules() : filter.get(m_addressSpace
                .getContent().getModules());
      } else {
        localCachedValues = new ArrayList<INaviModule>();
      }
    }

    m_cachedValues = localCachedValues;
    return new ArrayList<INaviModule>(localCachedValues);
  }

  @Override
  public int getRowCount() {
    return getModules().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(VIEWS_COLUMN, new IntComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(CREATION_DATE_COLUMN, new DateComparator()));
    sorters.add(new Pair<Integer, Comparator<?>>(MODIFICATION_DATE_COLUMN, new DateComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final INaviModule module = getModules().get(row);

    switch (col) {
      case NAME_COLUMN:
        return module.getConfiguration().getName();
      case DESCRIPTION_COLUMN:
        return module.getConfiguration().getDescription();
      case VIEWS_COLUMN:
        return module.isLoaded() ? module.getContent().getViewContainer().getViews().size()
            : module.getCustomViewCount() + module.getFunctionCount() + 1;
      case CREATION_DATE_COLUMN:
        return module.getConfiguration().getCreationDate();
      case MODIFICATION_DATE_COLUMN:
        return module.getConfiguration().getModificationDate();
      default:
        throw new IllegalStateException("IE01160: Invalid column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return (col == NAME_COLUMN) || (col == DESCRIPTION_COLUMN);
  }

  @Override
  public void setFilter(final IFilter<INaviModule> filter) {
    m_cachedValues = null;

    super.setFilter(filter);
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    if ((col != NAME_COLUMN) && (col != DESCRIPTION_COLUMN)) {
      throw new IllegalStateException("IE01161: Column can not be edited");
    }

    final INaviModule module = getModules().get(row);

    if (col == NAME_COLUMN) {
      try {
        module.getConfiguration().setName((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00156: " + "Could not save address space name";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new name of the address space '%s' could not be saved.", m_addressSpace
                    .getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The address space keeps its old name."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    } else if (col == DESCRIPTION_COLUMN) {
      try {
        module.getConfiguration().setDescription((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00157: " + "Could not save address space description";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The new description of the address space '%s' could not be saved.", m_addressSpace
                    .getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The address space keeps its old description."});

        NaviErrorDialog.show(null, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Keeps the table model up to date when the address space changes.
   */
  private class InternalAddressSpaceContentListener implements IAddressSpaceContentListener {
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      m_cachedValues = null;

      module.addListener(m_modulesListener);

      fireTableDataChanged();
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
      // The image bases are not shown in the table
    }

    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      m_cachedValues = null;

      module.removeListener(m_modulesListener);

      fireTableDataChanged();
    }
  }

  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_contentListener);
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      addressSpace.getContent().addListener(m_contentListener);
    }
  }

  /**
   * Keeps the table model up to date when the modules change.
   */
  private class InternalModulesListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule container, final INaviView view) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedDescription(final INaviModule module, final String description) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void loadedModule(final INaviModule module) {
      m_cachedValues = null;

      fireTableDataChanged();
    }
  }
}
