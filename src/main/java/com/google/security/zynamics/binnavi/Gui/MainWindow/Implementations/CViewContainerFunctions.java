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



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Window;

import javax.swing.JOptionPane;



/**
 * Contains helper functions for view container actions.
 */
public final class CViewContainerFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewContainerFunctions() {
  }

  /**
   * Deletes one or more views from a view container.
   * 
   * @param parent Parent window used for dialogs.
   * @param viewContainer Container from which the views are deleted.
   * @param views The views to delete.
   */
  public static void deleteViews(final Window parent, final IViewContainer viewContainer,
      final INaviView[] views) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following views?\n\n%s",
        CNameListGenerators.getNameList(views))) == JOptionPane.YES_OPTION) {
      for (final INaviView view : views) {
        try {
          viewContainer.deleteView(view);
        } catch (final CouldntDeleteException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00148: " + "Could not delete view";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  String.format("The view '%s' could not be deleted.", view.getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The view was not deleted and can still be used."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        }
      }
    }
  }
}
