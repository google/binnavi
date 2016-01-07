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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.DragAndDrop;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

/**
 * Transferable class that is used to drag & drop a list of modules from tables.
 */
public final class CTagTransferable implements Transferable {
  /**
   * Flavors supported by this drag & drop handler.
   */
  private static final DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[1];

  /**
   * Data flavor for drag & dropping modules.
   */
  public static final DataFlavor TAG_FLAVOR;

  /**
   * The modules to drag & drop.
   */
  private final List<INaviModule> m_modules;

  static {
    TAG_FLAVOR = new DataFlavor(TreeNode.class, "BinNavi Tags");
    SUPPORTED_FLAVORS[0] = TAG_FLAVOR;
  }

  /**
   * Creates a new transferable object that can be used to drag & drop a list of modules.
   * 
   * @param modules The modules to drag & drop.
   */
  public CTagTransferable(final List<INaviModule> modules) {
    Preconditions.checkNotNull(modules, "IE01929: Modules argument can't be null");

    m_modules = new ArrayList<INaviModule>(modules);
  }

  /**
   * Returns the modules that are drag & dropped by this transferable object.
   * 
   * @return A list of modules.
   */
  public List<INaviModule> getModules() {
    return new ArrayList<INaviModule>(m_modules);
  }

  @Override
  public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
    if (flavor.equals(TAG_FLAVOR)) {
      return new ArrayList<INaviModule>(m_modules);
    }

    throw new UnsupportedFlavorException(flavor);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS.clone();
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.equals(TAG_FLAVOR);
  }
}
