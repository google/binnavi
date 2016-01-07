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
package com.google.security.zynamics.binnavi.Gui.GraphSettings;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;

import java.awt.GridLayout;

import javax.swing.JComboBox;
import javax.swing.border.TitledBorder;

/**
 * Settings panel where disassembly settings can be configured.
 */
public final class CDisassemblyPanel extends CAbstractSettingsPanel {
  /**
   * Used to switch simplified memory access on/off.
   */
  private final JComboBox<String> m_cbSimplifiedMemoryAccess = new JComboBox<String>();

  /**
   * Creates a new circular settings panel.
   *
   * @param settings Settings object that provides the graph settings to display.
   */
  public CDisassemblyPanel(final ZyGraphViewSettings settings) {
    super(new GridLayout(1, 1));

    setBorder(new TitledBorder("Disassembly Settings"));

    Preconditions.checkNotNull(settings, "IE00666: Settings argument can not be null");

    CSettingsPanelBuilder.addComboBox(this, m_cbSimplifiedMemoryAccess,
        "Simplified Variable Access" + ":",
            "Simplifies variable access instructions (example: 'mov eax, [esp + var_4]' "
            + "is turned into 'mov eax, var_4)",
        settings.getDisplaySettings().getSimplifiedVariableAccess());
  }

  @Override
  public boolean updateSettings(final ZyGraphViewSettings settings) {
    settings.getDisplaySettings().setSimplifiedVariableAccess(
        m_cbSimplifiedMemoryAccess.getSelectedIndex() == 0);

    return false;
  }
}
