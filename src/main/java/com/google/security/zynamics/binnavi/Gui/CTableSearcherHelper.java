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
package com.google.security.zynamics.binnavi.Gui;

import java.awt.Window;

import javax.swing.JOptionPane;
import javax.swing.JTable;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.gui.tables.CTableSearcher;

/**
 * Small helper class that provides common table searching.
 */
public final class CTableSearcherHelper {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTableSearcherHelper() {
  }

  /**
   * Searches through a table.
   * 
   * @param parent Parent window used for dialogs.
   * @param table The table to search through.
   */
  public static void search(final Window parent, final JTable table) {
    Preconditions.checkNotNull(parent, "IE01198: Parent argument can not be null");
    Preconditions.checkNotNull(table, "IE01199: Table argument can not be null");

    final CTableSearcher searcher = new CTableSearcher(parent, "", table, 0);

    String searchText = "";

    do {
      searchText =
          (String) JOptionPane.showInputDialog(parent, "Search", Constants.DEFAULT_WINDOW_TITLE,
              JOptionPane.QUESTION_MESSAGE, null, null, searchText);

      if ((searchText != null) && (searchText.length() > 0) && !searcher.search(searchText)) {
        JOptionPane.showMessageDialog(parent, "Search string not found",
            Constants.DEFAULT_WINDOW_TITLE, JOptionPane.ERROR_MESSAGE);
      }
    } while ((searchText != null) && (searchText.length() > 0));
  }
}
