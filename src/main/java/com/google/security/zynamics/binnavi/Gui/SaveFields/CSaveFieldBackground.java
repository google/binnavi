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
package com.google.security.zynamics.binnavi.Gui.SaveFields;

import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.CUtilityFunctions;


/**
 * Provides the background texture shown in fields that were modified.
 */
public final class CSaveFieldBackground {
  /**
   * The background texture to show.
   */
  private TexturePaint m_texture;

  /**
   * Creates a new background object.
   */
  public CSaveFieldBackground() {
    if (m_texture == null) {
      try {
        final BufferedImage backgroundImage =
            ImageIO.read(CMain.class.getResource("data/unsaved_bg.png"));
        final Double rect =
            new Rectangle2D.Double(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());
        m_texture = new TexturePaint(backgroundImage, rect);
      } catch (final IOException e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Paints the background onto a canvas.
   * 
   * @param g2d The canvas to draw on.
   * @param width The width of the canvas.
   * @param height The height of the canvas.
   */
  public void paint(final Graphics2D g2d, final int width, final int height) {
    g2d.setPaint(m_texture);
    g2d.fillRect(0, 0, width, height);
  }
}
