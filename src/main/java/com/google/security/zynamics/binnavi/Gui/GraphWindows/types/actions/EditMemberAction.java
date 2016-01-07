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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.MemberDialog;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * The action to edit the currently selected member in the type editor.
 */
public class EditMemberAction extends AbstractAction {

  private final TypeManager typeManager;
  private final JFrame owner;
  private final TypeMember selectedMember;

  public EditMemberAction(final JFrame owner, final TypeManager typeManager,
      final TypeMember selectedMember) {
    super("Edit Member");
    this.typeManager = typeManager;
    this.selectedMember = selectedMember;
    this.owner = owner;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final MemberDialog dialog =
        MemberDialog.createEditMemberDialog(owner, typeManager, selectedMember);
    GuiHelper.centerChildToParent(owner, dialog, true);
    dialog.setVisible(true);
    if (!dialog.wasCanceled()) {
      try {
        switch (selectedMember.getParentType().getCategory()) {
          case STRUCT:
            typeManager.updateStructureMember(selectedMember, dialog.getBaseType(),
                dialog.getMemberName(), selectedMember.getBitOffset().get());
            break;
          case UNION:
            typeManager.updateUnionMember(selectedMember, dialog.getBaseType(),
                dialog.getMemberName());
            break;
          case FUNCTION_PROTOTYPE:
            typeManager.updateFunctionPrototypeMember(selectedMember, dialog.getBaseType(),
                dialog.getMemberName(), selectedMember.getArgumentIndex().get());
          default:
            throw new IllegalStateException("Error: can not edit a member of a non compound type.");
        }
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
