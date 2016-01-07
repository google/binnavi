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

import com.google.common.primitives.Ints;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractTreeTable;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.CDebuggerNodeMenuBuilder;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer.Component.Help.CDebuggersTableHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.CEmptyUpdater;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;

import javax.swing.JPopupMenu;
import javax.swing.JTree;

/**
 * This table displays all debugger templates that are registered in a database.
 */
public final class CDebuggersTable extends CAbstractTreeTable<DebuggerTemplate> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5075738529815870485L;

  /**
   * The database that contains the debugger templates.
   */
  private final IDatabase m_database;

  /**
   * Creates a new debuggers table.
   * 
   * @param projectTree The project tree of the main window.
   * @param database The database that contains the debugger templates.
   */
  public CDebuggersTable(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CDebuggersModel(database), new CDebuggersTableHelp());

    m_database = database;
  }

  /**
   * Returns the debuggers that correspond to the selected table rows.
   * 
   * @param sortSelectedRows The selected rows.
   * 
   * @return The debuggers that correspond to the selected rows.
   */
  private DebuggerTemplate[] getDebuggers(final int[] sortSelectedRows) {
    final DebuggerTemplate[] projects = new DebuggerTemplate[sortSelectedRows.length];

    for (int i = 0; i < projects.length; i++) {
      projects[i] =
          m_database.getContent().getDebuggerTemplateManager().getDebugger(sortSelectedRows[i]);
    }

    return projects;
  }

  /**
   * This function is used to determine what debuggers are the targets of a debuggers context menu.
   * If the clicked row is inside the row selection interval, the selected debuggers are the target.
   * If the clicked row is outside the selection interval, the clicked row is selected and the
   * clicked debugger is the target.
   * 
   * @param sortedRow The sorted row index of the clicked debugger.
   * 
   * @return The debugger targets for the context menu.
   */
  private DebuggerTemplate[] getSelectedDebuggers(final int sortedRow) {
    final int[] sortSelectedRows = getSortSelectedRows();
    if (Ints.asList(sortSelectedRows).indexOf(sortedRow) != -1) {
      return getDebuggers(sortSelectedRows);
    } else {
      final int viewRow = convertRowIndexToView(sortedRow);
      setRowSelectionInterval(viewRow, viewRow);
      return new DebuggerTemplate[] {m_database.getContent().getDebuggerTemplateManager()
          .getDebugger(sortedRow)};
    }
  }


  @Override
  protected void deleteRows() {
    CDatabaseFunctions.deleteDebuggers(getParentWindow(), m_database,
        getDebuggers(getSortSelectedRows()), new CEmptyUpdater());
  }

  @Override
  protected JPopupMenu getPopupMenu(final int x, final int y, final int row) {
    final CDebuggerNodeMenuBuilder menu =
        new CDebuggerNodeMenuBuilder(getProjectTree(), null, this, m_database,
            getSelectedDebuggers(row));

    return menu.getPopupMenu();
  }

  @Override
  protected void handleDoubleClick(final int row) {
    // Don't do anything when a debugger template is double-clicked.
  }
}
