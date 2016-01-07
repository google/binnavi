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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Implementations;

import java.awt.Color;

import javax.swing.ImageIcon;

import y.view.DefaultBackgroundRenderer;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;

/**
 * Contains helper functions for layouting graph views.
 */
public final class CLayoutFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CLayoutFunctions() {
  }

  /**
   * Updates the background depending on the graph settings.
   *
   * @param graph The graph whose backround is updated.
   */
  public static void updateBackground(final ZyGraph graph) {
    Preconditions.checkNotNull(graph, "IE02126: Graph argument can not be null");

    // TODO: Buffer and share the background gradient between views

    final DefaultBackgroundRenderer dBGRender = new DefaultBackgroundRenderer(graph.getView());

    if (graph.getSettings().getDisplaySettings().getGradientBackground()) {
      dBGRender.setImage(
          new ImageIcon(CMain.class.getResource("data/gradientbackground2.jpg")).getImage());
      dBGRender.setMode(DefaultBackgroundRenderer.FULLSCREEN);
    } else {
      dBGRender.setColor(new Color(250, 250, 250));
    }

    graph.getView().setBackgroundRenderer(dBGRender);

    graph.updateViews();
  }

}
