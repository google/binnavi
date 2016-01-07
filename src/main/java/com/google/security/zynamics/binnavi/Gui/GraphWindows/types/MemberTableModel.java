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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.types;

import com.google.security.zynamics.binnavi.disassembly.types.BaseType;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Table model that holds all data to describe a set of members when the user creates a new base
 * type.
 */
public class MemberTableModel extends AbstractTableModel {

  private static final String[] COLUMN_HEADERS =
      {"Offset (bytes) / Argument index", "Type", "Name"};

  private final List<MemberTableRowData> model = new ArrayList<MemberTableRowData>();
  private final List<Boolean> validRows = new ArrayList<Boolean>();
  private boolean isOffsetEditable = true;

  public static final int INDEX_COLUMN = 0;
  public static final int TYPE_COLUMN = 1;
  public static final int NAME_COLUMN = 2;

  /**
   * Adds a new row to the table model with the given data.
   *
   * @param rowData The row data to add to the table model.
   */
  public void addRow(final MemberTableRowData rowData) {
    model.add(rowData);
    validRows.add(false);
    fireTableRowsInserted(model.size() - 1, model.size() - 1);
  }

  /**
   * Adds an empty row at the given index. The existing row at the offset and subsequent rows are
   * shifted towards the end of the table model.
   *
   * @param rowIndex The index where to add a new row.
   */
  public void addRow(final int rowIndex) {
    model.add(rowIndex, new MemberTableRowData());
    validRows.add(rowIndex, false);
    fireTableRowsInserted(rowIndex, rowIndex);
  }

  /**
   * Removes the corresponding row from the table model and triggers a GUI update.
   *
   * @param rowIndex The index of the row be removed from the model.
   */
  public void deleteRow(final int rowIndex) {
    model.remove(rowIndex);
    fireTableRowsDeleted(rowIndex, rowIndex);
  }

  @Override
  public Class<?> getColumnClass(final int columndIndex) {
    switch (columndIndex) {
      case INDEX_COLUMN:
        return Integer.class;
      case TYPE_COLUMN:
        return BaseType.class;
      case NAME_COLUMN:
        return String.class;
      default:
        return null;
    }
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return COLUMN_HEADERS[columnIndex];
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final MemberTableRowData element = model.get(rowIndex);
    switch (columnIndex) {
      case INDEX_COLUMN:
        return element.getIndex();
      case TYPE_COLUMN:
        return element.getBaseType();
      case NAME_COLUMN:
        return element.getName();
      default:
        return null;
    }
  }

  public void setOffsetEditable(final boolean value) {
    isOffsetEditable = value;
  }

  @Override
  public void setValueAt(final Object value, final int rowIndex, final int columnIndex) {
    final MemberTableRowData element = model.get(rowIndex);
    switch (columnIndex) {
      case INDEX_COLUMN:
        element.setIndex((Integer) value);
        break;
      case TYPE_COLUMN:
        element.setBaseType((BaseType) value);
        break;
      case NAME_COLUMN:
        element.setName((String) value);
        break;
      default:
        break;
    }
    model.set(rowIndex, element);
    fireTableRowsUpdated(rowIndex, rowIndex);
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return isOffsetEditable ? true : columnIndex != INDEX_COLUMN;
  }

  @Override
  public int getRowCount() {
    return model.size();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_HEADERS.length;
  }

  public MemberTableRowData getRow(final int rowIndex) {
    return model.get(rowIndex);
  }

  /**
   * Marks a row data set as valid or invalid. An invalid row is rendered with a red background
   * color as an indicator for the user that he needs to correct his input.
   *
   * @param rowIndex The index of the row to mark.
   * @param valid True iff the row should be marked as valid.
   */
  public void markRow(final int rowIndex, final boolean valid) {
    validRows.set(rowIndex, valid);
  }

  /**
   * Returns whether the corresponding row represents a valid constellation of member data.
   *
   * @param rowIndex The index of the row.
   * @return True iff the row contains valid member data.
   */
  public boolean isRowValid(final int rowIndex) {
    if (rowIndex >= validRows.size()) {
      return false;
    }
    return validRows.get(rowIndex);
  }
}
