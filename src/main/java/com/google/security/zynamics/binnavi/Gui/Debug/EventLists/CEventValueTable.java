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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

/**
 * Table that displays the register values for individual trace events.
 */
public final class CEventValueTable extends JTable {

  /**
   * The raw table model.
   */
  private final CEventValueTableModel m_model;

  /**
   * Creates a new table object.
   *
   * @param model The model of the table.
   */
  public CEventValueTable(final CEventValueTableModel model) {
    super(model);
    m_model = model;
    setRowSorter(new TableRowSorter<CEventValueTableModel>(model));
    setFont(GuiHelper.MONOSPACED_FONT);
  }

  /**
   * Sets the event whose values are displayed in the table.
   *
   * @param event The event or null if no event values should be shown.
   */
  public void setEvent(final ITraceEvent event) {
    m_model.setEvent(event);
  }
}
