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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * The action to edit an existing {@link TypeInstance type instance}.
 */
public class EditTypeInstanceAction extends AbstractAction {

  private final JFrame owner;
  private final TypeInstance instance;
  private final TypeInstanceContainer instanceContainer;
  private final TypeManager typeManager;

  public EditTypeInstanceAction(final JFrame owner, final TypeManager typeManager,
      final TypeInstance instance, final TypeInstanceContainer instanceContainer) {
    super("Edit type instance");
    this.owner = owner;
    this.typeManager = typeManager;
    this.instance = instance;
    this.instanceContainer = instanceContainer;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TypeInstanceDialog dlg =
        TypeInstanceDialog.instantiateEditTypeInstanceDialog(owner, typeManager, instance);
    GuiHelper.centerChildToParent(owner, dlg, true);
    dlg.setVisible(true);
    if (dlg.wasOkClicked()) {
      try {
        instanceContainer.setInstanceName(instance, dlg.getInstanceName());
      } catch (CouldntSaveDataException | CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
      } 
    }
  }
}
