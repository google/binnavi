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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

/**
 * The panel component that is shown at the bottom of each graph window. In this component the user
 * has the option to interact with the debugger in various ways.
 */
public class CBottomPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -1690375770284161511L;

  /**
   * Provides the individual tabs shown in the panel.
   */
  private final IFilledList<IResultsPanel> m_factory;

  /**
   * Tabbed pane shown in the panel that allows to switch between different views.
   */
  private final JTabbedPane m_tabbedPane = new JTabbedPane();

  /**
   * Listener that shows individual results panel when new results come in.
   */
  private final IResultsPanelListener m_internalPanelListener = new InternalPanelListener();

  /**
   * Creates a new bottom panel object.
   *
   * @param panels The panels to be shown in the bottom panel.
   */
  public CBottomPanel(final IFilledList<IResultsPanel> panels) {
    super(new BorderLayout());

    Preconditions.checkNotNull(panels, "IE01658: Panels can't be null");

    m_factory = new FilledList<IResultsPanel>(panels);

    for (final IResultsPanel panel : panels) {
      panel.addListener(m_internalPanelListener);
    }

    createGui();
  }

  /**
   * Creates the sub-components of the panel.
   */
  private void createGui() {
    for (final IResultsPanel panel : m_factory) {
      m_tabbedPane.addTab(panel.getTitle(), panel.getComponent());
    }

    add(m_tabbedPane, BorderLayout.CENTER);

    m_tabbedPane.setVisible(true);
  }

  /**
   * This function can be overwritten by child classes to clean up their resources.
   */
  protected void disposeInternal() {
    // Empty default implementation
  }

  /**
   * Adds a tab to the tabbed pane.
   *
   * @param title Title shown in the tab.
   * @param component The component shown when the tab is active.
   */
  public final void addTab(final String title, final JComponent component) {
    Preconditions.checkNotNull(title, "IE01228: Title argument can not be null");
    Preconditions.checkNotNull(component, "IE01229: Component argument can not be null");

    m_tabbedPane.addTab(title, component);
  }

  /**
   * Frees allocated resources.
   */
  public final void dispose() {
    for (final IResultsPanel panel : m_factory) {
      panel.removeListener(m_internalPanelListener);
      panel.dispose();
    }

    disposeInternal();
  }

  /**
   * Activates one of the components shown in the tabbed pane.
   *
   * @param component The component to be shown.
   */
  public final void selectTab(final JComponent component) {
    Preconditions.checkNotNull(component, "IE01230: Component argument can not be null");

    m_tabbedPane.setSelectedComponent(component);
  }

  /**
   * Activates tabs when new results come in.
   */
  private class InternalPanelListener implements IResultsPanelListener {
    @Override
    public void show(final JComponent panel) {
      selectTab(panel);
    }
  }
}
