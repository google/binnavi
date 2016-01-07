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
package com.google.security.zynamics.binnavi.Gui.MainWindow;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.plugins.IPluginInterfaceListener;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.Database.CDatabaseManager;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.CProjectTree;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.IProjectTreeNode;

/**
 * This class creates the panel that is shown in the main window. On the left side of the panel
 * there is the tree that shows the databases/projects/... that are available. On the right side of
 * the panel a panel is shown whose content depends on the currently selected node.
 */
public final class CProjectMainPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 2093952613078997535L;

  /**
   * The main window of BinNavi where this panel belongs to.
   */
  private final JFrame m_window;

  /**
   * The component panel of the main panel is the panel where the components are shown that depend
   * on the current selection inside the project tree.
   */
  private final JPanel m_componentPanel = new JPanel(new BorderLayout());

  /**
   * Project tree shown in the main window.
   */
  private final CProjectTree m_projectTree;

  /**
   * Listener that updates the GUI when plugins are loaded.
   */
  private final InternalPluginListener m_pluginListener = new InternalPluginListener();

  /**
   * Creates a new main panel.
   * 
   * @param window Parent window of the panel.
   * @param databaseManager The database manager that contains the known databases.
   * 
   * @throws IllegalArgumentException Thrown if any of the arguments is null.
   */
  public CProjectMainPanel(final JFrame window, final CDatabaseManager databaseManager) {
    super(new BorderLayout());
    m_window = Preconditions.checkNotNull(window, "IE01830: Window argument can't be null");
    Preconditions.checkNotNull(databaseManager, "IE01831: Database manager argument can't be null");
    m_projectTree = new CProjectTree(m_window, databaseManager);
    add(createMainSplitPane(), BorderLayout.CENTER);
    PluginInterface.instance().addListener(m_pluginListener);
  }

  /**
   * Creates the main split pane. The main split pane divides the main window into project tree on
   * the left side of the main window and the state-specific views on the right side of the main
   * window.
   * 
   * @return The main split pane.
   */
  private JSplitPane createMainSplitPane() {
    final JSplitPane mainSplitPane = new JSplitPane();
    mainSplitPane.setOneTouchExpandable(true);
    mainSplitPane.setDividerLocation(250);
    mainSplitPane.setResizeWeight(0.33);
    mainSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

    m_componentPanel.setBackground(Color.WHITE);

    mainSplitPane.setLeftComponent(createProjectTreePanel());
    mainSplitPane.setRightComponent(m_componentPanel);
    mainSplitPane.setBorder(new LineBorder(Color.GRAY));

    return mainSplitPane;
  }

  /**
   * Creates the project tree that is shown on the left side of the main window.
   * 
   * @return The project tree panel that contains the project tree.
   */
  private JPanel createProjectTreePanel() {
    final JPanel projectTreePanel = new JPanel(new BorderLayout());
    projectTreePanel.setBorder(null);
    projectTreePanel.setBackground(Color.WHITE);

    m_projectTree.setBorder(new EmptyBorder(2, 4, 2, 2));
    m_projectTree.addTreeSelectionListener(new InternalTreeSelectionListener());

    final JScrollPane pane = new JScrollPane(m_projectTree);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    pane.setBorder(null);

    projectTreePanel.add(pane, BorderLayout.CENTER);

    return projectTreePanel;
  }

  /**
   * Changes the appearance of the main window whenever a new node is selected.
   * 
   * @param node The selected node.
   */
  private void nodeSelected(final IProjectTreeNode node) {
    // Depending on the selected tree node, we get rid of
    // the old content of the right side and create a new
    // node-dependent right side.

    final Component component = node.getComponent();

    m_componentPanel.removeAll();

    if (component != null) {
      m_componentPanel.add(component);
    }

    m_componentPanel.updateUI();

    m_window.setJMenuBar(node.getMainMenu());

    m_window.getJMenuBar().updateUI();
  }

  /**
   * Selects the root node of the project tree.
   */
  public void init() {
    m_projectTree.setSelectionPath(new TreePath(m_projectTree.getRootNode()));
  }

  /**
   * Listener that updates the GUI when plugins are loaded.
   */
  private class InternalPluginListener implements IPluginInterfaceListener {
    @Override
    public void loadedPlugins() {
      final IProjectTreeNode node = (IProjectTreeNode) m_projectTree.getLastSelectedPathComponent();

      if (node != null) {
        m_window.setJMenuBar(node.getMainMenu());

        m_window.getJMenuBar().updateUI();
      }
    }
  }

  /**
   * This listener class is responsible for updating the right side of the main window whenever a
   * new node from the tree is selected.
   */
  private class InternalTreeSelectionListener implements TreeSelectionListener {
    @Override
    public void valueChanged(final TreeSelectionEvent event) {
      final DefaultMutableTreeNode node =
          (DefaultMutableTreeNode) m_projectTree.getLastSelectedPathComponent();

      if (node != null) {
        // A new node was selected

        nodeSelected((IProjectTreeNode) node);
      }
      // else: Suppress change during a Drag & Drop event
    }
  }
}
