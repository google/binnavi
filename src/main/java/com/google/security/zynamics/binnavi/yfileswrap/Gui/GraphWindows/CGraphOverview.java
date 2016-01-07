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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.GraphWindows;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraphLayeredRenderer;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyOverview;

import y.view.Graph2DView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;


/**
 * Panel class that displays a small graph overview in each graph panel.
 */
public final class CGraphOverview extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6219846624898744131L;

  /**
   * Default fogginess of the overview.
   */
  private static final float DEFAULT_FOG_COLOR = .90f;

  /**
   * Default height and width of the overview panel in pixels.
   */
  private static final int DEFAULT_SIZE = 200;

  /**
   * Creates a new overview panel.
   *
   * @param view The view to be displayed in the panel.
   */
  public CGraphOverview(final ZyGraph zygraph) {
    super(new BorderLayout());
    Graph2DView view = zygraph.getView();

    Preconditions.checkNotNull(view, "IE01616: View argument can not be null");

    setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Overview"));

    final ZyOverview overview = new ZyOverview(view);
    overview.setGraph2DRenderer(new ZyGraphLayeredRenderer<ZyOverview>(overview));

    // animates the scrolling
    overview.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);

    // blurs the part of the graph which can currently not be seen
    overview.putClientProperty("Overview.PaintStyle", "Funky");

    // allows zooming from within the overview
    overview.putClientProperty("Overview.AllowZooming", Boolean.TRUE);

    // provides functionality for navigation via keyboard (zoom in (+), zoom out (-), navigation
    // with arrow keys)
    overview.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);

    // determines how to differ between the part of the graph that can currently be seen, and the
    // rest
    overview.putClientProperty("Overview.Inverse", Boolean.TRUE);

    // determines the border color of the border between seen and unseen regions of the graph
    overview.putClientProperty("Overview.BorderColor", Color.lightGray);

    // determines the degree of blurriness
    overview.putClientProperty("Overview.funkyTheta", new Double(0.9));

    // determines the color of the part of the graph that can currently not be seen
    overview.putClientProperty("Overview.FogColor",
        new Color(DEFAULT_FOG_COLOR, DEFAULT_FOG_COLOR, DEFAULT_FOG_COLOR, 0.5f));

    overview.setAntialiasedPainting(true);
    overview.setDoubleBuffered(true);
    overview.setPreferredSize(new Dimension(DEFAULT_SIZE, DEFAULT_SIZE));
    overview.setMinimumSize(new Dimension(0, 0)); // required to restore collapsed split panes later

    add(overview, BorderLayout.CENTER);
  }
}
