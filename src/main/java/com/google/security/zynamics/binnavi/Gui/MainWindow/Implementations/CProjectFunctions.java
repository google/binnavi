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
import javax.swing.JOptionPane;
import javax.swing.JTree;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Loaders.CProjectLoader;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.INodeSelectionUpdater;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Updaters.ITreeUpdater;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * Contains helper functions for working with projects.
 */
public final class CProjectFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CProjectFunctions() {
  }

  /**
   * Creates a new default address space in a project.
   * 
   * @param parent Parent window used for dialogs.
   * @param newProject Project where the address space is created.
   * 
   * @throws CouldntSaveDataException Thrown if the address space could not be created.
   */
  private static void createDefaultAddressSpace(final JFrame parent, final INaviProject newProject)
      throws CouldntSaveDataException {
    final CAddressSpace addressSpace =
        newProject.getContent().createAddressSpace("Default address space");

    try {
      addressSpace.load();
    } catch (final CouldntLoadDataException exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00139: " + "Default address space could not be loaded";
      final String innerDescription =
          CUtilityFunctions
              .createDescription(
                  "The default address space of the new project could not be loaded.",
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The new project was created but its default address space could not be loaded."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    } catch (final LoadCancelledException e) {
      // Do nothing
    }
  }

  /**
   * Adds a new address space with a default name to a given project.
   * 
   * @param parent Parent window used for dialogs.
   * @param project The project where the new address space is added.
   * @param updater Updates the project tree after the execution is complete.
   */
  public static void addAddressSpace(final Window parent, final INaviProject project,
      final INodeSelectionUpdater updater) {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation = new CDefaultProgressOperation("", false, true);
        operation.getProgressPanel().setMaximum(2);

        try {
          operation.getProgressPanel().setText("Creating new address space");
          operation.getProgressPanel().next();

          final CAddressSpace addressSpace =
              project.getContent().createAddressSpace("New Address Space");

          operation.getProgressPanel().setText("Loading new address space");

          addressSpace.load();

          operation.getProgressPanel().next();
          operation.stop();

          updater.setObject(addressSpace);
          updater.update();
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00136: " + "Could not add address space";
          final String innerDescription =
              CUtilityFunctions.createDescription(String.format(
                  "It was not possible to add a new address space to the project '%s'.", project
                      .getConfiguration().getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The address space was not created."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        } catch (final CouldntLoadDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00137: " + "Could not load the new address space";
          final String innerDescription =
              CUtilityFunctions.createDescription(String.format(
                  "The new address space in project '%s' was created but it could not be loaded.",
                  project.getConfiguration().getName()),
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The address space was created but not loaded."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        } catch (final LoadCancelledException e) {
          // Do nothing
        }
      }
    }.start();
  }

  /**
   * Copies a view to a project.
   * 
   * @param parent Parent window used for dialogs.
   * @param project The project to where the view is copied.
   * @param view The view to copy to the project.
   */
  public static void copyView(final JFrame parent, final INaviProject project, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE01835: Parent argument can not be null");
    Preconditions.checkNotNull(project, "IE01836: Project argument can not be null");
    Preconditions.checkNotNull(view, "IE01837: View argument can not be null");

    if (!view.isLoaded()) {
      try {
        view.load();
      } catch (final CouldntLoadDataException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage =
            "E00138: " + "View could not be copied because it could not be loaded";
        final String innerDescription =
            CUtilityFunctions.createDescription(
                String.format("The view '%s' could not be copied.", view.getName()),
                new String[] {"There was a problem with the database connection."},
                new String[] {"The new view was not created."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);

        return;
      } catch (final CPartialLoadException e) {
        // TODO: This
        CUtilityFunctions.logException(e);

        return;
      } catch (final LoadCancelledException e) {
        return;
      }
    }

    project.getContent().createView(view, view.getName(), view.getConfiguration().getDescription());

    view.close();

    try {
      view.save();
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);

      final String innerMessage = "E00206: " + "Could not save view";
      final String innerDescription =
          CUtilityFunctions.createDescription(
              String.format("The function '%s' could not be saved.", view.getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The graph remains unsaved."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
    }
  }

  /**
   * Creates a new project.
   * 
   * @param parent Parent window used for dialogs.
   * @param database Database where the project is created.
   * @param updater Responsible for updating the project tree after the project was created.
   */
  public static void createProject(final JFrame parent, final IDatabase database,
      final INodeSelectionUpdater updater) {
    new Thread() {
      @Override
      public void run() {
        try {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("", false, true);
          operation.getProgressPanel().setMaximum(3);

          operation.getProgressPanel().setText("Creating new project");

          final INaviProject newProject = database.getContent().addProject("New Project");

          operation.getProgressPanel().next();

          try {
            newProject.load();
          } catch (final LoadCancelledException e) {
            // Do nothing
          }

          operation.getProgressPanel().next();

          createDefaultAddressSpace(parent, newProject);

          updater.setObject(newProject);
          updater.update();

          operation.getProgressPanel().next();
          operation.stop();
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00140: " + "New project could not be created";
          final String innerDescription =
              CUtilityFunctions.createDescription(
                  "It was not possible to create a new project in the selected database.",
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"No new project was created in the selected database."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        } catch (final CouldntLoadDataException exception) {
          CUtilityFunctions.logException(exception);

          final String innerMessage = "E00141: " + "New project could not be loaded";
          final String innerDescription =
              CUtilityFunctions.createDescription("The new project could not be loaded.",
                  new String[] {"There was a problem with the database connection."},
                  new String[] {"The new project was created but it could not be loaded."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
        }
      }
    }.start();
  }

  /**
   * Opens one or more projects.
   * 
   * @param projectTree Project tree of the main window.
   * @param projects The projects to load.
   */
  public static void openProjects(final JTree projectTree, final INaviProject[] projects) {
    for (final INaviProject project : projects) {
      CProjectLoader.loadProject(projectTree, project);
    }
  }

  /**
   * Removes an address spaces from a project.
   * 
   * @param parent Parent window used for dialogs.
   * @param project The project the address space belongs to.
   * @param addressSpaces The address spaces to be removed from the project.
   * @param updater Updates the project tree after the address space was removed.
   */
  public static void removeAddressSpace(final JFrame parent, final INaviProject project,
      final INaviAddressSpace[] addressSpaces, final ITreeUpdater updater) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to delete the following address spaces from the project?\n\n%s",
        CNameListGenerators.getNameList(addressSpaces))) == JOptionPane.YES_OPTION) {
      for (final INaviAddressSpace addressSpace : addressSpaces) {
        new Thread() {
          @Override
          public void run() {
            final CDefaultProgressOperation operation =
                new CDefaultProgressOperation("", false, true);
            operation.getProgressPanel().setMaximum(1);

            operation.getProgressPanel().setText(
                "Removing address space" + ": " + addressSpace.getConfiguration().getName());
            operation.getProgressPanel().next();

            if (addressSpace.isLoaded()) {
              addressSpace.close();
            }

            if (addressSpace.isLoaded()) {
              final String innerMessage = "E00123: " + "Address space could not be deleted";
              final String innerDescription =
                  CUtilityFunctions.createDescription(String.format(
                      "BinNavi could not delete the address space '%s'.", addressSpace
                          .getConfiguration().getName()),
                      new String[] {"BinNavi or one of the active plugins vetoed the deletion "
                          + "operation."},
                      new String[] {"The address space can not be deleted until the delete "
                          + "operation is not vetoed anymore."});

              NaviErrorDialog.show(parent, innerMessage, innerDescription);
            } else {
              try {
                project.getContent().removeAddressSpace(addressSpace);

                updater.update();
              } catch (final CouldntDeleteException exception) {
                CUtilityFunctions.logException(exception);

                final String innerMessage = "E00143: " + "Address space could not be deleted";
                final String innerDescription =
                    CUtilityFunctions.createDescription(
                        "The selected address space could not be deleted.",
                        new String[] {"There was a problem with the database connection."},
                        new String[] {"The address space was not deleted."});

                NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
              }
            }
            operation.stop();
          }
        }.start();
      }
    }
  }
}
