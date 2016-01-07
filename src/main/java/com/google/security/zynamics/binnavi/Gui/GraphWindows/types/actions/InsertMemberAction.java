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

/**
 * Inserts a new member into a compound type right after an existing member.
 */
public class InsertMemberAction extends AbstractAction {

  private final JFrame owner;
  private final TypeManager typeManager;
  private final TypeMember existingMember;

  /**
   * @param owner
   * @param typeManager
   * @param existingMember
   */
  public InsertMemberAction(
      final JFrame owner, final TypeManager typeManager, final TypeMember existingMember) {
    super(String.format("Insert member after '%s'", existingMember.getName()));
    this.owner = owner;
    this.typeManager = typeManager;
    this.existingMember = existingMember;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final MemberDialog dlg = MemberDialog.createBuildNewMemberDialog(owner, typeManager);
    dlg.setVisible(true);
    if (!dlg.wasCanceled()) {
      final BaseType memberType = dlg.getBaseType();
      final String memberName = dlg.getMemberName();
      try {
        typeManager.insertMemberAfter(existingMember, memberType, memberName);
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}