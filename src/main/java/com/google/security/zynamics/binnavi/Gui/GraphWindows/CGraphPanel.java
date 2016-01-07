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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu.CGraphWindowMenuBar;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Bottom.CCombinedBottomPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Center.CCombinedCenterPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IFrontEndDebuggerProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Left.CCombinedLeftPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.PerspectiveType;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Right.CCombinedRightPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Toolbar.CGraphToolBar;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.helpers.CSizeSettingsNormalizer;
import com.google.security.zynamics.binnavi.ZyGraph.Painters.CDebuggerPainter;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.CGraphHotkeys;
import com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows.CGraphPanelSynchronizer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

import com.jidesoft.swing.JideSplitPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JMenuBar;
import javax.swing.JPanel;

/**
 * The CGraphPanel class is used to display a single graph and its associated components like the
 * node chooser panel or the debug panel.
 */
public final class CGraphPanel extends JPanel implements IGraphPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1468419960151528961L;

  /**
   * Information about the graph displayed in the panel.
   */
  private final CGraphModel m_model;

  /**
   * Synchronizes events in graph and view objects with the displayed graph.
   */
  private final CGraphPanelSynchronizer m_graphSynchronizer;

  /**
   * The menu bar that is shown in the parent window when the graph panel is active.
   */
  private CGraphWindowMenuBar m_menuBar;

  /**
   * Main toolbar of the graph panel.
   */
  private CGraphToolBar m_toolBar;

  /**
   * Panel shown at the bottom of the graph panel.
   */
  private final CCombinedBottomPanel m_bottomPanel;

  /**
   * Panel shown in the center of the graph panel.
   */
  private final CCombinedCenterPanel m_centerPanel;

  /**
   * Panel shown on the right of the graph panel.
   */
  private final CCombinedRightPanel m_rightPanel;

  /**
   * Panel shown on the left of the graph panel.
   */
  private final CCombinedLeftPanel m_leftPanel;

  /**
   * Splits the center panel and the bottom panel.
   */
  private JideSplitPane m_graphSplitter;

  /**
   * Splits left, center, and right panels.
   */
  private JideSplitPane m_graphTaggingSplitter;

  /**
   * Describes the active GUI perspective.
   */
  private final CPerspectiveModel m_viewModel;

  /**
   * Contains the dialogs that are specific for this panel.
   */
  private final CGraphPanelDialogs m_dialogs;

  /**
   * Creates a new graph panel object.
   * 
   * @param model Model of the graph displayed in the panel.
   */
  public CGraphPanel(final CGraphModel model) {
    super(new BorderLayout());

    Preconditions.checkNotNull(model, "IE01617: Model argument can't be null");

    m_model = model;
    m_model.setPanel(this);

    m_viewModel = new CPerspectiveModel(model);

    final CGraphWindow parent = m_model.getParent();
    final ZyGraph graph = m_model.getGraph();

    final InternalViewSwitcher viewSwitcher = new InternalViewSwitcher();

    m_toolBar =
        new CGraphToolBar(parent, model.getGraphPanel(), graph, m_model.getViewContainer()
            .getModules());
    m_menuBar = new CGraphWindowMenuBar(m_model, viewSwitcher);

    m_graphSynchronizer = new CGraphPanelSynchronizer(m_model, m_menuBar);

    final CDebugPerspectiveModel debugPerspective =
        ((CDebugPerspectiveModel) m_viewModel.getModel(PerspectiveType.DebugPerspective));

    m_bottomPanel = new CCombinedBottomPanel(model, m_graphSynchronizer, m_viewModel);
    m_centerPanel = new CCombinedCenterPanel(model.getGraph(), debugPerspective);
    m_rightPanel = new CCombinedRightPanel(parent, model, m_viewModel);
    m_leftPanel =
        new CCombinedLeftPanel(model, m_viewModel, m_toolBar.getSearchPanel().getSearchField());

    createGui();

    m_dialogs = new CGraphPanelDialogs(model, m_menuBar);

    CGraphHotkeys.registerHotKeys(parent, this,
        (IFrontEndDebuggerProvider) m_viewModel.getModel(PerspectiveType.DebugPerspective),
        m_toolBar.getSearchPanel().getSearchField(), m_toolBar.getGotoAddressField());

    for (final IDebugger debugger : model.getDebuggerProvider().getDebuggers()) {
      if (debugger.isConnected()) {
        debugPerspective.setActiveDebugger(debugger);
        viewSwitcher.activateDebugView();
        break;
      }
    }

    final IDebugger debugger = debugPerspective.getCurrentSelectedDebugger();

    if ((debugger != null) && debugger.isConnected()) {
      for (final TargetProcessThread thread : debugger.getProcessManager().getThreads()) {
        final RelocatedAddress address = thread.getCurrentAddress();

        if (address != null) {
          CDebuggerPainter.updateDebuggerHighlighting(graph, debugger.memoryToFile(address),
              debugger.getModule(address));
        }
      }
    }
  }

  /**
   * Sets up all the splitters and other minor GUI elements of the graph panel.
   */
  private void createGui() {
    m_graphSplitter = new JideSplitPane() {
      private static final long serialVersionUID = -4363828908016863289L;

      // ESCA-JAVA0025: Workaround for Case 1168
      @Override
      public void updateUI() {
        // Workaround for Case 1168: The mere presence of a JIDE component
        // screws up the look and feel.
      }
    };

    m_graphSplitter.setOrientation(JideSplitPane.VERTICAL_SPLIT);

    m_graphSplitter.addPane(m_centerPanel);
    m_graphSplitter.addPane(m_bottomPanel);

    m_graphTaggingSplitter = new JideSplitPane() {
      private static final long serialVersionUID = -9037540212052390552L;

      // ESCA-JAVA0025: Workaround for Case 1168
      @Override
      public void updateUI() {
        // Workaround for Case 1168: The mere presence of a JIDE component
        // screws up the look and feel.
      }
    };

    m_graphTaggingSplitter.setOrientation(JideSplitPane.HORIZONTAL_SPLIT);
    m_graphTaggingSplitter.addPane(m_leftPanel);
    m_graphTaggingSplitter.addPane(m_graphSplitter);
    m_graphTaggingSplitter.addPane(m_rightPanel);

    m_graphTaggingSplitter.setDoubleBuffered(true);
    m_graphTaggingSplitter.setOneTouchExpandable(true);
    m_graphTaggingSplitter.setMinimumSize(new Dimension(0, 0));

    m_graphSplitter.setDoubleBuffered(true);
    m_graphSplitter.setOneTouchExpandable(true);
    m_graphSplitter.setMinimumSize(new Dimension(0, 0));

    add(m_graphTaggingSplitter);

    add(m_toolBar, BorderLayout.NORTH);
  }

  @Override
  public void close(final boolean askSave) {
    if (!askSave || CPanelCloser.closeTab(m_model.getParent(), this)) {
      m_model.getParent().close(this);
    }
  }

  /**
   * Frees allocated resources.
   */
  @Override
  public void dispose() {
    m_graphSynchronizer.dispose();

    m_bottomPanel.dispose();
    m_centerPanel.dispose();

    m_model.dispose();

    final INaviView view = m_model.getGraph().getRawView();

    view.close();

    m_menuBar.dipose();
    m_toolBar.dispose();

    // Note: The following line is necessary for garbage collection
    // because the menus keep references to the graph panel and
    // this leads to circular object references.
    m_menuBar.removeAll();
    m_toolBar.removeAll();

    m_menuBar = null;
    m_toolBar = null;

    m_rightPanel.dispose();
    m_leftPanel.dispose();

    m_dialogs.dispose();

    removeAll();
  }

  /**
   * Returns the graph panel specific dialogs.
   * 
   * @return The graph panel specific dialogs.
   */
  public CGraphPanelDialogs getDialogs() {
    return m_dialogs;
  }

  @Override
  public JMenuBar getMenu() {
    return m_menuBar;
  }

  /**
   * Returns information about the graph displayed in the panel.
   * 
   * @return Information about the graph displayed in the panel.
   */
  @Override
  public CGraphModel getModel() {
    return m_model;
  }

  @Override
  public JPanel getPanel() {
    return this;
  }

  /**
   * Returns the node tagging tree shown in this graph panel.
   * 
   * @return The node tagging tree shown in this graph panel.
   */
  public CTagsTree getTagsTree() {
    return m_rightPanel.getTree();
  }

  /**
   * Returns the toolbar of the graph panel.
   * 
   * @return The toolbar of the graph panel.
   */
  public CGraphToolBar getToolbar() {
    return m_toolBar;
  }

  /**
   * Returns the perspective model of the graph panel.
   * 
   * @return The perspective model of the graph panel.
   */
  public CPerspectiveModel getViewModel() {
    return m_viewModel;
  }

  /**
   * Part of a workaround.
   */
  public void updateSplitters() {
    // Required because JSplitters suck.
    final GeneralSettingsConfigItem.GraphWindowConfigItem settings =
        ConfigManager.instance().getGeneralSettings().getGraphWindow();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    final int leftPanelSize =
        CSizeSettingsNormalizer.getSize(settings.getSizeLeftPanel(), screenSize.width, 200);
    final int rightPanelSize =
        CSizeSettingsNormalizer.getSize(settings.getSizeRightPanel(), screenSize.width,
            screenSize.width - 200);
    final int bottomPanelSize =
        CSizeSettingsNormalizer.getSize(settings.getSizeBottomPanel(), screenSize.height,
            screenSize.height - 400);

    m_graphTaggingSplitter.setDividerLocation(0, leftPanelSize);
    m_graphTaggingSplitter.setDividerLocation(1, rightPanelSize);
    m_graphSplitter.setDividerLocation(0, bottomPanelSize);

    CGraphPanelSettingsSynchronizer.initializeSynchronizer(m_graphSplitter, m_graphTaggingSplitter);
  }

  /**
   * Toggles between the different graph window perspectives.
   */
  private class InternalViewSwitcher implements IViewSwitcher {
    @Override
    public void activateDebugView() {
      m_viewModel.setActiveView(PerspectiveType.DebugPerspective);
    }

    @Override
    public void activateStandardView() {
      m_viewModel.setActiveView(PerspectiveType.StandardPerspective);
    }
  }
}
