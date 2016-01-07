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
package com.google.security.zynamics.binnavi.api2.plugins;

import com.google.security.zynamics.binnavi.API.gui.GraphFrame;
import com.google.security.zynamics.binnavi.api2.IPluginInterface;

import java.util.List;

import javax.swing.JComponent;

// / Used to extend the main menu of graph windows.
/**
 * This interface can be implemented by all plugins that want to extend the main menu of graph
 * windows.
 */
public interface IGraphMenuPlugin extends IPlugin<IPluginInterface> {
  // ! Invoked after a graph closed.
  /**
   * Invoked after a graph frame was closed. This gives the plugin the opportunity to free allocated
   * resources.
   * 
   * @param graphFrame Frame that was closed.
   */
  void closed(GraphFrame graphFrame);

  // ! Extends the graph window plugin menu.
  /**
   * Returns the components that should be added to the main menu of the graph window.
   * 
   * @param graphFrame Frame where the view is shown.
   * 
   * @return A list of components that are added to the plugin menu.
   */
  List<JComponent> extendPluginMenu(GraphFrame graphFrame);
}
