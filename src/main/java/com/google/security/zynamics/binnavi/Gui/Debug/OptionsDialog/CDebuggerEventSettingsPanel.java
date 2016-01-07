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
package com.google.security.zynamics.binnavi.Gui.Debug.OptionsDialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;


/**
 * Defines the content of the debugger event settings panel in the debugger options dialog.
 */
public class CDebuggerEventSettingsPanel extends JPanel {
  private static final long serialVersionUID = -2916323542401386851L;

  private final JCheckBox m_breakOnDllLoadCheckbox;
  private final JCheckBox m_breakOnDllUnloadCheckbox;

  /**
   * Creates a new instance of the debugger events panel.
   */
  public CDebuggerEventSettingsPanel(final DebuggerEventSettings eventSettings) {
    super(new BorderLayout());

    final JPanel innerPanel = new JPanel(new BorderLayout());

    final JPanel componentPanel = new JPanel(new GridLayout(2, 2));

    m_breakOnDllLoadCheckbox = new JCheckBox("Break on module load");
    m_breakOnDllUnloadCheckbox = new JCheckBox("Break on module unload");

    m_breakOnDllLoadCheckbox.setSelected(eventSettings.getBreakOnDllLoad());
    m_breakOnDllUnloadCheckbox.setSelected(eventSettings.getBreakOnDllUnload());

    componentPanel.add(m_breakOnDllLoadCheckbox);
    componentPanel.add(m_breakOnDllUnloadCheckbox);

    innerPanel.add(new JScrollPane(componentPanel), BorderLayout.NORTH);

    add(innerPanel);
  }

  /**
   * Get a copy of the current debug event settings.
   *
   * @return The copy of the debug event settings.
   */
  public DebuggerEventSettings getSettings() {
    return new DebuggerEventSettings(
        m_breakOnDllLoadCheckbox.isSelected(), m_breakOnDllUnloadCheckbox.isSelected());
  }
}
