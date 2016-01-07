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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CTableSearcherHelper;
import com.google.security.zynamics.binnavi.Gui.HotKeys;

/**
 * Action class that can be used to search through a table.
 */
public final class CSearchTableAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8871079733308461907L;

  /**
   * Parent window used for dialogs.
   */
  private final Window m_parent;

  /**
   * Table to be searched through.
   */
  private final JTable m_table;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   * @param table Table to be searched through.
   */
  public CSearchTableAction(final Window parent, final JTable table) {
    super("Search");
    m_parent = Preconditions.checkNotNull(parent, "IE01922: Parent argument can't be null");
    m_table = Preconditions.checkNotNull(table, "IE01923: Table argument can't be null");
    putValue(ACCELERATOR_KEY, HotKeys.SEARCH_HK.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CTableSearcherHelper.search(m_parent, m_table);
  }
}
