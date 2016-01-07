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
package com.google.security.zynamics.binnavi.Gui.FunctionSelection;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;

/**
 * Panel where the available functions to select from are displayed.
 */
public final class CFunctionSelectionPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6713333934216452230L;

  /**
   * Tree where the available functions to select from are displayed.
   */
  private final CFunctionSelectionTree m_tree;

  /**
   * Creates a new function selection panel.
   *
   * @param database The database where the functions come from.
   * @param actionProvider Provides the implementations of the available actions.
   */
  public CFunctionSelectionPanel(final IDatabase database, final IActionProvider actionProvider) {
    super(new BorderLayout());

    Preconditions.checkNotNull(database, "IE01573: Database argument can not be null");

    Preconditions.checkNotNull(actionProvider, "IE01574: Action provider argument can not be null");

    m_tree = new CFunctionSelectionTree(database, actionProvider);

    add(m_tree);
  }

  /**
   * Returns the function selected in the function selection tree.
   *
   * @return The selected function or null if no function is selected.
   */
  public INaviFunction getSelectedFunction() {
    final TreePath path = m_tree.getSelectionPath();

    final Object selectedNode = path.getLastPathComponent();

    return selectedNode instanceof CFunctionIconNode
        ? ((CFunctionIconNode) selectedNode).getFunction() : null;
  }
}
