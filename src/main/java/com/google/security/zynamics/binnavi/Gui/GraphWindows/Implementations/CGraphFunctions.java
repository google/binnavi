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


import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CDataflowViewCreator;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.reil.translators.InternalTranslationException;

/**
 * Contains helper functions for working with graphs.
 */
public final class CGraphFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphFunctions() {
  }

  /**
   * Creates a new view that shows the data flow graph of a view.
   * 
   * @param parent Window where the new view is shown.
   * @param container Container where the new view is created.
   * @param view The view whose data flow graph is created.
   */
  public static void showDataflowGraph(final CGraphWindow parent, final IViewContainer container,
      final INaviView view) {
    try {
      final INaviView dataflowView = CDataflowViewCreator.create(container, view);

      CViewOpener.showView(parent, container, dataflowView, parent);
    } catch (final InternalTranslationException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00110: " + "Could not create dataflow graph";
      final String innerDescription =
          CUtilityFunctions.createDescription(
              String.format("BinNavi could not create the data flow graph of view '%s'.",
                  view.getName()), new String[] {"An error occurred in the REIL translator code."},
              new String[] {"This is an internal error which you can not fix yourself. "
                  + "Please report the bug to the zynamics support team."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription);
    }
  }
}
