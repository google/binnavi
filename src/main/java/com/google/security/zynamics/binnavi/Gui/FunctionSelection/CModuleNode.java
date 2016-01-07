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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeModel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.Modules.IModuleListener;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

/**
 * Module node class for the function selection dialog.
 */
public final class CModuleNode extends IconNode implements IFunctionTreeNode {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7938396730896978085L;

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
   * The module represented by the node.
   */
  private final INaviModule m_module;

  /**
   * The tree model of the function selection tree.
   */
  private final DefaultTreeModel m_model;

  /**
   * Provides the implementations of the available actions.
   */
  private final IActionProvider m_actionProvider;

  /**
   * Updates the module node when the module changes in some relevant way.
   */
  private final IModuleListener m_internalModuleListener = new InternalModuleListener();

  /**
   * Creates a new module node object.
   *
   * @param module The module represented by the node.
   * @param model The tree model of the function selection tree.
   * @param actionProvider Provides the implementations of the available actions.
   */
  public CModuleNode(final INaviModule module, final DefaultTreeModel model,
      final IActionProvider actionProvider) {
    Preconditions.checkNotNull(module, "IE01577: Module argument can not be null");

    Preconditions.checkNotNull(model, "IE01578: Model argument can not be null");

    Preconditions.checkNotNull(actionProvider, "IE01579: Action provider argument can not be null");

    m_module = module;
    m_model = model;
    m_actionProvider = actionProvider;

    m_module.addListener(m_internalModuleListener);

    createChildren();
  }

  /**
   * Creates one child node for each function of a module except for imported functions.
   */
  private void createChildren() {
    if (m_module.isLoaded()) {
      for (final INaviFunction function :
          m_module.getContent().getFunctionContainer().getFunctions()) {
        if (function.getType() != FunctionType.IMPORT) {
          add(new CFunctionIconNode(function));
        }
      }
    }
  }

  @Override
  public void doubleClicked() {
    // Load modules on double-click

    if (!m_module.isLoaded()) {
      m_actionProvider.loadModule(m_module);
    }
  }

  @Override
  public Icon getIcon() {
    return m_module.isLoaded() ? ICON_MODULE : ICON_MODULE_GRAY;
  }

  @Override
  public String toString() {
    return String.format("%s (%d/%d)", m_module.getConfiguration().getName(),
        m_module.getFunctionCount(), m_module.getCustomViewCount());
  }

  /**
   * Updates the module node when the module changes in some relevant way.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void addedView(final INaviModule module, final INaviView view) {
      m_model.nodeChanged(CModuleNode.this);
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      m_model.nodeChanged(CModuleNode.this);
    }

    /**
     * When the module is loaded, create the child nodes.
     */
    @Override
    public void loadedModule(final INaviModule module) {
      createChildren();

      m_model.nodeStructureChanged(CModuleNode.this);
    }
  }
}
