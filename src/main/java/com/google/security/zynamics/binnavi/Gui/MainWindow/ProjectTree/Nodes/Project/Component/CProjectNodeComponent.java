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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.Default.CAddressSpacesTablePanel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CCreationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CDescriptionHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CModificationDateHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CNameHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Project.Component.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.Progress.CGlobalProgressManager;
import com.google.security.zynamics.binnavi.Gui.Progress.IProgressOperation;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CDefaultFieldDescription;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.IInputPanelListener;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.gui.JCheckedListbox;
import com.google.security.zynamics.zylib.gui.SwingInvoker;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CProgressPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Component that is displayed on the right side of the main window when a project node is selected.
 */
public final class CProjectNodeComponent extends CAbstractNodeComponent {
  /**
   * Database the project belongs to.
   */
  private final IDatabase m_database;

  /**
   * The project whose information is displayed in the component.
   */
  private final INaviProject m_project;

  /**
   * Border that displays the number of address spaces in the project.
   */
  private final TitledBorder m_titledBorder;

  /**
   * Used to display name, description, creation date, and modification date of the project.
   */
  private final CStandardEditPanel m_stdEditPanel;

  /**
   * Displays the address spaces of the project.
   */
  private final CAddressSpacesTable m_table;

  /**
   * Displays the debuggers selected for the project.
   */
  private final JPanel m_checkedListPanel;

  /**
   * Displays the debuggers selected for the project.
   */
  private JCheckedListbox<DebuggerTemplate> m_checkedList;

  /**
   * Keeps the database model up to date when projects change.
   */
  private final InternalProjectListener m_projectListener = new InternalProjectListener();

  /**
   * Updates the GUI when something relevant happens in the debugger template manager.
   */
  private final InternalDebuggerDescriptionManagerListener m_debuggerManagerListener =
      new InternalDebuggerDescriptionManagerListener();

  /**
   * Button used to save the input back to the module.
   */
  private final JButton m_saveButton =
      new CHelpButton(CActionProxy.proxy(new SaveAction()), new CSaveHelp());

  /**
   * Updates the Save button depending on the input state.
   */
  private final UpdateListener m_updateListener = new UpdateListener();

  /**
   * Creates a new component object.
   *
   * @param projectTree Project tree that is updated when certain events happen.
   * @param database Database the project belongs to.
   * @param project The project whose information is displayed in the component.
   * @param container View container of the project.
   */
  public CProjectNodeComponent(final JTree projectTree, final IDatabase database,
      final INaviProject project, final IViewContainer container) {
    super(new BorderLayout());
    Preconditions.checkNotNull(projectTree, "IE01985: Project tree argument can not be null");
    m_project = Preconditions.checkNotNull(project, "IE01986: Project argument can't be null");
    m_database = Preconditions.checkNotNull(database, "IE01987: Database argument can't be null");
    final CDefaultFieldDescription<String> nameInfo =
        new CDefaultFieldDescription<String>(project.getConfiguration().getName(), new CNameHelp());
    final CDefaultFieldDescription<String> descriptionInfo = new CDefaultFieldDescription<String>(
        project.getConfiguration().getDescription(), new CDescriptionHelp());
    final CDefaultFieldDescription<Date> creationInfo = new CDefaultFieldDescription<Date>(
        project.getConfiguration().getCreationDate(), new CCreationDateHelp());
    final CDefaultFieldDescription<Date> modificationInfo = new CDefaultFieldDescription<Date>(
        project.getConfiguration().getModificationDate(), new CModificationDateHelp());
    m_stdEditPanel = new CStandardEditPanel("Project", nameInfo, descriptionInfo, creationInfo,
        modificationInfo);
    m_checkedListPanel = new JPanel(new BorderLayout());
    m_table = new CAddressSpacesTable(projectTree, database, m_project, container);
    m_titledBorder = new TitledBorder(getBorderText());
    createGui();
    m_database.getContent().getDebuggerTemplateManager().addListener(m_debuggerManagerListener);
    project.addListener(m_projectListener);
    m_checkedList.addListSelectionListener(m_updateListener);
    m_stdEditPanel.addInputListener(m_updateListener);
    updateSaveButton();
  }

  /**
   * Creates the elements of this component.
   */
  private void createGui() {
    final JPanel topPanel = new JPanel(new BorderLayout());
    final JPanel innerTopPanel = new JPanel(new BorderLayout());
    innerTopPanel.add(m_stdEditPanel);
    topPanel.add(innerTopPanel);
    final JPanel debuggerChooserPanel = new JPanel(new BorderLayout());
    debuggerChooserPanel.setBorder(new TitledBorder("Project Debuggers"));
    m_checkedList = new JCheckedListbox<>(new Vector<DebuggerTemplate>(), false);
    updateCheckedListPanel();
    final JScrollPane debuggerScrollPane = new JScrollPane(m_checkedList);
    m_checkedListPanel.add(debuggerScrollPane);
    debuggerChooserPanel.add(m_checkedListPanel, BorderLayout.CENTER);
    debuggerChooserPanel.setMinimumSize(new Dimension(0, 128));
    debuggerChooserPanel.setPreferredSize(new Dimension(0, 128));
    innerTopPanel.add(debuggerChooserPanel, BorderLayout.SOUTH);
    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
    buttonPanel.setBorder(new EmptyBorder(0, 0, 5, 2));
    buttonPanel.add(new JPanel());
    buttonPanel.add(m_saveButton);
    topPanel.add(buttonPanel, BorderLayout.SOUTH);
    final JPanel bottomPanel = new CAddressSpacesTablePanel(m_table);
    final JScrollPane scrollPane = new JScrollPane(m_table);
    bottomPanel.setBorder(m_titledBorder);
    setBorder(new EmptyBorder(0, 0, 0, 1));
    bottomPanel.add(scrollPane);
    final JSplitPane splitPane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, topPanel, bottomPanel);
    splitPane.setOneTouchExpandable(true);
    splitPane.setDividerLocation(splitPane.getMinimumDividerLocation());
    splitPane.setResizeWeight(0.5);
    add(splitPane);
  }

  /**
   * Generates the border text of the lower panel depending on the project state and the number of
   * address spaces in the project.
   *
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d Address Spaces in Project '%s'", m_project.getAddressSpaceCount(),
        m_project.getConfiguration().getName());
  }

  /**
   * Generates a vector of debugger objects which is used to fill the debugger list box.
   *
   * @return The generated vector.
   */
  private Vector<DebuggerTemplate> getDebuggerVector() {
    final Vector<DebuggerTemplate> debuggers = new Vector<>();
    for (final DebuggerTemplate debugger : m_database.getContent().getDebuggerTemplateManager()) {
      debuggers.add(debugger);
    }
    return debuggers;
  }

  /**
   * Returns the currently selected debuggers.
   *
   * @return The currently selected debuggers.
   */
  private Set<DebuggerTemplate> getSelectedDebuggers() {
    final Set<DebuggerTemplate> selectedTemplates = new HashSet<>();
    final ListModel<DebuggerTemplate> model = m_checkedList.getModel();
    for (int i = 0; i < model.getSize(); ++i) {
      final DebuggerTemplate debugger = model.getElementAt(i);
      if (m_checkedList.isChecked(i)) {
        selectedTemplates.add(debugger);
      }
    }
    return selectedTemplates;
  }

  /**
   * Saves the information from the GUI back into the project object.
   */
  private void save() {
    new Thread() {
      // We are using a thread to save the information so that the GUI is not blocked
      // during database access.

      @Override
      public void run() {
        final CProjectSaveProgressOperation progressOperation = new CProjectSaveProgressOperation();
        progressOperation.getProgressPanel().setText(
            "Saving project configuration" + ": " + "Saving Name");
        saveName();
        progressOperation.getProgressPanel().next();
        progressOperation.getProgressPanel().setText(
            "Saving project configuration" + ": " + "Saving Description");
        saveDescription();
        progressOperation.getProgressPanel().next();
        progressOperation.getProgressPanel().setText(
            "Saving project configuration" + ": " + "Saving Debuggers");
        saveDebuggers();
        progressOperation.getProgressPanel().next();
        progressOperation.stop();
        updateSaveButton();
      }
    }.start();
  }

  /**
   * Saves the configured project debuggers to the database.
   */
  private void saveDebuggers() {
    try {
      final ListModel<DebuggerTemplate> model = m_checkedList.getModel();
      final List<DebuggerTemplate> oldDebuggers = m_project.getConfiguration().getDebuggers();

      for (int i = 0; i < model.getSize(); ++i) {
        final DebuggerTemplate debugger = model.getElementAt(i);

        if (m_checkedList.isChecked(i) && !oldDebuggers.contains(debugger)) {
          m_project.getConfiguration().addDebugger(debugger);
        } else if (!m_checkedList.isChecked(i) && oldDebuggers.contains(debugger)) {
          m_project.getConfiguration().removeDebugger(model.getElementAt(i));
        }
      }
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);

      final String innerMessage = "E00173: " + "Could not save project debuggers";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The new debuggers of the project '%s' could not be saved.",
          m_project.getConfiguration().getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The project keeps its old debuggers."});

      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(CProjectNodeComponent.this),
          innerMessage, innerDescription, e);
    }
  }

  /**
   * Saves the project description to the database.
   */
  private void saveDescription() {
    try {
      m_project.getConfiguration().setDescription(m_stdEditPanel.getDescription());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);
      final String innerMessage = "E00172: " + "Could not save project description";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The new description of the project '%s' could not be saved.",
          m_project.getConfiguration().getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The project keeps its old description."});
      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(CProjectNodeComponent.this),
          innerMessage, innerDescription, e);
    }
  }

  /**
   * Saves the project name to the database.
   */
  private void saveName() {
    try {
      m_project.getConfiguration().setName(m_stdEditPanel.getNameString());
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);
      final String innerMessage = "E00171: " + "Could not save project name";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The new name of the project '%s' could not be saved.",
          m_project.getConfiguration().getName()),
          new String[] {"There was a problem with the database connection."},
          new String[] {"The project keeps its old name."});
      NaviErrorDialog.show(SwingUtilities.getWindowAncestor(CProjectNodeComponent.this),
          innerMessage, innerDescription, e);
    }
  }

  /**
   * Because the checked list box can not yet deal with growing or shrinking models, the checked
   * list box is recreated by this function if necessary.
   */
  private void updateCheckedListPanel() {
    m_checkedList.removeListSelectionListener(m_updateListener);
    m_checkedListPanel.removeAll();
    m_checkedList = new JCheckedListbox<>(getDebuggerVector(), false);
    m_checkedList.addListSelectionListener(m_updateListener);
    final JScrollPane debuggerScrollPane = new JScrollPane(m_checkedList);
    m_checkedListPanel.add(debuggerScrollPane);
    final Collection<DebuggerTemplate> debuggers = m_project.getConfiguration().getDebuggers();
    final ListModel<DebuggerTemplate> model = m_checkedList.getModel();
    for (int i = 0; i < model.getSize(); ++i) {
      final DebuggerTemplate debuggerId = model.getElementAt(i);
      m_checkedList.setChecked(i, debuggers.contains(debuggerId));
    }
    m_checkedList.updateUI();
    updateUI();
  }

  /**
   * Updates the Save button depending on the input state.
   */
  private void updateSaveButton() {
    m_saveButton.setEnabled(!m_stdEditPanel.getNameString().equals(
        m_project.getConfiguration().getName()) || !m_stdEditPanel.getDescription().equals(
        m_project.getConfiguration().getDescription()) || !getSelectedDebuggers().equals(
        new HashSet<DebuggerTemplate>(m_project.getConfiguration().getDebuggers())));
  }

  /**
   * Invoked to tell the component to clean up allocated resources.
   */
  @Override
  public void dispose() {
    m_project.removeListener(m_projectListener);
    m_database.getContent().getDebuggerTemplateManager().removeListener(m_debuggerManagerListener);
    m_table.dispose();
  }

  /**
   * Helper class to show a progress dialog during project saving.
   */
  private static class CProjectSaveProgressOperation implements IProgressOperation {
    /**
     * The progress panel used to show project saving progress.
     */
    private final CProgressPanel m_progressPanel = new CProgressPanel("", false, true);

    /**
     * Creates a new progress operation object.
     */
    public CProjectSaveProgressOperation() {
      m_progressPanel.setMaximum(3);
      m_progressPanel.start();
      CGlobalProgressManager.instance().add(this);
    }

    @Override
    public String getDescription() {
      return "Saving project configuration";
    }

    @Override
    public CProgressPanel getProgressPanel() {
      return m_progressPanel;
    }

    /**
     * Stops the progress operation.
     */
    public void stop() {
      m_progressPanel.stop();

      CGlobalProgressManager.instance().remove(this);
    }
  }

  /**
   * Updates the GUI when something relevant happens in the debugger template manager of the
   * database the project belongs to.
   */
  private class InternalDebuggerDescriptionManagerListener implements
      IDebuggerTemplateManagerListener {
    @Override
    public void addedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      updateCheckedListPanel();
    }

    @Override
    public void removedDebugger(final DebuggerTemplateManager manager,
        final DebuggerTemplate debugger) {
      updateCheckedListPanel();
    }
  }

  /**
   * Keeps the database model up to date when projects change.
   */
  private class InternalProjectListener extends CProjectListenerAdapter {
    @Override
    public void addedAddressSpace(final INaviProject project, final CAddressSpace space) {
      m_titledBorder.setTitle(getBorderText());

      updateUI();
    }

    @Override
    public void changedDescription(final INaviProject project, final String description) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setDescription(description);
        }
      }.invokeLater();
    }

    @Override
    public void changedModificationDate(final INaviProject project, final Date date) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setModificationDate(date);
        }
      }.invokeLater();
    }

    @Override
    public void changedName(final INaviProject project, final String name) {
      new SwingInvoker() {
        // Necessary because of Case 2282: Don't update the GUI from a non-GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setNameString(name);
          m_titledBorder.setTitle(getBorderText());
          updateUI();
        }
      }.invokeLater();
    }

    @Override
    public void loadedProject(final CProject project) {
      m_titledBorder.setTitle(getBorderText());
      updateCheckedListPanel();
      updateUI();
    }

    @Override
    public void removedAddressSpace(final INaviProject project, final INaviAddressSpace space) {
      m_titledBorder.setTitle(getBorderText());
      updateUI();
    }
  }

  /**
   * Action class that is used to save the data from the GUI back to the project object.
   */
  private class SaveAction extends AbstractAction {
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
   * Updates the Save button depending on the input state.
   */
  private class UpdateListener implements IInputPanelListener, ListSelectionListener {
    @Override
    public void changedInput() {
      updateSaveButton();
    }

    @Override
    public void valueChanged(final ListSelectionEvent event) {
      updateSaveButton();
    }
  }

}
