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

/**
 * Interface for all classes that want to be notified when graph panels are opened or closed.
 */
public interface IGraphWindowListener {
  /**
   * Invoked after a graph panel was closed.
   */
  void graphPanelClosed();

  /**
   * Invoked after a graph panel was opened.
   *
   * @param graphPanel The graph panel that was opened.
   */
  void graphPanelOpened(CGraphPanel graphPanel);
}
