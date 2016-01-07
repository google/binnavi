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
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Transferable class that is used to drag & drop a list of views from tables.
 */
public final class CViewTransferable implements Transferable {
  /**
   * Data flavors supported by this drag & drop handler.
   */
  private static final DataFlavor[] SUPPORTED_FLAVORS = new DataFlavor[1];

  /**
   * Data flavor for drag & dropping views.
   */
  public static final DataFlavor VIEW_FLAVOR;

  /**
   * The views to drag & drop.
   */
  private final List<INaviView> m_views;

  static {
    VIEW_FLAVOR = new DataFlavor(CView.class, "BinNavi Views");
    SUPPORTED_FLAVORS[0] = VIEW_FLAVOR;
  }

  /**
   * Creates a new transferable object that can be used to drag & drop a list of views.
   * 
   * @param views The views to drag & drop.
   */
  public CViewTransferable(final List<INaviView> views) {
    Preconditions.checkNotNull(views, "IE01937: Views argument can't be null");

    m_views = new ArrayList<INaviView>(views);
  }

  @Override
  public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException {
    if (flavor.equals(VIEW_FLAVOR)) {
      return new ArrayList<INaviView>(m_views);
    }

    throw new UnsupportedFlavorException(flavor);
  }

  @Override
  public DataFlavor[] getTransferDataFlavors() {
    return SUPPORTED_FLAVORS.clone();
  }

  /**
   * Returns the views that are drag & dropped by this transferable object.
   * 
   * @return A list of views.
   */
  public List<INaviView> getViews() {
    return new ArrayList<INaviView>(m_views);
  }

  @Override
  public boolean isDataFlavorSupported(final DataFlavor flavor) {
    return flavor.equals(VIEW_FLAVOR);
  }
}
