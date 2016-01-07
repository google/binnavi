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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;



/**
 * Contains implementations of address space related actions.
 */
public final class CAddressSpaceFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CAddressSpaceFunctions() {
  }

  /**
   * Adds a module to an address space.
   * 
   * @param parent Parent window used to display dialogs.
   * @param addressSpace The address space where the module is added.
   * @param module The module that is added to the address space.
   */
  public static void addModule(final JFrame parent, final INaviAddressSpace addressSpace,
      final INaviModule module) {
    if (CMessageBox.showYesNoQuestion(parent, String.format(
        "Do you really want to add the module '%s' to the address space '%s'?", module
            .getConfiguration().getName(), addressSpace.getConfiguration().getName())) == JOptionPane.YES_OPTION) {
      new Thread() {
        @Override
        public void run() {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("Adding modules to address space", false, true);

          operation.getProgressPanel().setMaximum(1);
          operation.getProgressPanel().setText(
              "Adding module to address space" + ": '" + module.getConfiguration().getName() + "'");

          try {
            addressSpace.getContent().addModule(module);
          } catch (final CouldntSaveDataException exception) {
            CUtilityFunctions.logException(exception);

            final String message = "E00026: " + "Module could not be added to address space";
            final String description =
                CUtilityFunctions
                    .createDescription(
                        String
                            .format(
                                "The module '%s' could not be added to the address space '%s'. Try adding the module to the address space again. If the problem persists, disconnect from and reconnect to the database, restart com.google.security.zynamics.binnavi, or contact the BinNavi support.",
                                module.getConfiguration().getName(), addressSpace
                                    .getConfiguration().getName()),
                        new String[] {"Database connection problems."},
                        new String[] {"The address space remains unchanged."});

            NaviErrorDialog.show(parent, message, description, exception);
          }

          operation.stop();
        }
      }.start();
    }
  }

  /**
   * Creates a project view with the combined call graphs of all modules of an address space.
   * 
   * @param parent Parent window used for dialogs.
   * @param container Context in which the new view is opened.
   * @param project The project where the combined view is created.
   * @param addressSpace Provides the modules whose call graphs are combined.
   */
  public static void createCombinedCallgraph(final JFrame parent, final IViewContainer container,
      final INaviProject project, final INaviAddressSpace addressSpace) {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation =
            new CDefaultProgressOperation("Creating combined call graph" + ": "
                + addressSpace.getConfiguration().getName(), true, false);

        operation.getProgressPanel().setText("Creating combined call graph");

        final INaviView view = CCallgraphCombiner.createCombinedCallgraph(project, addressSpace);

        operation.stop();

        CShowViewFunctions.showViewInLastWindow(parent, container, new INaviView[] {view});
      }
    }.start();
  }

  /**
   * Loads one or more address spaces.
   * 
   * @param projectTree Project tree of the main window.
   * @param addressSpaces The address spaces to load.
   */
  public static void loadAddressSpaces(final JTree projectTree,
      final INaviAddressSpace[] addressSpaces) {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation =
            new CDefaultProgressOperation("Loading address spaces", false, true);

        operation.getProgressPanel().setMaximum(addressSpaces.length);

        for (final INaviAddressSpace addressSpace : addressSpaces) {
          operation.getProgressPanel().setText(
              "Loading address spaces" + ": '" + addressSpace.getConfiguration().getName() + "'");

          try {
            addressSpace.load();

            new SwingInvoker() {
              @Override
              protected void operation() {
                CNodeExpander.expandNode(projectTree, addressSpace);
              }
            }.invokeLater();
          } catch (final CouldntLoadDataException exception) {
            CUtilityFunctions.logException(exception);

            final String message = "E00109: " + "Address space could not be loaded";
            final String description =
                CUtilityFunctions
                    .createDescription(
                        String
                            .format(
                                "The address space '%s' could not be loaded. Try loading the address space again. If the problem persists, disconnect from and reconnect to the database, restart com.google.security.zynamics.binnavi, or contact the BinNavi support.",
                                addressSpace.getConfiguration().getName()),
                        new String[] {"Database connection problems."},
                        new String[] {"The address space was not loaded."});

            NaviErrorDialog.show(SwingUtilities.getWindowAncestor(projectTree), message,
                description, exception);
          } catch (final LoadCancelledException e) {
            // Do nothing
          } finally {
            operation.getProgressPanel().next();
          }
        }

        operation.stop();
      }
    }.start();
  }

  /**
   * Removes a module from an address space.
   * 
   * @param parent Parent window used to display dialogs.
   * @param addressSpace The address space the module belongs to.
   * @param modules The modules to be removed from the address space.
   */
  public static void removeModules(final JFrame parent, final INaviAddressSpace addressSpace,
      final INaviModule[] modules) {
    new Thread() {
      @Override
      public void run() {
        if (CMessageBox.showYesNoQuestion(parent, String.format(
            "Do you really want to remove the following modules from the address space?\n\n%s",
            CNameListGenerators.getNameList(modules))) == JOptionPane.YES_OPTION) {
          final CDefaultProgressOperation operation =
              new CDefaultProgressOperation("Removing modules from address space", false, true);

          operation.getProgressPanel().setMaximum(modules.length);

          for (final INaviModule module : modules) {
            operation.getProgressPanel().setText(
                "Removing modules from address space" + ": '" + module.getConfiguration().getName()
                    + "'");

            try {
              addressSpace.getContent().removeModule(module);
            } catch (final CouldntDeleteException exception) {
              CUtilityFunctions.logException(exception);

              final String message = "E00027: " + "Module could not be removed from address space";
              final String description =
                  CUtilityFunctions
                      .createDescription(
                          String
                              .format(
                                  "The module '%s' could not be removed from the address space '%s'. Try removing the module from the address space again. If the problem persists, disconnect from and reconnect to the database, restart com.google.security.zynamics.binnavi, or contact the BinNavi support.",
                                  module.getConfiguration().getName(), addressSpace
                                      .getConfiguration().getName()),
                          new String[] {"Database connection problems."},
                          new String[] {"The address space remains unchanged."});

              NaviErrorDialog.show(parent, message, description, exception);
            } catch (final CouldntSaveDataException exception) {
              CUtilityFunctions.logException(exception);
            }

            operation.getProgressPanel().next();
          }

          operation.stop();
        }
      }
    }.start();
  }
}
