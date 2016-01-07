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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeSubstitutionDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * The action that allows the user to attach a type to a given operand tree node (i.e. register).
 */
public class TypeSubstitutionAction extends AbstractAction {

  private final TypeManager typeManager;
  private final JFrame owner;
  private final INaviOperandTreeNode selectedNode;
  private final BaseType stackFrame;

  private TypeSubstitutionAction(final JFrame owner, final String title,
      final TypeManager typeManager, final BaseType stackFrame,
      final INaviOperandTreeNode selectedNode) {
    super(title);
    this.typeManager =
        Preconditions.checkNotNull(typeManager, "IE02880: Type manager can not be null.");
    this.owner = owner;
    this.selectedNode = selectedNode;
    this.stackFrame = stackFrame;
  }

  /**
   * Instantiates a new action instance that can be used to create a new type substitution.
   *
   * @param owner The GUI component that owns the dialog shown by this action.
   * @param typeManager The type manager that holds the currently active type system.
   * @param stackFrame The stack frame of the current function. Must be null if no stack frame
   *        exists.
   * @param selectedNode The operand tree node that should receive the new type substitution.
   * @return A new instance that can be used to create a new type substitution.
   */
  public static TypeSubstitutionAction instantiateCreateTypeSubstitution(final JFrame owner,
      final TypeManager typeManager, final BaseType stackFrame,
      final INaviOperandTreeNode selectedNode) {
    return new TypeSubstitutionAction(
        owner, "Create type substitution", typeManager, stackFrame, selectedNode);
  }

  /**
   * Instantiates a new action instance that can be used to edit an existing type substitution.
   *
   * @param owner The GUI component that owns the dialog shown by this action.
   * @param typeManager The type manager that holds the currently active type system.
   * @param stackFrame The stack frame of the current function. Must be null if no stack frame
   *        exists.
   * @param selectedNode The operand tree node whose existing type substitution should be edited.
   * @return A new instance that can be used to edit an existing type substitution.
   */
  public static TypeSubstitutionAction instantiateEditTypeSubstitution(final JFrame owner,
      final TypeManager typeManager, final BaseType stackFrame,
      final INaviOperandTreeNode selectedNode) {
    return new TypeSubstitutionAction(
        owner, "Edit type substitution", typeManager, stackFrame, selectedNode);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TypeSubstitutionDialog dlg =
        new TypeSubstitutionDialog(owner, selectedNode, typeManager, stackFrame);
    GuiHelper.centerChildToParent(owner, dlg, true);
    dlg.setVisible(true);
  }
}
