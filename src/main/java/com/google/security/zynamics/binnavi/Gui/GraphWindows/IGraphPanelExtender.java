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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;

import javax.swing.JPanel;

/**
 * Interface to be used by classes that want to extend the tabbed pane shown at the bottom of graph
 * windows.
 */
public interface IGraphPanelExtender {
  /**
   * Adds a new tab to the tabbed pane.
   *
   * @param string Title of the new tab.
   * @param panel Component shown in the new tab.
   */
  void addTab(String string, JPanel panel);

  /**
   * Opens a view in a graph panel.
   *
   * @param container Context in which the view is opened.
   * @param view The view to be opened.
   */
  void openView(IViewContainer container, INaviView view);

  /**
   * Registers a component that extends the context menu of code nodes.
   *
   * @param extension The extension object.
   */
  @Deprecated // Replace by ICodeNodePlugin or something
  void registerCodeNodeExtension(ICodeNodeExtension extension);

  /**
   * Activates a given tab.
   *
   * @param panel The component in the tab to activate.
   */
  void selectTab(JPanel panel);
}
