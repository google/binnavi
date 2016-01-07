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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;

/**
 * Action that can be used to delete debugger templates.
 */
public final class CDeleteDebuggerDescriptionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8680823615809281492L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Database from which the debugger templates are deleted.
   */
  private final IDatabase m_database;

  /**
   * Debugger templates to be deleted.
   */
  private final DebuggerTemplate[] m_debuggers;

  /**
   * Updates the project tree after the action was executed.
   */
  private final ITreeUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database from which the debugger templates are deleted.
   * @param debuggers Debugger templates to be deleted.
   * @param updater Updates the project tree after the action was executed.
   */
  public CDeleteDebuggerDescriptionAction(final JFrame parent, final IDatabase database,
      final DebuggerTemplate[] debuggers, final ITreeUpdater updater) {
    super("Delete Debugger");

    m_parent = Preconditions.checkNotNull(parent, "IE01873: Parent argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE01874: Database argument can not be null");
    m_debuggers =
        Preconditions.checkNotNull(debuggers, "IE01875: Debuggers argument can not be null")
            .clone();
    m_updater = Preconditions.checkNotNull(updater, "IE02867: updater argument can not be null");

    for (final DebuggerTemplate debuggerTemplate : debuggers) {
      Preconditions
          .checkNotNull(debuggerTemplate, "IE01876: Debuggers list contains null-elements");
    }

    putValue(ACCELERATOR_KEY, HotKeys.DELETE_HK.getKeyStroke());
    putValue(MNEMONIC_KEY, (int) "HK_MENU_DELETE_DEBUGGERS".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.deleteDebuggers(m_parent, m_database, m_debuggers, m_updater);
  }
}
