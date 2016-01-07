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
package com.google.security.zynamics.binnavi.Gui.WindowManager;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Window manager class that keeps track of open graph windows.
 */
public final class CWindowManager implements Iterable<CGraphWindow> {
  /**
   * Only globally valid instance of the window manager class.
   */
  private static CWindowManager m_instance = new CWindowManager();

  /**
   * Listeners notified about changes in graph windows.
   */
  private final ListenerProvider<IWindowManagerListener> m_listeners =
      new ListenerProvider<IWindowManagerListener>();

  /**
   * Open graph windows.
   */
  private final List<CGraphWindow> m_graphs = new ArrayList<CGraphWindow>();

  /**
   * Used to synchronize the window manager with the open graph windows.
   */
  private final InternalWindowListener m_listener = new InternalWindowListener();

  /**
   * Singleton class; please use {@link #instance} instead.
   */
  private CWindowManager() {
    // Singleton
  }

  /**
   * Returns the only valid instance of the window manager class.
   *
   * @return The only valid instance of the window manager class.
   */
  public static CWindowManager instance() {
    return m_instance;
  }

  /**
   * Unregisters a graph window when it is closed.
   *
   * @param window The graph window to unregister.
   */
  private void unregister(final CGraphWindow window) {
    if (!m_graphs.remove(window)) {
      throw new IllegalStateException("IE01206: Graph window wasn't registered");
    }

    for (final IWindowManagerListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because are notifying listeners.
      try {
        listener.windowClosed(this, window);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
        NaviLogger.severe("Internal Error: Listener caused an unknown exception");
      }
    }
  }

  /**
   * Adds a new listener object that is notified about changes in the window manager.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IWindowManagerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns the last opened graph window.
   *
   * @return The last opened graph window or null if no graph window is open.
   */
  public CGraphWindow getLastWindow() {
    return m_graphs.isEmpty() ? null : m_graphs.get(m_graphs.size() - 1);
  }

  /**
   * Returns a list of all open graph windows.
   *
   * @return A list of all open graph windows.
   */
  public List<CGraphWindow> getOpenWindows() {
    return new ArrayList<CGraphWindow>(m_graphs);
  }

  /**
   * Determines whether a graph view is open in some window.
   *
   * @param view The view to test.
   *
   * @return True, if the view is open. False, otherwise.
   */
  public boolean isOpen(final INaviView view) {
    Preconditions.checkNotNull(view, "IE01297: View argument can not be null");

    for (final IGraphContainerWindow graphContainer : getOpenWindows()) {
      for (final IGraphPanel window : graphContainer) {
        if (window.getModel().getGraph().getRawView() == view) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Brings the given {@link INaviView view} to front
   *
   * @param view The {@link INaviView view} to bring to front.
   */
  public void bringViewToFront(final INaviView view) {
    for (final CGraphWindow graphContainer : getOpenWindows()) {
      for (final IGraphPanel window : graphContainer) {
        if (window.getModel().getGraph().getRawView() == view) {
          graphContainer.activate(window);
          graphContainer.toFront();
          return;
        }
      }
    }
  }

  @Override
  public Iterator<CGraphWindow> iterator() {
    return new ArrayList<CGraphWindow>(m_graphs).iterator();
  }

  /**
   * Registers a new graph window with the window manager.
   *
   * @param window The graph window to register.
   */
  public void register(final CGraphWindow window) {
    Preconditions.checkNotNull(window, "IE02084: Graph window can't be null");

    if (m_graphs.contains(window)) {
      throw new IllegalStateException("IE01205: Cannot register graph window twice");
    }

    m_graphs.add(window);

    window.addWindowListener(m_listener);

    for (final IWindowManagerListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because are notifying listeners.
      try {
        listener.windowOpened(this, window);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }

  }

  /**
   * Removes a listener object from the window manager.
   *
   * @param listener The listener object to remove.
   */
  public void removeListener(final IWindowManagerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Window listener used to remove closed graph windows from the list of managed windows.
   */
  private class InternalWindowListener extends WindowAdapter {
    @Override
    public void windowClosed(final WindowEvent event) {
      final CGraphWindow window = (CGraphWindow) event.getSource();

      unregister(window);

      window.getFrame().removeWindowListener(this);
    }
  }
}
