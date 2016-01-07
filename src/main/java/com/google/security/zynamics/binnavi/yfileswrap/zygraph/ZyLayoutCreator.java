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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph;

import y.layout.CanonicMultiStageLayouter;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Settings.ZyGraphLayoutSettings;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.CircularStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalStyle;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.layouters.ZyGraphLayouter;

/**
 * Creates layouter objects from layout settings.
 */
public final class ZyLayoutCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private ZyLayoutCreator() {
  }

  /**
   * Creates a circular layouter from the current settings.
   * 
   * @param settings The settings object to create the layouter from.
   * 
   * @return The created layouter.
   */
  public static CanonicMultiStageLayouter getCircularLayout(final ZyGraphLayoutSettings settings) {
    Preconditions.checkNotNull(settings, "IE00894: Settings argument can not be null");

    final CircularStyle style = settings.getCircularSettings().getStyle();
    final long minNodeDist = settings.getCircularSettings().getMinimumNodeDistance();

    return ZyGraphLayouter.createCircularLayouter(style, minNodeDist);
  }

  /**
   * Creates a hierarchic layouter from the current settings.
   * 
   * @param settings The settings object to create the layouter from.
   * 
   * @return The created layouter.
   */
  public static CanonicMultiStageLayouter getHierarchicLayout(final ZyGraphLayoutSettings settings) {
    Preconditions.checkNotNull(settings, "IE00895: Settings argument can not be null");

    final HierarchicStyle style = settings.getHierarchicalSettings().getStyle();
    final long minLayerDist = settings.getHierarchicalSettings().getMinimumLayerDistance();
    final long minNodeDist = settings.getHierarchicalSettings().getMinimumNodeDistance();
    final long minEdgeDist = settings.getHierarchicalSettings().getMinimumEdgeDistance();
    final long minNodeEdgeDist = settings.getHierarchicalSettings().getMinimumNodeEdgeDistance();
    final HierarchicOrientation orientation = settings.getHierarchicalSettings().getOrientation();

    return ZyGraphLayouter.createHierarchicalLayouter(style, minLayerDist, minNodeDist,
        minEdgeDist, minNodeEdgeDist, orientation);
  }

  /**
   * Creates an orthogonal layouter from the current settings.
   * 
   * @param settings The settings object to create the layouter from.
   * 
   * @return The created layouter.
   */
  public static CanonicMultiStageLayouter getOrthogonalLayout(final ZyGraphLayoutSettings settings) {
    Preconditions.checkNotNull(settings, "IE00896: Settings argument can not be null");

    final OrthogonalStyle style = settings.getOrthogonalSettings().getStyle();
    final long gridSize = settings.getOrthogonalSettings().getMinimumNodeDistance();

    return ZyGraphLayouter.createOrthoLayouter(style, gridSize, true);
  }
}
