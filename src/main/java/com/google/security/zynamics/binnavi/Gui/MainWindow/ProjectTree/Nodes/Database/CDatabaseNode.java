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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.CDatabaseListenerAdapter;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Database.CDatabaseSettingsPanel;
import com.google.security.zynamics.binnavi.Gui.Database.IDatabaseSettingsPanelListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.COpenDatabaseAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Database.Component.CDatabaseNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.DebuggerContainer.CDebuggerContainerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.CModuleContainerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ProjectContainer.CProjectContainerNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.TagContainer.CTagContainerNode;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

/**
 * Represents database nodes in the project tree.
 */
public final class CDatabaseNode extends CProjectTreeNode<IDatabase> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8655562357921734518L;

  /**
   * Icon used for loaded databases.
   */
  private static final ImageIcon ICON_DATABASE_LOADED = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/database_connected.png"));

  /**
   * Icon used for closed databases.
   */
  private static final ImageIcon ICON_DATABASE_CLOSED = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/database_closed.png"));

  /**
   * The database that provides the information necessary to build the child nodes of the database
   * node.
   */
  private final IDatabase m_database;

  /**
   * Listens on the database and updates the child nodes of the database node if necessary.
   */
  private final InternalDatabaseListener m_listener;

  /**
   * Listener that updates the node on changes to the database settings panel.
   */
  private final IDatabaseSettingsPanelListener m_panelListener =
      new InternalDatabaseSettingsPanelListener();

  /**
   * Creates a new node object.
   * 
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node this node.
   * @param database The database that provides the information necessary to build the child nodes
   *        of the database node.
   */
  public CDatabaseNode(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CDatabaseNodeComponent(database);
      }
    }, new CDatabaseNodeMenuBuilder(projectTree, parentNode, database), database);

    m_database = Preconditions.checkNotNull(database, "IE01962: Database can't be null");

    createChildren();

    // Add a listener that keeps track of the database.
    m_listener = new InternalDatabaseListener();
    m_database.addListener(m_listener);

    ((CDatabaseNodeComponent) getComponent()).getPanel().addListener(m_panelListener);
  }

  /**
   * Creates the child nodes of a database node. A database node has child nodes that represent the
   * projects, the modules, and the raw modules in the database.
   */
  @Override
  protected void createChildren() {
    if (m_database.isConnected() && m_database.isLoaded()) {
      add(new CProjectContainerNode(getProjectTree(), m_database));
      add(new CModuleContainerNode(getProjectTree(), m_database));
      // add(new CRawModuleContainerNode(getProjectTree(),m_database));
      add(new CDebuggerContainerNode(getProjectTree(), m_database));
      add(new CTagContainerNode(getProjectTree(), m_database));
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    ((CDatabaseNodeComponent) getComponent()).getPanel().removeListener(m_panelListener);

    m_database.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Connect on double-click if there is no connection to the
    // database yet.

    if (!m_database.isConnected() && !m_database.getConfiguration().getIdentity().isEmpty()
        && !m_database.getConfiguration().getUser().isEmpty()
        && !m_database.getConfiguration().getPassword().isEmpty()) {
      final Action action =
          CActionProxy.proxy(new COpenDatabaseAction(getProjectTree(), m_database));
      action.actionPerformed(new ActionEvent(this, 0, ""));
    }
  }

  @Override
  public Icon getIcon() {
    return m_database.isLoaded() ? ICON_DATABASE_LOADED : ICON_DATABASE_CLOSED;
  }

  @Override
  public String toString() {
    return m_database.getConfiguration().getDescription();
  }

  /**
   * This listener keeps track of changes in the database and updates the tree on any relevant
   * changes.
   */
  private class InternalDatabaseListener extends CDatabaseListenerAdapter {
    @Override
    public void addedProject(final IDatabase connection, final INaviProject newProject) {
      getTreeModel().nodeChanged(CDatabaseNode.this);
    }

    @Override
    public void changedDescription(final IDatabase database, final String description) {
      // Necessary because the name of the database is
      // displayed in the tree.

      getTreeModel().nodeChanged(CDatabaseNode.this);
    }

    /**
     * If the database is closed, all child nodes of the database node must be removed from the
     * tree.
     */
    @Override
    public void closedDatabase(final IDatabase connection) {
      // When the database is closed, remove the child nodes of the tree
      // because the database is no longer accessible.

      new SwingInvoker() {
        @Override
        protected void operation() {
          // Give the child nodes the opportunity to free allocated resources.
          deleteChildren();

          // Make sure the tree is updated.
          getTreeModel().nodeStructureChanged(CDatabaseNode.this);
        }
      }.invokeAndWait();
    }

    @Override
    public void loadedDatabase(final IDatabase database) {
      // When the database is loaded, the child nodes (project container,
      // module container, raw module container) need to be displayed.

      new SwingInvoker() {
        @Override
        protected void operation() {
          createChildren();

          // Make sure the tree is updated.
          getTreeModel().nodeStructureChanged(CDatabaseNode.this);
        }
      }.invokeAndWait();
    }
  }

  /**
   * Listener that updates the node on changes to the database settings panel.
   */
  private class InternalDatabaseSettingsPanelListener implements IDatabaseSettingsPanelListener {
    @Override
    public void changedConnectionSettings(final CDatabaseSettingsPanel databaseSettingsPanel,
        final boolean changed) {
      ((CDatabaseNodeMenuBuilder) getMenuBuilder()).allowConnection(!changed);
    }
  }
}
