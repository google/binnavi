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
package com.google.security.zynamics.binnavi.Gui.InsertViewDialog;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Loaders.CModuleLoader;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import java.awt.Window;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;

/**
 * Class for module nodes in view selection dialogs.
 */
public final class ViewSelectionModuleNode extends IconNode implements IViewSelectionTreeNode {
  /**
   * Icon used for loaded modules.
   */
  private static final ImageIcon ICON_MODULE =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module.png"));

  /**
   * Icon used for grayed modules.
   */
  private static final ImageIcon ICON_MODULE_GRAY =
      new ImageIcon(CMain.class.getResource("data/projecttreeicons/project_module_gray.png"));

  /**
   * Parent window of the dialog.
   */
  private final Window m_dialog;

  /**
   * The module represented by the node.
   */
  private final INaviModule m_module;

  /**
   * The tree model of the function selection tree.
   */
  private final DefaultTreeModel m_model;

  /**
   * Updates the node on changes in the module.
   */
  private final IModuleListener m_internalModuleListener = new InternalModuleListener();

  /**
   * Creates a new module node.
   *
   * @param parent Parent window of the dialog.
   * @param module The module represented by the node.
   * @param model Tree model of the tree the node belongs to.
   */
  public ViewSelectionModuleNode(final Window parent, final INaviModule module,
      final DefaultTreeModel model) {
    Preconditions.checkNotNull(module, "IE01821: Module argument can not be null");

    m_dialog = parent;
    m_module = module;
    m_model = model;

    m_module.addListener(m_internalModuleListener);

    createChildren();
  }

  /**
   * Creates the children of the node.
   */
  private void createChildren() {
    if (m_module.isLoaded()) {
      for (final INaviView view : m_module.getContent().getViewContainer().getViews()) {
        if (view.getNodeCount() == 0) {
          continue;
        }

        add(new CViewIconNode(view));
      }
    }
  }

  @Override
  public void doubleClicked() {
    // Load modules on double-click

    if (!m_module.isLoaded()) {
      CModuleLoader.loadModule(m_dialog, m_module);
    }
  }

  @Override
  public Icon getIcon() {
    return m_module.isLoaded() ? ICON_MODULE : ICON_MODULE_GRAY;
  }

  @Override
  public String toString() {
    return m_module.getConfiguration().getName();
  }

  /**
   * Updates the module node when the module changes in some relevant way.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      m_model.nodeChanged(ViewSelectionModuleNode.this);
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      m_model.nodeChanged(ViewSelectionModuleNode.this);
    }

    /**
     * When the module is loaded, create the child nodes.
     */
    @Override
    public void loadedModule(final INaviModule module) {
      createChildren();

      m_model.nodeStructureChanged(ViewSelectionModuleNode.this);
    }
  }
}
