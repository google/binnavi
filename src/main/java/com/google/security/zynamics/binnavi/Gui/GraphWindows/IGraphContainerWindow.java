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

import javax.swing.JFrame;

/**
 * Interface for graph window objects where one or more graph panels are shown.
 */
public interface IGraphContainerWindow extends Iterable<IGraphPanel> {
  /**
   * Activates a given panel.
   *
   * @param panel The panel to activate.
   */
  void activate(IGraphPanel panel);

  /**
   * Adds another graph to the container.
   *
   * @param graph The graph to add to the container.
   */
  void addGraph(IGraphPanel graph);

  /**
   * Adds a listener object that is notified about changes in the graph window.
   *
   * @param listener The listener object to add.
   */
  void addListener(final IGraphWindowListener listener);

  /**
   * Closes a graph panel.
   *
   * @param panel The panel to close.
   */
  void close(IGraphPanel panel);

  /**
   * Returns the frame object of the window.
   *
   * @return The frame object of the window.
   */
  JFrame getFrame();

  /**
   * Returns the number of open graphs.
   *
   * @return The number of open graphs.
   */
  int getOpenGraphCount();

  /**
   * Removes a listener object that was previously notified about changes in the graph window.
   *
   * @param listener The listener to remove.
   */
  void removeListener(final IGraphWindowListener listener);

  /**
   * Brings the graph window to the foreground.
   */
  void show();
}
