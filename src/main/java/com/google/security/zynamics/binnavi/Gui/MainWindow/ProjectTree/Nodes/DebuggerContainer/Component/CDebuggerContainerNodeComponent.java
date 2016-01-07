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

import javax.swing.JTree;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;



/**
 * Component that is displayed on the right side of the main window whenever a debuggers node was
 * selected.
 */
public final class CDebuggerContainerNodeComponent extends CTablePanel<DebuggerTemplate> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1427827839135966321L;

  /**
   * Database that contains the debugger templates displayed in this component.
   */
  private final IDatabase m_database;

  /**
   * Updates the GUI on relevant debugger-related changes.
   */
  private final InternalDebuggerDescriptionManagerListener m_listener;

  /**
   * Creates a new component object.
   * 
   * @param projectTree Project tree to be updated on certain events.
   * @param database Database that contains the debugger templates displayed in this component.
   */
  public CDebuggerContainerNodeComponent(final JTree projectTree, final IDatabase database) {
    super(new CDebuggersTable(projectTree, database), null, null);

    // CDebuggersTable checks the arguments

    m_database = database;

    m_listener = new InternalDebuggerDescriptionManagerListener();
    m_database.getContent().getDebuggerTemplateManager().addListener(m_listener);

    updateBorderText(getBorderText());
  }

  /**
   * Creates the border text which shows how many debuggers are stored in the database.
   * 
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d Debuggers in Database '%s'", m_database.getContent()
        .getDebuggerTemplateManager().debuggerCount(), m_database.getConfiguration()
        .getDescription());
  }

  @Override
  protected void disposeInternal() {
    m_database.getContent().getDebuggerTemplateManager().removeListener(m_listener);
  }

  /**
   * Updates the GUI on relevant debugger-related changes.
   */
  private class InternalDebuggerDescriptionManagerListener implements
      IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      updateBorderText(getBorderText());
    }

    @Override
    public void removedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      updateBorderText(getBorderText());
    }
  }
}
