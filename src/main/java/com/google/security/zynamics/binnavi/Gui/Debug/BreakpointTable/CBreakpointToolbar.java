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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CDisableAllAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CDisableAllViewAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CEnableAllAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CEnableAllViewAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CRemoveAllAction;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.Actions.CRemoveAllViewAction;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Toolbar class for the breakpoint panel.
 */
public final class CBreakpointToolbar extends JToolBar {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3859654765731117058L;

  /**
   * Creates a new breakpoint toolbar.
   *
   * @param parent Parent window used for dialogs.
   * @param debuggerProvider Provides the debuggers where breakpoints can be set.
   * @param view View that is shown in the window the panel belongs to.
   */
  public CBreakpointToolbar(
      final JFrame parent, final BackEndDebuggerProvider debuggerProvider, final INaviView view) {
    Preconditions.checkNotNull(parent, "IE01341: Parent argument can not be null");
    Preconditions.checkNotNull(
        debuggerProvider, "IE01342: Debugger provider argument can not be null");
    Preconditions.checkNotNull(view, "IE01343: View argument can not be null");

    setFloatable(false);

    createAndAddButtonToToolbar(new CRemoveAllAction(parent, debuggerProvider),
        "data/deleteallbreakpoints_up.png", "data/deleteallbreakpoints_hover.png",
        "data/deleteallbreakpoints_down.png");
    createAndAddButtonToToolbar(new CDisableAllAction(debuggerProvider),
        "data/disableallbreakpoints_up.png", "data/disableallbreakpoints_hover.png",
        "data/disableallbreakpoints_down.png");
    createAndAddButtonToToolbar(new CEnableAllAction(debuggerProvider),
        "data/enableallbreakpoints_up.png", "data/enableallbreakpoints_hover.png",
        "data/enableallbreakpoints_down.png");
    addSeparator();
    createAndAddButtonToToolbar(new CRemoveAllViewAction(parent, debuggerProvider, view),
        "data/deleteviewbreakpoints_up.png", "data/deleteviewbreakpoints_hover.png",
        "data/deleteviewbreakpoints_up.png");
    createAndAddButtonToToolbar(new CDisableAllViewAction(debuggerProvider, view),
        "data/disableviewbreakpoints_up.png", "data/disableviewbreakpoints_hover.png",
        "data/disableviewbreakpoints_down.png");
    createAndAddButtonToToolbar(new CEnableAllViewAction(debuggerProvider, view),
        "data/enableviewbreakpoints_up.png", "data/enableviewbreakpoints_hover.png",
        "data/enableviewbreakpoints_down.png");
  }

  /**
   * Creates a new button and adds it to the toolbar.
   *
   * @param action The action object to execute when the button is clicked.
   * @param defaultIconPath Path to the icon that is shown on the button.
   * @param rolloverIconPath Path to the rollover icon of the button.
   * @param pressedIconPath Path to the icon that is shown when the button is pressed.
   *
   * @return The created button.
   */
  private JButton createAndAddButtonToToolbar(final AbstractAction action,
      final String defaultIconPath, final String rolloverIconPath, final String pressedIconPath) {
    final JButton button = add(CActionProxy.proxy(action));
    button.setBorder(new EmptyBorder(0, 0, 0, 0));

    button.setIcon(new ImageIcon(CMain.class.getResource(defaultIconPath)));
    button.setRolloverIcon(new ImageIcon(CMain.class.getResource(rolloverIconPath)));
    button.setPressedIcon(new ImageIcon(CMain.class.getResource(pressedIconPath)));

    return button;
  }
}
