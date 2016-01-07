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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeDataFlavor;
import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.types.BaseType;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraphDragAndDropSupport;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.TransferHandler.TransferSupport;

/**
 * A wrapper class for the graph and the transfer support used during a drag and drop operation on a
 * ZyGraph instance. This class can be easily mocked and thus enables transfer handlers to be tested
 * even though {@link TransferSupport} is a final class.
 */
public class DragAndDropSupportWrapper {

  private TransferSupport support;
  private final ZyGraph graph;

  public DragAndDropSupportWrapper(final ZyGraph graph) {
    this.graph = graph;
  }

  /**
   * Tests whether the drop operation is allowed .
   *
   * @return Returns whether the drop operation is allowed.
   */
  public boolean acceptDrop() {
    return support.isDrop() && support.isDataFlavorSupported(TypeDataFlavor.BASE_TYPE_FLAVOR)
        && support.getComponent() instanceof ZyGraph2DView;
  }

  /**
   * Determines the base type that was dropped.
   *
   * @return The dropped base type.
   * @throws UnsupportedFlavorException Thrown if the drop entity is not a base type.
   * @throws IOException Thrown if an exception during drag and drop occurred.
   */
  public BaseType getDroppedBaseType() throws UnsupportedFlavorException, IOException {
    return (BaseType) support.getTransferable().getTransferData(TypeDataFlavor.BASE_TYPE_FLAVOR);
  }

  /**
   * Determines the operand tree node instance on which the user dropped a base type.
   *
   * @return The operand tree node instance the user dropped base type on.
   */
  public INaviOperandTreeNode determineDropNode() {
    final Object dropTarget = ZyGraphDragAndDropSupport.getDragOverState(
        graph, support.getDropLocation().getDropPoint()).getDragOverObject().getObject();
    return (dropTarget instanceof INaviOperandTreeNode) ? (INaviOperandTreeNode) dropTarget : null;
  }

  /**
   * Sets the transfer support instance which contains all relevant information to handle the drag
   * and drop operation.
   *
   * @param support The transfer support for the drag and drop operation.
   */
  public void setSupport(final TransferSupport support) {
    this.support = support;
  }
}