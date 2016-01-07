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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.CAddressSpaceNode;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.CModuleNode;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.gui.dndtree.DNDTree;

/**
 * Drag & Drop handler for dragging modules into address spaces.
 */
public final class CModulesToAddressSpaceHandler extends CAbstractDropHandler {
  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates a new handler object.
   * 
   * @param parent Parent window used for dialogs.
   */
  public CModulesToAddressSpaceHandler(final JFrame parent) {
    super(CModuleTransferable.MODULE_FLAVOR);

    Preconditions.checkNotNull(parent, "IE01928: Parent argument can not be null");

    m_parent = parent;
  }

  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    if ((parentNode instanceof CAddressSpaceNode) && (draggedNode instanceof CModuleNode)) {
      final CModuleNode draggedModule = (CModuleNode) draggedNode;
      final CAddressSpaceNode targetAddressSpace = (CAddressSpaceNode) parentNode;

      return targetAddressSpace.getObject().isLoaded()
          && !targetAddressSpace.getObject().getContent().getModules()
              .contains(draggedModule.getObject())
          && targetAddressSpace.getObject().inSameDatabase(draggedModule.getObject());
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean canHandle(final DefaultMutableTreeNode parentNode, final Object data) {
    if (parentNode instanceof CAddressSpaceNode) {
      final List<CModule> modules = (List<CModule>) data;

      if (modules.isEmpty()) {
        return false;
      }

      return modules.get(0).inSameDatabase(((CAddressSpaceNode) parentNode).getObject());
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void drop(final DefaultMutableTreeNode parentNode, final Object data) {
    final CAddressSpaceNode addressSpaceNode = (CAddressSpaceNode) parentNode;

    final INaviAddressSpace addressSpace = addressSpaceNode.getObject();

    final List<CModule> modules = (List<CModule>) data;

    for (final CModule module : modules) {
      CAddressSpaceFunctions.addModule(m_parent, addressSpace, module);
    }
  }

  @Override
  public void drop(final DNDTree target, final DefaultMutableTreeNode parentNode,
      final DefaultMutableTreeNode draggedNode) {
    final CModuleNode draggedNodeNode = (CModuleNode) draggedNode;
    final CAddressSpaceNode parentNodeNode = (CAddressSpaceNode) parentNode;

    CAddressSpaceFunctions.addModule(m_parent, parentNodeNode.getObject(),
        draggedNodeNode.getObject());

    target.setSelectionPath(new TreePath(parentNodeNode.getLastLeaf().getPath()));
    target.setSelectionPath(new TreePath(draggedNodeNode.getPath()));
  }
}
