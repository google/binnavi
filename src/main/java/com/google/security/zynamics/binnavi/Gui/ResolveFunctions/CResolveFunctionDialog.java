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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Loaders.CModuleLoader;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CFunctionHelpers;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.gui.CDialogEscaper;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.CPanelTwoButtons;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Using this dialog it is possible to resolve imported functions by name.
 */
public final class CResolveFunctionDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5712062553289156840L;

  /**
   * Module that contains the imported functions to be forwarded.
   */
  private final INaviModule m_module;

  /**
   * List that contains all parent module strings from the module.
   */
  private final JComboBox<String> m_parentBox;

  /**
   * List that contains all possible target modules.
   */
  private final JComboBox<CModuleWrapper> m_targetModuleBox;

  /**
   * Creates a new function resolving dialog.
   *
   * @param owner The parent window of the dialog.
   * @param database The dialog that contains the target modules.
   * @param module The source module.
   */
  public CResolveFunctionDialog(final Window owner, final IDatabase database,
      final INaviModule module) {
    super(owner, "Resolve imported functions", ModalityType.APPLICATION_MODAL);

    Preconditions.checkNotNull(database, "IE02062: Database argument can't be null");
    Preconditions.checkState(database.isLoaded(),
        "IE02063: Database must be loaded before functions can be redirected");
    m_module = Preconditions.checkNotNull(module, "IE02064: Module argument can't be null");
    Preconditions.checkState(module.isLoaded(),
        "IE02065: Module must be loaded before functions can be redirected");
    Preconditions.checkState(module.inSameDatabase(database),
        "IE02066: Module is not in the given database");

    setLayout(new BorderLayout());

    new CDialogEscaper(this);

    final JPanel outerPanel = new JPanel(new BorderLayout());

    outerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

    final JPanel innerPanel = new JPanel(new GridLayout(2, 3));

    innerPanel.setBorder(new LineBorder(Color.BLUE));

    innerPanel.add(createLabel("Source Module"));
    innerPanel.add(createLabel("Parent String"));
    innerPanel.add(createLabel("Target Module"));

    innerPanel.add(createLabel(module.getConfiguration().getName()));

    m_parentBox = new JComboBox<String>(generateParentBoxVector(module));
    m_parentBox.setBorder(new EmptyBorder(0, 5, 5, 5));

    innerPanel.add(m_parentBox);

    m_targetModuleBox = new JComboBox<CModuleWrapper>(generateTargetBoxVector(database, module));
    m_targetModuleBox.setBorder(new EmptyBorder(0, 5, 5, 5));

    innerPanel.add(m_targetModuleBox);

    outerPanel.add(innerPanel);

    add(outerPanel);

    add(new CPanelTwoButtons(new InternalListener(), "Apply", "Close"), BorderLayout.SOUTH);

    pack();
  }

  /**
   * Small helper function that creates a padded label.
   *
   * @param string The label string.
   *
   * @return The created label.
   */
  private static JLabel createLabel(final String string) {
    final JLabel label = new JLabel(string);

    label.setBorder(new EmptyBorder(5, 5, 5, 5));

    return label;
  }

  /**
   * Creates a list of all parent module strings of the functions of a module.
   *
   * @param module The module that contains the functions.
   *
   * @return The list of parent module strings.
   */
  private static Vector<String> generateParentBoxVector(final INaviModule module) {
    final HashSet<String> parents = new HashSet<String>();

    for (final INaviFunction function : module.getContent().getFunctionContainer().getFunctions()) {
      final String name = function.getOriginalModulename();

      if (!name.equals(module.getConfiguration().getName())) {
        parents.add(name);
      }
    }

    return new Vector<String>(parents);
  }

  /**
   * Generates a list of all potential target modules.
   *
   * @param database The database that contains the modules.
   * @param sourceModule The source module that should not appear in the list of target modules.
   *
   * @return The list of target modules.
   */
  private static Vector<CModuleWrapper> generateTargetBoxVector(final IDatabase database,
      final INaviModule sourceModule) {
    final Vector<CModuleWrapper> wrappers = new Vector<CModuleWrapper>();

    for (final INaviModule module : database.getContent().getModules()) {
      if (module == sourceModule) {
        continue;
      }

      wrappers.add(new CModuleWrapper(module));
    }

    return wrappers;
  }

  /**
   * Takes the information from the GUI and forwards functions.
   */
  private void resolveFunctions() {
    final Object selectedParentString = m_parentBox.getSelectedItem();

    if (selectedParentString == null) {
      return;
    }

    final Object selectedModule = m_targetModuleBox.getSelectedItem();

    if (selectedModule == null) {
      return;
    }

    final INaviModule targetModule = ((CModuleWrapper) selectedModule).getObject();

    if (!targetModule.isLoaded()) {
      if (CMessageBox.showYesNoQuestion(this,
          "The target module must be loaded before functions can be forwarded.\n\n"
          + "Do you want to load the target module now?") == JOptionPane.NO_OPTION) {
        return;
      }

      CModuleLoader.loadModule(this, targetModule);
    }

    int counter = 0;

    final String parentString = selectedParentString.toString();

    for (final INaviFunction sourceFunction :
        m_module.getContent().getFunctionContainer().getFunctions()) {
      if (sourceFunction.getOriginalModulename().equalsIgnoreCase(parentString)
          && CFunctionHelpers.isForwardableFunction(sourceFunction)) {
        String sourceFunctionName = sourceFunction.getName();
        if (sourceFunctionName.startsWith("__imp_")) {
          sourceFunctionName = sourceFunctionName.substring("__imp_".length());
        }

        try {
          final INaviFunction targetFunction =
              targetModule.getContent().getFunctionContainer().getFunction(sourceFunctionName);

          sourceFunction.setForwardedFunction(targetFunction);

          ++counter;
        } catch (final MaybeNullException exception) {
          // There is no function in the target modules with the name of the source function.
        } catch (final CouldntSaveDataException exception) {
          CUtilityFunctions.logException(exception);

          final String message = "E00023: " + "Could not save function forwarding";
          final String description = CUtilityFunctions.createDescription(String.format(
              "Could not forward the function '%s' from module '%s' to module '%s'",
              sourceFunction.getName(), m_module.getConfiguration().getName(),
              targetModule.getConfiguration().getName()),
              new String[] {"The database connection was dropped while saving."}, new String[] {
                  "The changes in function forwarding were not saved. Try saving function "
                  + "forwarding again. If necessary, close the connection to the database and "
                  + "reconnect."});

          NaviErrorDialog.show(this, message, description, exception);
        }
      }
    }

    if (counter == 0) {
      CMessageBox.showInformation(this, "No functions suitable for forwarding were found.");
    } else {
      CMessageBox.showInformation(this, String.format(
          "%d functions were forwarded from module '%s' to module '%s'", counter,
          m_module.getConfiguration().getName(), targetModule.getConfiguration().getName()));
    }
  }

  /**
   * Action handler for the buttons of the dialog.
   */
  private class InternalListener extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2029903028284147876L;

    @Override
    public void actionPerformed(final ActionEvent event) {
      if (event.getActionCommand().equals("Close")) {
        dispose();
      } else {
        resolveFunctions();
      }
    }
  }
}
