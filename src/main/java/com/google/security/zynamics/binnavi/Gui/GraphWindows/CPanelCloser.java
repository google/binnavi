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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CNameShortener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphSaver;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CSaveProgress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.gui.CMessageBox;

/**
 * This class can be used to close a graph tab and prompting the user whether he wants to save the
 * graph before.
 */
public final class CPanelCloser {
  /**
   * You are not supposed to instantiate this class.
   */
  private CPanelCloser() {
  }

  /**
   * Closes a modified graph.
   *
   * @param parent The parent window of the panel.
   * @param panel The panel to be closed.
   *
   * @return True, if the panel can be closed. False, if the user vetoed the operation.
   */
  private static boolean closeModifiedGraph(final JFrame parent, final IGraphPanel panel) {
    final ZyGraph graph = panel.getModel().getGraph();

    final int result = CMessageBox.showYesNoCancelQuestion(parent, String.format(
        "Do you want to save the view '%s' before closing?",
        CNameShortener.shorten(graph.getRawView())));

    if (result == JOptionPane.YES_OPTION) {
      if (graph.getRawView().getType() == ViewType.Native) {
        saveNativeGraph(parent, panel);
      } else {
        saveNonNativeGraph(parent, graph);
      }
    }

    return (result == JOptionPane.YES_OPTION) || (result == JOptionPane.NO_OPTION);
  }

  /**
   * Saves a native graph.
   *
   * @param parent The parent window of the panel.
   * @param panel The panel to be closed.
   */
  private static void saveNativeGraph(final JFrame parent, final IGraphPanel panel) {
    final ZyGraph graph = panel.getModel().getGraph();

    final CSaveProgress progress =
        CGraphSaver.saveAs(parent, graph, panel.getModel().getViewContainer());

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
   * Saves a non-native graph.
   *
   * @param parent The parent window of the panel.
   * @param graph The graph to save.
   */
  private static void saveNonNativeGraph(final JFrame parent, final ZyGraph graph) {
    final CSaveProgress progress = CGraphSaver.save(parent, graph);

    while (!progress.isDone()) {
      try {
        Thread.sleep(100);
      } catch (final InterruptedException e) {
        // restore the interrupted status of the thread.
        // http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Asks the user whether he wants to save a graph before closing a panel.
   *
   * @param parent The parent window of the panel.
   * @param panel The panel to be closed.
   *
   * @return True, if the panel can be closed. False, if the user wants to keep the panel open.
   */
  public static boolean closeTab(final JFrame parent, final IGraphPanel panel) {
    Preconditions.checkNotNull(parent, "IE01630: Parent argument can not be null");

    Preconditions.checkNotNull(panel, "IE01631: Panel argument can not be null");

    final ZyGraph graph = panel.getModel().getGraph();

    return graph.getRawView().wasModified() ? closeModifiedGraph(parent, panel) : true;
  }
}
