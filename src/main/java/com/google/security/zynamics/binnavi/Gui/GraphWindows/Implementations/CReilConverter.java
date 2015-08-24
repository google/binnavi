/*
Copyright 2015 Google Inc. All Rights Reserved.

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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.REIL.CReilViewCreator;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;



/**
 * Contains helper classes for showing the REIL graph of a view.
 */
public final class CReilConverter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CReilConverter() {
  }

  /**
   * Turns a view into REIL code and shows a graph that contains that REIL code.
   * 
   * @param parent Window in which the REIL graph is shown.
   * @param viewContainer Context in which the REIL view is generated.
   * @param module Module where the view is created.
   * @param view View to be converted to REIL code.
   */
  public static void showReilGraph(final CGraphWindow parent, final IViewContainer viewContainer,
      final INaviModule module, final INaviView view) {
    final ReilCreationThread thread = new ReilCreationThread(module, view);

    CProgressDialog.showEndless(parent, "Creating REIL graph" + " ...", thread);

    final Exception exception = thread.getException();

    if (exception != null) {
      if (exception instanceof CouldntSaveDataException) {
        CUtilityFunctions.logException(exception);

        // This can never happen because name changes in unsaved views do not throw
      } else {
        CUtilityFunctions.logException(exception);

        final String innerMessage = "E00111: Could not translate view to REIL";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("BinNavi could not create the REIL code of view '%s'.",
                    view.getName()),
                new String[] {"An error occurred in the REIL translator code."},
                new String[] {"This is an internal error which you can not fix yourself. "
                    + "Please report the bug to the zynamics support team."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription);
      }

      return;
    }

    CViewOpener.showView(parent, viewContainer, thread.getReilView(), parent);
  }

  /**
   * Thread to create the REIL view in the background while a progress dialog is shown.
   */
  private static class ReilCreationThread extends CEndlessHelperThread {
    /**
     * Module where the view is created.
     */
    private final INaviModule m_module;

    /**
     * View to be converted to REIL code.
     */
    private final INaviView m_view;

    /**
     * The created REIL view.
     */
    private INaviView m_reilView;

    /**
     * Creates a new thread object.
     * 
     * @param module Module where the view is created.
     * @param view View to be converted to REIL code.
     */
    public ReilCreationThread(final INaviModule module, final INaviView view) {
      m_module = module;
      m_view = view;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      m_reilView = CReilViewCreator.create(m_module, m_view);

      m_reilView.getConfiguration().setName(String.format("REIL View of '%s'", m_view.getName()));
    }

    /**
     * Returns the created REIL view.
     * 
     * @return The created REIL view.
     */
    public INaviView getReilView() {
      return m_reilView;
    }
  }
}
