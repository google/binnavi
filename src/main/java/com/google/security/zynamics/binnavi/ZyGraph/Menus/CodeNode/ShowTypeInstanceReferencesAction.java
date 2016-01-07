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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeInstanceReferencesDialog;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.CModuleNodeComponent;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceReference;

/**
 * The action to show a dialog that displays all {@link TypeInstanceReference instance references}
 * of a {@link INaviOperandTreeNode tree node}.
 */
public class ShowTypeInstanceReferencesAction extends AbstractAction {

  private final JFrame owner;
  private final List<TypeInstanceReference> references;
  private final INaviModule module;

  /**
   * @param owner The GUI component that owns the dialog shown by this action.
   * @param references The list of instance references to be displayed in the dialog.
   * @param module
   */
  public ShowTypeInstanceReferencesAction(final JFrame owner,
      final List<TypeInstanceReference> references, final INaviModule module) {
    super("Show xrefs");
    this.owner = owner;
    this.references = references;
    this.module = module;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TypeInstanceReferencesDialog dlg = new TypeInstanceReferencesDialog(owner, references);
    dlg.setVisible(true);
    if (!dlg.wasCancelled()) {
      CModuleNodeComponent.focusTypeInstance(module, dlg.getSelectedXRef().getTypeInstance());
    }
  }
}
