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
import com.google.security.zynamics.binnavi.disassembly.types.TypeMember;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * A table that allows the user to specify all required information to create or update a set of
 * {@link TypeMember} instances. The entries in the table are always sorted by offset. Therefore,
 * all indices into the table model have to be translated from view to model indices.
 */
public class MemberTable extends JTable {

  /**
   * Renders all text left aligned (integers are rendered right aligned by the default renderer) and
   * sets the background color to red if the corresponding row is marked as invalid in the model.
   */
  private class InvalidRowTableCellRenderer extends DefaultTableCellRenderer {

    private final Color defaultBackgroundColor = UIManager.getColor("Table.dropCellBackground");
    private final Color invalidBackgroundColor = new Color(255, 150, 150);

    public InvalidRowTableCellRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table,
        Object value,
        boolean isSelected,
        boolean hasFocus,
        int row,
        int column) {
      final Component renderer =
          super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      final int modelIndex = convertRowIndexToModel(row);
      if (getModel().isRowValid(modelIndex)) {
        renderer.setBackground(defaultBackgroundColor);
      } else {
        renderer.setBackground(invalidBackgroundColor);
      }
      return renderer;
    }
  }

  /**
   * Converts {@link BaseType} instances to their corresponding type name.
   */
  private class BaseTypeCellRenderer extends InvalidRowTableCellRenderer {

    @Override
    public void setValue(final Object value) {
      final BaseType baseType = (BaseType) value;
      setText((baseType == null) ? "" : baseType.getName());
    }
  }

  /**
   * Instantiates a new member table.
   *
   * @param model The member table model to use.
   * @param baseTypes The list of base types in the current type system to be used in the cell
   *        editor of the types column.
   */
  public MemberTable(final MemberTableModel model, final List<BaseType> baseTypes) {
    super(model);
    setSurrendersFocusOnKeystroke(true);
    addMouseListener(new MemberTableMouseListener(this));
    // Base types are edited via a combobox.
    getColumnModel().getColumn(MemberTableModel.TYPE_COLUMN).setCellEditor(new DefaultCellEditor(
        new TypeComboBox(new TypeListModel(baseTypes, new TypeListModel.PrototypesFilter()))));
    // Make sure base types are rendered with their name only, not their toString method.
    getColumnModel().getColumn(MemberTableModel.TYPE_COLUMN)
        .setCellRenderer(new BaseTypeCellRenderer());
    // Force the table to consume all available space in the parent view port so we get click
    // events to trigger the popup menu even if the table is empty.
    setFillsViewportHeight(true);
    // Assign custom renderers that set the background color of rows to red if these rows represent
    // an invalid constellation of members.
    getColumnModel().getColumn(MemberTableModel.NAME_COLUMN)
        .setCellRenderer(new InvalidRowTableCellRenderer());
    getColumnModel().getColumn(MemberTableModel.INDEX_COLUMN)
        .setCellRenderer(new InvalidRowTableCellRenderer());
  }

  @Override
  public MemberTableModel getModel() {
    return (MemberTableModel) super.getModel();
  }
}
