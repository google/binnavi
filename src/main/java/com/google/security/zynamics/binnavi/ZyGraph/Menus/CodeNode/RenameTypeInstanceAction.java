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

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EditVariableDialog;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;

/**
 * Displays a dialog to rename the given {@link TypeInstance type instance}.
 */
public class RenameTypeInstanceAction extends AbstractAction {

  private final JFrame parent;
  private final TypeInstance typeInstance;
  private final TypeInstanceContainer instanceContainer;

  public RenameTypeInstanceAction(
      final JFrame parent,
      final TypeInstanceContainer instanceContainer,
      final TypeInstance typeInstance) {
    super("Rename variable");
    this.parent = parent;
    this.instanceContainer = instanceContainer;
    this.typeInstance = typeInstance;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final EditVariableDialog dialog =
        EditVariableDialog.CreateEditVariableDialog(parent, typeInstance.getName());
    dialog.setVisible(true);
    if (dialog.wasOkClicked()) {
      try {
        instanceContainer.setInstanceName(typeInstance, dialog.getVariableName());
      } catch (CouldntSaveDataException | CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
      } 
    }
  }
}
