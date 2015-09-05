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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.types.DragAndDropSupportWrapper;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.TransferHandler;

/**
 * Handles the transfer of base types from the type editor to the ZyGraph2DView component. This
 * handler only implements functionality for the drop operation onto the ZyGraph2DView component.
 * Creation of the corresponding transferable object is implemented in the transfer handler of the
 * types tree component.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class BaseTypeTransferHandler extends TransferHandler {

  private final TypeManager typeManager;
  // The dnd support wrapper is injected into this transfer handler to make it easily testable
  // (since TransferSupport is a final class).
  private final DragAndDropSupportWrapper wrapper;

  /**
   * Creates a new BaseTypeTransferHandler instance that handles creation of a new/update of an
   * existing type substitution based on a dropped {@link BaseType base type} instance.
   *
   * @param typeManager The type manager that is used to create or update a type substitution.
   * @param wrapper The drag and drop wrapper that encapsulates the current drag and drop operation.
   */
  public BaseTypeTransferHandler(final TypeManager typeManager,
      final DragAndDropSupportWrapper wrapper) {
    this.typeManager = typeManager;
    this.wrapper = wrapper;
  }

  private static boolean isLegalDropNode(final INaviOperandTreeNode node) {
    return node != null && node.getType() == ExpressionType.REGISTER
        && isSubstitutableExpression(node);
  }

  private static boolean isSubstitutableExpression(final INaviOperandTreeNode node) {
    return node.getChildren().size() == 0
        || (node.hasAddendSibling() && node.determineAddendValue() >= 0);
  }

  /**
   * Locate operand tree node and use type manager to assign type substitution.
   */
  private boolean createOrUpdateTypeSubstitution(final DragAndDropSupportWrapper wrapper)
      throws UnsupportedFlavorException, IOException, CouldntSaveDataException {
    final INaviOperandTreeNode node = wrapper.determineDropNode();
    if (!isLegalDropNode(node)) {
      return false;
    }
    final BaseType baseType = wrapper.getDroppedBaseType();
    if (node.getTypeSubstitution() != null) {
      typeManager.updateTypeSubstitution(node, node.getTypeSubstitution(), baseType,
          new ArrayList<TypeMember>(), 0 /* offset */
      );
    } else {
      // When creating a substitution via drag and drop, the offset is always zero to have a
      // better workflow for the user (otherwise we would need an additional dialog each time).
      typeManager.createTypeSubstitution(node, baseType, node.getOperandPosition(), 0 /* offset */,
          node.getInstructionAddress());
    }
    return true;
  }

  @Override
  public boolean canImport(final TransferSupport support) {
    wrapper.setSupport(support);
    return wrapper.acceptDrop();
  }

  @Override
  public boolean importData(final TransferSupport support) {
    wrapper.setSupport(support);
    if (!wrapper.acceptDrop()) {
      return false;
    }
    try {
      return createOrUpdateTypeSubstitution(wrapper);
    } catch (IOException | UnsupportedFlavorException | CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);
      return false;
    } 
  }
}
