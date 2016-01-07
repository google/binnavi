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

import com.google.security.zynamics.binnavi.Gui.CNameShortener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.JGraphTab;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;



/**
 * Contains helper functions for closing graphs.
 */
public final class CGraphCloser {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphCloser() {
  }

  /**
   * Closes a bunch of graph panels.
   *
   * @param parent Parent window used for dialogs.
   * @param graphs The graph panels to close.
   */
  private static void close(final Window parent, final List<CGraphPanel> graphs) {
    final List<IGraphPanel> modifiedGraphs = collectModifiedGraphPanels(graphs);

    if (modifiedGraphs.isEmpty()) {
      closeAll(graphs);
    } else {
      final int result =
          CMessageBox.showYesNoCancelQuestion(parent, generateMessage(modifiedGraphs));

      if (result == JOptionPane.YES_OPTION) {
        for (final IGraphPanel panel : modifiedGraphs) {
          saveGraph(parent, panel.getModel());
        }

        for (final CGraphPanel panel : graphs) {
          panel.close(false);
        }
      } else if (result == JOptionPane.NO_OPTION) {
        closeAll(graphs);
      } else {
        return;
      }
    }
  }

  /**
   * Closes all graph panels in a list.
   *
   * @param panels The graph panels to close.
   */
  private static void closeAll(final Iterable<CGraphPanel> panels) {
    for (final CGraphPanel panel : panels) {
      panel.close(false);
    }
  }

  /**
   * Collects the panels of all the graphs that were modified since the last save operation.
   *
   * @param panels The panels to check.
   *
   * @return The subset of the input list that contains only the modified graph panels.
   */
  private static List<IGraphPanel> collectModifiedGraphPanels(final List<CGraphPanel> panels) {
    final List<IGraphPanel> modifiedGraphs = new ArrayList<IGraphPanel>();

    for (final CGraphPanel panel : panels) {
      if (panel.getModel().getGraph().getRawView().wasModified()) {
        modifiedGraphs.add(panel);
      }
    }

    return modifiedGraphs;
  }

  /**
   * Generates the save message for a list of graphs.
   *
   * @param modifiedGraphs The modified graphs that require saving.
   *
   * @return The message string to show to the user.
   */
  private static String generateMessage(final List<IGraphPanel> modifiedGraphs) {
    final StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("Save changes to the following graphs?");
    stringBuilder.append('\n');

    for (final IGraphPanel panel : modifiedGraphs) {
      stringBuilder.append(
          "- " + CNameShortener.shorten(panel.getModel().getGraph().getRawView()) + "\n");
    }

    return stringBuilder.toString();
  }

  /**
   * Saves a graph and waits until the save operation is complete.
   *
   * @param parent Parent window used for dialogs.
   * @param model Provides the graph to save.
   */
  private static void saveGraph(final Window parent, final CGraphModel model) {
    if (model.getGraph().getRawView().getType() == ViewType.Native) {
      saveNativeGraph(parent, model);
    } else {
      saveNonNativeGraph(parent, model);
    }
  }

  /**
   * Saves a native graph and waits until the graph is saved.
   *
   * @param parent Parent window used for dialogs.
   * @param model Provides the graph to save.
   */
  private static void saveNativeGraph(final Window parent, final CGraphModel model) {
    final CSaveProgress progress = CGraphSaver.saveAs(
        parent, model.getGraph(), model.getViewContainer());

    while (!progress.isDone()) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Saves a non-native graph and waits until the graph is saved.
   *
   * @param parent Parent window used for dialogs.
   * @param model Provides the graph to save.
   */
  private static void saveNonNativeGraph(final Window parent, final CGraphModel model) {
    final CSaveProgress progress = CGraphSaver.save(parent, model.getGraph());

    while (!progress.isDone()) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        java.lang.Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Closes a graph panel.
   *
   * @param panel The panel to close.
   */
  public static void close(final CGraphPanel panel) {
    panel.close(true);
  }

  /**
   * Closes a graph window. Before closing the graph window, the user is asked whether he wants to
   * save modified graphs.
   *
   * @param graphTab Provides all the graph components in the window.
   */
  public static void close(final JGraphTab graphTab) {
    final List<CGraphPanel> graphs = new ArrayList<CGraphPanel>();

    for (int i = 0; i < graphTab.getTabCount(); i++) {
      graphs.add(((CGraphPanel) graphTab.getComponentAt(i)));
    }

    close(SwingUtilities.getWindowAncestor(graphTab), graphs);
  }

  /**
   * Closes all but the given graph panel.
   *
   * @param graphTab The clicked tabbed pane.
   * @param panel The panel NOT to close.
   */
  public static void closeOthers(final JGraphTab graphTab, final CGraphPanel panel) {
    final List<CGraphPanel> graphs = new ArrayList<CGraphPanel>();

    for (int i = 0; i < graphTab.getTabCount(); i++) {
      final CGraphPanel currentPanel = ((CGraphPanel) graphTab.getComponentAt(i));

      if (currentPanel != panel) {
        graphs.add(currentPanel);
      }
    }

    close(SwingUtilities.getWindowAncestor(graphTab), graphs);
  }
}
