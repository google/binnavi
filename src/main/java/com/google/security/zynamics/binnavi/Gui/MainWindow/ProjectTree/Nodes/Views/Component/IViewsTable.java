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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component;

import javax.swing.JTable;

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;


/**
 * Interface for tables that display views.
 */
public interface IViewsTable {
  /**
   * Returns the index of the column that displays the view name.
   * 
   * @return The view name column index.
   */
  int getNameColumn();

  /**
   * Returns the view from the given unsorted index.
   * 
   * @param row The unsorted index.
   * 
   * @return The view on the given unsorted row.
   */
  INaviView getUnsortedView(int row);

  /**
   * Returns itself.
   * 
   * @return The table itself.
   */
  JTable self();
}
