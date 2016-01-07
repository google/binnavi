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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CModuleFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractLazyComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CProjectTreeNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.CModuleNodeComponent;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents module nodes in the project tree.
 */
public final class CModuleNode extends CProjectTreeNode<INaviModule> {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5643276356053733957L;

  /**
   * Icon used for loaded modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module.png"));

  /**
   * Icon used for unloaded modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_GRAY =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module_gray.png"));

  /**
   * Icon used for incomplete modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_BROKEN =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module_broken.png"));

  /**
   * Icon used for not yet converted modules in the project tree.
   */
  private static final ImageIcon ICON_MODULE_UNCONVERTED =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module_light_gray.png"));

  /**
   * The module described by the node.
   */
  private final INaviModule m_module;

  /**
   * Context in which views of the represented module are opened.
   */
  private final IViewContainer m_contextContainer;

  /**
   * Updates the node on important changes in the represented module.
   */
  private final InternalModuleListener m_listener;

  /**
   * Constructor for modules inside projects.
   *
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of this node.
   * @param database Database the module belongs to.
   * @param addressSpace The address space the module belongs to.
   * @param module Module represented by this node.
   * @param contextContainer The container in whose context the views are opened.
   */
  public CModuleNode(final JTree projectTree,
      final DefaultMutableTreeNode parentNode,
      final IDatabase database,
      final INaviAddressSpace addressSpace,
      final INaviModule module,
      final CProjectContainer contextContainer) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CModuleNodeComponent(projectTree, database, addressSpace, module,
            contextContainer);
      }
    }, new CModuleNodeMenuBuilder(projectTree,
        parentNode,
        database,
        addressSpace,
        new INaviModule[] {module},
        null), module);
    
    Preconditions.checkNotNull(database, "IE01972: Database argument can't be null");
    Preconditions.checkNotNull(addressSpace, "IE01973: Address space can't be null");
    m_module = Preconditions.checkNotNull(module, "IE01974: Module can't be null");

    m_contextContainer = contextContainer;

    createChildren();

    m_listener = new InternalModuleListener();
    m_module.addListener(m_listener);
  }

  /**
   * Constructor for modules outside projects.
   *
   * @param projectTree Project tree of the main window.
   * @param parentNode Parent node of this node.
   * @param database Database the module belongs to.
   * @param module Module represented by this node.
   * @param contextContainer The container in whose context the views are opened.
   */
  public CModuleNode(final JTree projectTree, final DefaultMutableTreeNode parentNode,
      final IDatabase database, final INaviModule module, final CModuleContainer contextContainer) {
    super(projectTree, new CAbstractLazyComponent() {
      @Override
      protected CAbstractNodeComponent createComponent() {
        return new CModuleNodeComponent(projectTree, database, null, module, contextContainer);
      }
    }, new CModuleNodeMenuBuilder(projectTree,
        parentNode,
        database,
        null,
        new INaviModule[] {module},
        null), module);

    Preconditions.checkNotNull(database, "IE01970: Database can't be null");
    
    m_module = Preconditions.checkNotNull(module, "IE01971: Module can't be null");
    m_contextContainer = contextContainer;

    createChildren();

    m_listener = new InternalModuleListener();
    m_module.addListener(m_listener);
  }

  /**
   * Creates the child nodes of the module node. One node is added for the native Call graph of the
   * module, another node is added that contains all native Flow graph views of the module.
   */
  @Override
  protected void createChildren() {}

  @Override
  public void dispose() {
    super.dispose();

    m_contextContainer.dispose();
    m_module.removeListener(m_listener);

    deleteChildren();
  }

  @Override
  public void doubleClicked() {
    if (m_module.getConfiguration().getRawModule().isComplete() && !m_module.isLoaded()) {
      CModuleFunctions.loadModules(getProjectTree(), new INaviModule[] {m_module});
    }
  }

  @Override
  public CModuleNodeComponent getComponent() {
    return (CModuleNodeComponent) super.getComponent();
  }

  @Override
  public Icon getIcon() {
    if (m_module.getConfiguration().getRawModule().isComplete() && m_module.isInitialized()) {
      return m_module.isLoaded() ? ICON_MODULE : ICON_MODULE_GRAY;
    } else if (m_module.getConfiguration().getRawModule().isComplete()
        && !m_module.isInitialized()) {
      return ICON_MODULE_UNCONVERTED;
    } else {
      return ICON_MODULE_BROKEN;
    }
  }

  @Override
  public String toString() {
    return m_module.getConfiguration().getName() + " (" + m_module.getFunctionCount() + "/"
        + m_module.getCustomViewCount() + ")";
  }

  /**
   * Updates the node on important changes in the represented module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      getTreeModel().nodeChanged(CModuleNode.this);
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      getTreeModel().nodeChanged(CModuleNode.this);
    }

    @Override
    public void deletedView(final INaviModule module, final INaviView view) {
      getTreeModel().nodeChanged(CModuleNode.this);
    }

    /**
     * When the module is loaded, create the child nodes.
     */
    @Override
    public void loadedModule(final INaviModule module) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          createChildren();

          getTreeModel().nodeStructureChanged(CModuleNode.this);
        }
      }.invokeAndWait();
    }
  }
}
