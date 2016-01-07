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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CLoadAddressSpaceAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.CAddressSpaceNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNode;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents address space nodes in the project tree.
 */
public final class CAddressSpaceNode extends CProjectTreeNode<INaviAddressSpace> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -927976512288945336L;

  /**
   * Icon used to display loaded address spaces in the project tree.
   */
  private static final ImageIcon ICON_ADDRESSSPACE = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/addressspace2.png"));

  /**
   * Icon used to display unloaded address spaces in the project tree.
   */
  private static final ImageIcon ICON_ADDRESSSPACE_GRAY = new ImageIcon(
      CMain.class.getResource("data/projecttreeicons/addressspace_gray.png"));

  /**
   * Database the address space belongs to.
   */
  private final IDatabase m_database;

  /**
   * Project the address space belongs to.
   */
  private final INaviProject m_project;

  /**
   * The address space described by the node.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Listens on the address space and updates the node or its child nodes if something important
   * happens.
   */
  private final InternalAddressSpaceListener m_addrSpaceListener =
      new InternalAddressSpaceListener();

  private final InternalAddressSpaceConfigurationListener m_addrSpaceConfigurationListener =
      new InternalAddressSpaceConfigurationListener();

  private final InternalAddressSpaceContentListener m_addrSpaceContentListener =
      new InternalAddressSpaceContentListener();

  /**
   * Creates a new node object.
   * 
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of this node.
   * @param database Database the address space belongs to.
   * @param project Project the address space belongs to.
   * @param addressSpace The address space described by the node.
   * @param container Context in which the views of the address space are opened.
   */
  public CAddressSpaceNode(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database, final INaviProject project, final INaviAddressSpace addressSpace,
      final IViewContainer container) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CAddressSpaceNodeComponent(projectTree, database, project, addressSpace);
      }
    }, new CAddressSpaceNodeMenuBuilder(projectTree, parentNode, null, database, project,
        new INaviAddressSpace[] {addressSpace}, container), addressSpace);

    m_project = Preconditions.checkNotNull(project, "IE01941: Project can't be null");
    m_addressSpace =
        Preconditions.checkNotNull(addressSpace, "IE01942: Address space can't be null");
    m_database = Preconditions.checkNotNull(database, "IE02344: Database argument can not be null");

    createChildren();

    m_addressSpace.addListener(m_addrSpaceListener);
    m_addressSpace.getConfiguration().addListener(m_addrSpaceConfigurationListener);
  }

  /**
   * Creates the child nodes of address space nodes. One child node is added for each module found
   * in the project.
   */
  @Override
  protected void createChildren() {
    if (m_addressSpace.isLoaded()) {
      m_addressSpace.getContent().addListener(m_addrSpaceContentListener);

      for (final INaviModule module : m_addressSpace.getContent().getModules()) {
        add(new CModuleNode(getProjectTree(), this, m_database, m_addressSpace, module,
            new CProjectContainer(m_database, m_project, m_addressSpace)));
      }
    }
  }

  @Override
  public void dispose() {
    super.dispose();

    if (m_addressSpace.isLoaded()) {
      m_addressSpace.getContent().removeListener(m_addrSpaceContentListener);
    }

    m_addressSpace.removeListener(m_addrSpaceListener);
    m_addressSpace.getConfiguration().removeListener(m_addrSpaceConfigurationListener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    if (!m_addressSpace.isLoaded()) {
      final Action action =
          CActionProxy.proxy(new CLoadAddressSpaceAction(getProjectTree(),
              new INaviAddressSpace[] {m_addressSpace}));
      action.actionPerformed(new ActionEvent(this, 0, ""));
    }
  }

  @Override
  public Icon getIcon() {
    return m_addressSpace.isLoaded() ? ICON_ADDRESSSPACE : ICON_ADDRESSSPACE_GRAY;
  }

  @Override
  public String toString() {
    return m_addressSpace.getConfiguration().getName() + " ("
        + (m_addressSpace.isLoaded() ? m_addressSpace.getContent().getModules().size() : "?") + ")";
  }

  private class InternalAddressSpaceConfigurationListener extends
      CAddressSpaceConfigurationListenerAdapter {
    @Override
    public void changedName(final INaviAddressSpace addressSpace, final String name) {
      getTreeModel().nodeChanged(CAddressSpaceNode.this);
    }
  }

  private class InternalAddressSpaceContentListener implements IAddressSpaceContentListener {
    /**
     * When a module is added to the address space, a new node that represents the module must be
     * added to the tree.
     */
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          add(new CModuleNode(getProjectTree(), CAddressSpaceNode.this, m_database, addressSpace,
              module, new CProjectContainer(m_database, m_project, addressSpace)));

          getTreeModel().nodeStructureChanged(CAddressSpaceNode.this);
        }

      }.invokeAndWait();
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
    }

    /**
     * When a module was removed from the address space, the corresponding tree node that represents
     * the removed module must be removed from the tree.
     */
    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          // Delete the node that represents the removed module.
          for (int i = 0; i < getChildCount(); i++) {
            final CModuleNode node = (CModuleNode) getChildAt(i);

            if (node.getObject() == module) {
              node.dispose();
              remove(node);
              break;
            }
          }

          getTreeModel().nodeStructureChanged(CAddressSpaceNode.this);
        }
      }.invokeAndWait();
    }
  }

  /**
   * This listener keeps track of relevant address space events and updates the tree if something
   * important happens.
   */
  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_addrSpaceContentListener);
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          createChildren();

          getTreeModel().nodeStructureChanged(CAddressSpaceNode.this);
        }
      }.invokeAndWait();
    }
  }
}
