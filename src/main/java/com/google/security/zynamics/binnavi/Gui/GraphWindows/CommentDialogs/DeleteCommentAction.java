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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

import com.google.common.base.Preconditions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTable;

/**
 * This class is the action performed when a users triggers a delete comment operation in and
 * comment editing table.
 */
public class DeleteCommentAction extends AbstractAction {
  private final JTable table;
  private final int row;
  private final int column;

  public DeleteCommentAction(final JTable table, final int row, final int column) {
    super("Delete Comment");
    this.table = Preconditions.checkNotNull(table, "IE02635: table argument can not be null");
    this.row = Preconditions.checkPositionIndex(row, this.table.getModel().getRowCount());
    this.column = Preconditions.checkPositionIndex(column, this.table.getModel().getColumnCount());
  }

  @Override
  public void actionPerformed(final ActionEvent arg0) {
    if (table.getModel().isCellEditable(row, column)) {
      table.getModel().setValueAt(null, row, column);
    }
  }
}
