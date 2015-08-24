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



import java.awt.Window;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Contains helper functions for working with views.
 */
public final class CViewFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewFunctions() {
  }

  /**
   * Removes a given tag from a list of views.
   * 
   * @param views The views from which the tag is removed.
   * @param tag The tag to remove.
   */
  public static void removeTags(final INaviView[] views, final CTag tag) {
    for (final INaviView view : views) {
      try {
        view.getConfiguration().untagView(tag);
      } catch (final CouldntSaveDataException exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Renames a view back to its original name.
   * 
   * @param parent Parent window used for dialogs.
   * @param view The view to rename.
   * @param originalName The original name of the view.
   */
  public static void renameBack(final Window parent, final INaviView view, final String originalName) {
    try {
      view.getConfiguration().setName(originalName);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00063: " + "View name could not be changed";
      final String innerDescription =
          CUtilityFunctions.createDescription(
              String.format("The view name of view '%s' could not be changed.", view.getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The view was not updated and the new view name is lost."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Stars views.
   * 
   * @param parent Parent window used for dialogs.
   * @param views The views to star.
   */
  public static void star(final Window parent, final INaviView[] views) {
    for (final INaviView view : views) {
      try {
        view.getConfiguration().setStared(true);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00090: " + "View could not be stared";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view '%s' could not be stared.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Tags a given view.
   * 
   * @param parent Parent window used for dialogs.
   * @param view View to be tagged.
   * @param tag Tag to tag the view with.
   */
  public static void tagView(final JFrame parent, final INaviView view, final CTag tag) {
    try {
      view.getConfiguration().tagView(tag);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00149: " + "Could not tag view";
      final String innerDescription =
          CUtilityFunctions.createDescription(
              String.format("The view '%s' could not be tagged.", view.getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The view remains untagged."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
    }
  }

  /**
   * Toggles the star state of views.
   * 
   * @param parent Parent window used for dialogs.
   * @param views The views whose star state is toggled.
   */
  public static void toggleStars(final Window parent, final INaviView[] views) {
    for (final INaviView view : views) {
      try {
        view.getConfiguration().setStared(!view.getConfiguration().isStared());
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00142: " + "View star could not be toggled";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The star state of view '%s' could not be toggled.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Unstars views.
   * 
   * @param parent Parent window used for dialogs.
   * @param views The views to unstar.
   */
  public static void unstar(final Window parent, final INaviView[] views) {
    for (final INaviView view : views) {
      try {
        view.getConfiguration().setStared(false);
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00176: " + "View could not be unstared";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view '%s' could not be unstared.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The view star was not updated."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }
}
