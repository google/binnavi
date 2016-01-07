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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;

import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ViewSearcher.CViewSearcherDialog;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.awt.Window;



/**
 * Contains helper classes for searching through views.
 */
public final class CViewSearcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewSearcher() {
  }

  /**
   * Searches through the views of a view container.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Container to search through.
   * @param address The initial address to search for. This argument can be null.
   */
  public static void searchView(final Window parent, final IViewContainer container,
      final IAddress address) {
    final CViewSearcherDialog dlg = new CViewSearcherDialog(parent, container, address);

    dlg.setVisible(true);

    final INaviView result = dlg.getSelectionResult();

    if (result != null) {
      CViewOpener.showView(parent, container, result, CWindowManager.instance().getLastWindow());
    }
  }
}
