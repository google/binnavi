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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer.Component;



import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTableModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.comparators.IntComparator;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.net.NetHelpers;

/**
 * This model is used by the debuggers table to display all debuggers that are available in a
 * database.
 */
public final class CDebuggersModel extends CAbstractTreeTableModel<DebuggerTemplate> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -884636367490425260L;

  /**
   * Index of the column where debugger names are shown.
   */
  private static final int NAME_COLUMN = 0;

  /**
   * Index of the column where debugger hosts are shown.
   */
  private static final int HOST_COLUMN = 1;

  /**
   * Index of the column where debugger ports are shown.
   */
  private static final int PORT_COLUMN = 2;

  /**
   * Names of the columns shown in the table.
   */
  private static final String[] COLUMNS = {"Name", "Host", "Port"};

  /**
   * The database that contains the debuggers.
   */
  private final IDatabase m_database;

  /**
   * Updates the table model when the debugger templates managed by the debugger template model
   * change.
   */
  private final InternalDebuggerDescriptionManagerListener m_debuggerManagerListener =
      new InternalDebuggerDescriptionManagerListener();

  /**
   * Updates the table model when debugger descriptions change.
   */
  private final InternalDebuggerDescriptionListener m_debuggerListener =
      new InternalDebuggerDescriptionListener();

  /**
   * Creates a new debuggers table model for a given database.
   * 
   * @param database The database that contains the debuggers.
   */
  public CDebuggersModel(final IDatabase database) {
    m_database = Preconditions.checkNotNull(database, "IE01969: Database argument can't be null");

    // We need some listeners that update the table when the debugger information changes.

    final DebuggerTemplateManager debuggerManager =
        m_database.getContent().getDebuggerTemplateManager();

    debuggerManager.addListener(m_debuggerManagerListener);

    for (final DebuggerTemplate debugger : debuggerManager) {
      debugger.addListener(m_debuggerListener);
    }
  }

  /**
   * Changes the host of a given debugger.
   * 
   * @param debugger The debugger whose host is changed.
   * @param host The new host of the debugger.
   */
  private static void setHost(final DebuggerTemplate debugger, final String host) {
    // TODO: Separate model from view

    try {
      debugger.setHost(host);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00162: " + "Could not change the debugger host";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger host could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger host was saved"},
                  new String[] {"The debugger host was not saved. Please try to find out what went wrong with the database connection and try to save the debugger host again."});

      NaviErrorDialog.show(null, message, description, e);
    }
  }

  /**
   * Changes the name of a given debugger.
   * 
   * @param debugger The debugger whose name is changed.
   * @param name The new name of the debugger.
   */
  private static void setName(final DebuggerTemplate debugger, final String name) {
    // TODO: Separate model from view

    try {
      debugger.setName(name);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00161: " + "Could not change the debugger name";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger name could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger name was saved"},
                  new String[] {"The debugger name was not saved. Please try to find out what went wrong with the database connection and try to save the debugger name again."});

      NaviErrorDialog.show(null, message, description, e);
    }
  }

  /**
   * Changes the port of a given debugger.
   * 
   * @param debugger The debugger whose port is changed.
   * @param port The new host of the debugger.
   */
  private static void setPort(final DebuggerTemplate debugger, final String port) {
    // TODO: Separate model from view
    try {
      if (NetHelpers.isValidPort(port)) {
        debugger.setPort(Integer.parseInt(port));
      } else {
        CMessageBox.showError(null, "Not a valid port.");
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00163: " + "Could not change the debugger port";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger port could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger port was saved"},
                  new String[] {"The debugger port was not saved. Please try to find out what went wrong with the database connection and try to save the debugger port again."});

      NaviErrorDialog.show(null, message, description, e);
    }
  }

  @Override
  public void delete() {
    final DebuggerTemplateManager debuggerManager =
        m_database.getContent().getDebuggerTemplateManager();

    debuggerManager.removeListener(m_debuggerManagerListener);

    for (final DebuggerTemplate debugger : debuggerManager) {
      debugger.removeListener(m_debuggerListener);
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

  @Override
  public int getRowCount() {
    return m_database.getContent().getDebuggerTemplateManager().debuggerCount();
  }

  @Override
  public List<Pair<Integer, Comparator<?>>> getSorters() {
    final List<Pair<Integer, Comparator<?>>> sorters =
        new ArrayList<Pair<Integer, Comparator<?>>>();

    sorters.add(new Pair<Integer, Comparator<?>>(PORT_COLUMN, new IntComparator()));

    return sorters;
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    switch (col) {
      case NAME_COLUMN:
        return m_database.getContent().getDebuggerTemplateManager().getDebugger(row).getName();
      case HOST_COLUMN:
        return m_database.getContent().getDebuggerTemplateManager().getDebugger(row).getHost();
      case PORT_COLUMN:
        return m_database.getContent().getDebuggerTemplateManager().getDebugger(row).getPort();
      default:
        throw new IllegalStateException("IE01163: Invalid column");
    }
  }

  @Override
  public boolean isCellEditable(final int row, final int col) {
    return true;
  }

  @Override
  public void setValueAt(final Object value, final int row, final int col) {
    if ((col != NAME_COLUMN) && (col != HOST_COLUMN) && (col != PORT_COLUMN)) {
      throw new IllegalStateException("IE01164: Column can not be edited");
    }

    if (col == NAME_COLUMN) {
      setName(m_database.getContent().getDebuggerTemplateManager().getDebugger(row), (String) value);
    } else if (col == HOST_COLUMN) {
      setHost(m_database.getContent().getDebuggerTemplateManager().getDebugger(row), (String) value);
    } else if (col == PORT_COLUMN) {
      setPort(m_database.getContent().getDebuggerTemplateManager().getDebugger(row),
          value.toString());
    }
  }

  /**
   * Updates the table model when debugger descriptions change.
   */
  private class InternalDebuggerDescriptionListener implements IDebuggerTemplateListener {
    @Override
    public void changedHost(final DebuggerTemplate debugger) {
      fireTableDataChanged();
    }

    @Override
    public void changedName(final DebuggerTemplate debugger) {
      fireTableDataChanged();
    }

    @Override
    public void changedPort(final DebuggerTemplate debugger) {
      fireTableDataChanged();
    }
  }

  /**
   * Updates the table model when the debugger templates managed by the debugger template model
   * change.
   */
  private class InternalDebuggerDescriptionManagerListener implements
      IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      debugger.addListener(m_debuggerListener);

      fireTableDataChanged();
    }

    @Override
    public void removedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      debugger.removeListener(m_debuggerListener);

      fireTableDataChanged();
    }
  }
}
