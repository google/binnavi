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
package com.google.security.zynamics.binnavi.API.gui;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;


// / Window where graphs are shown.
/**
 * Represents windows in which the graph frames are shown.
 */
public final class GraphWindow implements ApiObject<CGraphWindow> {
  /**
   * Wrapped internal graph window object.
   */
  private final CGraphWindow m_window;

  // / @cond INTERNAL
  /**
   * Creates a new API graph window object.
   *
   * @param window Wrapped internal graph window object.
   */
  // / @endcond
  public GraphWindow(final CGraphWindow window) {
    m_window = window;
  }

  @Override
  public CGraphWindow getNative() {
    return m_window;
  }

  // ! Frame object of the graph window.
  /**
   * Returns the frame object of the graph window. This frame object can be used as the parent
   * window of message boxes and other dialogs.
   *
   * @return The frame object of the graph window.
   */
  public JFrame getFrame() {
    return m_window;
  }

  // ! Printable representation of the graph window.
  /**
   * Returns a string representation of the graph window.
   *
   * @return A string representation of the graph window.
   */
  @Override
  public String toString() {
    return "Graph Window";
  }
}
