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

import com.google.common.util.concurrent.FutureCallback;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import java.awt.Window;

/**
 * Contains code for showing views in windows.
 */
public final class CShowViewFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CShowViewFunctions() {
  }

  /**
   * Opens views in the last opened window.
   *
   * @param parent Parent window used for dialogs.
   * @param container Context in which the views are opened.
   * @param views The views to open.
   */
  public static void showViewInLastWindow(final Window parent, final IViewContainer container,
      final INaviView[] views) {
    showViews(parent, container, views, CWindowManager.instance().getLastWindow());
  }

  /**
   * Opens views in a new window.
   *
   * @param parent Parent window used for dialogs.
   * @param container Context in which the views are opened.
   * @param views The views to open.
   */
  public static void showViewInNewWindow(final Window parent, final IViewContainer container,
      final INaviView[] views) {
    showViews(parent, container, views, null);
  }

  public static void showViewsAndPerformCallBack(final Window parent,
      final IViewContainer container, final INaviView[] views, final CGraphWindow window,
      final FutureCallback<Boolean> callBack) {
    for (final INaviView view : views) {
      CViewOpener.showViewAndCallBack(parent, container, view, window, callBack);
    }
  }

  /**
   * Opens views in a given window.
   *
   * @param parent Parent window used for dialogs.
   * @param container Context in which the views are opened.
   * @param views The views to open.
   * @param window The window where the views are opened.
   */
  public static void showViews(final Window parent, final IViewContainer container,
      final INaviView[] views, final CGraphWindow window) {
    for (final INaviView view : views) {
      CViewOpener.showView(parent, container, view, window);
    }
  }
}
