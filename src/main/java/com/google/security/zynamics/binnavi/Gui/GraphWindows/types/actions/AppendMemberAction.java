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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.MemberDialog;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * The action to add a new member to an existing base type in the type editor.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class AppendMemberAction extends AbstractAction {

  private final JFrame owner;
  private final TypeManager typeManager;
  private final BaseType selectedType;

  public AppendMemberAction(
      final JFrame owner, final TypeManager typeManager, final BaseType selectedType) {
    super("Append member");
    this.owner = owner;
    this.typeManager = typeManager;
    this.selectedType = selectedType;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final MemberDialog dlg = MemberDialog.createBuildNewMemberDialog(owner, typeManager);
    GuiHelper.centerChildToParent(owner, dlg, true);
    dlg.setVisible(true);
    if (!dlg.wasCanceled()) {
      final String name = dlg.getMemberName();
      final BaseType baseType = dlg.getBaseType();
      try {
        final TypeMember member = typeManager.appendMember(selectedType, baseType, name);
        if (member == null) {
          CMessageBox.showInformation(owner,
              "Unable to append member since that would create a recursive type definition.");
        }
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}