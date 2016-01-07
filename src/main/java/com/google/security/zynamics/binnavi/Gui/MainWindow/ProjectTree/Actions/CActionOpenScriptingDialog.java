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
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CWindowFunctions;

/**
 * Action class that can be used to display a scripting dialog.
 */
public final class CActionOpenScriptingDialog extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 703083415304140168L;

  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent window used for dialogs.
   */
  public CActionOpenScriptingDialog(final JFrame parent) {
    super("New Scripting Window");

    m_parent = Preconditions.checkNotNull(parent, "IE01846: Parent argument can not be null");

    putValue(MNEMONIC_KEY, (int) "HK_MENU_SCRIPTING".charAt(0));
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    CWindowFunctions.showScriptingDialog(m_parent);
  }
}
