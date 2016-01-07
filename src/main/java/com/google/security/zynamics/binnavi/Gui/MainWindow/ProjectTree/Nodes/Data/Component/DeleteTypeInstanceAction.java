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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * The action to delete an existing type instance from the database and
 * {@link TypeInstanceContainer}.
 */
public class DeleteTypeInstanceAction extends AbstractAction {

  private static final long serialVersionUID = -4546221812249933355L;
  private final JFrame owner;
  private final TypeInstance instance;
  private final TypeInstanceContainer instanceContainer;

  public DeleteTypeInstanceAction(final JFrame owner, final TypeInstance instance,
      final TypeInstanceContainer instanceContainer) {
    super("Delete instance");
    this.owner = owner;
    this.instance = instance;
    this.instanceContainer = instanceContainer;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final int result =
        CMessageBox.showYesNoQuestion(
            owner,
            String.format("Do you really want to delete the type instance '%s'?",
                instance.getName()));
    if (result == JOptionPane.YES_OPTION) {
      try {
        instanceContainer.deleteInstance(instance);
      } catch (final CouldntDeleteException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
