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


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Class that can be used to create the proper window title of a graph window.
 */
public final class CWindowTitle {
  /**
   * You are not supposed to instantiate this class.
   */
  private CWindowTitle() {
  }

  /**
   * Generates the proper window title for the graph window depending on its current state.
   *
   * @param panel The active graph panel.
   *
   * @return The window title of the graph window.
   */
  public static String generate(final IGraphPanel panel) {
    Preconditions.checkNotNull(panel, "IE01637: Panel argument can not be null");

    final INaviView view = panel.getModel().getGraph().getRawView();

    final String containerName = panel.getModel().getViewContainer().getName();
    final String viewName = view.getName();
    final String viewDescription = view.getConfiguration().getDescription();

    if ("".equals(viewDescription)) {
      return String.format("%s - %s - %s", viewName, containerName, Constants.DEFAULT_WINDOW_TITLE);
    } else {
      return String.format("%s - %s - %s - %s", viewName, containerName, viewDescription,
          Constants.DEFAULT_WINDOW_TITLE);
    }
  }
}
