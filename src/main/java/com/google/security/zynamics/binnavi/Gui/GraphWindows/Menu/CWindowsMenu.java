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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IViewSwitcher;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CShowHotkeysAction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CSwitchToDebugView;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CSwitchToStandardView;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Actions.CActionContextHelp;


/**
 * Windows menu of the graph window menu bar.
 */
public final class CWindowsMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3052158107015083660L;

  /**
   * Creates the Window
   *
   * @param parent Parent window used for dialogs.
   * @param viewSwitcher Toggles between the available perspectives of the graph window.
   */
  public CWindowsMenu(final JFrame parent, final IViewSwitcher viewSwitcher) {
    super("Window");

    setMnemonic("HK_MENU_WINDOW".charAt(0));

    add(CActionProxy.proxy(new CSwitchToStandardView(viewSwitcher)));
    add(CActionProxy.proxy(new CSwitchToDebugView(viewSwitcher)));

    addSeparator();

    add(new JMenuItem(new CShowHotkeysAction(parent)));
    add(new JMenuItem(new CActionContextHelp(parent)));
  }
}
