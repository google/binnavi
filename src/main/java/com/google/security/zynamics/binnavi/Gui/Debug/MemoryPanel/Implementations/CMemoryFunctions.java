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
package com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.LastDirFileChooser;
import com.google.security.zynamics.binnavi.Gui.Debug.Goto.CGotoDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.CMemoryViewer;
import com.google.security.zynamics.binnavi.Gui.Debug.SearchMemory.CSearchDialog;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SearchReply;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.memmanager.Memory;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.JHexPanel.IDataProvider;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView.DefinitionStatus;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Implementations of the memory functions available from the memory panel.
 */
public final class CMemoryFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CMemoryFunctions() {
  }

  /**
   * Checks arguments for validity and throws an exception if they are not.
   *
   * @param parent Parent argument to check.
   * @param debugger Debugger argument to check.
   */
  private static void checkArguments(final Window parent, final IDebugger debugger) {
    Preconditions.checkNotNull(parent, "IE01434: Parent argument can not be null");

    Preconditions.checkNotNull(debugger, "IE01433: Debugger argument can not be null");
  }

  /**
   * Checks arguments for validity and throws an exception if they are not.
   *
   * @param parent Parent argument to check.
   * @param debugger Debugger argument to check.
   * @param offset Offset argument to check.
   */
  private static void checkArguments(
      final Window parent, final IDebugger debugger, final IAddress offset) {
    checkArguments(parent, debugger);

    Preconditions.checkNotNull(offset, "IE01432: Offset argument can not be null");
  }

  /**
   * Displays a Save File dialog.
   *
   * @param parent Parent window of the dialog.
   *
   * @return A pair with the return value of the dialog and the selected file.
   */
  private static Pair<Integer, File> showSaveDialog(final JFrame parent) {
    final LastDirFileChooser fileChooser = new LastDirFileChooser();
    fileChooser.setDialogTitle("Save Data to File");
    final int val = fileChooser.showSaveDialog(parent);
    return new Pair<Integer, File>(val, fileChooser.getSelectedFile());
  }

  /**
   * Dumps a given memory section of the target process to a file.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that requests the target process memory.
   * @param dataProvider Collects the requested memory.
   * @param offset Start offset of the section to dump.
   * @param size Number of bytes to dump.
   */
  public static void dumpMemoryRange(final JFrame parent, final IDebugger debugger,
      final IDataProvider dataProvider, final IAddress offset, final int size) {
    checkArguments(parent, debugger, offset);

    final Pair<Integer, File> result = showSaveDialog(parent);

    if (result.first() == JFileChooser.APPROVE_OPTION) {
      loadAll(parent, debugger, offset, size);

      if (dataProvider.hasData(offset.toLong(), size)) {
        try {
          final byte[] data = dataProvider.getData(offset.toLong(), size);

          FileUtils.writeBinaryFile(result.second(), data);

          CMessageBox.showInformation(
              parent, String.format("Memory data was successfully written to %s", result.second()));
        } catch (final IOException e) {
          final String innerMessage = "E00151: " + "Could not write memory data to the target file";
          final String innerDescription = CUtilityFunctions.createDescription(String.format(
              "The memory data of the selected range could not be written to the file '%s'.",
              result.second().getAbsolutePath()), new String[] {
              "You do not have sufficient rights to write to the file.",
              "Another program locks the selected file.", "The disk is full."},
              new String[] {"The memory data was not written to the file."});

          NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
        }
      } else {
        final String innerMessage = "E00152: " + "Could not read memory";
        final String innerDescription = CUtilityFunctions.createDescription(
            "The memory section to be dumped could not be read.",
            new String[] {"There was a problem with the database connection."}, new String[] {
                "The memory data could not be read and the output file was not written."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription);
      }
    }
  }

  /**
   * Sets the caret of a hex control to a given offset.
   *
   * @param model Model that contains the memory viewer where the offset is changed.
   * @param offset The offset to go to.
   * @param focusMemoryWindow True, if the focus should be transferred to the memory view.
   */
  public static void gotoOffset(
      final CDebugPerspectiveModel model, final IAddress offset, final boolean focusMemoryWindow) {
    model.setActiveMemoryAddress(offset, focusMemoryWindow);
  }

  /**
   * Shows the Goto Offset dialog and sets the caret of a hex control to the entered offset.
   *
   * @param parent Parent window used for dialogs.
   * @param view Hex view to focus after the Goto operation.
   * @param model Model that contains the memory viewer where the offset is changed.
   */
  public static void gotoOffset(
      final JFrame parent, final CMemoryViewer view, final CDebugPerspectiveModel model) {
    final IDebugger debugger = model.getCurrentSelectedDebugger();

    if (debugger == null) {
      return;
    }

    final TargetProcessThread activeThread = debugger.getProcessManager().getActiveThread();

    final Memory memory = debugger.getProcessManager().getMemory();

    final CDefaultMemoryExpressionBinding binding =
        new CDefaultMemoryExpressionBinding(activeThread, memory);

    final CGotoDialog dlg = new CGotoDialog(parent, model.getCurrentSelectedDebugger()
        .getProcessManager().getMemoryMap(), binding, model.getGotoAddress());

    dlg.setVisible(true);

    final IAddress value = dlg.getValue();

    if (value != null) {
      model.setGotoAddress(value);
      model.setActiveMemoryAddress(value, true);

      view.requestFocusInWindow();
    }
  }

  /**
   * Loads all data of a memory section.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that loads the data.
   * @param offset Start of the memory section.
   * @param size Number of bytes to load.
   */
  public static void loadAll(
      final JFrame parent, final IDebugger debugger, final IAddress offset, final int size) {
    checkArguments(parent, debugger, offset);

    final CDumpAllWaiter waiter = new CDumpAllWaiter(debugger, offset, size);

    CProgressDialog.showEndless(parent, "Loading memory" + " ...", waiter);

    if (waiter.getException() != null) {
      CUtilityFunctions.logException(waiter.getException());

      final String innerMessage = "E00078: " + "Could not load memory section";
      final String innerDescription = CUtilityFunctions.createDescription(String.format(
          "The memory section starting at address '%s' could not loaded.", offset.toHexString()),
          new String[] {"There was a problem with the connection to the debug client."},
          new String[] {"The memory data was not loaded."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, waiter.getException());
    }
  }

  /**
   * Shows a Search dialog and searches through the memory of target process afterwards.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that requests the target memory.
   * @param memoryView Memory view where the search result is shown.
   */
  public static void searchMemory(
      final Window parent, final IDebugger debugger, final CMemoryViewer memoryView) {
    checkArguments(parent, debugger);

    Preconditions.checkNotNull(memoryView, "IE01431: Memory view argument can not be null");

    // Show the search dialog
    final CSearchDialog dlg = new CSearchDialog(parent);

    final byte[] data = dlg.getSearchData();

    // Make sure that the user entered data and clicked the OK button
    if (data != null && data.length != 0) {
      final JHexView hexView = memoryView.getHexView();

      final long start = hexView.getCurrentOffset();
      final int size = (int) (hexView.getLastOffset() - hexView.getCurrentOffset());

      final CSearchWaiter waiter = new CSearchWaiter(debugger, new CAddress(start), size, data);

      CProgressDialog.showEndless(parent, "Loading memory" + " ...", waiter);

      hexView.uncolorizeAll();

      if (waiter.getException() == null) {
        final SearchReply reply = waiter.getReply();

        if (reply != null) {

          final IAddress offset = reply.getAddress();

          if (reply.success()) {
            // Make sure that the memory data is actually available
            if (hexView.isEnabled() && hexView.getDefinitionStatus() == DefinitionStatus.DEFINED) {
              // It is not necessary to make sure that the offset is
              // actually part of the currently visible memory range.
              // If it is not, the new memory range is loaded automatically.

              hexView.colorize(5, offset.toLong(), data.length, Color.BLACK, Color.YELLOW);

              hexView.gotoOffset(offset.toLong());
              hexView.requestFocusInWindow();
            }
          } else {
            // Tell the user that the search string was not found
            CMessageBox.showInformation(parent, "The specified search string was not found.");
          }
        }
      } else {
        CUtilityFunctions.logException(waiter.getException());

        final String innerMessage = "E00079: " + "Could not search through memory";
        final String innerDescription = CUtilityFunctions.createDescription(
            "It was not possible to send the search request to the debug client.",
            new String[] {"There was a problem with the connection to the debug client."},
            new String[] {"The search operation could not be started."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, waiter.getException());
      }
    }
  }
}
