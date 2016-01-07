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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Views.Component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.google.common.base.Preconditions;

/**
 * This class is used to draw the breakpoint icon into function tables.
 */
public final class CBreakpointImage extends BufferedImage {
  /**
   * Stroke used to draw the breakpoint image.
   */
  private static final BasicStroke DEFAULT_STROKE = new BasicStroke(2);

  /**
   * Creates a new breakpoint image.
   * 
   * @param backgroundColor The background color of the breakpoint image.
   * @param breakpointColor The breakpoint color of the breakpoint image.
   */
  public CBreakpointImage(final Color backgroundColor, final Color breakpointColor) {
    super(16, 16, BufferedImage.TYPE_INT_RGB);

    Preconditions.checkNotNull(backgroundColor,
        "IE02021: Background color argument can not be null");
    Preconditions.checkNotNull(breakpointColor,
        "IE02022: Breakpoint color argument can not be null");

    final Graphics2D g2d = createGraphics();

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setStroke(DEFAULT_STROKE);

    g2d.setColor(backgroundColor);
    g2d.fillRect(0, 0, getHeight(), getWidth());

    g2d.setColor(breakpointColor);
    g2d.fillOval(4, 4, 8, 8);

    g2d.setColor(Color.BLACK);
    g2d.drawOval(4, 4, 8, 8);
  }
}
