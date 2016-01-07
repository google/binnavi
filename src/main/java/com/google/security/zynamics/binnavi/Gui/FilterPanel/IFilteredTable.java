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
package com.google.security.zynamics.binnavi.Gui.FilterPanel;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Interface for all tables that want to be filtered.
 * 
 * @param <T> Type of the elements to filter.
 */
public interface IFilteredTable<T> {
  /**
   * Frees allocated resources.
   */
  void dispose();

  /**
   * Returns the table model.
   * 
   * @return The table model.
   */
  AbstractTableModel getModel();

  /**
   * Returns the raw table model.
   * 
   * @return The raw table model.
   */
  IFilteredTableModel<T> getTreeTableModel();

  /**
   * Returns the table component object.
   * 
   * @return The table component object.
   */
  JTable self();

  /**
   * Enables or disables the table.
   * 
   * @param enabled True, to enable the table. False, to disable it.
   */
  void setEnabled(boolean enabled);
}
