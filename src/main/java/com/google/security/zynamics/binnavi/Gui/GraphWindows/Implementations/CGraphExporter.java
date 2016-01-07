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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.LastDirFileChooser;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.GraphExporters;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Contains methods for exporting graphs into different image formats.
 */
public final class CGraphExporter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphExporter() {
  }

  /**
   * Exports the current view as a PNG image after prompting the user for a filename.
   *
   * @param parent Parent frame that is used to display error messages.
   * @param graph Graph to be exported to a PNG file.
   */
  public static void exportAsPng(final JFrame parent, final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE01735: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01736: Graph argument can not be null");

    final LastDirFileChooser fileChooser = new LastDirFileChooser();
    final int retval = fileChooser.showSaveDialog(parent);
    if (retval == JFileChooser.APPROVE_OPTION) {
      try {
        if (!GraphExporters.exportAllAsPNG(
            graph, fileChooser.getSelectedFile().getAbsolutePath())) {
          throw new IOException("Failed to write the PNG");
        }
      } catch (final IOException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00194: " + "Could not save view to PNG file";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The view '%s' could not be written to the file '%s'.", graph.getViewName(),
            fileChooser.getSelectedFile().getAbsolutePath()),
            new String[] {"There was a problem writing the PNG file."},
            new String[] {"The view was not written to the PNG file."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

  /**
   * Exports the current view as a SVG image after prompting the user for a filename.
   *
   * @param parent Parent frame that is used to display error messages.
   * @param graph Graph to be exported to a SVG file.
   */
  public static void exportAsSvg(final JFrame parent, final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE01737: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01738: Graph argument can not be null");

    final LastDirFileChooser fileChooser = new LastDirFileChooser();
    final int retval = fileChooser.showSaveDialog(parent);
    if (retval == JFileChooser.APPROVE_OPTION) {
      try {
        if(!GraphExporters.exportAllAsSVG(
            graph, fileChooser.getSelectedFile().getAbsolutePath())) {
          throw new IOException("Failed to save SVG");
        }
      } catch (final IOException e) {
        CUtilityFunctions.logException(e);

        final String innerMessage = "E00195: " + "Could not save view to SVG file";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The view '%s' could not be written to the file '%s'.", graph.getViewName(),
            fileChooser.getSelectedFile().getAbsolutePath()),
            new String[] {"There was a problem writing the PNG file."},
            new String[] {"The view was not written to the PNG file."});

        NaviErrorDialog.show(parent, innerMessage, innerDescription, e);
      }
    }
  }

}
