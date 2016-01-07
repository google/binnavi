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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Bottom.Debug;

import com.google.security.zynamics.binnavi.Gui.Debug.Bookmarks.CBookmarkPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable.CBreakpointPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.CombinedMemoryPanel.CCombinedMemoryPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CTracesPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.History.CDebuggerHistoryPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel.CModulesPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.StatusLabel.CStatusPanel;
import com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel.CThreadInformationPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CBottomPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.IResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Center.CDebuggerControlPanel;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import java.awt.BorderLayout;



/**
 * Panel object shown at the bottom of graph views if the debug perspective is active.
 */
public final class CDebugBottomPanel extends CBottomPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5256588789206770620L;

  /**
   * Panel where the debugging toolbar is shown.
   */
  private final CDebuggerControlPanel m_debuggerBox;

  /**
   * Label that shows debugger events.
   */
  private final CStatusPanel m_statusLabel;

  /**
   * Creates a new panel object.
   *
   * @param model Provides the data needed by the components in the panel.
   * @param debugPerspectiveModel Describes the debug perspective.
   */
  public CDebugBottomPanel(
      final CGraphModel model, final CDebugPerspectiveModel debugPerspectiveModel) {
    super(getPanels(model, debugPerspectiveModel));

    m_debuggerBox = new CDebuggerControlPanel(model.getParent(), debugPerspectiveModel);

    add(m_debuggerBox, BorderLayout.NORTH);

    m_statusLabel = new CStatusPanel(model.getDebuggerProvider());

    add(m_statusLabel, BorderLayout.SOUTH);
  }

  /**
   * Creates the panels that are shown in the panel.
   *
   * @param model Provides the data needed by the components in the panel.
   * @param debugPerspectiveModel Describes the debug perspective.
   *
   * @return The created panels.
   */
  private static IFilledList<IResultsPanel> getPanels(
      final CGraphModel model, final CDebugPerspectiveModel debugPerspectiveModel) {
    final IFilledList<IResultsPanel> debugPanels = new FilledList<IResultsPanel>();

    debugPanels.add(new CCombinedMemoryPanel(model.getParent(), debugPerspectiveModel));
    debugPanels.add(new CModulesPanel(debugPerspectiveModel));
    debugPanels.add(new CThreadInformationPanel(debugPerspectiveModel));
    debugPanels.add(new CBreakpointPanel(model.getParent(), model.getDebuggerProvider(), model
        .getGraph(), model.getViewContainer()));
    debugPanels.add(new CTracesPanel(debugPerspectiveModel, model.getGraph(),
        model.getViewContainer().getTraceProvider(), model.getGraphPanel()));
    debugPanels.add(new CBookmarkPanel(model.getDebuggerProvider()));
    debugPanels.add(new CDebuggerHistoryPanel(debugPerspectiveModel));

    return debugPanels;
  }

  @Override
  protected void disposeInternal() {
    m_debuggerBox.dispose();
    m_statusLabel.dispose();
  }
}
