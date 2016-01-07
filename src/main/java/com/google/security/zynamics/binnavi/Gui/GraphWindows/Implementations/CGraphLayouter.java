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

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessProgressDialog;

/**
 * Contains functions that can be used to layout graphs.
 */
public final class CGraphLayouter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGraphLayouter() {
  }

  /**
   * Warns the user that layouting could take a while.
   * 
   * @param parent Parent window of the dialog.
   * @param graph Graph to be layouted.
   * 
   * @return True, if layouting should proceed. False, if the user cancelled layouting.
   */
  private static boolean askLayout(final JFrame parent, final ZyGraph graph) {
    if (graph.visibleNodeCount() >= graph.getSettings().getLayoutSettings()
        .getLayoutCalculationTimeWarningThreshold()) {
      return JOptionPane.YES_OPTION == CMessageBox.showYesNoQuestion(parent,
          "Layouting this graph can take a while. Do you want to continue?");
    }

    return true;
  }

  /**
   * Checks whether any of the arguments are null.
   * 
   * @param parent Argument to check for null.
   * @param graph Argument to check for null.
   */
  private static void checkArguments(final JFrame parent, final ZyGraph graph) {
    Preconditions.checkNotNull(parent, "IE01746: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01747: Graph argument can not be null");
  }

  /**
   * Does a circular layout operation on the graph.
   * 
   * @param parent Parent frame of the graph.
   * @param graph The graph to layout.
   */
  public static void doCircularLayout(final JFrame parent, final ZyGraph graph) {
    checkArguments(parent, graph);

    if (!askLayout(parent, graph)) {
      return;
    }

    graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);

    refreshLayout(parent, graph);
  }

  /**
   * Does a hierarchic layout operation on the graph.
   * 
   * @param parent Parent frame of the graph.
   * @param graph The graph to layout.
   */
  public static void doHierarchicLayout(final JFrame parent, final ZyGraph graph) {
    checkArguments(parent, graph);

    if (!askLayout(parent, graph)) {
      return;
    }

    graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.HIERARCHIC);

    refreshLayout(parent, graph);
  }

  /**
   * Does an orthogonal layout operation on the graph.
   * 
   * @param parent Parent frame of the graph.
   * @param graph The graph to layout.
   */
  public static void doOrthogonalLayout(final JFrame parent, final ZyGraph graph) {
    checkArguments(parent, graph);

    if (!askLayout(parent, graph)) {
      return;
    }

    graph.getSettings().getLayoutSettings().setDefaultGraphLayout(LayoutStyle.ORTHOGONAL);

    refreshLayout(parent, graph);
  }

  /**
   * Takes the currently set graph layouter and uses it on the graph while showing a progress
   * dialog.
   * 
   * @param parent Parent frame of the graph.
   * @param graph The graph to layout.
   */
  public static void refreshLayout(final JFrame parent, final ZyGraph graph) {
    checkArguments(parent, graph);

    final LayoutWaiter waiter = new LayoutWaiter(graph);

    final CEndlessProgressDialog dlg =
        new CEndlessProgressDialog(parent, Constants.DEFAULT_WINDOW_TITLE,
            "Calculating the new graph layout" + "...", waiter);

    waiter.start();

    dlg.setVisible(true);
  }

  /**
   * Toggles the state of automatic layouting in the graph.
   * 
   * @param graph The graph to be toggled.
   */
  public static void toggleAutomaticLayouting(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE01748: Graph argument can not be null");

    graph.getSettings().getLayoutSettings()
        .setAutomaticLayouting(!graph.getSettings().getLayoutSettings().getAutomaticLayouting());
  }

  /**
   * Waiter class that is used to display a progress dialog during layout operations.
   */
  private static class LayoutWaiter extends CEndlessHelperThread {
    /**
     * Graph to be layouted.
     */
    private final ZyGraph m_graph;

    /**
     * Creates a new thread object.
     * 
     * @param graph Graph to be layouted.
     */
    private LayoutWaiter(final ZyGraph graph) {
      m_graph = graph;
    }

    @Override
    public void closeRequested() {
      finish();
    }

    @Override
    public void runExpensiveCommand() {
      m_graph.doLayout();
    }
  }
}
