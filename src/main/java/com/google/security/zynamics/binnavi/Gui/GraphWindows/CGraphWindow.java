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

import java.awt.BorderLayout;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CIconInitializer;
import com.google.security.zynamics.binnavi.Gui.CNameShortener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphCloser;
import com.google.security.zynamics.binnavi.Gui.Progress.CProgressStatusBar;
import com.google.security.zynamics.binnavi.disassembly.views.CViewListenerAdapter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviGraphListenerAdapter;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.general.ListenerProvider;

/**
 * The graph window class is a top-level window that can be used to display one or more graphs.
 * Individual graphs are shown in a tab component.
 *
 *  The standard behavior is that this window must have at least one open graph. As soon as the last
 * graph in the window is closed, the whole window is closed too.
 */
public final class CGraphWindow extends JFrame implements IGraphContainerWindow {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3265629894301476389L;

  /**
   * Tabbed pane component where all the graphs are shown.
   */
  private final JGraphTab m_tabbedPane = new JGraphTab(this);

  /**
   * Listeners that are notified about opening and closing graph tabs.
   */
  private final ListenerProvider<IGraphWindowListener> m_graphPanelListener =
      new ListenerProvider<IGraphWindowListener>();

  /**
   * Listener that updates the window when something in a view changed.
   */
  private final InternalViewListener m_viewListener = new InternalViewListener();

  /**
   * Listener that keeps track of changes in ZyGraph objects.
   */
  private final InternalGraphListener m_graphListener = new InternalGraphListener();

  /**
   * Creates a new graph navigator component with a single graph to display.
   */
  public CGraphWindow() {
    CIconInitializer.initializeWindowIcons(this);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    addWindowListener(new CWindowCloser(m_tabbedPane));

    final InternalTabListener listener = new InternalTabListener();
    m_tabbedPane.getModel().addChangeListener(listener);
    m_tabbedPane.addContainerListener(listener);

    addWindowStateListener(new WorkaroundListener());

    m_tabbedPane.addMouseListener(new InternalMouseListener());

    add(m_tabbedPane);

    final JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.add(new CProgressStatusBar(), BorderLayout.EAST);

    add(statusBar, BorderLayout.SOUTH);
  }

  /**
   * Determines whether there is already a graph panel open inside the window that displays a given
   * view.
   *
   * @param view The view to search for.
   *
   * @return True, if the view is already open. False, if the view is not yet open.
   */
  private boolean alreadyListening(final INaviView view) {
    return viewOpenCount(view) > 0;
  }

  /**
   * Returns the currently active graph window. This is the graph window that's currently selected
   * and visible to the user.
   *
   * @return The currently active graph window.
   */
  private IGraphPanel getActiveGraphWindow() {
    return (IGraphPanel) m_tabbedPane.getSelectedComponent();
  }

  /**
   * Updates the window title of the current window if the view whose name changed equals the view
   * displayed in the current window.
   *
   * @param view The view whose name changed.
   */
  private void updateCurrentWindowTitle(final INaviView view) {
    final IGraphPanel window = getActiveGraphWindow();

    if (window == null) {
      return;
    }

    final INaviView currentView = window.getModel().getGraph().getRawView();

    if (view == currentView) {
      setTitle(CWindowTitle.generate(window));
    }
  }

  /**
   * Updates the window when something relevant (like a new tab was selected) happens.
   */
  private void updateWindow() {
    final IGraphPanel window = getActiveGraphWindow();

    if (window == null) {
      return;
    }

    setTitle(CWindowTitle.generate(window));

    setJMenuBar(window.getMenu());
  }

  /**
   * Counts how many tabs are open inside the window that show a given view.
   *
   * @param view The view to count.
   *
   * @return The number of tabs in the current window that show the view.
   */
  private int viewOpenCount(final INaviView view) {
    int counter = 0;

    for (int i = 0; i < getOpenGraphCount(); i++) {
      final IGraphPanel component = (IGraphPanel) m_tabbedPane.getComponentAt(i);

      if (view == component.getModel().getGraph().getRawView()) {
        counter++;
      }
    }

    return counter;
  }

  @Override
  public void activate(final IGraphPanel panel) {
    m_tabbedPane.setSelectedComponent(panel.getPanel());
  }

  @Override
  public void addGraph(final IGraphPanel panel) {
    // Create a new tab for the graph and select the new tab

    if (panel.getPanel() == null) {
      throw new IllegalStateException("IE01145: Invalid panel object returned");
    }

    final ZyGraph graph = panel.getModel().getGraph();

    graph.addListener(m_graphListener);

    final INaviView rawView = graph.getRawView();

    if (!alreadyListening(rawView)) {
      // We need one listener per view to update the window title and
      // other stuff that depends on the view.

      rawView.addListener(m_viewListener);
    }

    m_tabbedPane.addTab(CNameShortener.shorten(rawView), panel.getPanel());
  }

  @Override
  public void addListener(final IGraphWindowListener listener) {
    m_graphPanelListener.addListener(listener);
  }

  /**
   * Closes the window.
   *
   * @return True, if the window was closed. False, if the user vetoed the close.
   */
  public boolean close() {
    CGraphCloser.close(m_tabbedPane);

    return m_tabbedPane.getTabCount() == 0;
  }

  @Override
  public void close(final IGraphPanel panel) {
    Preconditions.checkNotNull(panel, "IE01625: Panel argument can't be null");

    if (m_tabbedPane.indexOfComponent(panel.getPanel()) == -1) {
      throw new IllegalStateException("IE01146: Panel argument does not belong to the tab");
    }

    m_tabbedPane.remove(panel.getPanel());
  }

  @Override
  public JFrame getFrame() {
    return this;
  }

  @Override
  public int getOpenGraphCount() {
    return m_tabbedPane.getTabCount();
  }

  /**
   * Determines whether a given graph panel is the active graph panel of the window.
   *
   * @param panel The panel to check.
   *
   * @return True, if the given panel is the active graph panel. False, otherwise.
   */
  public boolean isActiveGraph(final IGraphPanel panel) {
    return m_tabbedPane.getSelectedComponent() == panel.getPanel();
  }

  @Override
  public Iterator<IGraphPanel> iterator() {
    return new GraphIterator(m_tabbedPane);
  }

  @Override
  public void removeListener(final IGraphWindowListener listener) {
    m_graphPanelListener.removeListener(listener);
  }

  /**
   * Keeps track of changing views in graph objects and updates all necessary stuff when a view
   * changes.
   */
  private class InternalGraphListener extends NaviGraphListenerAdapter {
    @Override
    public void changedView(final INaviView oldView, final INaviView newView) {
      if (viewOpenCount(oldView) == 0) {
        oldView.removeListener(m_viewListener);
      }

      // Do not check whether we're already listening on this view.
      // Since the only way this function is invoked is when the old
      // view was saved as a new view, we cannot already be listening on
      // it.
      newView.addListener(m_viewListener);

      m_tabbedPane.updateRegisterHeaders();
      updateCurrentWindowTitle(newView);
    }
  }

  /**
   * Listener that handles context menu clicks on tabs.
   */
  private class InternalMouseListener extends MouseAdapter {
    /**
     * Shows a popup menu for the given event.
     *
     * @param event The event that triggered the menu.
     */
    private void showPopupMenu(final MouseEvent event) {
      final int index = m_tabbedPane.indexAtLocation(event.getX(), event.getY());

      if (index == -1) {
        return;
      }

      final CGraphPanel child = (CGraphPanel) m_tabbedPane.getComponentAt(index);

      final JPopupMenu menu = new JGraphTabMenu(m_tabbedPane, child);

      menu.show(event.getComponent(), event.getX(), event.getY());
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        showPopupMenu(event);
      }
    }
  }

  /**
   * Keeps track of selection changes in the tab view and updates things like the window menu or the
   * window title depending on the information from the newly selected graph.
   */
  private class InternalTabListener implements ChangeListener, ContainerListener {
    /**
     * Closes the window and frees allocated resources.
     */
    private void closeWindow() {
      setJMenuBar(null);

      setVisible(false);
      dispose();
    }

    @Override
    public void componentAdded(final ContainerEvent event) {
      for (final IGraphWindowListener listener : m_graphPanelListener) {
        // ESCA-JAVA0166: Catch Exception because we are calling a listener function
        try {
          listener.graphPanelOpened((CGraphPanel) event.getChild());
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }
    }

    @Override
    public void componentRemoved(final ContainerEvent event) {
      final IGraphPanel removedPanel = (IGraphPanel) event.getChild();

      final ZyGraph graph = removedPanel.getModel().getGraph();

      graph.removeListener(m_graphListener);

      removedPanel.dispose();

      if (viewOpenCount(graph.getRawView()) == 0) {
        // If the last tab that shows the given view was closed, we can
        // remove the listener from that view.

        graph.getRawView().removeListener(m_viewListener);
      }

      if (m_tabbedPane.getTabCount() == 0) {
        // Note: Do not merge this block with the one after the listener notification
        // I want to the window to go away first, before the listeners are notified.
        // But if the window is unregistered in the first block, the notification
        // for the closed graph does not arrive anymore.

        closeWindow();
      } else {
        updateWindow();
      }

      for (final IGraphWindowListener listener : m_graphPanelListener) {
        try {
          listener.graphPanelClosed();
        } catch (final Exception e) {
          CUtilityFunctions.logException(e);
        }
      }

      if (m_tabbedPane.getTabCount() == 0) {
        // Note: Do not merge this block with the one above the listener
        // notification

        closeWindow();
      }
    }

    @Override
    public void stateChanged(final ChangeEvent event) {
      updateWindow();

      final int index = m_tabbedPane.getSelectedIndex();

      for (int i = 0; i < m_tabbedPane.getTabCount(); i++) {
        final CGraphPanel panel = (CGraphPanel) m_tabbedPane.getComponentAt(i);

        panel.getToolbar().getSearchPanel().showResultsDialog(i == index);
      }
    }
  }

  /**
   * Keeps track of changes in views and updated the window if something important happened.
   */
  private class InternalViewListener extends CViewListenerAdapter {
    @Override
    public void changedDescription(final INaviView view, final String description) {
      updateCurrentWindowTitle(view);
    }

    @Override
    public void changedName(final INaviView view, final String name) {
      updateCurrentWindowTitle(view);
      m_tabbedPane.updateRegisterHeaders();
    }
  }

  /**
   * Listener that provides a workaround for setting the correct window size.
   */
  private class WorkaroundListener implements WindowStateListener {
    // The workaround here is that we have to create the window with the size
    // it has when it is maximized. When the user then un-maximizes the window,
    // we need to set the unmaximized size manually.

    /**
     * Flag that says whether a window minimization operation is the first minimization operation of
     * this window.
     */
    private boolean firstShrinking = true;

    @Override
    public void windowStateChanged(final WindowEvent event) {
      if (firstShrinking && event.getNewState() == NORMAL) {
        setSize((int) (getWidth() * 0.75), (int) (getHeight() * 0.75));

        firstShrinking = false;
      }
    }
  }
}
