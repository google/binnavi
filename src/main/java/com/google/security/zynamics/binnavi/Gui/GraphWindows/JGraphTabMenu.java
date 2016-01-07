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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionClose;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionCloseOthers;


/**
 * Context menu of graph window tabs.
 */
public final class JGraphTabMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -7308594634763512556L;

  /**
   * Creates a new graph tab menu.
   *
   * @param tab The tabbed pane that was clicked.
   * @param child The panel whose tab component was clicked.
   */
  public JGraphTabMenu(final JGraphTab tab, final CGraphPanel child) {
    add(new JMenuItem(new CActionClose(child)));
    add(new JMenuItem(new CActionCloseOthers(tab, child)));
  }
}
