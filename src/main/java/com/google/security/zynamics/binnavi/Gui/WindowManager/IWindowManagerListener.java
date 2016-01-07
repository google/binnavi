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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;

/**
 * Interface to be implemented by all classes that want to be notified about changes in open graph
 * windows.
 */
public interface IWindowManagerListener {
  /**
   * Invoked after a graph window was closed.
   *
   * @param windowManager The window manager that managed the closed window.
   * @param window The window that was closed.
   */
  void windowClosed(CWindowManager windowManager, IGraphContainerWindow window);

  /**
   * Invoked after a graph window was opened.
   *
   * @param windowManager The window manager that manages the new window.
   * @param window The window that was opened.
   */
  void windowOpened(CWindowManager windowManager, IGraphContainerWindow window);
}
