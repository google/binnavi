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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types.actions;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeDialog;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * The action to edit the type that is currently selected in the type editor.
 */
public class EditTypeAction extends AbstractAction {

  private final JFrame owner;
  private final TypeManager typeManager;
  private final BaseType selectedType;

  public EditTypeAction(
      final JFrame owner, final TypeManager typeManager, final BaseType selectedType) {
    super("Edit type");
    this.owner = owner;
    this.typeManager = typeManager;
    this.selectedType = selectedType;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TypeDialog dlg = TypeDialog.createEditTypeDialog(owner, typeManager, selectedType);
    GuiHelper.centerChildToParent(owner, dlg, true);
    dlg.setVisible(true);
  }
}