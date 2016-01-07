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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

/**
 * Contains functions for opening functions in a new graph window.
 */
public final class CGraphOpener {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphOpener() {
  }

  /**
   * Opens a function in a new graph window.
   *
   * @param parent The window where the new graph is shown.
   * @param container The container the new graph belongs to.
   * @param function The function to be shown.
   */
  public static void showFunction(
      final CGraphWindow parent, final IViewContainer container, final INaviFunction function) {
    final INaviModule module = function.getModule();

    final INaviView view = module.getContent().getViewContainer().getView(function);

    CViewOpener.showView(parent, container, view, parent);
  }
}
