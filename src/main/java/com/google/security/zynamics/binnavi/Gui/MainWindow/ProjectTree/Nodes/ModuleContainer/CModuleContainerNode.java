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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.CModuleContainerComponent;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;

/**
 * Represents module container nodes in the project tree.
 */
public final class CModuleContainerNode extends CProjectTreeNode<Object> {
  /**
   * Icon that is shown when there are modules in the container.
   */
  private static final ImageIcon ICON_MODULE_CONTAINER =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/modules_container3.png"));

  /**
   * The database that provides the module information.
   */
  private final IDatabase m_database;

  /**
   * Listens on the database updates the node or its child nodes if something important happens.
   */
  private final InternalDatabaseListener m_listener;

  /**
   * True, to sort the children by name. False, otherwise.
   */
  private boolean sortByName = false;

  /**
   * Creates a new module container node.
   *
   * @param projectTree The tree where the node is added to.
   * @param database The database that contains the modules listed under this node.
   */
  public CModuleContainerNode(final JTree projectTree, final IDatabase database) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CModuleContainerComponent(projectTree, database);
      }
    }, new CModuleContainerNodeMenuBuilder(projectTree, database));
    Preconditions.checkNotNull(database, "IE01979: Database can't be null");
    m_database = database;
    createChildren();
    // Add a listener that keeps track of the database.
    m_listener = new InternalDatabaseListener();
    m_database.addListener(m_listener);
  }

  /**
   * Creates the child nodes of module container nodes. One child node is added for each module
   * found in the database.
   */
  @Override
  protected void createChildren() {
    if (m_database.isLoaded()) {
      // There are modules without a raw module
      // There are modules with raw modules
      // There are raw modules without a module

      for (final INaviModule module : m_database.getContent().getModules()) {
        add(new CModuleNode(getProjectTree(), CModuleContainerNode.this, m_database, module,
            new CModuleContainer(m_database, module)));
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    m_database.removeListener(m_listener);
    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Nothing to do here.
  }

  @SuppressWarnings("unchecked")
  @Override
  public TreeNode getChildAt(final int index) {
    final ArrayList<CModuleNode> sortedChildren = new ArrayList<CModuleNode>(children);

    if (sortByName) {
      Collections.sort(sortedChildren, new Comparator<CModuleNode>() {
        @Override
        public int compare(final CModuleNode lhs, final CModuleNode rhs) {
          return lhs.getObject().getConfiguration().getName()
              .compareTo(rhs.getObject().getConfiguration().getName());
        }
      });
    }

    return sortedChildren.get(index);
  }

  @Override
  public Icon getIcon() {
    return ICON_MODULE_CONTAINER;
  }

  /**
   * Determines whether the children of this node are sorted by name or by ID.
   *
   * @return True, to sort children by name. False, to sort them by ID.
   */
  public boolean isSorted() {
    return sortByName;
  }

  /**
   * Sets the sorting order.
   *
   * @param sorted True, to order child nodes by name. False, to sort them by ID.
   */
  public void setSorted(final boolean sorted) {
    sortByName = sorted;
    getTreeModel().nodeStructureChanged(CModuleContainerNode.this);
  }

  @Override
  public String toString() {
    return "Modules" + " (" + m_database.getContent().getModules().size() + ")";
  }

  /**
   * This listener keeps track of relevant changes in the database and updates the tree should a
   * relevant change occur.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedModule(final IDatabase database, final INaviModule module) {
      getTreeModel().insertNodeInto(new CModuleNode(getProjectTree(), CModuleContainerNode.this,
          database, module, new CModuleContainer(database, module)), CModuleContainerNode.this,
          getChildCount());
    }

    @Override
    public void deletedModule(final IDatabase database, final INaviModule module) {
      // Remove the node that represents the deleted project.
      for (int i = 0; i < getChildCount(); i++) {
        final CModuleNode node = (CModuleNode) getChildAt(i);
        if (node.getObject() == module) {
          getTreeModel().removeNodeFromParent(node);
          node.dispose();
          break;
        }
      }
    }
  }
}
