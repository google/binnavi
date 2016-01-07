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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

/**
 * Handles the creation of popup menus for the MemberTable.
 */
public class MemberTableMouseListener extends MouseAdapter {

  private final MemberTable table;
  
  public MemberTableMouseListener(final MemberTable table) {
    this.table = table;
  }
  
  /**
   * An action to append an empty row.
   */
  private class AddMemberAction extends AbstractAction {

    public AddMemberAction() {
      super("Add member");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      table.getModel().addRow(new MemberTableRowData());
    }
  }

  /**
   * An action to insert an empty row before an existing row.
   */
  private class InsertMemberBeforeAction extends AbstractAction {

    private final int rowIndex;
    
    public InsertMemberBeforeAction(final int rowIndex, final String memberName) {
      super("Insert member before " + memberName);
      this.rowIndex = rowIndex;
    }
    
    @Override
    public void actionPerformed(final ActionEvent event) {
      table.getModel().addRow(rowIndex);
    }
  }
  
  /**
   * An action to insert an empty row after an existing row.
   */
  private class InsertMemberAfterAction extends AbstractAction {
   
    private final int rowIndex;
    
    public InsertMemberAfterAction(final int rowIndex, final String memberName) {
      super("Insert member after " + memberName);
      this.rowIndex = rowIndex;
    }
    
    @Override
    public void actionPerformed(final ActionEvent event) {
      if (rowIndex + 1 == table.getModel().getRowCount()) {
        table.getModel().addRow(new MemberTableRowData());
      } else {
        table.getModel().addRow(rowIndex + 1);
      }
    }
  }
  
  /**
   * An action to delete a row.
   */
  private class DeleteMemberAction extends AbstractAction {

    private final int rowIndex;
    
    public DeleteMemberAction(final int rowIndex, String memberName) {
      super("Delete member " + memberName);
      this.rowIndex = rowIndex;
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      table.getModel().deleteRow(rowIndex);
    }
  }
  
  @Override
  public void mousePressed(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      showPopup(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      showPopup(event);
    }
  }
  
  private void showPopup(final MouseEvent event) {
    final JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.add(new AddMemberAction());
    final int selectedRow = table.getSelectedRow();
    if (selectedRow != -1) {
      final MemberTableRowData row = table.getModel().getRow(selectedRow);
      popupMenu.add(new InsertMemberBeforeAction(selectedRow, row.getName()));
      popupMenu.add(new InsertMemberAfterAction(selectedRow, row.getName()));
      popupMenu.add(new DeleteMemberAction(selectedRow, row.getName()));
    }
    popupMenu.show(event.getComponent(), event.getX(), event.getY());
  }
}