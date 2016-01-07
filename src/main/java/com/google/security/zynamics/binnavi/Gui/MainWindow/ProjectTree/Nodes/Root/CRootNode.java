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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Root;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Database.Interfaces.DatabaseManagerListener;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTree;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.CDatabaseNode;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.MainWindow.ProjectTree.Nodes.Root.Component.CRootNodeComponent;

/**
 * Represents the (invisible) root node of the project tree.
 */
public final class CRootNode extends CProjectTreeNode<Object> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5465122680831083053L;

  /**
   * The database manager that provides the information necessary to build the child nodes of the
   * root node.
   */
  private final CDatabaseManager m_databaseManager;

  /**
   * Listens on the database manager and updates the child nodes of the root node if necessary.
   */
  private final InternalDatabaseManagerListener m_listener;

  /**
   * Creates a new root node object.
   * 
   * @param projectTree Project tree of the main window.
   * @param databaseManager Database manager that contains all databases shown in the project tree.
   */
  public CRootNode(final CProjectTree projectTree, final CDatabaseManager databaseManager) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CRootNodeComponent();
      }
    }, new CRootNodeMenuBuilder(projectTree));

    Preconditions.checkNotNull(databaseManager, "IE01995: Database manager can't be null");

    m_databaseManager = databaseManager;

    createChildren();

    // Add a listener to keep track of database changes.
    m_listener = new InternalDatabaseManagerListener();
    m_databaseManager.addListener(m_listener);
  }

  /**
   * Creates the child nodes of the node. One node for each known database is created.
   */
  @Override
  protected void createChildren() {
    // Add one node for each known database.
    for (final IDatabase database : m_databaseManager) {
      add(new CDatabaseNode(getProjectTree(), this, database));
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    m_databaseManager.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // The node is invisible anyway.
  }

  @Override
  public String toString() {
    // The node is invisible anyway.
    return "BinNavi Project Tree";
  }

  /**
   * This listener is necessary for tree updating. When databases are added or removed from the
   * database manager, the tree must be updated.
   */
  private class InternalDatabaseManagerListener implements DatabaseManagerListener {
    /**
     * When a new database is added to the database manager, a node that represents the database
     * must be added to the tree.
     */
    @Override
    public void addedDatabase(final IDatabaseManager databaseManager, final IDatabase database) {
      // Make sure the database is not added twice
      for (int i = 0; i < getChildCount(); i++) {
        final CDatabaseNode child = (CDatabaseNode) getChildAt(i);

        if (child.getObject() == database) {
          throw new IllegalStateException("IE01177: Database should not be added twice");
        }
      }

      // Add a new child node that represents the new database
      add(new CDatabaseNode(getProjectTree(), CRootNode.this, database));

      // Make sure the tree is updated.
      getTreeModel().nodeStructureChanged(CRootNode.this);
    }

    @Override
    public void removedDatabase(final IDatabaseManager databaseManager, final IDatabase database) {
      // Make sure the database is not added twice
      for (int i = 0; i < getChildCount(); i++) {
        final CDatabaseNode child = (CDatabaseNode) getChildAt(i);

        if (child.getObject() == database) {
          child.dispose();

          remove(child);

          // Make sure the tree is updated.
          getTreeModel().nodeStructureChanged(CRootNode.this);

          return;
        }
      }
    }

    @Override
    public void reorderedDatabases(final IDatabaseManager databaseManager,
        final IDatabase database, final int index) {
      // Give all child nodes the opportunity to free allocated resources
      deleteChildren();

      // Remove and recreate the child nodes
      removeAllChildren();
      createChildren();

      // Make sure the tree is updated.
      getTreeModel().nodeStructureChanged(CRootNode.this);
    }
  }

}
