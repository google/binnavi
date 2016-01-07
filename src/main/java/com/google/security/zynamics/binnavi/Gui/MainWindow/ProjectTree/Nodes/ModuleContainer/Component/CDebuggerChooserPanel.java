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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component;



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboBox;
import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboModel;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CLabeledComponent;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.IDebuggerContainer;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.Help.COriginalAddressHelp;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.Help.CRelocatedAddressHelp;
import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveFormattedField;
import com.google.security.zynamics.binnavi.Help.CHelpSaveFormattedField;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModuleListenerAdapter;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.CHexFormatter;
import com.google.security.zynamics.zylib.gui.SwingInvoker;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;



/**
 * Panel used to configure the debugger of a module.
 */
public final class CDebuggerChooserPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6446549535947125186L;

  /**
   * Address space the module belongs to. This argument can be null.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * Module whose debugger is configured.
   */
  private final INaviModule m_module;

  /**
   * Used to set the file base address of the module.
   */
  private final CSaveFormattedField m_fileBaseAddr = new CHelpSaveFormattedField(
      new DefaultFormatterFactory(new CHexFormatter(8)), new COriginalAddressHelp());

  /**
   * Used to set the image base address of the module.
   */
  private final CSaveFormattedField m_imageBaseAddr = new CHelpSaveFormattedField(
      new DefaultFormatterFactory(new CHexFormatter(8)), new CRelocatedAddressHelp());

  /**
   * Used to display the name of the selected debugger in address space modules.
   */
  private final JLabel m_debuggerName = new JLabel();

  /**
   * Used to select a debugger for global modules.
   */
  private final CDebuggerComboBox m_debuggerCombo;

  /**
   * Updates the GUI on relevant changes in the address space.
   */
  private final InternalAddressSpaceListener m_addressSpaceListener =
      new InternalAddressSpaceListener();

  private final InternalAddressSpaceConfigurationListener m_addressSpaceConfigurationListener =
      new InternalAddressSpaceConfigurationListener();

  private final InternalAddressSpaceContentListener m_addressSpaceContentListener =
      new InternalAddressSpaceContentListener();

  /**
   * Updates the node on important changes in the represented module.
   */
  private final InternalModuleListener m_listener = new InternalModuleListener();

  /**
   * Listeners that are notified about changes in the debugger configuration.
   */
  private final ListenerProvider<IDebuggerChooserPanelListener> m_listeners =
      new ListenerProvider<IDebuggerChooserPanelListener>();

  /**
   * Creates a new panel object.
   * 
   * @param addressSpace Address space the module belongs to. This argument can be null.
   * @param module Module whose debugger is configured.
   * @param container Provides the available debuggers.
   */
  public CDebuggerChooserPanel(final INaviAddressSpace addressSpace, final INaviModule module,
      final IDebuggerContainer container) {
    super(new BorderLayout());

    m_addressSpace = addressSpace;
    m_module = module;

    final JPanel debuggerChooserPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    debuggerChooserPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

    final JPanel debuggerPanel = new JPanel(new BorderLayout());

    if (addressSpace == null) {
      final JLabel debuggerNameLabel = new JLabel("Debugger" + ":");

      debuggerNameLabel.setPreferredSize(new Dimension(170, 25));
      m_debuggerCombo = new CDebuggerComboBox(new CDebuggerComboModel(container));
      m_debuggerCombo.setBorder(new EmptyBorder(0, 5, 0, 0));

      updateDebuggersComboBox();

      debuggerPanel.add(debuggerNameLabel, BorderLayout.WEST);
      debuggerPanel.add(m_debuggerCombo, BorderLayout.CENTER);

      m_imageBaseAddr.setText(module.getConfiguration().getImageBase().toHexString());
    } else {
      m_debuggerCombo = null;
      final JLabel debuggerNameLabel = new JLabel("Name");

      debuggerNameLabel.setPreferredSize(new Dimension(170, 25));
      m_debuggerName.setBorder(new EmptyBorder(0, 5, 0, 0));

      updateDebuggerLabel();

      debuggerPanel.add(debuggerNameLabel, BorderLayout.WEST);
      debuggerPanel.add(m_debuggerName, BorderLayout.CENTER);

      m_imageBaseAddr.setText(addressSpace.getContent().getImageBase(module).toHexString());
    }

    m_fileBaseAddr.setText(module.getConfiguration().getFileBase().toHexString());

    debuggerChooserPanel.add(debuggerPanel);

    debuggerChooserPanel.add(new CLabeledComponent("Original Base Address" + ":",
        new COriginalAddressHelp(), m_fileBaseAddr), BorderLayout.SOUTH);
    debuggerChooserPanel.add(new CLabeledComponent("Relocated Base Address" + ":",
        new CRelocatedAddressHelp(), m_imageBaseAddr), BorderLayout.SOUTH);

    setBorder(new TitledBorder((addressSpace == null ? "Module" : "Address Space") + " "
        + "Debugger"));

    add(debuggerChooserPanel, BorderLayout.CENTER);

    final UpdateListener updateListener = new UpdateListener();

    m_fileBaseAddr.getDocument().addDocumentListener(updateListener);
    m_imageBaseAddr.getDocument().addDocumentListener(updateListener);

    if (m_debuggerCombo != null) {
      m_debuggerCombo.addActionListener(updateListener);
    }

    m_module.addListener(m_listener);

    if (m_addressSpace != null) {
      m_addressSpace.addListener(m_addressSpaceListener);
      m_addressSpace.getConfiguration().addListener(m_addressSpaceConfigurationListener);
    }
  }

  /**
   * Determines whether the entered file base is different from the image base stored in the model.
   * 
   * @return True, if the file base changed.
   */
  private boolean isFileBaseModified() {
    final String fileBaseText = getFileBase();

    final boolean fileBaseChanged =
        "".equals(fileBaseText)
            || !new CAddress(Convert.hexStringToLong(fileBaseText)).equals(m_module
                .getConfiguration().getFileBase());

    return fileBaseChanged;
  }

  /**
   * Determines whether the entered image base is different from the image base stored in the model.
   * 
   * @return True, if the image base changed.
   */
  private boolean isImageBaseModified() {
    final String imageBaseText = getImageBase();

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
   * Updates the debugger label in case the module is local to an address space.
   */
  private void updateDebuggerLabel() {
    final DebuggerTemplate debugger = m_addressSpace.getConfiguration().getDebuggerTemplate();
    m_debuggerName.setText(debugger == null ? "-" : debugger.getName());
  }

  /**
   * Updates the debugger combobox in case the module is a global module.
   */
  private void updateDebuggersComboBox() {
    if (m_debuggerCombo != null) {
      m_debuggerCombo.setSelectedDebugger(m_module.getConfiguration().getDebuggerTemplate());
    }
  }

  /**
   * Updates the backgrounds in the save fields.
   */
  private void updateSaveFields() {
    m_imageBaseAddr.setModified(isImageBaseModified());
    m_fileBaseAddr.setModified(isFileBaseModified());

    if (m_debuggerCombo != null) {
      m_debuggerCombo.setModified(getSelectedDebugger() != m_module.getConfiguration()
          .getDebuggerTemplate());
    }
  }

  /**
   * Adds a listener that is notified about changes in the panel.
   * 
   * @param listener The listener object to add.
   */
  public void addListener(final IDebuggerChooserPanelListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    if (m_addressSpace != null) {
      m_addressSpace.removeListener(m_addressSpaceListener);
      m_addressSpace.getConfiguration().removeListener(m_addressSpaceConfigurationListener);
    }
  }

  /**
   * Returns the file base entered by the user.
   * 
   * @return The file base entered by the user.
   */
  public String getFileBase() {
    return m_fileBaseAddr.getText();
  }

  /**
   * Returns the image base entered by the user.
   * 
   * @return The image base entered by the user.
   */
  public String getImageBase() {
    return m_imageBaseAddr.getText();
  }

  /**
   * Returns the debugger selected by the user.
   * 
   * @return The debugger selected by the user.
   */
  public DebuggerTemplate getSelectedDebugger() {
    return m_debuggerCombo == null ? null : m_debuggerCombo.getSelectedDebugger();
  }

  /**
   * Removes a listener object from the panel.
   * 
   * @param listener The listener object to remove.
   */
  public void removeListener(final IDebuggerChooserPanelListener listener) {
    m_listeners.removeListener(listener);
  }

  private class InternalAddressSpaceConfigurationListener extends
      CAddressSpaceConfigurationListenerAdapter {
    @Override
    public void changedDebugger(final INaviAddressSpace addressSpace,
        final DebuggerTemplate debugger) {
      updateDebuggerLabel();

      updateSaveFields();
    }
  }

  private class InternalAddressSpaceContentListener implements IAddressSpaceContentListener {
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
      if (module == m_module) {
        m_imageBaseAddr.setText(address.toHexString());

        updateSaveFields();
      }
    }

    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
    }
  }

  /**
   * Updates the GUI on relevant changes in the address space.
   */
  private class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_addressSpaceContentListener);
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      addressSpace.getContent().addListener(m_addressSpaceContentListener);
    }
  }

  /**
   * Updates the GUI on relevant changes in the module.
   */
  private class InternalModuleListener extends CModuleListenerAdapter {
    @Override
    public void changedDebuggerTemplate(final INaviModule module, final DebuggerTemplate template) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          updateDebuggersComboBox();

          updateSaveFields();
        }
      }.invokeLater();
    }

    @Override
    public void changedFileBase(final INaviModule module, final IAddress fileBase) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          m_fileBaseAddr.setText(fileBase.toHexString());

          updateSaveFields();
        }
      }.invokeLater();
    }

    @Override
    public void changedImageBase(final INaviModule module, final IAddress imageBase) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          m_imageBaseAddr.setText(imageBase.toHexString());

          updateSaveFields();
        }
      }.invokeLater();
    }

    @Override
    public void loadedModule(final INaviModule module) {
      new SwingInvoker() {
        @Override
        protected void operation() {
          updateDebuggersComboBox();
        }
      }.invokeLater();
    }
  }

  /**
   * Listener used to update the Save button on input changes.
   */
  private class UpdateListener implements DocumentListener, ActionListener {
    /**
     * Notifies attached listeners that the debugger configuration changed.
     */
    private void notifyListeners() {
      for (final IDebuggerChooserPanelListener listener : m_listeners) {
        try {
          listener.inputChanged();
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      updateSaveFields();

      notifyListeners();
    }

    @Override
    public void changedUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyListeners();
    }

    @Override
    public void insertUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyListeners();
    }

    @Override
    public void removeUpdate(final DocumentEvent event) {
      updateSaveFields();

      notifyListeners();
    }
  }
}
