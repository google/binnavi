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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphCloser;

/**
 * Used to handle close events of the main window.
 */
public final class CWindowCloser extends WindowAdapter {
  /**
   * he graph pane of the window.
   */
  private final JGraphTab m_tabbedPane;

  /**
   * Creates a new window closer object.
   *
   * @param graphPane The graph pane of the window.
   */
  public CWindowCloser(final JGraphTab graphPane) {
    Preconditions.checkNotNull(graphPane, "IE01636: Graph pane argument can not be null");

    m_tabbedPane = graphPane;
  }

  @Override
  public void windowClosing(final WindowEvent event) {
    CGraphCloser.close(m_tabbedPane);
  }
}
