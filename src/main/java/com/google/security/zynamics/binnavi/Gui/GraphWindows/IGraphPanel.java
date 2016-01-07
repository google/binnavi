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

import java.awt.Component;

import javax.swing.JMenuBar;

/**
 * Interface for objects that want to represent graph panels.
 */
public interface IGraphPanel {
  /**
   * Closes the graph panel.
   *
   * @param askSave Flag that specifies whether the user is asked whether he wants to close the
   *        graph panel.
   */
  void close(boolean askSave);

  /**
   * Frees allocated resources.
   */
  void dispose();

  /**
   * Returns the menu shown when the graph panel is active.
   *
   * @return The menu shown when the graph panel is active.
   */
  JMenuBar getMenu();

  /**
   * Returns the model that describes the graph shown in the panel.
   *
   * @return The model that describes the graph shown in the panel.
   */
  CGraphModel getModel();

  /**
   * Returns the panel component.
   *
   * @return The panel component.
   */
  Component getPanel();
}
