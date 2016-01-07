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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CDatabaseFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;

/**
 * Can be used to create a new default debugger template.
 */
public final class CCreateDebuggerDescriptionAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 4049078090679021771L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Database where the debugger template is created.
   */
  private final IDatabase m_database;

  /**
   * Name of the new debugger template.
   */
  private final String m_name;

  /**
   * Host of the new debugger template.
   */
  private final String m_host;

  /**
   * Port of the new debugger template.
   */
  private final int m_port;

  /**
   * Updates the project tree after the action was executed.
   */
  private final INodeSelectionUpdater m_updater;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database where the debugger template is created.
   * @param name Name of the new debugger template.
   * @param host Host of the new debugger template.
   * @param port Port of the new debugger template.
   * @param updater Updates the project tree after the action was executed.
   */
  public CCreateDebuggerDescriptionAction(final JFrame parent, final IDatabase database,
      final String name, final String host, final int port, final INodeSelectionUpdater updater) {
    super("Create Debugger");

    m_parent = Preconditions.checkNotNull(parent, "IE01861: Parent argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE01862: Database argument can not be null");
    m_name = Preconditions.checkNotNull(name, "IE01863: Name argument can not be null");
    m_host = Preconditions.checkNotNull(host, "IE01864: Host argument can not be null");

    m_port = port;
    m_updater = updater;

    putValue(MNEMONIC_KEY, (int) "HK_MENU_CREATE_DEBUGGER_DESCRIPTION".charAt(0));

  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CDatabaseFunctions.addDebugger(m_parent, m_database, m_name, m_host, m_port, m_updater);
  }
}
