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
package com.google.security.zynamics.binnavi.Gui.ResolveFunctions;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CFunctionHelpers;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.zylib.gui.CMessageBox;

public class CResolveAllFunctionDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8199639355220737061L;

  /**
   * The database where the resolving takes place.
   */
  private final IDatabase m_database;

  /**
   * The list of modules where function resolving will be performed.
   */
  private final List<INaviModule> m_targetModules;

  /**
   * The list of modules where to where functions can be resolved
   */
  private final List<INaviModule> m_sourceModules;

  /**
   * Creates a new function resolving dialog for the complete database.
   *
   * @param owner The parent window of the dialog.
   * @param database The database where the resolving takes place.
   */
  public CResolveAllFunctionDialog(final Window owner, final IDatabase database) {
    m_database = Preconditions.checkNotNull(database, "IE02352: Database argument can't be null");
    Preconditions.checkArgument(
        database.isLoaded(), "Error: Database must be loaded before functions can be redirected");
    m_targetModules = loadModules(m_database.getContent().getModules());
    m_sourceModules = loadModules(m_database.getContent().getModules());

    final String message = String.format(
        "Do you really want to resolve all functions in the database '%s'?",
        database.getConfiguration().getName());

    resultDialog(questionDialog(owner, message));
  }

  /**
   * Creates a new function resolving dialog for an address space.
   *
   * @param owner The parent window of the dialog.
   * @param database The database where the resolving takes place.
   * @param addressSpace The address space containing the modules where the function forwarding will
   *        take place.
   */
  public CResolveAllFunctionDialog(
      final Window owner, final IDatabase database, final INaviAddressSpace addressSpace) {
    Preconditions.checkNotNull(addressSpace, "IE02353: addressSpace argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE02354: Database argument can't be null");
    Preconditions.checkArgument(
        database.isLoaded(), "Error: Database must be loaded before functions can be redirected");
    m_targetModules = loadModules(addressSpace.getContent().getModules());
    m_sourceModules = loadModules(addressSpace.getContent().getModules());

    final String message = String.format(
        "Do you really want to resolve all functions in the address space '%s'?",
        addressSpace.getConfiguration().getName());

    resultDialog(questionDialog(owner, message));
  }

  /**
   * Creates a new function resolving dialog for a single module.
   *
   * @param owner The parent window of the dialog.
   * @param database The dialog that contains the target modules.
   * @param module The module where the functions are resolved.
   */
  public CResolveAllFunctionDialog(
      final Window owner, final IDatabase database, final INaviModule module) {
    Preconditions.checkNotNull(module, "IE02355: module argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE02062: Database argument can't be null");
    Preconditions.checkArgument(
        database.isLoaded(), "IE02063: Database must be loaded before functions can be redirected");

    m_targetModules = loadModules(Lists.newArrayList(module));
    m_sourceModules = loadModules(database.getContent().getModules());

    final String message = String.format(
        "Do you really want to resolve all functions in the module '%s'?",
        module.getConfiguration().getName());

    resultDialog(questionDialog(owner, message));
  }

  /**
   * Creates a new function resolving dialog for a project.
   *
   * @param owner The parent window of the dialog.
   * @param database The database where the resolving takes place.
   * @param project The projects containing the modules where the function forwarding will take
   *        place.
   */
  public CResolveAllFunctionDialog(
      final Window owner, final IDatabase database, final INaviProject project) {
    Preconditions.checkNotNull(project, "IE02356: project argument can not be null");
    m_database = Preconditions.checkNotNull(database, "IE02357: Database argument can't be null");
    Preconditions.checkArgument(
        database.isLoaded(), "Error: Database must be loaded before functions can be redirected");

    final String message = String.format(
        "Do you really want to resolve all functions in the project '%s'?",
        project.getConfiguration().getName());

    m_targetModules = new ArrayList<INaviModule>();
    m_sourceModules = new ArrayList<INaviModule>();

    for (final INaviAddressSpace addressSpace : project.getContent().getAddressSpaces()) {
      m_targetModules.addAll(loadModules(addressSpace.getContent().getModules()));
      m_sourceModules.addAll(loadModules(addressSpace.getContent().getModules()));
    }

    resultDialog(questionDialog(owner, message));

  }

  private static void exceptionDialog(final INaviModule currentModule, final JDialog dialog,
      final CouldntLoadDataException exception) {
    CUtilityFunctions.logException(exception);

    final String message = "Error: " + "Could not load function forwarding";
    final String description = CUtilityFunctions.createDescription(
        String.format("Could not load '%s'", currentModule.getConfiguration().getName()),
        new String[] {"The database connection was dropped while saving."}, new String[] {
            "The changes in function forwarding were not saved. Try saving function forwarding again. If necessary, close the connection to the database and reconnect."});

    NaviErrorDialog.show(dialog, message, description, exception);
  }

  private List<INaviModule> loadModules(final List<INaviModule> modules) {
    final ArrayList<INaviModule> loadedModules = new ArrayList<INaviModule>();

    for (final INaviModule currentModule : modules) {
      if (!currentModule.isLoaded()) {
        try {
          currentModule.load();
        } catch (final CouldntLoadDataException exception) {
          exceptionDialog(currentModule, this, exception);
        } catch (final LoadCancelledException exception) {
          // ignore
        }
      }
      loadedModules.add(currentModule);
    }
    return loadedModules;
  }

  private int questionDialog(final Window owner, final String message) {
    if (JOptionPane.YES_OPTION == CMessageBox.showYesNoCancelQuestion(owner, message)) {
      return resolveAllFunctions();
    }
    return 0;
  }

  /**
   * Takes the information from the GUI and forwards functions.
   */
  private int resolveAllFunctions() {
    int counter = 0;

    for (final INaviModule currentModule : m_targetModules) {
      for (final INaviFunction currentFunction :
          currentModule.getContent().getFunctionContainer().getFunctions()) {
        final String originalName = currentFunction.getOriginalModulename();

        if (!originalName.equalsIgnoreCase(currentModule.getConfiguration().getName())
            && !originalName.equalsIgnoreCase("")) {
          for (final INaviModule targetModule : m_sourceModules) {
            final String targetModuleName = targetModule.getConfiguration().getName();

            if (targetModuleName.toUpperCase().contains(originalName.toUpperCase())
                && CFunctionHelpers.isForwardableFunction(currentFunction)
                && (currentFunction.getForwardedFunctionModuleId() == 0)) {
              String currentFunctionName = currentFunction.getName();

              if (currentFunctionName.startsWith("__imp_")) {
                currentFunctionName = currentFunctionName.substring("__imp_".length());
              }

              try {
                final INaviFunction targetFunction = targetModule.getContent()
                    .getFunctionContainer().getFunction(currentFunctionName);
                currentFunction.setForwardedFunction(targetFunction);
                ++counter;
              } catch (final MaybeNullException exception) {

              } catch (final CouldntSaveDataException exception) {
                CUtilityFunctions.logException(exception);
              }
            }
          }
        }
      }
    }
    return counter;
  }

  private void resultDialog(final int counter) {
    if (counter == 0) {
      return;
    } else {
      CMessageBox.showInformation(getParent(),
          "Forwared " + counter + " functions in " + m_targetModules.size() + " modules.");
    }
  }
}
