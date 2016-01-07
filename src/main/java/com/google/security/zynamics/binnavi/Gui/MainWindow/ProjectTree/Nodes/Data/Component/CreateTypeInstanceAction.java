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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.types.Section;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstance;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * The action to display a dialog in order to create a new {@link TypeInstance}.
 */
public class CreateTypeInstanceAction extends AbstractAction {

  private static final long serialVersionUID = -2416538403230825588L;
  private final TypeInstanceContainer instanceContainer;
  private final JFrame owner;
  private final TypeManager typeManager;
  private final Section section;
  private final Long sectionOffset;

  /**
   * Creates a new type instance action without specifying an offset.
   */
  public CreateTypeInstanceAction(final JFrame owner,
      final TypeInstanceContainer instanceContainer, final TypeManager typeManager,
      final Section section) {
    this(owner, instanceContainer, typeManager, section, null);
  }

  public CreateTypeInstanceAction(final JFrame owner,
      final TypeInstanceContainer instanceContainer, final TypeManager typeManager,
      final Section section, final Long sectionOffset) {
    super("Create type instance");
    this.owner = owner;
    this.typeManager = typeManager;
    this.instanceContainer = instanceContainer;
    this.section = section;
    this.sectionOffset = sectionOffset;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    final TypeInstanceDialog dlg =
        TypeInstanceDialog.instantiateCreateTypeInstanceDialog(owner, typeManager, section,
            sectionOffset);
    GuiHelper.centerOnScreen(dlg);
    dlg.setVisible(true);
    if (dlg.wasOkClicked()) {
      try {
        instanceContainer.createInstance(dlg.getInstanceName(), null, dlg.getInstanceType(),
            section, dlg.getSectionOffset());
      } catch (CouldntSaveDataException | CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
