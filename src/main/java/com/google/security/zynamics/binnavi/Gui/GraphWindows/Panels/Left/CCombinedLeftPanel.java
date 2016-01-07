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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IPerspectiveModelListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left.Debug.CDebugLeftPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left.Standard.CStandardLeftPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.PerspectiveType;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchField;

import java.awt.BorderLayout;

import javax.swing.JPanel;

/**
 * Panel that is shown on the left side of graph windows. The content of this panel changes
 * depending on the active perspective.
 */
public final class CCombinedLeftPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4794578753327568747L;

  /**
   * Panel shown in standard perspective mode.
   */
  private final CStandardLeftPanel m_leftPanel;

  /**
   * Panel shown in debug perspective mode.
   */
  private final CDebugLeftPanel m_debugLeftPanel;

  /**
   * Provides information about the selected perspectives.
   */
  private final CPerspectiveModel m_perspectiveModel;

  /**
   * Keeps track of changes in the perspective.
   */
  private final IPerspectiveModelListener m_listener = new InternalPerspectiveListener();

  /**
   * Creates a new panel object.
   *
   * @param model Model of the graph the panel works on.
   * @param perspectiveModel Describes the selected perspective.
   * @param searchField Search field used to search through the graph.
   */
  public CCombinedLeftPanel(final CGraphModel model, final CPerspectiveModel perspectiveModel,
      final CGraphSearchField searchField) {
    super(new BorderLayout());

    m_perspectiveModel = perspectiveModel;

    m_leftPanel =
        new CStandardLeftPanel(model.getGraph(), model.getSelectionHistory(), searchField);
    m_debugLeftPanel = new CDebugLeftPanel(model.getParent(), model.getDebuggerProvider(),
        (CDebugPerspectiveModel) perspectiveModel.getModel(PerspectiveType.DebugPerspective));

    add(m_leftPanel);

    perspectiveModel.addListener(m_listener);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_perspectiveModel.removeListener(m_listener);

    m_leftPanel.delete();
    m_debugLeftPanel.dispose();
  }

  /**
   * Updates the panel on changes in the perspective.
   */
  private class InternalPerspectiveListener implements IPerspectiveModelListener {
    @Override
    public void changedActivePerspective(final PerspectiveType activeView) {
      removeAll();

      if (activeView == PerspectiveType.DebugPerspective) {
        add(m_debugLeftPanel);
      } else {
        add(m_leftPanel);
      }

      updateUI();
    }
  }
}
