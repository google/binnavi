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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;

/**
 * The action that allows the user to delete an existing type substitution from the given operand
 * tree node.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class DeleteTypeSubstitutionMenuAction extends AbstractAction {

  private final TypeManager typeManager;
  private final COperandTreeNode selectedNode;

  public DeleteTypeSubstitutionMenuAction(
      final TypeManager typeManager, final COperandTreeNode selectedNode) {
    super("Delete type substitution");
    this.typeManager = typeManager;
    this.selectedNode = selectedNode;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    try {
      typeManager.deleteTypeSubstitution(selectedNode);
    } catch (final CouldntDeleteException exception) {
      CUtilityFunctions.logException(exception);
    }
  }
}
