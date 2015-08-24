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
package com.google.security.zynamics.binnavi.Gui.Loaders;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

import javax.swing.JFrame;

/**
 * This class is used to load views without opening a corresponding graph window. This is useful for
 * all kinds of functions that require the content of a view but do not require a visible graph
 * window.
 */
public final class CViewLoader {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewLoader() {
  }

  /**
   * Loads a view and shows an error message if the view can not be loaded.
   *
   * @param parent Parent window used for error dialogs.
   * @param container Context in which the view is loaded.
   * @param view View to be loaded.
   */
  public static void load(final JFrame parent, final IViewContainer container, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE00011: Parent argument can not be null");
    Preconditions.checkNotNull(container, "IE00012: Container argument can not be null");
    Preconditions.checkNotNull(view, "IE00013: View argument can not be null");

    if (view.isLoaded()) {
      return;
    }

    final ViewLoaderThread thread = new ViewLoaderThread(view);

    CProgressDialog.showEndless(parent, String.format("Loading view '%s'", view.getName()), thread);

    final Exception exception = thread.getException();

    if (exception != null) {
      if (exception instanceof CouldntLoadDataException) {
        CUtilityFunctions.logException(exception);

        final String message = "E00050: Could not load view";
        final String description =
            CUtilityFunctions.createDescription(
                String.format("The view '%s' could not be loaded.", view.getName()), new String[] {
                    "There were problems with the database connection.",
                    "Malformed data was found in the database."},
                new String[] {"The view was not loaded."});

        NaviErrorDialog.show(parent, message, description, exception);
      } else if (exception instanceof CPartialLoadException) {
        CUtilityFunctions.logException(exception);

        final String moduleName =
            ((CPartialLoadException) exception).getModule().getConfiguration().getName();

        final String message = "E00051: Could not load view";
        final String description =
            CUtilityFunctions.createDescription(String.format(
                "The view '%s' could not be loaded because it "
                    + "depends on the unloaded module '%s'.", view.getName(), moduleName),
                new String[] {String.format("Module '%s' is not loaded.", moduleName)},
                new String[] {String.format(
                    "The view can not be loaded before the module '%s' is loaded.", moduleName)});

        NaviErrorDialog.show(parent, message, description, exception);
      }
    }
  }

  /**
   * Thread used to display a progress dialog while the view is being loaded.
   */
  private static class ViewLoaderThread extends CEndlessHelperThread {
    /**
     * The view to load.
     */
    private final INaviView m_view;

    /**
     * Creates a new view loader thread.
     *
     * @param view The view to load.
     */
    private ViewLoaderThread(final INaviView view) {
      m_view = Preconditions.checkNotNull(view, "IE00014: View argument can not be null");
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      m_view.load();
    }
  }
}
