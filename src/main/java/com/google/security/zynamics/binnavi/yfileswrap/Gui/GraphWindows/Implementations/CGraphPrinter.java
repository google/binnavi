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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Implementations;

import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.ErrorDialog.CNaviErrorDialog;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import y.option.OptionHandler;
import y.view.Graph2DPrinter;

/**
 * Contains functions for printing a graph.
 */
public final class CGraphPrinter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphPrinter() {
  }

  /**
   * Prints a graph. The user is given the opportunity to choose a number of options from a dialog
   * before.
   *
   * @param parent Parent window of created dialogs.
   * @param graph The graph to print.
   */
  public static void print(final JFrame parent, final ZyGraph graph) {
    final String[] area = {"Print the visible part of the graph only", "Print the whole graph"};

    final OptionHandler printOptions = new OptionHandler("Print Options");

    printOptions.addInt("Poster rows", 1);
    printOptions.addInt("Poster columns", 1);
    printOptions.addBool("Add poster coordinates", false);
    printOptions.addEnum("Print Area", area, 1);

    final Graph2DPrinter gprinter = new Graph2DPrinter(graph.getView());

    // show custom print dialog and adopt values
    if (!printOptions.showEditor()) {
      return;
    }

    gprinter.setPosterRows(printOptions.getInt("Poster rows"));
    gprinter.setPosterColumns(printOptions.getInt("Poster columns"));
    gprinter.setPrintPosterCoords(printOptions.getBool("Add poster coordinates"));

    if (printOptions.get("Print Area").equals("Print the whole graph")) {
      gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
    } else {
      gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
    }

    // show default print dialogs
    final PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat pageFormat = printJob.defaultPage();
    final PageFormat pageFormat2 = printJob.pageDialog(pageFormat);

    if (pageFormat2 == pageFormat) {
      return;
    }

    pageFormat = pageFormat2;

    // setup printjob.
    // Graph2DPrinter is of type Printable
    printJob.setPrintable(gprinter, pageFormat);

    if (printJob.printDialog()) {
      try {
        printJob.print();
      } catch (final PrinterException exception) {
        final String innerMessage = "E00119: " + "Graph could not be printed";
        final String innerDescription = CUtilityFunctions.createDescription(String.format(
            "The graph '%s' could not be printed because there was a problem with the printer.",
            graph.getRawView().getName()), new String[] {"There was a problem with the printer."},
            new String[] {"The print operation could not be completed."});

        CNaviErrorDialog.show(parent, innerMessage, innerDescription, exception);
      }
    }
  }
}
