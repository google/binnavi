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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import javax.swing.JPopupMenu;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.COpenFunctionAction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;


/**
 * Context menu of cross references tables.
 */
public final class CCrossReferencesTableMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -9044548806189262326L;

  /**
   * Creates a new menu object.
   *
   * @param parent Window in which the function view is shown.
   * @param container View container that provides the context of the view.
   * @param function Function to be opened.
   */
  public CCrossReferencesTableMenu(
      final CGraphWindow parent, final IViewContainer container, final INaviFunction function) {
    add(new COpenFunctionAction(parent, container, function));
  }
}
