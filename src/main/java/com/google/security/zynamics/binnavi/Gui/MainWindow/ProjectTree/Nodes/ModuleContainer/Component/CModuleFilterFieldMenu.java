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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component;

import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.ModuleContainer.Component.Actions.CFilterViewsAction;


/**
 * Context menu for module filter fields.
 */
public final class CModuleFilterFieldMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -3680577198646886823L;

  /**
   * Creates a new menu object.
   * 
   * @param filterField The filter field the context menu belongs to.
   */
  public CModuleFilterFieldMenu(final JTextField filterField) {
    add(new CFilterViewsAction(filterField));
  }
}
