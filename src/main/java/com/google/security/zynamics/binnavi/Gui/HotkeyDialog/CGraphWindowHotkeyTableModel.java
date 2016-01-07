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
package com.google.security.zynamics.binnavi.Gui.HotkeyDialog;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Gui.HotKeys;

/**
 * Table model that drives the table that displays all available graph window hotkeys.
 */
public final class CGraphWindowHotkeyTableModel extends AbstractTableModel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4174902701474843040L;

  /**
   * Names of the columns shown in the model.
   */
  private static final String[] COLUMN_NAMES = {"Hotkey", "Description"};

  /**
   * Data shown in the table.
   */
  private static final ArrayList<Object[]> hotKeys = new ArrayList<Object[]>();

  /**
   * Splits a title string into two cells.
   * 
   * @param titleString The title string to split.
   * 
   * @return The cells for the title string.
   */
  private static Object[] makeTitle(final String titleString) {
    return new Object[] {new CLeftTitle(titleString.substring(0, titleString.length() / 2)),
        new CRightTitle(titleString.substring(titleString.length() / 2))};
  }

  private static void fillTable() {
    hotKeys.clear();
    hotKeys.add(makeTitle("Graph"));
    loadKeys(HotKeys.graphHotKeys);
    hotKeys.add(makeTitle("Graph Misc"));
    hotKeys.add(new Object[] {"Alt-Right Click",
        "Opens instruction comments dialog when clicking on an instruction"});
    hotKeys.add(new Object[] {"Shift+Alt-Right Click",
        "Opens instruction comments dialog when clicking on an instruction"});
    hotKeys.add(new Object[] {"Ctrl+Alt-Right Click",
        "Highlights instruction when clicking on an instruction."});
    hotKeys.add(makeTitle("Graph toolbar"));
    loadKeys(HotKeys.graphToolbarHotKeys);
    hotKeys.add(makeTitle("Graph mouse interaction"));
    hotKeys.add(new Object[] {"Mouse wheel", "Zoom to mouse cursor"});
    hotKeys.add(new Object[] {"Shift-Mouse wheel", "Zoom to selection"});
    hotKeys.add(new Object[] {"Ctrl-Mouse wheel", "Scroll vertically"});
    hotKeys.add(new Object[] {"Ctrl-Alt-Mouse wheel", "Scroll horizontally"});
    hotKeys.add(new Object[] {"Ctrl-Mouse wheel", "Resize the magnifying glass"});
    hotKeys.add(new Object[] {"CTRL-DOUBLE-LEFTCLICK",
        "Opens function when clicking on function nodes"});
    hotKeys.add(new Object[] {"CTRL-DOUBLE-LEFTCLICK",
        "Opens function when clicking on function calls in code nodes"});
    hotKeys.add(new Object[] {"MOUSEWHEEL-Click", "Sets the focus into a node"});
    hotKeys.add(new Object[] {"ALT-LEFTCLICK", "Sets the focus into a node"});
    hotKeys.add(new Object[] {"SHIFT-ARROWKEYS",
        "Select the content of a node once the node has the focus"});
    hotKeys.add(makeTitle("Generic"));
    loadKeys(HotKeys.genericHotKeys);
    hotKeys.add(makeTitle("Debugger"));
    loadKeys(HotKeys.debuggerHotKeys);
    hotKeys.add(makeTitle("Database dialog"));
    loadKeys(HotKeys.dbHotKeys);
    hotKeys.add(makeTitle("Main window"));
    loadKeys(HotKeys.mainWindowHotKeys);
  }

  private static void loadKeys(final ImmutableList<HotKey> list) {
    for (final HotKey currentHotKey : list) {

      final String modifiers =
          currentHotKey.getKeyStroke().getModifiers() != 0 ? KeyEvent
              .getModifiersExText(currentHotKey.getKeyStroke().getModifiers()) + "-" : "";

      hotKeys.add(new Object[] {
          modifiers + KeyEvent.getKeyText(currentHotKey.getKeyStroke().getKeyCode()),
          currentHotKey.getDescription()});
    }
  }

  public CGraphWindowHotkeyTableModel() {
    fillTable();
  }

  @Override
  public int getColumnCount() {
    return COLUMN_NAMES.length;
  }

  @Override
  public String getColumnName(final int index) {
    return COLUMN_NAMES[index];
  }

  @Override
  public int getRowCount() {
    return hotKeys.size();
  }

  @Override
  public Object getValueAt(final int row, final int col) {
    return hotKeys.get(row)[col];
  }
}
