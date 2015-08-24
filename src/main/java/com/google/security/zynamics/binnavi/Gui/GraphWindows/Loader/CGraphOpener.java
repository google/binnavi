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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader;

import java.awt.Window;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

/**
 * Contains helper functions for showing a graph in a window.
 */
public final class CGraphOpener {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphOpener() {
  }

  /**
   * Shows a graph in a window. (client code which wants to open a view should take a look at the
   * class CViewOpener).
   * 
   * @param container
   *          The context in which the view is opened.
   * @param view
   *          The view that is turned into a graph.
   * @param window
   *          Graph window where the graph is shown. If this value is null, the graph is shown
   *          in a new window.
   * @param parent
   *          Parent window that is used as the parent of all dialogs.
   */
  public static void showGraph(final IViewContainer container, final INaviView view,
      final CGraphWindow window, final Window parent) {

    CWindowManager.instance().bringViewToFront(view);

    new Thread() {
      @Override
      public void run() {
        final CViewLoader viewLoader = new CViewLoader(window, container, view);
        final CViewLoadOperation operation = new CViewLoadOperation(view);
        try {
          if (!CWindowManager.instance().isOpen(view)) {
            viewLoader.load();
          }
        } catch (final CPartialLoadException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = exception.getMessage();
          final String innerDescription = CUtilityFunctions.createDescription(
              String.format("The view '%s' could not be opened because not all required "
                  + "modules are loaded.", view.getName()), new String[] {},
              new String[] { "You have to load all necessary modules before opening "
                  + "this view." });

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        } catch (final Exception exception) {
          final String innerMessage = "E00122: " + "View could not be opened";
          final String innerDescription = CUtilityFunctions.createDescription(
              String.format("The view '%s' could not be opened.", view.getName()),
              new String[] { "There was a problem with the database connection." },
              new String[] { "The view can not be opened because not all necessary data "
                  + "was loaded from the database." });

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        } finally {
          operation.stop();
        }
      }
    }.start();
  }

  private static Callable<Boolean> generateViewLoader(final INaviView view,
      final IViewContainer container, final CGraphWindow window, final Window parent) {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        final CViewLoader viewLoader = new CViewLoader(window, container, view);
        final CViewLoadOperation operation = new CViewLoadOperation(view);
        try {
          if (!view.isLoaded()) {
            viewLoader.load();
          }
          return true;
        } catch (final CPartialLoadException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = exception.getMessage();
          final String innerDescription = CUtilityFunctions.createDescription(
              String.format("The view '%s' could not be opened because not all required "
                  + "modules are loaded.", view.getName()), new String[] {},
              new String[] { "You have to load all necessary modules before opening "
                  + "this view." });

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
          return false;
        } catch (final Exception exception) {
          final String innerMessage = "E00122: " + "View could not be opened";
          final String innerDescription = CUtilityFunctions.createDescription(
              String.format("The view '%s' could not be opened.", view.getName()),
              new String[] { "There was a problem with the database connection." },
              new String[] { "The view can not be opened because not all necessary data "
                  + "was loaded from the database." });

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
          return false;
        } finally {
          operation.stop();
        }
      }
    };
  }

  public static void showGraphAndPerformCallBack(final IViewContainer container,
      final INaviView view, final CGraphWindow window, final Window parent,
      final FutureCallback<Boolean> callBack) {

    CWindowManager.instance().bringViewToFront(view);

    final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors
        .newFixedThreadPool(10));

    final ListenableFuture<Boolean> loader = service.submit(generateViewLoader(view, container,
        window, parent));

    Futures.addCallback(loader, callBack);
  }

  /**
   * Process operation for loading views.
   */
  private static final class CViewLoadOperation implements IProgressOperation {
    /**
     * The view to load.
     */
    private final INaviView m_view;

    /**
     * The progress panel of the operation.
     */
    private final CProgressPanel m_progressBar;

    /**
     * Creates a new operation object.
     * 
     * @param view
     *          The view to load.
     */
    public CViewLoadOperation(final INaviView view) {
      m_view = view;

      m_progressBar = new CProgressPanel(String.format("Loading view '%s'", view.getName()), true,
          false);

      m_progressBar.start();

      CGlobalProgressManager.instance().add(this);
    }

    @Override
    public String getDescription() {
      return String.format("Loading view '%s'", m_view.getName());
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressBar;
    }

    /**
     * Stops the operation.
     */
    public void stop() {
      m_progressBar.stop();

      CGlobalProgressManager.instance().remove(this);
    }
  }
}
