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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component;



import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CLabeledComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component.Help.CHostHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component.Help.CNameHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component.Help.CPortHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Debugger.Component.Help.CSaveHelp;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveField;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveFormattedField;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Help.CHelpButton;
import com.google.security.zynamics.binnavi.Help.CHelpSaveField;
import com.google.security.zynamics.binnavi.Help.CHelpSaveFormattedField;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener;
import com.google.security.zynamics.zylib.gui.CDecFormatter;

/**
 * Component that is displayed on the right side of the main window whenever a debugger node was
 * selected.
 */
public final class CDebuggerNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1348721039872070862L;

  /**
   * Debugger template that is edited in the component.
   */
  private final DebuggerTemplate m_debugger;

  /**
   * Field where the name of the debugger can be edited.
   */
  private final CSaveField m_nameTextField;

  /**
   * Field where the host of the debugger can be edited.
   */
  private final CSaveField m_hostTextField;

  /**
   * Field where the port of the debugger can be edited.
   */
  private final CSaveFormattedField m_portTextField;

  /**
   * Updates the GUI on changes in the debugger template.
   */
  private final InternalDebuggerDescriptionListener m_listener =
      new InternalDebuggerDescriptionListener();

  /**
   * Button used to save the input back to the debugger.
   */
  private final JButton m_saveButton = new CHelpButton(CActionProxy.proxy(new SaveAction()),
      new CSaveHelp());

  /**
   * Creates a new component object.
   * 
   * @param debugger Debugger template that is edited in the component.
   */
  public CDebuggerNodeComponent(final DebuggerTemplate debugger) {
    super(new BorderLayout());

    m_debugger = Preconditions.checkNotNull(debugger, "IE01967: Debugger argument can't be null");

    m_nameTextField = new CHelpSaveField(debugger.getName(), new CNameHelp());
    m_hostTextField = new CHelpSaveField(debugger.getHost(), new CHostHelp());
    m_portTextField = new CHelpSaveFormattedField(new CDecFormatter(5), new CPortHelp());
    m_portTextField.setText(String.valueOf(debugger.getPort()));

    m_debugger.addListener(m_listener);

    createGui();

    final UpdateListener updateListener = new UpdateListener();

    m_nameTextField.getDocument().addDocumentListener(updateListener);
    m_hostTextField.getDocument().addDocumentListener(updateListener);
    m_portTextField.getDocument().addDocumentListener(updateListener);

    updateGui();
  }

  /**
   * Creates the GUI of the component.
   */
  private void createGui() {
    final JPanel basePanel = new JPanel(new BorderLayout());

    final JPanel titledBorderHelperPanel = new JPanel(new BorderLayout());
    titledBorderHelperPanel.setBorder(new TitledBorder("Debugger"));

    final JPanel containerPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    containerPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

    final JPanel buttonPanel = new JPanel(new GridLayout(1, 2));

    buttonPanel.setBorder(new EmptyBorder(5, 0, 5, 2));
    buttonPanel.add(new JPanel());
    buttonPanel.add(m_saveButton);

    containerPanel.add(new CLabeledComponent("Name" + ":", new CNameHelp(), m_nameTextField));
    containerPanel.add(new CLabeledComponent("Host" + ":", new CHostHelp(), m_hostTextField));
    containerPanel.add(new CLabeledComponent("Port" + ":", new CPortHelp(), m_portTextField));

    titledBorderHelperPanel.add(containerPanel, BorderLayout.NORTH);

    basePanel.add(titledBorderHelperPanel, BorderLayout.NORTH);
    basePanel.add(buttonPanel, BorderLayout.CENTER);

    add(basePanel, BorderLayout.NORTH);
  }

  /**
   * Saves the data from the GUI to the debugger object.
   */
  private void save() {
    try {
      m_debugger.setName(m_nameTextField.getText());
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00053: " + "Could not save the new debugger name";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger name could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger name was saved"},
                  new String[] {"The debugger name was not saved. Please try to find out what went wrong with the database connection and try to save the debugger name again."});

      NaviErrorDialog
          .show(SwingUtilities.getWindowAncestor(this), message, description, exception);
    }

    try {
      m_debugger.setHost(m_hostTextField.getText());
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00158: " + "Could not save the new debugger description";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger description could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger description was saved"},
                  new String[] {"The debugger description was not saved. Please try to find out what went wrong with the database connection and try to save the debugger description again."});

      NaviErrorDialog
          .show(SwingUtilities.getWindowAncestor(this), message, description, exception);
    }

    try {
      m_debugger.setPort(Integer.parseInt(m_portTextField.getText()));
    } catch (final CouldntSaveDataException exception) {
      CUtilityFunctions.logException(exception);

      final String message = "E00159: " + "Could not save the new debugger port";
      final String description =
          CUtilityFunctions
              .createDescription(
                  "The new debugger port could not be saved to the database.",
                  new String[] {"There was a problem with the connection to the database while the debugger port was saved"},
                  new String[] {"The debugger port was not saved. Please try to find out what went wrong with the database connection and try to save the debugger port again."});

      NaviErrorDialog
          .show(SwingUtilities.getWindowAncestor(this), message, description, exception);
    }
  }

  /**
   * Updates the GUI depending on the state of the input fields.
   */
  private void updateGui() {
    final boolean nameModified = !m_nameTextField.getText().equals(m_debugger.getName());
    final boolean hostModified = !m_hostTextField.getText().equals(m_debugger.getHost());
    final boolean portModified =
        !m_portTextField.getText().equals(String.valueOf(m_debugger.getPort()));

    m_saveButton.setEnabled(nameModified || hostModified || portModified);

    m_nameTextField.setModified(nameModified);
    m_hostTextField.setModified(hostModified);
    m_portTextField.setModified(portModified);
  }

  @Override
  public void dispose() {
    m_debugger.removeListener(m_listener);
  }

  /**
   * Updates the GUI on relevant events in the debugger template.
   */
  private class InternalDebuggerDescriptionListener implements IDebuggerTemplateListener {
    @Override
    public void changedHost(final DebuggerTemplate debugger) {
      m_hostTextField.setText(debugger.getHost());
      updateGui();
    }

    @Override
    public void changedName(final DebuggerTemplate debugger) {
      m_nameTextField.setText(debugger.getName());
      updateGui();
    }

    @Override
    public void changedPort(final DebuggerTemplate debugger) {
      m_portTextField.setText(String.valueOf(debugger.getPort()));
      updateGui();
    }
  }

  /**
   * Action that is used to store the information from the GUI to the debugger template object.
   */
  private class SaveAction extends AbstractAction {
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 8909931652747685283L;

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
   * Listener that updates the Save button on input changes.
   */
  private class UpdateListener implements DocumentListener {
    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateGui();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateGui();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateGui();
    }
  }
}
