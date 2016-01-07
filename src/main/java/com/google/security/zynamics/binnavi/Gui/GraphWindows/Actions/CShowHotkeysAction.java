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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.HotkeyDialog.CGraphWindowHotkeysDialog;
import com.google.security.zynamics.zylib.gui.GuiHelper;



/**
 * Action class for showing the hotkeys dialog.
 */
public final class CShowHotkeysAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8814709394951577591L;

  /**
   * Parent of the dialog.
   */
  private final JFrame m_parent;

  /**
   * Creates a new action object.
   * 
   * @param parent Parent of the dialog.
   */
  public CShowHotkeysAction(final JFrame parent) {
    super("Show Available Hotkeys");
    m_parent = Preconditions.checkNotNull(parent, "IE02838: parent argument can not be null");
    putValue(ACCELERATOR_KEY, HotKeys.GRAPH_SHOW_HOTKEYS_ACCELERATOR_KEY.getKeyStroke());
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final CGraphWindowHotkeysDialog dlg = new CGraphWindowHotkeysDialog(m_parent);
    GuiHelper.centerChildToParent(m_parent, dlg, true);
    dlg.setVisible(true);
  }
}
