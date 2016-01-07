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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * Transferable wrapper class to support drag and drop operations within the type editor.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class TransferableMemberNodes implements Transferable {

  private final List<TypeMemberTreeNode> nodes;

  public TransferableMemberNodes(final List<TypeMemberTreeNode> selectedNodes) {
    this.nodes = selectedNodes;
  }

  // We need to override this method but do not explicitly throw IOException, thus the suppress
  // warning annotation.
  @SuppressWarnings("unused")
  @Override
  public Object getTransferData(final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return nodes;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {TypeDataFlavor.TYPE_MEMBER_FLAVOR};
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.equals(TypeDataFlavor.TYPE_MEMBER_FLAVOR);
  }
}