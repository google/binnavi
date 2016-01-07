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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CStaredItemFunctions;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.Pair;

/**
 * The table model is used to fill the modules table with information about the modules.
 */
public final class CModulesModel extends CAbstractTreeTableModel<INaviModule> {
  /**
   * Icon used for loaded modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project_module.png"));

  /**
   * Icon used for unloaded modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_GRAY = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project_module_gray.png"));

  /**
   * Icon used for incomplete modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_BROKEN = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project_module_broken.png"));

  /**
   * Icon used for not yet converted modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_UNCONVERTED = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project_module_light_gray.png"));

  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3257027186612001802L;

  /**
   * Initialization state
   */
  private static final int STATE = 0;

  /**
   * Index of the column where module names are shown.
   */
  private static final int NAME_COLUMN = 1;

  /**
   * Index of the column where module descriptions are shown.
   */
  private static final int DESCRIPTION_COLUMN = 2;

  /**
   * Index of the column where the number of views in modules are shown.
   */
  private static final int VIEWS_COLUMN = 3;

  /**
   * Index of the column where module creation dates are shown.
   */
  private static final int CREATION_DATE_COLUMN = 4;

  /**
   * Index of the column where module modification dates are shown.
   */
  private static final int MODIFICATION_DATE_COLUMN = 5;

  /**
   * Names of the columns shown in the table.
   */
  private final String[] COLUMNS = {"State", "Name", "Description", "Views", "Creation Date",
      "Modification Date"};

  /**
   * Database that contains the module information.
   */
  private final IDatabase m_database;

  /**
   * Listener that keeps the table model up to date when modules are added to or removed from the
   * database.
   */
  private final InternalDatabaseListener m_databaseListener = new InternalDatabaseListener();

  /**
   * Listener that keeps the table model up to date when module properties change.
   */
  private final InternalModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * The displayed modules are cached for performance reasons.
   */
  private List<INaviModule> m_cachedValues = null;

  /**
   * Creates a new modules model.
   * 
   * @param database The database that contains the module information.
   */
  public CModulesModel(final IDatabase database) {
    Preconditions.checkNotNull(database, "IE01214: Database can't be null");

    m_database = database;

    m_database.addListener(m_databaseListener);

    if (m_database.isLoaded()) {
      for (final INaviModule module : m_database.getContent().getModules()) {
        module.addListener(m_moduleListener);
      }
    }
  }

  private Icon getIcon(final INaviModule module) {
    if (module.getConfiguration().getRawModule().isComplete() && module.isInitialized()) {
      return module.isLoaded() ? ICON_MODULE : ICON_MODULE_GRAY;
    } else if (module.getConfiguration().getRawModule().isComplete() && !module.isInitialized()) {
      return ICON_MODULE_UNCONVERTED;
    } else {
      return ICON_MODULE_BROKEN;
    }
  }

  @Override
  public void delete() {
    m_database.removeListener(m_databaseListener);

    if (m_database.isLoaded()) {
      for (final INaviModule module : m_database.getContent().getModules()) {
        module.removeListener(m_moduleListener);
      }
    }
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    switch (columnIndex) {
      case NAME_COLUMN:
        return String.class;
      case STATE:
        return ImageIcon.class;
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

      if (m_database.isConnected()) {
        localCachedValues =
            filter == null ? m_database.getContent().getModules() : filter.get(m_database
                .getContent().getModules());
      } else {
        localCachedValues = new ArrayList<INaviModule>();
      }
    }

    CStaredItemFunctions.sort(localCachedValues);

    m_cachedValues = localCachedValues;
    return new ArrayList<INaviModule>(localCachedValues);
  }

  @Override
  public int getRowCount() {
    return getModules().size();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    // the columns of our model only contains standard data types which are already comparable.
    return new ArrayList<Pair<Integer, Comparator<?>>>();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    final INaviModule module = getModules().get(row);

    switch (col) {
      case NAME_COLUMN:
        return module.getConfiguration().getName();
      case STATE:
        return getIcon(module);
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
        throw new IllegalStateException("IE01166: Invalid column");
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
      throw new IllegalStateException("IE01167: Column can not be edited");
    }

    final INaviModule module = getModules().get(row);

    if (col == NAME_COLUMN) {
      try {
        module.getConfiguration().setName((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String message = "E00167: " + "Could not change the module name";
        final String description =
            CUtilityFunctions
                .createDescription(
                    "The new module name could not be saved to the database.",
                    new String[] {"There was a problem with the connection to the database while the module name was saved"},
                    new String[] {"The module name was not saved. Please try to find out what went wrong with the database connection and try to save the module name again."});

        NaviErrorDialog.show(null, message, description, e);
      }
    } else if (col == DESCRIPTION_COLUMN) {
      try {
        module.getConfiguration().setDescription((String) value);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String message = "E00168: " + "Could not change the module description";
        final String description =
            CUtilityFunctions
                .createDescription(
                    "The new module description could not be saved to the database.",
                    new String[] {"There was a problem with the connection to the database while the module description was saved"},
                    new String[] {"The module description was not saved. Please try to find out what went wrong with the database connection and try to save the module description again."});

        NaviErrorDialog.show(null, message, description, e);
      }
    }
  }

  /**
   * Listener that keeps the table model up to date when modules are added to or removed from the
   * database.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedModule(final IDatabase database, final INaviModule module) {
      m_cachedValues = null;

      module.addListener(m_moduleListener);

      fireTableDataChanged();
    }

    @Override
    public void deletedModule(final IDatabase database, final INaviModule module) {
      m_cachedValues = null;

      module.removeListener(m_moduleListener);

      fireTableDataChanged();
    }
  }

  /**
   * Listener that keeps the table model up to date when module properties change.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
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
    public void changedStarState(final INaviModule module, final boolean isStared) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void loadedModule(final INaviModule module) {
      m_cachedValues = null;

      fireTableDataChanged();
    }

    @Override
    public void initializedModule(final INaviModule module) {
      m_cachedValues = null;

      fireTableDataChanged();
    }
  }
}
