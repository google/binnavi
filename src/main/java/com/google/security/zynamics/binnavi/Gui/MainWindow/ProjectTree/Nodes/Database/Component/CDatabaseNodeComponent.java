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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.Component;

import java.awt.BorderLayout;

import javax.swing.JPanel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Database.CDatabaseSettingsPanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;

/**
 * Component that is displayed on the right side of the main window whenever a database node was
 * selected.
 */
public final class CDatabaseNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1518629700873008646L;

  /**
   * Panel where the database connection options can be displayed.
   */
  private final CDatabaseSettingsPanel m_panel;

  /**
   * Creates a new database node component.
   * 
   * @param database The database to be edited from the component.
   */
  public CDatabaseNodeComponent(final IDatabase database) {
    super(new BorderLayout());

    Preconditions.checkNotNull(database, "IE01964: Database argument can't be null");

    m_panel = new CDatabaseSettingsPanel(database);

    final JPanel innerPanel = new JPanel(new BorderLayout());

    innerPanel.add(m_panel, BorderLayout.SOUTH);

    add(innerPanel, BorderLayout.NORTH);

    if (database.isConnecting() || database.isLoading()) {
      showProgressPanel();
    }
  }

  /**
   * Shows the progress panel to the user.
   */
  private void showProgressPanel() {
    m_panel.setEnabled(false);
  }

  @Override
  public void dispose() {
    m_panel.delete();
  }

  /**
   * Returns the settings input panel of the component.
   * 
   * @return The settings input panel.
   */
  public CDatabaseSettingsPanel getPanel() {
    return m_panel;
  }
}
