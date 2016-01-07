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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CLoadProjectAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.CAddressSpaceNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.CProjectNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Traces.CTracesNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Project.CProjectViewsContainerNode;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

/**
 * Represents a project node in the project tree.
 */
public final class CProjectNode extends CProjectTreeNode<INaviProject> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7833552492435605478L;

  /**
   * Icon used in the project tree if the project is loaded.
   */
  private static final ImageIcon ICON_PROJECTS_CONTAINER = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project3.png"));

  /**
   * Icon used in the project tree if the project is closed.
   */
  private static final ImageIcon ICON_PROJECTS_CONTAINER_GRAY = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/project3_gray.png"));

  /**
   * The database the project belongs to.
   */
  private final IDatabase m_database;

  /**
   * The project that is described by the node.
   */
  private final INaviProject m_project;

  /**
   * Listens on the project and updates the node and its children if something import happens.
   */
  private final InternalProjectListener m_listener;

  /**
   * View container object for the represented project.
   */
  private final CProjectContainer m_container;

  /**
   * Creates a new project node.
   * 
   * @param projectTree The project tree of the main window.
   * @param parentNode Parent node of the project node.
   * @param database Database the project belongs to.
   * @param project Project the node represents.
   * @param contextContainer The container in whose context the views are opened.
   */
  public CProjectNode(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database, final INaviProject project, final CProjectContainer contextContainer) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CProjectNodeComponent(projectTree, database, project, contextContainer);
      }
    }, new CProjectNodeMenuBuilder(projectTree, parentNode, database, new INaviProject[] {project},
        null), project);

    Preconditions.checkNotNull(database, "IE01980: Database can't be null");

    Preconditions.checkNotNull(project, "IE01981: Project can't be null");

    m_project = project;
    m_database = database;
    m_container = contextContainer;

    createChildren();

    m_listener = new InternalProjectListener();
    m_project.addListener(m_listener);
  }

  /**
   * Creates the child nodes of project nodes. One child node is added for each address space found
   * in the project.
   */
  @Override
  protected void createChildren() {
    if (m_project.isLoaded()) {
      for (final INaviAddressSpace addressSpace : m_project.getContent().getAddressSpaces()) {
        add(new CAddressSpaceNode(getProjectTree(), this, m_database, m_project, addressSpace,
            m_container));
      }

      add(new CProjectViewsContainerNode(getProjectTree(), m_project, m_container));
      add(new CTracesNode(getProjectTree(), m_container));
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    m_container.dispose();
    m_project.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    // Open projects on double-click.
    if (!m_project.isLoaded()) {
      final Action action =
          CActionProxy.proxy(new CLoadProjectAction(getProjectTree(),
              new INaviProject[] {m_project}));
      action.actionPerformed(new ActionEvent(this, 0, ""));
    }
  }

  @Override
  public CProjectNodeComponent getComponent() {
    return (CProjectNodeComponent) super.getComponent();
  }

  @Override
  public Icon getIcon() {
    return m_project.isLoaded() ? ICON_PROJECTS_CONTAINER : ICON_PROJECTS_CONTAINER_GRAY;

  }

  @Override
  public String toString() {
    return m_project.getConfiguration().getName() + " (" + m_project.getAddressSpaceCount() + ")";
  }

  /**
   * Listens on the project and updates the node and its children if something import happens.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    /**
     * When an address space is added to the project, a new node that represents this address space
     * must be added to the tree.
     */
    @Override
    public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          insert(new CAddressSpaceNode(getProjectTree(), CProjectNode.this, m_database, project,
              space, m_container), getChildCount() - 2);

          getTreeModel().nodeStructureChanged(CProjectNode.this);
        }
      }.invokeAndWait();
    }

    @Override
    public void changedName(final INaviProject project, final String name) {
      getTreeModel().nodeChanged(CProjectNode.this);
    }

    /**
     * When a project is loaded, the nodes that represent the address spaces must be added as child
     * nodes to the project node.
     */
    @Override
    public void loadedProject(final CProject project) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          createChildren();

          getTreeModel().nodeStructureChanged(CProjectNode.this);
        }
      }.invokeAndWait();
    }

    /**
     * When an address space was removed from the project, the corresponding node must be removed
     * from the tree.
     */
    @Override
    public void removedAddressSpace(final INaviProject project, final INaviAddressSpace addressSpace) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          // Remove the node that represents the deleted address space.
          for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof CAddressSpaceNode) {
              final CAddressSpaceNode node = (CAddressSpaceNode) getChildAt(i);

              if (node.getObject() == addressSpace) {
                node.dispose();
                remove(node);
                break;
              }
            }
          }

          getTreeModel().nodeStructureChanged(CProjectNode.this);
        }
      }.invokeAndWait();
    }
  }
}
