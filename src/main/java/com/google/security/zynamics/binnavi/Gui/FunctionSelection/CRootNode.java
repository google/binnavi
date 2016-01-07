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

import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Root node of the function selection tree.
 */
public final class CRootNode extends IconNode implements IFunctionTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2435554212994775878L;

  /**
   * The database where the functions come from.
   */
  private final IDatabase m_database;

  /**
   * The tree model of the function selection tree.
   */
  private final DefaultTreeModel m_model;

  /**
   * Provides the implementations of the available actions.
   */
  private final IActionProvider m_actionProvider;

  /**
   * Creates a new root node.
   *
   * @param database The database where the functions come from.
   * @param model The tree model of the function selection tree.
   * @param actionProvider Provides the implementations of the available actions.
   */
  public CRootNode(final IDatabase database, final DefaultTreeModel model,
      final IActionProvider actionProvider) {
    Preconditions.checkNotNull(database, "IE01580: Database argument can not be null");

    Preconditions.checkNotNull(model, "IE01581: Model argument can not be null");

    Preconditions.checkNotNull(actionProvider, "IE01582: Action provider argument can not be null");

    m_database = database;
    m_model = model;
    m_actionProvider = actionProvider;

    createChildren();
  }

  /**
   * Creates one child node for each module in the database.
   */
  private void createChildren() {
    if (m_database.isLoaded()) {
      for (final INaviModule module : m_database.getContent().getModules()) {
        add(new CModuleNode(module, m_model, m_actionProvider));
      }
    }
  }

  @Override
  public void doubleClicked() {
    // Node is invisible anyway
  }
}
