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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Actions.CFilterByEdgesAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Actions.CFilterByInstructionAction;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Module.Component.Actions.CFilterByNodesAction;


/**
 * Context menu for text fields used to filter native functions.
 */
public final class CNativeFunctionViewFilterFieldMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6735892611202198739L;

  /**
   * Creates a new context menu object.
   * 
   * @param filterField The filter field for which the menu is created.
   */
  public CNativeFunctionViewFilterFieldMenu(final JTextField filterField) {
    add(new CFilterByInstructionAction(filterField));
    add(new CFilterByNodesAction(filterField));
    add(new CFilterByEdgesAction(filterField));
  }
}
