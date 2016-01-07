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

import javax.swing.JComponent;

/**
 * Interface for panels that want to extend the bottom panel of graph windows.
 */
public interface IResultsPanel {
  /**
   * Adds a listener that is notified about changes in the results panel.
   *
   * @param listener The listener to add.
   */
  void addListener(IResultsPanelListener listener);

  /**
   * Frees allocated resources.
   */
  void dispose();

  /**
   * Returns the component shown in the tab.
   *
   * @return The component shown in the tab.
   */
  JComponent getComponent();

  /**
   * Title used in the tab of the bottom panel.
   *
   * @return The tab title.
   */
  String getTitle();

  /**
   * Removes a listener object.
   *
   * @param listener The listener to remove.
   */
  void removeListener(IResultsPanelListener listener);
}
