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

import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.InsertViewDialog.CViewSelectionDialog;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewLoader;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.GuiHelper;

/**
 * Contains functions to insert a view into another view.
 */
public final class CViewInserter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewInserter() {
  }

  /**
   * Loads a view, waits until the load operation is complete and returns whether loading the view
   * was successful or not.
   *
   * @param parent Parent window used to display dialogs.
   * @param viewContainer The container that provides the views that can be inserted.
   * @param view The view to load.
   *
   * @return True, if the view could be loaded. False, otherwise.
   */
  private static boolean loadView(
      final JFrame parent, final IViewContainer viewContainer, final INaviView view) {
    CViewLoader.load(parent, viewContainer, view);

    return view.isLoaded();
  }

  /**
   * Asks the user for a view and inserts that view into a graph.
   *
   * @param parent Parent window used to display dialogs.
   * @param graph The graph where the view is inserted.
   * @param viewContainer The container that provides the views that can be inserted.
   */
  public static void insertView(
      final JFrame parent, final ZyGraph graph, final IViewContainer viewContainer) {
    Preconditions.checkNotNull(parent, "IE00008: Parent argument can not be null");

    Preconditions.checkNotNull(graph, "IE00009: Graph argument can not be null");

    Preconditions.checkNotNull(viewContainer, "IE00010: View container argument can not be null");

    final CViewSelectionDialog dlg = new CViewSelectionDialog(parent, viewContainer);

    GuiHelper.centerChildToParent(parent, dlg, true);

    dlg.setVisible(true);

    final INaviView view = dlg.getView();

    if (view != null) {
      if (!view.isLoaded() && !loadView(parent, viewContainer, view)) {
        // View can not be loaded for some reason
        return;
      }

      com.google.security.zynamics.binnavi.disassembly.algorithms.CViewInserter.insertView(view, graph.getRawView());
      graph.updateViews();

      view.close();
    }
  }
}
