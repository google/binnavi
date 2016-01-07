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

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;

/**
 * Encapsulates a base type in order to support drag and drop operations between the type editor and
 * the Graph2D component.
 *
 * @author jannewger (Jan Newger)
 *
 */
public class TransferableBaseType implements Transferable {

  private final BaseType baseType;

  public TransferableBaseType(final BaseType baseType) {
    this.baseType = baseType;
  }

  @SuppressWarnings("unused")
  @Override
  public Object getTransferData(final DataFlavor flavor)
      throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(flavor)) {
      throw new UnsupportedFlavorException(flavor);
    }
    return baseType;
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {TypeDataFlavor.BASE_TYPE_FLAVOR};
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.equals(TypeDataFlavor.BASE_TYPE_FLAVOR);
  }
}