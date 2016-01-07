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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes;

import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;

/**
 * Interface that must be implemented by all nodes that are put into the project tree.
 */
public interface IProjectTreeNode {
  /**
   * Called right before a node is removed from the tree. This gives the node the option to remove
   * listeners and to clean up other resources.
   */
  void dispose();

  /**
   * Called whenever the node is double-clicked. This gives the node the option to do stuff besides
   * expanding itself.
   */
  void doubleClicked();

  /**
   * Returns the component that is shown on the right side of the main window when the node is
   * selected.
   * 
   * @return A valid component.
   */
  Component getComponent();

  /**
   * Returns the main menu to be displayed in the main window when the node is selected.
   * 
   * @return The main menu to be displayed.
   */
  JMenuBar getMainMenu();

  /**
   * Returns the menu that is shown when the node is right-clicked. If this value is null, no menu
   * is displayed.
   * 
   * @return The menu that is shown when the node is right-clicked.
   */
  JPopupMenu getPopupMenu();

}
