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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Bottom;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CBottomPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.IResultsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.CAbstractGraphPanelExtensionFactory;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.IGraphPanelExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanelExtender;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader.CGraphOpener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Bottom.Debug.CDebugBottomPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IPerspectiveModelListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.PerspectiveType;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.CGraphPanelSynchronizer;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

/**
 * Bottom panel shown in the graph window that switches its content depending on the active
 * perspective.
 */
public final class CCombinedBottomPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6230089818642210009L;

  /**
   * Graph model that provides the data for the components in this panel.
   */
  private final CGraphModel m_model;

  /**
   * Synchronizes events in graph and view objects with the displayed graph.
   */
  private final CGraphPanelSynchronizer m_synchronizer;

  /**
   * Describes the active GUI perspective.
   */
  private final CPerspectiveModel m_perspectiveModel;

  /**
   * Panel shown when the standard GUI perspective is active.
   */
  private final CBottomPanel m_standardBottomPanel =
      new CBottomPanel(new FilledList<IResultsPanel>());

  /**
   * Panel shown when the debug GUI perspective is active.
   */
  private final CDebugBottomPanel m_debugBottomPanel;

  /**
   * Provides callback methods that graph panel extension objects can use to extend the graph panel.
   */
  private final CGraphPanelExtender m_extender = new CGraphPanelExtender();

  /**
   * Updates the panel on changes in the selected perspective.
   */
  private final IPerspectiveModelListener m_listener = new InternalViewListener();

  private final List<IGraphPanelExtension> m_extensions;

  /**
   * Creates a new panel object.
   *
   * @param model Graph model that provides the data for the components in this panel.
   * @param synchronizer Synchronizes events in graph and view objects with the displayed graph.
   * @param perspectiveModel Describes the active GUI perspective.
   */
  public CCombinedBottomPanel(final CGraphModel model, final CGraphPanelSynchronizer synchronizer,
      final CPerspectiveModel perspectiveModel) {
    super(new BorderLayout());

    m_model = model;
    m_synchronizer = synchronizer;
    m_perspectiveModel = perspectiveModel;

    m_debugBottomPanel = new CDebugBottomPanel(model, ((CDebugPerspectiveModel) perspectiveModel
        .getModel(PerspectiveType.DebugPerspective)));

    add(m_standardBottomPanel);

    m_extensions = CAbstractGraphPanelExtensionFactory.getExtensions();

    registerExtensions();

    perspectiveModel.addListener(m_listener);
  }

  /**
   * Updates the GUI for a given perspective.
   *
   * @param perspective The perspective for which the GUI is updated.
   */
  private void activatePerspective(final PerspectiveType perspective) {
    removeAll();
    add(perspective == PerspectiveType.StandardPerspective
        ? m_standardBottomPanel : m_debugBottomPanel);

    updateUI();
  }

  /**
   * Visits the classes that want to extend the graph panel.
   */
  private void registerExtensions() {
    for (final IGraphPanelExtension extension : m_extensions) {
      extension.visit(m_model, m_extender);
    }
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    for (final IGraphPanelExtension extension : m_extensions) {
      extension.dispose();
    }

    m_perspectiveModel.removeListener(m_listener);

    m_standardBottomPanel.dispose();
    m_debugBottomPanel.dispose();
  }

  /**
   * Provides call back methods that graph panel extension objects can use to extend the graph
   * panel.
   */
  private class CGraphPanelExtender implements IGraphPanelExtender {
    @Override
    public void addTab(final String string, final JPanel panel) {
      m_standardBottomPanel.addTab(string, panel);
    }

    @Override
    public void openView(final IViewContainer container, final INaviView view) {
      CGraphOpener.showGraph(container, view, m_model.getParent(), m_model.getParent());
    }

    @Deprecated
    @Override
    public void registerCodeNodeExtension(final ICodeNodeExtension extension) {
      m_synchronizer.registerCodeNodeContextMenuExtension(extension);
    }

    @Override
    public void selectTab(final JPanel panel) {
      m_standardBottomPanel.selectTab(panel);
    }
  }

  /**
   * Updates the panel on changes in the selected perspective.
   */
  private class InternalViewListener implements IPerspectiveModelListener {
    @Override
    public void changedActivePerspective(final PerspectiveType activeView) {
      activatePerspective(activeView);
    }
  }
}
