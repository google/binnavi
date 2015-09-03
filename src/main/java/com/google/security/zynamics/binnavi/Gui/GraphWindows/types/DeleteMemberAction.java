/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Deletes the selected member(s) from the type system.
 */
public class DeleteMemberAction extends AbstractAction {

  private final JFrame owner;
  private final TypeEditor typeEditor;
  private final TypeManager typeManager;

  /**
   * @param owner The owner window that can be used to display GUI elements.
   * @param typeManager The type manager that holds the current type system.
   * @param typeEditor The type editor that is used to determine the currently selected members.
   */
  public DeleteMemberAction(final JFrame owner, final TypeManager typeManager,
      final TypeEditor typeEditor) {
    super("Delete member");
    this.owner = owner;
    this.typeManager = typeManager;
    this.typeEditor = typeEditor;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    final ImmutableList<TypeMember> members = typeEditor.getSelectedMembers();
    if (members.isEmpty() ||
        CMessageBox.showYesNoQuestion(owner, "Do you really want to delete these member(s)?")
        != JOptionPane.YES_OPTION) {
      return;
    }

    try {
      for (final TypeMember member : members) {
        typeManager.deleteMember(member);
      }
    } catch (CouldntDeleteException | CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
    } 
  }
}
