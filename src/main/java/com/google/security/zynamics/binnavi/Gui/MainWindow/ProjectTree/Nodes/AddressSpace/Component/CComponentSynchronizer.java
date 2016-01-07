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



import java.util.Date;

import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Gui.DebuggerComboBox.CDebuggerComboBox;
import com.google.security.zynamics.binnavi.Gui.StandardEditPanel.CStandardEditPanel;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.INaviAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceConfigurationListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceContent;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpaceListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceConfigurationListener;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceContentListener;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.IAddressSpaceListener;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.gui.SwingInvoker;



/**
 * Synchronizes an address space component with an address space.
 */
public final class CComponentSynchronizer {
  /**
   * The synchronized component.
   */
  private final CAddressSpaceNodeComponent m_component;

  /**
   * The synchronized address space.
   */
  private final INaviAddressSpace m_addressSpace;

  /**
   * The standard edit panel for modifying the address space.
   */
  private final CStandardEditPanel m_stdEditPanel;

  /**
   * The combobox for selecting the address space debugger.
   */
  private final CDebuggerComboBox m_debuggerCombo;

  /**
   * The border of the component.
   */
  private final TitledBorder m_titledBorder;

  /**
   * Listener that updates the GUI on relevant address space changes.
   */
  private final IAddressSpaceListener m_addressSpaceListener = new InternalAddressSpaceListener();

  private final IAddressSpaceConfigurationListener m_addressSpaceConfigurationListener =
      new InternalAddressSpaceConfigurationListener();

  private final IAddressSpaceContentListener m_addressSpaceContentListener =
      new InternalAddressSpaceContentListener();

  /**
   * Creates a new synchronizer object.
   * 
   * @param component The synchronized component.
   * @param addressSpace The synchronized address space.
   * @param stdEditPanel The standard edit panel for modifying the address space.
   * @param debuggerCombo The combobox for selecting the address space debugger.
   * @param titledBorder The border of the component.
   */
  public CComponentSynchronizer(final CAddressSpaceNodeComponent component,
      final INaviAddressSpace addressSpace, final CStandardEditPanel stdEditPanel,
      final CDebuggerComboBox debuggerCombo, final TitledBorder titledBorder) {
    m_component = component;
    m_addressSpace = addressSpace;
    m_stdEditPanel = stdEditPanel;
    m_debuggerCombo = debuggerCombo;
    m_titledBorder = titledBorder;

    addressSpace.addListener(m_addressSpaceListener);
    addressSpace.getConfiguration().addListener(m_addressSpaceConfigurationListener);

    if (addressSpace.isLoaded()) {
      addressSpace.getContent().addListener(m_addressSpaceContentListener);
    }

    m_titledBorder.setTitle(getBorderText());
  }

  /**
   * Creates the text of the border that displays the number of modules in the address space.
   * 
   * @return The created border text.
   */
  private String getBorderText() {
    return String.format("%d Modules in Address Space '%s'", m_addressSpace.getModuleCount(),
        m_addressSpace.getConfiguration().getName());
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    if (m_addressSpace.isLoaded()) {
      m_addressSpace.getContent().removeListener(m_addressSpaceContentListener);
    }

    m_addressSpace.removeListener(m_addressSpaceListener);
    m_addressSpace.getConfiguration().removeListener(m_addressSpaceConfigurationListener);
  }

  private class InternalAddressSpaceConfigurationListener extends
      CAddressSpaceConfigurationListenerAdapter {
    @Override
    public void changedDebugger(final INaviAddressSpace addressSpace,
        final DebuggerTemplate debugger) {
      m_debuggerCombo.setSelectedDebugger(debugger);
    }

    @Override
    public void changedDescription(final INaviAddressSpace addressSpace, final String description) {
      new SwingInvoker() {
        // Do not update the GUI from a non GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setDescription(description);
        }
      }.invokeLater();
    }

    @Override
    public void changedModificationDate(final CAddressSpace addressSpace, final Date date) {
      new SwingInvoker() {
        // Do not update the GUI from a non GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setModificationDate(date);
        }
      }.invokeLater();
    }

    @Override
    public void changedName(final INaviAddressSpace addressSpace, final String name) {
      new SwingInvoker() {
        // Do not update the GUI from a non GUI thread
        @Override
        protected void operation() {
          m_stdEditPanel.setNameString(name);
        }
      }.invokeLater();
    }
  }

  private final class InternalAddressSpaceContentListener implements IAddressSpaceContentListener {
    @Override
    public void addedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      m_titledBorder.setTitle(getBorderText());

      m_component.updateUI();
    }

    @Override
    public void changedImageBase(final INaviAddressSpace addressSpace, final INaviModule module,
        final IAddress address) {
    }

    @Override
    public void removedModule(final INaviAddressSpace addressSpace, final INaviModule module) {
      m_titledBorder.setTitle(getBorderText());

      m_component.updateUI();
    }
  }

  /**
   * Updates the GUI on relevant events in the address space.
   */
  private final class InternalAddressSpaceListener extends CAddressSpaceListenerAdapter {
    @Override
    public void closed(final INaviAddressSpace addressSpace, final CAddressSpaceContent content) {
      content.removeListener(m_addressSpaceContentListener);
    }

    @Override
    public void loaded(final INaviAddressSpace addressSpace) {
      addressSpace.getContent().addListener(m_addressSpaceContentListener);
    }
  }

}
