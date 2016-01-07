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
package com.google.security.zynamics.binnavi.Gui.Loaders;



import java.awt.Window;

import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader.CGraphOpener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CFunctionHelpers;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleContainer;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.FunctionType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Helper class that can be used to open views in graph windows.
 */
public final class CViewOpener {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewOpener() {
  }

  /**
   * Determines whether a function is imported and forwarded.
   * 
   * @param function The function to check.
   * 
   * @return True, if the function is imported and forwarded. False, otherwise.
   */
  private static boolean isForwardedImportedFunction(final INaviFunction function) {
    return (function != null) && CFunctionHelpers.isForwardableFunction(function)
        && (function.getForwardedFunctionAddress() != null);
  }

  /**
   * Determines whether a function is imported but not forwarded.
   * 
   * @param function The function to check.
   * 
   * @return True, if the function is imported but not forwarded. False, otherwise.
   */
  private static boolean isPureImportedFunction(final INaviFunction function) {
    return (function != null) && (function.getType() == FunctionType.IMPORT)
        && (function.getForwardedFunctionAddress() == null);
  }

  /**
   * Shows a forwarded function in a graph window.
   * 
   * @param parent Parent window that is used as the parent of all dialogs.
   * @param container View container that provides the context in which a view is opened.
   * @param view The view to show.
   * @param function The imported function to show.
   * @param window Graph window where the graph is shown. If this value is null, the graph is shown
   *        in a new window.
   */
  private static void showForwardedFunction(final Window parent, final IViewContainer container,
      final INaviView view, final INaviFunction function, final CGraphWindow window) {
    if (container instanceof CModuleContainer) {
      CMessageBox.showInformation(parent, "Please open forwarded views from inside a project.");

      return;
    }

    final IDatabase database = container.getDatabase();

    final int moduleId = function.getForwardedFunctionModuleId();

    final INaviModule forwardedModule = database.getContent().getModule(moduleId);

    if (forwardedModule == null) {
      final String message = "E00019: " + "Forwarded view can not be loaded (Unknown module)";
      final String description =
          CUtilityFunctions.createDescription(
              "BinNavi could not open the forwarded view because the module of "
                  + "the forwarding target is unknown.",
              new String[] {"Probably the result of a bug in BinNavi"},
              new String[] {"The view can not be opened. Try to update the "
                  + "forwarding target again. Restart BinNavi if the view "
                  + "can still not be opened. Contact the BinNavi support if necessary."});

      NaviErrorDialog.show(parent, message, description);
    } else if (forwardedModule.isLoaded()) {
      final IAddress address = function.getForwardedFunctionAddress();

      final INaviFunction forwardedFunction =
          forwardedModule.getContent().getFunctionContainer().getFunction(address);

      if (forwardedFunction == null) {
        final String message = "E00020: " + "Forwarded view can not be loaded (Unknown function)";
        final String description =
            CUtilityFunctions
                .createDescription(
                    "BinNavi could not open the forwarded view because the target function is unknown.",
                    new String[] {"Probably the result of a bug in BinNavi"},
                    new String[] {"The view can not be opened. Try to update the forwarding target "
                        + "again. Restart BinNavi if the view can still not be opened. Contact the "
                        + "BinNavi support if necessary."});

        NaviErrorDialog.show(parent, message, description);
      } else {
        final INaviView forwardedView =
            forwardedModule.getContent().getViewContainer().getView(forwardedFunction);

        if (forwardedView == null) {
          final String message = "E00107: " + "Forwarded view can not be loaded (Unknown view)";
          final String description =
              CUtilityFunctions.createDescription(
                  "BinNavi could not open the forwarded view because the target view is unknown.",
                  new String[] {"Probably the result of a bug in BinNavi"},
                  new String[] {"The view can not be opened. Try to update the forwarding target "
                      + "again. Restart BinNavi if the view can still not be opened. Contact the "
                      + "BinNavi support if necessary."});

          NaviErrorDialog.show(parent, message, description);
        } else {
          CGraphOpener.showGraph(container, forwardedView, window, parent);
        }
      }
    } else {
      if (CMessageBox.showYesNoQuestion(parent,
          "The view can not be opened because it is forwarded to an unloaded module.\n\n"
              + "Do you want to load the forwarded module now?") == JOptionPane.YES_OPTION) {
        CModuleLoader.loadModule(parent, forwardedModule);

        if (forwardedModule.isLoaded()) {
          // Just call this function recursively now that the module is loaded.

          showForwardedFunction(parent, container, view, function, window);
        }
      }
    }
  }

  /**
   * Shows a view in a graph window.
   * 
   * @param parent Parent window that is used as the parent of all dialogs.
   * @param viewContainer View container that provides the context in which a view is opened.
   * @param view The view to show.
   * @param window Graph window where the graph is shown. If this value is null, the graph is shown
   *        in a new window.
   */
  public static void showView(final Window parent, final IViewContainer viewContainer,
      final INaviView view, final CGraphWindow window) {
    Preconditions.checkNotNull(parent, "IE00015: Parent argument can not be null");
    Preconditions.checkNotNull(viewContainer, "IE00016: View container argument can not be null");
    Preconditions.checkNotNull(view, "IE00018: View argument can not be null");
    Preconditions.checkState(viewContainer.isLoaded(), "IE00017: View container must be loaded");
    Preconditions.checkState(viewContainer.getViews().contains(view),
        "IE00019: View is not part of the given view container");

    final INaviFunction function = viewContainer.getFunction(view);
    if (isPureImportedFunction(function)) {
      CMessageBox.showInformation(parent,
          "Imported functions are not part of a module and can not be opened.\n"
              + "Note that it is possible to open imported functions once they "
              + "are forwarded to real functions in other modules.");
      return;
    } else if (isForwardedImportedFunction(function)) {
      // Imported functions must be treated specially, because a forwarded
      // functions could be opened instead of the view.
      showForwardedFunction(parent, viewContainer, view, function, window);
    } else {
      CGraphOpener.showGraph(viewContainer, view, window, parent);
    }
  }

  // TODO (timkornau) merge with the other code above and make pretty before commit.
  // !!!
  public static void showViewAndCallBack(final Window parent, final IViewContainer viewContainer,
      final INaviView view, final CGraphWindow window, final FutureCallback<Boolean> callBack) {

    final INaviFunction function = viewContainer.getFunction(view);
    if (isPureImportedFunction(function)) {
      CMessageBox.showInformation(parent,
          "Imported functions are not part of a module and can not be opened.\n"
              + "Note that it is possible to open imported functions once they "
              + "are forwarded to real functions in other modules.");
      return;
    } else if (isForwardedImportedFunction(function)) {
      // Imported functions must be treated specially, because a forwarded
      // functions could be opened instead of the view.
      showForwardedFunction(parent, viewContainer, view, function, window);
    } else {
      CGraphOpener.showGraphAndPerformCallBack(viewContainer, view, window, parent, callBack);
    }

  }
}
