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
package com.google.security.zynamics.binnavi.Gui.plugins.output.implementations;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.LastDirFileChooser;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;
import com.google.security.zynamics.zylib.io.FileUtils;

import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JTextArea;



/**
 * Contains functions for working with plugin logs.
 */
public final class CLogConsoleFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CLogConsoleFunctions() {
  }

  /**
   * Copies the text of the output log to the clipboard.
   * 
   * @param area The text area of the output log.
   */
  public static void copy(final JTextArea area) {
    final String selectedText = area.getSelectedText();

    if (selectedText == null) {
      ClipboardHelpers.copyToClipboard(area.getText());
    } else {
      ClipboardHelpers.copyToClipboard(selectedText);
    }
  }

  /**
   * Saves a debug log to a file.
   * 
   * @param parent Parent window used for dialogs.
   * @param text The debug log to save.
   */
  public static void save(final Window parent, final String text) {
    final LastDirFileChooser chooser = new LastDirFileChooser();
    if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
      final File outputFile = chooser.getSelectedFile();

      if (outputFile != null) {
        try {
          FileUtils.writeTextFile(outputFile, text);
        } catch (final IOException e) {
          CUtilityFunctions.logException(e);
        }
      }
    }
  }
}
