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

import com.google.security.zynamics.binnavi.Gui.MainWindow.CProjectMainFrame;


// / BinNavi main window
/**
 * Represents the BinNavi main window.
 */
public final class MainWindow {
  /**
   * Wrapped internal main window object.
   */
  private final CProjectMainFrame m_window;

  // / @cond INTERNAL
  /**
   * Creates a new API main window object.
   * 
   * @param window Wrapped internal main window object.
   */
  // / @endcond
  public MainWindow(final CProjectMainFrame window) {
    m_window = window;
  }

  // ! Frame object of the main window.
  /**
   * Returns the frame object of the main window.
   * 
   * @return The frame object of the main window.
   */
  public JFrame getFrame() {
    return m_window;
  }

  // ! Printable representation of the main window.
  /**
   * Returns a string representation of the main window.
   * 
   * @return A string representation of the main window.
   */
  @Override
  public String toString() {
    return "BinNavi main window";
  }
}
