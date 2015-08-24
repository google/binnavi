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
package com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations;



import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;


/**
 * Contains code for staring and unstaring modules.
 */
public final class CModuleStaringFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CModuleStaringFunctions() {
  }

  /**
   * Stars modules.
   * 
   * @param parent Parent window used for dialogs.
   * @param modules The modules to star.
   */
  public static void star(final JFrame parent, final INaviModule[] modules) {
    for (final INaviModule module : modules) {
      try {
        module.getConfiguration().setStared(true);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00062: " + "Module could not be stared";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The module '%s' could not be stared.", module.getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The module star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Toggles the star state of modules.
   * 
   * @param parent Parent window used for dialogs.
   * @param modules The modules whose star state is toggled.
   */
  public static void toggleStars(final JFrame parent, final INaviModule[] modules) {
    for (final INaviModule module : modules) {
      try {
        module.getConfiguration().setStared(!module.getConfiguration().isStared());
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E000063: " + "Module star could not be toggled";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The star state of module '%s' could not be toggled.", module.getConfiguration()
                    .getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The module star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Unstars modules.
   * 
   * @param parent Parent window used for dialogs.
   * @param modules The modules to unstar.
   */
  public static void unstar(final JFrame parent, final INaviModule[] modules) {
    for (final INaviModule module : modules) {
      try {
        module.getConfiguration().setStared(false);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00081: " + "Module could not be unstared";
        final String innerDescription =
            CUtilityFunctions.createDescription(String.format(
                "The module '%s' could not be unstared.", module.getConfiguration().getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The module star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }
}
