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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphExporter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Action used for exporting a graph to an SVG file.
 */
public final class CActionGraphExportSVG extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -205906679257758059L;

  /**
   * Parent used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Graph to be exported.
   */
  private final ZyGraph m_graph;

  /**
   * Creates a new action object.
   *
   * @param parent Parent used for dialogs.
   * @param graph Graph to be exported.
   */
  public CActionGraphExportSVG(final JFrame parent, final ZyGraph graph) {
    super("Export as SVG");

    m_parent = parent;
    m_graph = graph;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CGraphExporter.exportAsSvg(m_parent, m_graph);
  }
}
