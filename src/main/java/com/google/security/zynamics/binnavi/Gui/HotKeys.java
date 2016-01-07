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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Gui.HotkeyDialog.HotKey;

public class HotKeys {

  public static HotKey CONTEXT_HELP_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F1,
      InputEvent.CTRL_DOWN_MASK), "Invokes the context sensitive help.");
  public static HotKey HELP_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),
      "Shows the contents of the associated help file.");
  public static HotKey ADDRESS_SPACE_SELECTION_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK),
      "Add a new address space with the default name to the current selected project.");
  public static HotKey CLOSE_DATABASE_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_D,
      InputEvent.CTRL_DOWN_MASK), "Close the currently selected database.");
  public static HotKey CREATE_PROJECT_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_N,
      InputEvent.CTRL_DOWN_MASK), "Create a new project with the default settings.");
  public static HotKey IMPORT_MODULE_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_I,
      InputEvent.CTRL_DOWN_MASK), "Import a module into BinNavi.");
  public static HotKey INITIALIZE_MODULE_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK), "Initialize the currently selected module(s).");
  public static HotKey OPEN_DATABASE_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Open the currently selected database.");
  public static HotKey RELOAD_PLUGINS_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
      "Reload the plugins available to BinNavi.");
  public static HotKey REFRESH_RAW_MODULES_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_F5, 0), "Refresh the raw modules which are in the database.");
  public static HotKey EXIT_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
      InputEvent.CTRL_DOWN_MASK), "Exit BinNavi.");
  public static HotKey LOAD_NEW_WINDOW_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_N,
      InputEvent.CTRL_DOWN_MASK), "Show in new window.");
  public static HotKey LOAD_LAST_WINDOW_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_L,
      InputEvent.CTRL_DOWN_MASK), "Show in last window.");

  public static ImmutableList<HotKey> mainWindowHotKeys = ImmutableList.of(CONTEXT_HELP_HK,
      HELP_HK, ADDRESS_SPACE_SELECTION_HK, CLOSE_DATABASE_HK, CREATE_PROJECT_HK, IMPORT_MODULE_HK,
      INITIALIZE_MODULE_ACCELERATOR_KEY, OPEN_DATABASE_ACCELERATOR_KEY,
      RELOAD_PLUGINS_ACCELERATOR_KEY, REFRESH_RAW_MODULES_ACCELERATOR_KEY, EXIT_ACCELERATOR_KEY,
      LOAD_LAST_WINDOW_HK, LOAD_NEW_WINDOW_HK);

  /**
   * Hotkeys for the database settings dialog.
   */
  public static HotKey DATABASE_SETTINGS_TEST_CONNECTION_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), "Test the current visible database connection.");
  public static HotKey DATABASE_SETTINGS_SAVE_CONNECTION_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), "Save the current visible database connection.");

  public static ImmutableList<HotKey> dbHotKeys = ImmutableList.of(
      DATABASE_SETTINGS_SAVE_CONNECTION_KEY, DATABASE_SETTINGS_TEST_CONNECTION_KEY);

  /**
   * Hotkeys for the debugger.
   */
  public static HotKey DEBUGGER_SINGLE_STEP_KEY = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
      0), "Single step");
  public static HotKey DEBUGGER_STEP_OVER_KEY = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "Step over");
  public static HotKey DEBUGGER_STEP_BLOCK_KEY = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F7,
      0), "Step block");
  public static HotKey DEBUGGER_RESUME_KEY = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
      "Resume");

  public static ImmutableList<HotKey> debuggerHotKeys = ImmutableList.of(DEBUGGER_RESUME_KEY,
      DEBUGGER_SINGLE_STEP_KEY, DEBUGGER_STEP_BLOCK_KEY, DEBUGGER_STEP_OVER_KEY);

  /**
   * HotKeys for the graph window.
   */
  public static HotKey GRAPH_AUTOMATIC_LAYOUT_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_A,
      InputEvent.ALT_DOWN_MASK), "Toggle the automatic layout of the graph.");
  public static HotKey GRAPH_GOTO_ADDRESS_FIELD_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_G, InputEvent.ALT_DOWN_MASK), "Goto address search field.");
  public static HotKey GRAPH_SEARCHFIELD_FOCUS_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), "Focus the search field");
  public static HotKey GRAPH_SEARCH_NEXT_KEY = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
      0), "Center the next search hit.");
  public static HotKey GRAPH_SEARCH_NEXT_ZOOM_KEY =
      new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK, false),
          "Center the next search hit and focus");
  public static HotKey GRAPH_SEARCH_PREVIOUS_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK, false), "Center the previous search hit.");
  public static HotKey GRAPH_SEARCH_PREVIOUS_ZOOM_KEY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, false),
      "Center the previous search hit and focus.");
  public static HotKey GRAPH_CHANGE_VIEW_DESCRIPTION_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK), "Change the description of the view.");
  public static HotKey GRAPH_CLOSE_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
      InputEvent.CTRL_DOWN_MASK), "Close the view.");
  public static HotKey GRAPH_DELETE_SELECTED_NODES_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_DELETE, 0), "Delete the currently selected nodes.");
  public static HotKey GRAPH_DELETE_SELECTED_NODES_KEEP_EDGES_HK = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK),
      "Delete the currently selected nodes and keep the associated edges");
  public static HotKey GRAPH_PRINT_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_P,
      InputEvent.CTRL_DOWN_MASK), "Opens the print dialog for printing the current graph.");
  public static HotKey GRAPH_PROXIMITY_BROWSING_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK),
      "Toggles the proximity browsing mode for the graph.");
  public static HotKey GRAPH_REDO_SELECTION_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
      InputEvent.CTRL_DOWN_MASK), "Redo the last selection of nodes in the graph.");
  public static HotKey GRAPH_SAVE_VIEW_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_S,
      InputEvent.CTRL_DOWN_MASK), "Saves the current view.");
  public static HotKey GRAPH_UNDO_SELECTION_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
      InputEvent.CTRL_DOWN_MASK), "Undo the last selection of nodes in the graph.");
  public static HotKey GRAPH_GROUP_SELECTION_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_G,
      InputEvent.CTRL_DOWN_MASK), "Groups the currently selected nodes into a group node.");
  public static HotKey GRAPH_SHOW_HOTKEYS_ACCELERATOR_KEY = new HotKey(KeyStroke.getKeyStroke('?'),
      "Shows the dialog for all available hot keys.");
  public static HotKey GRAPH_SWITCH_TO_DEBUG_PERSPECTIVE_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_D, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
      "Switch to the debug perspective.");
  public static HotKey GRAPH_SWITCH_TO_STANDARD_PERSPECTIVE_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
      "Switch to the standard perspective.");
  public static HotKey GRAPH_TOGGLE_SELECTED_GROUPS_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_PERIOD, InputEvent.CTRL_DOWN_MASK),
      "Toggles the expansion state of the currently selected group node.");
  public static HotKey GRAPH_UNGROUP_SELECTED_GROUPS_HK = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK),
      "Ungroups all nodes from the currently selected group node.");

  public static ImmutableList<HotKey> graphHotKeys = ImmutableList.of(GRAPH_AUTOMATIC_LAYOUT_HK,
      GRAPH_GOTO_ADDRESS_FIELD_KEY, GRAPH_SEARCHFIELD_FOCUS_KEY, GRAPH_SEARCH_NEXT_KEY,
      GRAPH_SEARCH_NEXT_ZOOM_KEY, GRAPH_SEARCH_PREVIOUS_KEY, GRAPH_SEARCH_PREVIOUS_ZOOM_KEY,
      GRAPH_CHANGE_VIEW_DESCRIPTION_HK, GRAPH_CLOSE_HK, GRAPH_DELETE_SELECTED_NODES_HK,
      GRAPH_DELETE_SELECTED_NODES_KEEP_EDGES_HK, GRAPH_PRINT_HK, GRAPH_PROXIMITY_BROWSING_HK,
      GRAPH_REDO_SELECTION_HK, GRAPH_SAVE_VIEW_HK, GRAPH_UNDO_SELECTION_HK,
      GRAPH_GROUP_SELECTION_HK, GRAPH_SWITCH_TO_DEBUG_PERSPECTIVE_HK,
      GRAPH_SWITCH_TO_STANDARD_PERSPECTIVE_HK, GRAPH_TOGGLE_SELECTED_GROUPS_HK,
      GRAPH_UNGROUP_SELECTED_GROUPS_HK);

  /**
   * Generic for more then one place.
   */
  public static HotKey DELETE_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
      "Delete");
  public static HotKey APPLY_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
      InputEvent.CTRL_DOWN_MASK), "Apply");
  public static HotKey SEARCH_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F,
      InputEvent.CTRL_DOWN_MASK), "Search");
  public static HotKey GOTO_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_G,
      InputEvent.ALT_DOWN_MASK, true), "Goto");
  public static HotKey LOAD_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_L,
      InputEvent.CTRL_DOWN_MASK), "Load");
  public static HotKey ESCAPE_HK = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false),
      "Exit the current open dialog without committing the last unsaved changes.");

  public static ImmutableList<HotKey> genericHotKeys = ImmutableList.of(DELETE_HK, APPLY_HK,
      SEARCH_HK, GOTO_HK, LOAD_HK, ESCAPE_HK);

  /**
   * Graph toolbar hotkeys.
   */
  public static HotKey GRAPH_TOOLBAR_ZOOM_IN = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0),
      "Zoom in the currently visible graph.");
  public static HotKey GRAPH_TOOLBAR_ZOOM_OUT = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "Zoom out the currently visible graph.");
  public static HotKey GRAPH_TOOLBAR_ZOOM_SELECTED = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_S, 0), "Zoom to the currently selected nodes.");
  public static HotKey GRAPH_TOOLBAR_ZOOM_FIT = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_M, 0),
      "Zoom the currently visible graph such that it fits the window.");
  public static HotKey GRAPH_TOOLBAR_TOGGLE_MAGNIFY = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_Z, 0), "Toggle the magnification mode.");
  public static HotKey GRAPH_TOOLBAR_FREEZE = new HotKey(KeyStroke.getKeyStroke(KeyEvent.VK_F,
      InputEvent.ALT_DOWN_MASK),
      "Freeze the currently visible graph such that node selection does not change the layout.");
  public static HotKey GRAPH_TOOLBAR_CIRCULAR_LAYOUT = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK), "Switch to circular layout mode.");
  public static HotKey GRAPH_TOOLBAR_ORTHOGONAL_LAYOUT = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK), "Switch to orthogonal layout mode.");
  public static HotKey GRAPH_TOOLBAR_HIERARCHIC_LAYOUT = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK), "Switch to hierachic layout mode.");
  public static HotKey GRAPH_TOOLBAR_DELETE_SELECTED = new HotKey(KeyStroke.getKeyStroke(
      KeyEvent.VK_DELETE, 0), "Delete the currently selected nodes including edges.");
  public static HotKey GRAPH_TOOLBAR_DELETE_SELECTED_KEEP_EDGES = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK),
      "Delete the currently selected nodes excluding edges.");

  public static ImmutableList<HotKey> graphToolbarHotKeys = ImmutableList.of(GRAPH_TOOLBAR_ZOOM_IN,
      GRAPH_TOOLBAR_ZOOM_OUT, GRAPH_TOOLBAR_ZOOM_SELECTED, GRAPH_TOOLBAR_ZOOM_FIT,
      GRAPH_TOOLBAR_TOGGLE_MAGNIFY, GRAPH_TOOLBAR_FREEZE, GRAPH_TOOLBAR_CIRCULAR_LAYOUT,
      GRAPH_TOOLBAR_ORTHOGONAL_LAYOUT, GRAPH_TOOLBAR_HIERARCHIC_LAYOUT,
      GRAPH_TOOLBAR_DELETE_SELECTED, GRAPH_TOOLBAR_DELETE_SELECTED_KEEP_EDGES);

  public static HotKey GUI_INITIALIZER_KEY_1 = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), "invalid");
  public static HotKey GUI_INITIALIZER_KEY_2 = new HotKey(
      KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0), "invalid");
}
