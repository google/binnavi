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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.TagContainer;



import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CAddTagAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractMenuBuilder;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTree;



/**
 * Builds context menus for tag container nodes.
 */
public final class CTagContainerNodeMenuBuilder extends CAbstractMenuBuilder {
  /**
   * Database where the tags are stored.
   */
  private final IDatabase m_database;

  /**
   * Creates a new menu builder.
   * 
   * @param projectTree Project tree of the main window.
   * @param database Database where the tags are stored.
   */
  public CTagContainerNodeMenuBuilder(final JTree projectTree, final IDatabase database) {
    super(projectTree);

    m_database = database;
  }

  @Override
  protected void createMenu(final JComponent menu) {
    final ITreeNode<CTag> rootNode = m_database.getContent().getViewTagManager().getRootTag();

    menu.add(new JMenuItem(CActionProxy.proxy(new CAddTagAction(getParent(), m_database
        .getContent().getViewTagManager(), rootNode, "New Tag"))));
  }

  @Override
  protected JMenu getMenu() {
    final JMenu menu = new JMenu("Tagging");

    menu.setMnemonic("HK_MENU_TAGGING".charAt(0));

    return menu;
  }
}
