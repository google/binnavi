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

import java.util.HashMap;

import y.view.Graph2DView;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.ZyGraph.CViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Contains functions related to view settings.
 */
public final class CSettingsFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CSettingsFunctions() {
  }

  /**
   * Saves the settings of a view to the database.
   *
   * @param view The view whose settings are saved.
   * @param view2d The yFiles view that shows the given view.
   * @param settings The settings to be stored.
   */
  public static void saveSettings(
      final INaviView view, final Graph2DView view2d, final ZyGraphViewSettings settings) {
    Preconditions.checkNotNull(view, "IE02132: View argument can not be null");
    Preconditions.checkNotNull(view2d, "IE02133: View2D argument can not be null");
    Preconditions.checkNotNull(settings, "IE02134: Settings argument can not be null");

    final HashMap<String, String> settingsMap = new HashMap<String, String>();

    settingsMap.put(CViewSettings.ANIMATION_SPEED,
        String.valueOf(settings.getDisplaySettings().getAnimationSpeed()));
    settingsMap.put(CViewSettings.AUTOLAYOUT_THRESHOLD,
        String.valueOf(settings.getLayoutSettings().getAutolayoutDeactivationThreshold()));
    settingsMap.put(CViewSettings.AUTOMATIC_LAYOUTING,
        String.valueOf(settings.getLayoutSettings().getAutomaticLayouting()));
    settingsMap.put(CViewSettings.CIRCULAR_LAYOUT_STYLE,
        String.valueOf(settings.getLayoutSettings().getCircularSettings().getStyle().ordinal()));
    settingsMap.put(CViewSettings.DISPLAY_MULTIPLE_EDGES_AS_ONE,
        String.valueOf(settings.getEdgeSettings().getDisplayMultipleEdgesAsOne()));
    settingsMap.put(CViewSettings.FUNCTION_NODE_INFORMATION,
        String.valueOf(settings.getDisplaySettings().getFunctionNodeInformation()));
    settingsMap.put(CViewSettings.GRADIENT_BACKGROUND,
        String.valueOf(settings.getDisplaySettings().getGradientBackground()));
    settingsMap.put(CViewSettings.HIERARCHIC_LAYOUT_STYLE, String.valueOf(
        settings.getLayoutSettings().getHierarchicalSettings().getStyle().ordinal()));
    settingsMap.put(CViewSettings.LAYOUT_ANIMATION,
        String.valueOf(settings.getLayoutSettings().getAnimateLayout()));
    settingsMap.put(CViewSettings.LAYOUT_CALCULATION_TRESHOLD,
        String.valueOf(settings.getLayoutSettings().getLayoutCalculationTimeWarningThreshold()));
    settingsMap.put(CViewSettings.MINIMUM_CIRCULAR_NODE_DISTANCE, String.valueOf(
        settings.getLayoutSettings().getCircularSettings().getMinimumNodeDistance()));
    settingsMap.put(CViewSettings.MINIMUM_HIERARCHIC_EDGE_DISTANCE, String.valueOf(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumEdgeDistance()));
    settingsMap.put(CViewSettings.MINIMUM_HIERARCHIC_LAYER_DISTANCE, String.valueOf(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumLayerDistance()));
    settingsMap.put(CViewSettings.MINIMUM_HIERARCHIC_NODE_DISTANCE, String.valueOf(
        settings.getLayoutSettings().getHierarchicalSettings().getMinimumNodeDistance()));
    settingsMap.put(CViewSettings.MINIMUM_ORTHOGONAL_NODE_DISTANCE, String.valueOf(
        settings.getLayoutSettings().getOrthogonalSettings().getMinimumNodeDistance()));
    settingsMap.put(CViewSettings.MOUSEWHEEL_ACTION,
        String.valueOf(settings.getMouseSettings().getMouseWheelAction().ordinal()));
    settingsMap.put(CViewSettings.ORTHOGONAL_LAYOUT_STYLE,
        String.valueOf(settings.getLayoutSettings().getOrthogonalSettings().getStyle().ordinal()));
    settingsMap.put(CViewSettings.ORTHOGONAL_ORIENTATION, String.valueOf(
        settings.getLayoutSettings().getOrthogonalSettings().getOrientation().ordinal()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING,
        String.valueOf(settings.getProximitySettings().getProximityBrowsing()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING_THRESHOLD,
        String.valueOf(settings.getProximitySettings().getProximityBrowsingActivationThreshold()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING_CHILDREN,
        String.valueOf(settings.getProximitySettings().getProximityBrowsingChildren()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING_FROZEN,
        String.valueOf(settings.getProximitySettings().getProximityBrowsingFrozen()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING_PARENTS,
        String.valueOf(settings.getProximitySettings().getProximityBrowsingParents()));
    settingsMap.put(CViewSettings.PROXIMITY_BROWSING_PREVIEW,
        String.valueOf(settings.getProximitySettings().getProximityBrowsingPreview()));
    settingsMap.put(CViewSettings.SCROLL_SENSIBILITY,
        String.valueOf(settings.getMouseSettings().getScrollSensitivity()));
    settingsMap.put(CViewSettings.SEARCH_CASE_SENSITIVE,
        String.valueOf(settings.getSearchSettings().getSearchCaseSensitive()));
    settingsMap.put(
        CViewSettings.SEARCH_REGEX, String.valueOf(settings.getSearchSettings().getSearchRegEx()));
    settingsMap.put(CViewSettings.SEARCH_SELECTED_ONLY,
        String.valueOf(settings.getSearchSettings().getSearchSelectedNodesOnly()));
    settingsMap.put(CViewSettings.SEARCH_VISIBLE_ONLY,
        String.valueOf(settings.getSearchSettings().getSearchVisibleNodesOnly()));
    settingsMap.put(CViewSettings.ZOOM_SENSIBILITY,
        String.valueOf(settings.getMouseSettings().getZoomSensitivity()));
    settingsMap.put(CViewSettings.SIMPLIFIED_VARIABLE_ACCESS,
        String.valueOf(settings.getDisplaySettings().getSimplifiedVariableAccess()));

    // Not really graph settings => No CViewSettings constants
    settingsMap.put("view_center_x", String.valueOf(view2d.getCenter().getX()));
    settingsMap.put("view_center_y", String.valueOf(view2d.getCenter().getY()));
    settingsMap.put("world_rect_x", String.valueOf(view2d.getWorldRect().x));
    settingsMap.put("world_rect_y", String.valueOf(view2d.getWorldRect().y));
    settingsMap.put("world_rect_width", String.valueOf(view2d.getWorldRect().width));
    settingsMap.put("world_rect_height", String.valueOf(view2d.getWorldRect().height));
    settingsMap.put("zoom", String.valueOf(view2d.getZoom()));

    try {
      // TODO: Pass this exception to the outside?

      view.saveSettings(settingsMap);
    } catch (final CouldntSaveDataException e) {
      CUtilityFunctions.logException(e);
    }
  }

}
