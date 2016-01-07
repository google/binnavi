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
package com.google.security.zynamics.binnavi.standardplugins.coverage;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.API.gui.GraphFrame;
import com.google.security.zynamics.binnavi.API.helpers.MessageBox;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 * Menu handler of the Visual Coverage menu of the Plugins menu.
 */
public final class VisualCoverageAction extends AbstractAction {

  /**
   * The graph frame whose menu is extended.
   */
  private GraphFrame graphFrame;

  /**
   * The active Visual Coverage process. This can be null if no coverage process is active.
   */
  private VisualCoverage coverage = null;

  /**
   * Keeps track of relevant changes in the visual coverage object.
   */
  private final IVisualCoverageListener traceListener = new InternalTraceListener();

  /**
   * Creates a new visual coverage action object.
   * 
   * @param graphFrame The graph frame whose menu is extended.
   */
  public VisualCoverageAction(final GraphFrame graphFrame) {
    super("Visual Coverage");

    this.graphFrame = graphFrame;
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
    // TODO (timkornau): Get a proper parent argument.
    final JFrame parent = null;

    if (coverage != null) {
      MessageBox.showInformation(parent, "Visual Coverage trace is already active");
      return;
    }

    final List<Debugger> debuggers = graphFrame.getDebuggers();

    if (debuggers.size() == 0) {
      MessageBox
          .showInformation(parent,
              "Visual Coverage trace can not be started because no debugger is configured for this graph.");
      return;
    } else if (debuggers.size() == 1) {
      final Debugger debugger = debuggers.get(0);

      coverage = new VisualCoverage(parent, debugger, graphFrame.getView2D());

      // Add a listener to recognize when the trace is done.
      coverage.addListener(traceListener);
    } else {
      MessageBox
          .showInformation(
              parent,
              "Visual Coverage trace can not be started because more than one debugger is configured for this graph.");
      return;
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    if (coverage != null) {
      coverage.removeListener(traceListener);

      coverage.dispose();

      coverage = null;
      graphFrame = null;
    }
  }

  public GraphFrame getFrame() {
    return graphFrame;
  }

  /**
   * Keeps track of relevant changes in the visual trace object.
   */
  private class InternalTraceListener implements IVisualCoverageListener {
    @Override
    public void finishedCoverage() {
      // Do not dispose the coverage object here; it cleans itself up
      // when finishing.

      coverage.removeListener(traceListener);

      coverage = null;
    }
  }
}
