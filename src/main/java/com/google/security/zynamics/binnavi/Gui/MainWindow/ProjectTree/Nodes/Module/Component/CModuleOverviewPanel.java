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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.Help.CCreationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.Help.CDescriptionHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.Help.CModificationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.Help.CNameHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Module.Component.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.CDebuggerChooserPanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.IDebuggerChooserPanelListener;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.CNativeCallgraphViewsNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.CNativeFunctionViewsNodeComponent;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CDefaultFieldDescription;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.IInputPanelListener;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.SwingInvoker;



/**
 * Shows the overview information of a panel.
 */
public final class CModuleOverviewPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3394022635802471859L;

  /**
   * Database where the module is stored.
   */
  private final IDatabase m_database;

  /**
   * Address space the module belongs to (this argument can be null in case of a global module).
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Module that provides the information displayed in the component.
   */
  private final INaviModule m_module;

  /**
   * Used to display name, description, creation date, and modification date of the module.
   */
  private final CStandardEditPanel m_stdEditPanel;

  /**
   * Used to display the native functions of the module.
   */
  private final CNativeFunctionViewsNodeComponent m_bottomPanel;

  /**
   * Used to display the native Call graph of the module.
   */
  private final CNativeCallgraphViewsNodeComponent m_middlePanel;

  /**
   * Used to configure the module debugger.
   */
  private final CDebuggerChooserPanel m_debuggerPanel;

  /**
   * Wraps the available debuggers.
   */
  private final CDatabaseDebuggerContainer m_debuggerContainer;

  /**
   * Updates the GUI on changes to the debugger configuration.
   */
  private final IDebuggerChooserPanelListener m_internalDebuggerPanelListener =
      new InternalDebuggerChooserListener();

  /**
   * Updates the GUI on relevant changes in the module.
   */
  private final InternalModuleListener m_moduleListener = new InternalModuleListener();

  /**
   * Keeps the Save button updated on changes.
   */
  private final UpdateListener m_updateListener = new UpdateListener();

  /**
   * Button used to save the input back to the module.
   */
  private final JButton m_saveButton = new CHelpButton(CActionProxy.proxy(new SaveAction()),
      new CSaveHelp());

  /**
   * Creates a new panel object.
   * 
   * @param projectTree Project tree that is updated on certain events.
   * @param database Database the module belongs to.
   * @param addressSpace Address space the module belongs to. This argument can be null.
   * @param module Module whose overview information is shown.
   * @param container Provides the views.
   */
  public CModuleOverviewPanel(final JTree projectTree, final IDatabase database,
      final INaviAddressSpace addressSpace, final INaviModule module, final IViewContainer container) {
    super(new BorderLayout());

    m_database = database;
    m_module = module;
    m_addressSpace = addressSpace;

    final CDefaultFieldDescription<String> nameInfo =
        new CDefaultFieldDescription<String>(module.getConfiguration().getName(), new CNameHelp());
    final CDefaultFieldDescription<String> descriptionInfo =
        new CDefaultFieldDescription<String>(module.getConfiguration().getDescription(),
            new CDescriptionHelp());
    final CDefaultFieldDescription<Date> creationInfo =
        new CDefaultFieldDescription<Date>(module.getConfiguration().getCreationDate(),
            new CCreationDateHelp());
    final CDefaultFieldDescription<Date> modificationInfo =
        new CDefaultFieldDescription<Date>(module.getConfiguration().getModificationDate(),
            new CModificationDateHelp());

    m_stdEditPanel =
        new CStandardEditPanel("Module", nameInfo, descriptionInfo, creationInfo, modificationInfo);

    m_middlePanel = new CNativeCallgraphViewsNodeComponent(projectTree, m_module, container);
    m_bottomPanel =
        new CNativeFunctionViewsNodeComponent(projectTree, m_database, m_module, container);

    m_debuggerContainer = new CDatabaseDebuggerContainer(database);
    m_debuggerPanel = new CDebuggerChooserPanel(addressSpace, module, m_debuggerContainer);

    m_debuggerPanel.addListener(m_internalDebuggerPanelListener);

    module.addListener(m_moduleListener);

    createGui();

    m_stdEditPanel.addInputListener(m_updateListener);

    updateSaveButton();
  }

  /**
   * Creates the GUI elements of the component.
   */
  private void createGui() {
    final JPanel topPanel = new JPanel(new BorderLayout());

    final JPanel innerTopPanel = new JPanel(new BorderLayout());

    topPanel.add(innerTopPanel);

    innerTopPanel.add(m_stdEditPanel);

    innerTopPanel.add(m_debuggerPanel, BorderLayout.SOUTH);

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.setBorder(new EmptyBorder(0, 0, 5, 2));
    buttonPanel.add(new JPanel());
    buttonPanel.add(m_saveButton);

    topPanel.add(buttonPanel, BorderLayout.SOUTH);

    final JPanel innerSp = new JPanel(new BorderLayout());
    m_middlePanel.setPreferredSize(new Dimension(m_middlePanel.getPreferredSize().width, 75));
    innerSp.add(m_middlePanel, BorderLayout.NORTH);
    innerSp.add(m_bottomPanel, BorderLayout.CENTER);

    final JSplitPane outerSp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, innerSp);
    outerSp.setOneTouchExpandable(true);

    outerSp.setDividerLocation(outerSp.getMinimumDividerLocation());
    outerSp.setResizeWeight(0.5);

    final JPanel innerPanel = new JPanel(new BorderLayout());

    innerPanel.add(outerSp);

    add(innerPanel);
  }

  /**
   * Determines whether the entered image base is different from the image base stored in the model.
   * 
   * @return True, if the image bases
   */
  private boolean isImageBaseModified() {
    final String imageBaseText = m_debuggerPanel.getImageBase();

    if ("".equals(imageBaseText)) {
      return true;
    }

    final CAddress enteredAddress = new CAddress(Convert.hexStringToLong(imageBaseText));

    if (m_addressSpace == null) {
      return !enteredAddress.equals(m_module.getConfiguration().getImageBase());
    } else {
      return !enteredAddress.equals(m_addressSpace.getContent().getImageBase(m_module));
    }
  }

  /**
   * Saves the information from the GUI back into the module object.
   */
  private void save() {
    final String fileBaseText = m_debuggerPanel.getFileBase();

    if (!Convert.isHexString(fileBaseText)) {
      CMessageBox.showError(this,
          "Could not save original base address. Value is not a valid hexadecimal address.");

      return;
    }

    final String imageBaseText = m_debuggerPanel.getImageBase();

    if (!Convert.isHexString(imageBaseText)) {
      CMessageBox.showError(this,
          "Could not save relocated base address. Value is not a valid hexadecimal address.");

      return;
    }

    new Thread() {
      // We are using a thread to save the information so that the GUI is not blocked
      // during database access.

      @Override
      public void run() {
        final CDefaultProgressOperation operation = new CDefaultProgressOperation("", false, true);
        operation.getProgressPanel().setMaximum(5);

        operation.getProgressPanel().setText("Saving module configuration" + ": " + "Saving Name");

        saveName();

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving module configuration" + ": " + "Saving Description");

        saveDescription();

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving module configuration" + ": " + "Saving Debugger");

        saveDebugger();

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving module configuration" + ": " + "Saving File Base");

        saveFileBase();

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving module configuration" + ": " + "Saving Image Base");

        saveImageBase();

        operation.getProgressPanel().next();
        operation.stop();

        updateSaveButton();
      }
    }.start();
  }

  /**
   * Saves the module debugger to the database.
   */
  private void saveDebugger() {
    if (m_addressSpace == null) {
      try {
        m_module.getConfiguration().setDebuggerTemplate(m_debuggerPanel.getSelectedDebugger());
      } catch (final CouldntSaveDataException e) {
        CUtilityFunctions.logException(e);

        final String message = "E00179: " + "Could not change the module debugger";
        final String description =
            CUtilityFunctions
                .createDescription(
                    "The new module debugger could not be saved to the database.",
                    new String[] {"There was a problem with the connection to the database while the debugger was saved"},
                    new String[] {"The debugger was not saved. Please try to find out what went wrong with the database connection and try to save the debugger again."});

        NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), message, description, e);
      }
    }
  }

  /**
   * Saves the module description to the database.
   */
  private void saveDescription() {
    try {
      m_module.getConfiguration().setDescription(m_stdEditPanel.getDescription());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00164: " + "Could not change the module description";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new module description could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the module description was saved"},
                  new String[] {"The module description was not saved. Please try to find out what went wrong with the database connection and try to save the module description again."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), message, description, e);
    }
  }

  /**
   * Saves the module file base to the database.
   */
  private void saveFileBase() {
    try {
      final CAddress fileBase =
          new CAddress(Convert.hexStringToLong(m_debuggerPanel.getFileBase()));

      m_module.getConfiguration().setFileBase(new CAddress(fileBase));

    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00165: " + "Could not change the module file base";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new module file base could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the module file base was saved"},
                  new String[] {"The module file base was not saved. Please try to find out what went wrong with the database connection and try to save the module file base again."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), message, description, e);
    }
  }

  /**
   * Saves the module image base to the database.
   */
  private void saveImageBase() {
    try {
      final CAddress imageBase =
          new CAddress(Convert.hexStringToLong(m_debuggerPanel.getImageBase()));

      if (m_addressSpace == null) {
        m_module.getConfiguration().setImageBase(imageBase);
      } else {
        m_addressSpace.getContent().setImageBase(m_module, imageBase);
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00166: " + "Could not change the module image base";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new module image base could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the module image base was saved"},
                  new String[] {"The module image base was not saved. Please try to find out what went wrong with the database connection and try to save the module image base again."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), message, description, e);
    }
  }

  /**
   * Saves the module name to the database.
   */
  private void saveName() {
    try {
      m_module.getConfiguration().setName(m_stdEditPanel.getNameString());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String message = "E00160: " + "Could not change the module name";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new module name could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the module name was saved"},
                  new String[] {"The module name was not saved. Please try to find out what went wrong with the database connection and try to save the module name again."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), message, description, e);
    }
  }

  /**
   * Updates the Save button depending on the input state.
   */
  private void updateSaveButton() {
    final String fileBaseText = m_debuggerPanel.getFileBase();

    final boolean fileBaseChanged =
        "".equals(fileBaseText)
            || !new CAddress(Convert.hexStringToLong(fileBaseText)).equals(m_module
                .getConfiguration().getFileBase());
    final boolean imageBaseChanged = isImageBaseModified();

    m_saveButton.setEnabled(!m_stdEditPanel.getNameString().equals(
        m_module.getConfiguration().getName())
        || !m_stdEditPanel.getDescription().equals(m_module.getConfiguration().getDescription())
        || fileBaseChanged
        || imageBaseChanged
        || (m_debuggerPanel.getSelectedDebugger() != m_module.getConfiguration()
            .getDebuggerTemplate()));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_module.removeListener(m_moduleListener);
    m_debuggerPanel.removeListener(m_internalDebuggerPanelListener);
    m_debuggerPanel.dispose();

    if (m_addressSpace == null) {
      m_debuggerContainer.dispose();
    }

    m_bottomPanel.dispose();
    m_middlePanel.dispose();
  }

  /**
   * Updates the GUI on changes to the debugger configuration.
   */
  private class InternalDebuggerChooserListener implements IDebuggerChooserPanelListener {
    @Override
    public void inputChanged() {
      updateSaveButton();
    }
  }

  /**
   * Updates the GUI on relevant changes in the module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedDescription(final INaviModule module, final String description) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setDescription(description);
        }
      }.invokeLater();
    }

    @Override
    public void changedModificationDate(final INaviModule module, final Date date) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setModificationDate(date);
        }
      }.invokeLater();
    }

    @Override
    public void changedName(final INaviModule module, final String name) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setNameString(name);
        }
      }.invokeLater();
    }
  }

  /**
   * Action class that is used to save the information from the GUI back into the module object.
   */
  private class SaveAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -5435176252593734737L;

    /**
     * Creates a new action object.
     */
    private SaveAction() {
      super("Save");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      save();
    }
  }

  /**
   * Listener used to update the Save button on input changes.
   */
  private class UpdateListener implements IInputPanelListener, DocumentListener, ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      updateSaveButton();
    }

    @Override
    public void changedInput() {
      updateSaveButton();
    }

    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateSaveButton();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateSaveButton();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateSaveButton();
    }
  }
}
