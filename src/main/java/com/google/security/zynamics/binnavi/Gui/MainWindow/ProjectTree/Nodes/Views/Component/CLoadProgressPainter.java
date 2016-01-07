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

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;

import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabaseLoadProgressReporter;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.GraphBuilderEvents;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyGraphBuilderManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ViewLoadEvents;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Builders.ZyGraphBuilder;


/**
 * Makes it possible to paint view progress into table cells.
 */
public final class CLoadProgressPainter {
  /**
   * You are not supposed to instantiate this class.
   */
  private CLoadProgressPainter() {
  }

  /**
   * Calculates the load percentage of the rendered view to display a progress bar.
   * 
   * @param view The view to be loaded.
   * 
   * @return The calculated load percentage.
   */
  private static double getLoadPercentage(final INaviView view) {
    final int totalSteps = ViewLoadEvents.values().length + GraphBuilderEvents.values().length;

    final int loadState = view.getLoadState();

    if (loadState == IDatabaseLoadProgressReporter.INACTIVE) {
      // The view is not currently loading.

      try {
        // The view was previously loaded and only needs to be built.

        final ZyGraphBuilder builder = ZyGraphBuilderManager.instance().getBuilder(view);
        return (1.0 * (ViewLoadEvents.values().length + builder.getBuildStep())) / totalSteps;
      } catch (final MaybeNullException exception) {
        // The view is neither loading nor building.

        return 0;
      }
    } else {
      // The view is currenty loading.

      return (1.0 * view.getLoadState()) / totalSteps;
    }
  }

  /**
   * Paints view load progress onto a graphics canvas.
   * 
   * @param view The view whose progress is painted.
   * @param graphics The graphics canvas to paint on.
   * @param width Width of the canvas.
   * @param height Height of the canvas.
   * @param background Default background color of the canvas.
   */
  public static void paint(final INaviView view, final Graphics graphics, final int width,
      final int height, final Color background) {
    graphics.setColor(background);
    graphics.fillRect(0, 0, width, height);

    final GradientPaint paint =
        new GradientPaint(0, 0, Color.WHITE, width, height, new Color(0, 128, 0));
    ((Graphics2D) graphics).setPaint(paint);

    graphics.fillRect(0, 0, (int) (getLoadPercentage(view) * width), height);
  }
}
