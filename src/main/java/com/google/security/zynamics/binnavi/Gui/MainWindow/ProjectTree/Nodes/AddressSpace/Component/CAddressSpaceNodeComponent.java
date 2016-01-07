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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component;



import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboBox;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboModel;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.CTablePanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CCreationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CDescriptionHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CModificationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CModuleFilterHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CNameHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.AddressSpace.Component.Implementations.CAddressSpaceFunctions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.filters.CModuleFilterCreator;
import com.google.security.zynamics.binnavi.Gui.Progress.CDefaultProgressOperation;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CDefaultFieldDescription;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.IInputPanelListener;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;

/**
 * Component that is displayed on the right side of the main window whenever an address space node
 * was selected.
 */
public final class CAddressSpaceNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6284210238971673084L;

  /**
   * Address space displayed in this component.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Border that shows the amount of modules in the address space.
   */
  private final TitledBorder m_titledBorder;

  /**
   * Used to display name, description, creation date, and modification date of the address space.
   */
  private final CStandardEditPanel m_stdEditPanel;

  /**
   * Displays the debuggers available for the address space.
   */
  private CDebuggerComboBox m_debuggerCombo;

  /**
   * Displays the modules of the address space.
   */
  private final CProjectModulesTable m_table;

  /**
   * Button used to save the input back to the address space.
   */
  private final JButton m_saveButton = new CHelpButton(CActionProxy.proxy(new SaveAction()),
      new CSaveHelp());

  /**
   * Provides the debuggers of the project,
   */
  private final CProjectDebuggerContainer m_debuggerContainer;

  /**
   * Keeps the Save button updated on changes.
   */
  private final UpdateListener m_updateListener = new UpdateListener();

  /**
   * Synchronizes the component with the underlying model.
   */
  private final CComponentSynchronizer m_synchronizer;

  /**
   * Creates a new address space component.
   * 
   * @param projectTree Project tree that is updated when certain events happen.
   * @param database Database the address space belongs to.
   * @param project Project the address space belongs to.
   * @param addressSpace Address space displayed in this component.
   */
  public CAddressSpaceNodeComponent(final JTree projectTree, final IDatabase database,
      final INaviProject project, final INaviAddressSpace addressSpace) {
    super(new BorderLayout());

    Preconditions.checkNotNull(database, "IE01948: Database argument can not be null");
    Preconditions.checkNotNull(project, "IE01949: Project argument can't be null");
    Preconditions.checkNotNull(addressSpace, "IE01950: Address space argument can't be null");

    Preconditions.checkArgument(database.inSameDatabase(project),
        "IE01951: The project is not in the given database");
    Preconditions.checkArgument(database.inSameDatabase(addressSpace),
        "IE01952: The address space is not in the given database");
    Preconditions.checkArgument(project.getContent().getAddressSpaces().contains(addressSpace),
        "IE01953: Address space does not belong to the given project");

    m_addressSpace = addressSpace;

    m_table = new CProjectModulesTable(projectTree, database, addressSpace);

    final CDefaultFieldDescription<String> nameInfo =
        new CDefaultFieldDescription<String>(addressSpace.getConfiguration().getName(),
            new CNameHelp());
    final CDefaultFieldDescription<String> descriptionInfo =
        new CDefaultFieldDescription<String>(addressSpace.getConfiguration().getDescription(),
            new CDescriptionHelp());
    final CDefaultFieldDescription<Date> creationInfo =
        new CDefaultFieldDescription<Date>(addressSpace.getConfiguration().getCreationDate(),
            new CCreationDateHelp());
    final CDefaultFieldDescription<Date> modificationInfo =
        new CDefaultFieldDescription<Date>(addressSpace.getConfiguration().getModificationDate(),
            new CModificationDateHelp());

    m_stdEditPanel =
        new CStandardEditPanel("Address Space", nameInfo, descriptionInfo, creationInfo,
            modificationInfo);

    m_debuggerContainer = new CProjectDebuggerContainer(project);

    m_titledBorder = new TitledBorder("");

    createGui();

    m_synchronizer =
        new CComponentSynchronizer(this, addressSpace, m_stdEditPanel, m_debuggerCombo,
            m_titledBorder);

    updateSaveButton();

    m_stdEditPanel.addInputListener(m_updateListener);
    m_debuggerCombo.addActionListener(m_updateListener);
  }

  /**
   * Creates all the necessary GUI components of this component.
   */
  private void createGui() {
    final JPanel topPanel = new JPanel(new BorderLayout());

    final JPanel innerTopPanel = new JPanel(new BorderLayout());

    final JPanel debuggerChooserPanel = new JPanel(new BorderLayout());
    debuggerChooserPanel.setBorder(new TitledBorder("Address Space Debugger"));

    m_debuggerCombo = new CDebuggerComboBox(new CDebuggerComboModel(m_debuggerContainer));
    m_debuggerCombo.setSelectedDebugger(m_addressSpace.getConfiguration().getDebuggerTemplate());

    final JPanel debuggerComboPanel = new JPanel(new BorderLayout());

    debuggerComboPanel.add(m_debuggerCombo, BorderLayout.CENTER);

    debuggerChooserPanel.add(debuggerComboPanel, BorderLayout.CENTER);

    innerTopPanel.add(m_stdEditPanel);
    innerTopPanel.add(debuggerChooserPanel, BorderLayout.SOUTH);

    topPanel.add(innerTopPanel);

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.setBorder(new EmptyBorder(0, 0, 5, 2));
    buttonPanel.add(new JPanel());
    buttonPanel.add(m_saveButton);

    topPanel.add(buttonPanel, BorderLayout.SOUTH);

    final JPanel bottomPanel =
        new CTablePanel<INaviModule>(m_table, new CModuleFilterCreator(), new CModuleFilterHelp());

    bottomPanel.setBorder(m_titledBorder);
    bottomPanel.add(new JScrollPane(m_table));

    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
    splitPane.setResizeWeight(0.5);

    setBorder(new EmptyBorder(0, 0, 0, 1));

    add(splitPane);
  }

  /**
   * Saves the information from the GUI back to the address space object.
   */
  private void save() {
    new Thread() {
      @Override
      public void run() {
        final CDefaultProgressOperation operation = new CDefaultProgressOperation("", false, true);
        operation.getProgressPanel().setMaximum(3);

        operation.getProgressPanel().setText(
            "Saving address space configuration" + ": " + "Saving Name");

        CAddressSpaceFunctions.saveName(
            SwingUtilities.getWindowAncestor(CAddressSpaceNodeComponent.this), m_addressSpace,
            m_stdEditPanel.getNameString());

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving address space configuration" + ": " + "Saving Description");

        CAddressSpaceFunctions.saveDescription(
            SwingUtilities.getWindowAncestor(CAddressSpaceNodeComponent.this), m_addressSpace,
            m_stdEditPanel.getDescription());

        operation.getProgressPanel().next();
        operation.getProgressPanel().setText(
            "Saving address space configuration" + ": " + "Saving Debugger");

        saveDebugger();

        operation.getProgressPanel().next();
        operation.stop();

        updateSaveButton();
      }
    }.start();
  }

  /**
   * Saves the configured address space debugger to the database.
   */
  private void saveDebugger() {
    try {
      m_addressSpace.getConfiguration().setDebuggerTemplate(m_debuggerCombo.getSelectedDebugger());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00155: " + "Could not save address space debugger";
      final String innerDescription =
          CUtilityFunctions.createDescription(String.format(
              "The new debugger of the address space '%s' could not be saved.", m_addressSpace
                  .getConfiguration().getName()),
              new String[] {"There was a problem with the database connection."},
              new String[] {"The address space keeps its old debugger."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(this), innerMessage, innerDescription,
          e);
    }
  }

  /**
   * Updates the Save button depending on the input state.
   */
  private void updateSaveButton() {
    m_saveButton.setEnabled(!m_stdEditPanel.getNameString().equals(
        m_addressSpace.getConfiguration().getName())
        || !m_stdEditPanel.getDescription().equals(
            m_addressSpace.getConfiguration().getDescription())
        || (m_debuggerCombo.getSelectedDebugger() != m_addressSpace.getConfiguration()
            .getDebuggerTemplate()));
  }

  @Override
  public void dispose() {
    m_synchronizer.dispose();
    m_debuggerContainer.dispose();

    m_table.dispose();
  }

  /**
   * Action invoked when the user clicks on the Save button.
   */
  private final class SaveAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = -2453848241255126009L;

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
   * Listener that is used to update the Save button on input changes.
   */
  private class UpdateListener implements ActionListener, IInputPanelListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      updateSaveButton();
    }

    @Override
    public void changedInput() {
      updateSaveButton();
    }
  }
}
