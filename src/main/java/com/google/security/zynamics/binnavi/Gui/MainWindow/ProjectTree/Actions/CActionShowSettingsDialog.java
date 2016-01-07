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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions;



import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CSettingsDialogFunctions;

/**
 * Action class that can be used to display the global settings dialog.
 */
public final class CActionShowSettingsDialog extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -8047118421636870349L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   */
  public CActionShowSettingsDialog(final JFrame parent) {
    super("Settings");

    m_parent = Preconditions.checkNotNull(parent, "IE01848: Parent argument can not be null");

    putValue(MNEMONIC_KEY, (int) "HK_MENU_SHOW_SETTINGS_DIALOG".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CSettingsDialogFunctions.showSettingsDialog(m_parent);
  }
}
