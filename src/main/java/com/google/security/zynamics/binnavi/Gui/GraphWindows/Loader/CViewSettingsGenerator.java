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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Loader;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.ZyGraph.CViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.general.Convert;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.CircularStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicStyle;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalStyle;

import java.text.ParseException;
import java.util.Map;



/**
 * Contains helper functions for converting graph settings strings into their native setting object
 * types.
 */
public final class CViewSettingsGenerator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CViewSettingsGenerator() {
  }

  /**
   * Turns a settings string value into a boolean value.
   *
   * @param rawSettings Map of settings strings.
   * @param settingName Name of the settings string to convert.
   * @param defaultValue Default value in case conversion fails.
   *
   * @return The converted settings value.
   */
  private static boolean createBooleanSetting(
      final Map<String, String> rawSettings, final String settingName, final boolean defaultValue) {
    final String settingString = rawSettings.get(settingName);

    if (settingString == null) {
      return defaultValue;
    } else {
      try {
        return Boolean.parseBoolean(settingString);
      } catch (final NumberFormatException exception) {
        CUtilityFunctions.logException(exception);

        return defaultValue;
      }
    }
  }

  /**
   * Turns a settings string value into an enumeration value.
   *
   * @param <T> Type of the returned enumeration value.
   *
   * @param rawSettings Map of settings strings.
   * @param settingName Name of the settings string to convert.
   * @param defaultValue Default value in case conversion fails.
   * @param parser Parser used for setting conversion.
   *
   * @return The converted settings value.
   */
  private static <T> T createEnumerationSetting(final Map<String, String> rawSettings,
      final String settingName, final T defaultValue, final IParser<T> parser) {
    final String settingString = rawSettings.get(settingName);

    if ((settingString != null) && Convert.isDecString(settingString)) {
      // ESCA-JAVA0166: Catch Exception because a listener function is called
      try {
        return parser.parse(Integer.parseInt(settingString));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);

        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }

  /**
   * Turns a settings string value into a double value.
   *
   * @param rawSettings Map of settings strings.
   * @param settingName Name of the settings string to convert.
   * @param defaultValue Default value in case conversion fails.
   *
   * @return The converted settings value.
   */
  public static double createDoubleSetting(
      final Map<String, String> rawSettings, final String settingName, final double defaultValue) {
    final String settingString = rawSettings.get(settingName);

    if (settingString == null) {
      return defaultValue;
    } else {
      try {
        return Double.parseDouble(settingString);
      } catch (final NumberFormatException exception) {
        CUtilityFunctions.logException(exception);

        return defaultValue;
      }
    }
  }

  /**
   * Turns a settings string value into an integer value.
   *
   * @param rawSettings Map of settings strings.
   * @param settingName Name of the settings string to convert.
   * @param defaultValue Default value in case conversion fails.
   *
   * @return The converted settings value.
   */
  public static int createIntegerSetting(
      final Map<String, String> rawSettings, final String settingName, final int defaultValue) {
    final String settingString = rawSettings.get(settingName);

    if ((settingString != null) && Convert.isDecString(settingString)) {
      try {
        return Integer.parseInt(settingString);
      } catch (final NumberFormatException exception) {
        CUtilityFunctions.logException(exception);

        return defaultValue;
      }
    } else {
      return defaultValue;
    }
  }

  /**
   * Creates the graph settings for a given view.
   *
   * @param view The view whose graph settings are generated.
   *
   * @return A pair of raw settings and the generated graph settings object.
   *
   * @throws CouldntLoadDataException Thrown if the view settings could not be loaded.
   */
  public static Pair<Map<String, String>, ZyGraphViewSettings> createSettings(final INaviView view)
      throws CouldntLoadDataException {
    final ZyGraphViewSettings defaultSettings = view.getGraphType() == GraphType.CALLGRAPH
        ? ConfigManager.instance().getDefaultCallGraphSettings()
        : ConfigManager.instance().getDefaultFlowGraphSettings();

    final Map<String, String> rawSettings = view.loadSettings();

    final ZyGraphViewSettings settings = new ZyGraphViewSettings(defaultSettings);

    settings.getDisplaySettings().setAnimationSpeed(createIntegerSetting(
        rawSettings, CViewSettings.ANIMATION_SPEED,
        defaultSettings.getDisplaySettings().getAnimationSpeed()));
    settings.getLayoutSettings().setAutolayoutActivisionThreshold(createIntegerSetting(
        rawSettings, CViewSettings.AUTOLAYOUT_THRESHOLD,
        defaultSettings.getLayoutSettings().getAutolayoutDeactivationThreshold()));
    settings.getLayoutSettings().setAutomaticLayouting(createBooleanSetting(
        rawSettings, CViewSettings.AUTOMATIC_LAYOUTING,
        defaultSettings.getLayoutSettings().getAutomaticLayouting()));

    settings.getLayoutSettings().getCircularSettings().setStyle(createEnumerationSetting(
        rawSettings, CViewSettings.CIRCULAR_LAYOUT_STYLE,
        defaultSettings.getLayoutSettings().getCircularSettings().getStyle(),
        new IParser<CircularStyle>() {
          @Override
          public CircularStyle parse(final int value) {
            return CircularStyle.parseInt(value);
          }
        }));

    settings.getEdgeSettings().setDisplayMultipleEdgesAsOne(createBooleanSetting(
        rawSettings, CViewSettings.DISPLAY_MULTIPLE_EDGES_AS_ONE,
        defaultSettings.getEdgeSettings().getDisplayMultipleEdgesAsOne()));
    settings.getDisplaySettings().setFunctionNodeInformation(createBooleanSetting(
        rawSettings, CViewSettings.FUNCTION_NODE_INFORMATION,
        defaultSettings.getDisplaySettings().getFunctionNodeInformation()));
    settings.getDisplaySettings().setGradientBackground(createBooleanSetting(
        rawSettings, CViewSettings.GRADIENT_BACKGROUND,
        defaultSettings.getDisplaySettings().getGradientBackground()));

    settings.getLayoutSettings().getHierarchicalSettings().setStyle(createEnumerationSetting(
        rawSettings, CViewSettings.HIERARCHIC_LAYOUT_STYLE,
        defaultSettings.getLayoutSettings().getHierarchicalSettings().getStyle(),
        new IParser<HierarchicStyle>() {
          @Override
          public HierarchicStyle parse(final int value) {
            return HierarchicStyle.parseInt(value);
          }
        }));

    settings.getLayoutSettings().setAnimateLayout(createBooleanSetting(
        rawSettings, CViewSettings.LAYOUT_ANIMATION,
        defaultSettings.getLayoutSettings().getAnimateLayout()));
    settings.getLayoutSettings().setLayoutCalculationTimeWarningThreshold(createIntegerSetting(
        rawSettings, CViewSettings.LAYOUT_CALCULATION_TRESHOLD,
        defaultSettings.getLayoutSettings().getLayoutCalculationTimeWarningThreshold()));
    settings.getLayoutSettings().getCircularSettings().setMinimumNodeDistance(createIntegerSetting(
        rawSettings, CViewSettings.MINIMUM_CIRCULAR_NODE_DISTANCE,
        defaultSettings.getLayoutSettings().getCircularSettings().getMinimumNodeDistance()));
    settings.getLayoutSettings()
        .getHierarchicalSettings().setMinimumEdgeDistance(createIntegerSetting(rawSettings,
            CViewSettings.MINIMUM_HIERARCHIC_EDGE_DISTANCE, defaultSettings.getLayoutSettings()
                .getHierarchicalSettings().getMinimumEdgeDistance()));
    settings.getLayoutSettings()
        .getHierarchicalSettings().setMinimumLayerDistance(createIntegerSetting(rawSettings,
            CViewSettings.MINIMUM_HIERARCHIC_LAYER_DISTANCE, defaultSettings.getLayoutSettings()
                .getHierarchicalSettings().getMinimumLayerDistance()));
    settings.getLayoutSettings()
        .getHierarchicalSettings().setMinimumNodeDistance(createIntegerSetting(rawSettings,
            CViewSettings.MINIMUM_HIERARCHIC_NODE_DISTANCE, defaultSettings.getLayoutSettings()
                .getHierarchicalSettings().getMinimumNodeDistance()));
    settings.getLayoutSettings()
        .getOrthogonalSettings().setMinimumNodeDistance(createIntegerSetting(
            rawSettings, CViewSettings.MINIMUM_ORTHOGONAL_NODE_DISTANCE,
            defaultSettings.getLayoutSettings().getOrthogonalSettings().getMinimumNodeDistance()));

    settings.getMouseSettings().setMousewheelAction(createEnumerationSetting(
        rawSettings, CViewSettings.MOUSEWHEEL_ACTION,
        defaultSettings.getMouseSettings().getMouseWheelAction(), new IParser<MouseWheelAction>() {
          @Override
          public MouseWheelAction parse(final int value) throws ParseException {
            return MouseWheelAction.parseInt(value);
          }
        }));

    settings.getLayoutSettings().getOrthogonalSettings().setStyle(createEnumerationSetting(
        rawSettings, CViewSettings.ORTHOGONAL_LAYOUT_STYLE,
        defaultSettings.getLayoutSettings().getOrthogonalSettings().getStyle(),
        new IParser<OrthogonalStyle>() {
          @Override
          public OrthogonalStyle parse(final int value) {
            return OrthogonalStyle.parseInt(value);
          }
        }));

    settings.getLayoutSettings().getOrthogonalSettings().setOrientation(createEnumerationSetting(
        rawSettings, CViewSettings.ORTHOGONAL_ORIENTATION,
        defaultSettings.getLayoutSettings().getOrthogonalSettings().getOrientation(),
        new IParser<OrthogonalOrientation>() {
          @Override
          public OrthogonalOrientation parse(final int value) {
            return OrthogonalOrientation.parseInt(value);
          }
        }));

    settings.getProximitySettings().setProximityBrowsing(createBooleanSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING,
        defaultSettings.getProximitySettings().getProximityBrowsing()));
    settings.getProximitySettings().setProximityBrowsingActivationThreshold(createIntegerSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING_THRESHOLD,
        defaultSettings.getProximitySettings().getProximityBrowsingActivationThreshold()));
    settings.getProximitySettings().setProximityBrowsingChildren(createIntegerSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING_CHILDREN,
        defaultSettings.getProximitySettings().getProximityBrowsingChildren()));
    settings.getProximitySettings().setProximityBrowsingFrozen(createBooleanSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING_FROZEN,
        defaultSettings.getProximitySettings().getProximityBrowsingFrozen()));
    settings.getProximitySettings().setProximityBrowsingPreview(createBooleanSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING_PREVIEW,
        defaultSettings.getProximitySettings().getProximityBrowsingPreview()));
    settings.getProximitySettings().setProximityBrowsingParents(createIntegerSetting(
        rawSettings, CViewSettings.PROXIMITY_BROWSING_PARENTS,
        defaultSettings.getProximitySettings().getProximityBrowsingParents()));
    settings.getMouseSettings().setScrollSensitivity(createIntegerSetting(
        rawSettings, CViewSettings.SCROLL_SENSIBILITY,
        defaultSettings.getMouseSettings().getScrollSensitivity()));
    settings.getSearchSettings().setSearchCaseSensitive(createBooleanSetting(
        rawSettings, CViewSettings.SEARCH_CASE_SENSITIVE,
        defaultSettings.getSearchSettings().getSearchCaseSensitive()));
    settings.getSearchSettings().setSearchRegEx(createBooleanSetting(
        rawSettings, CViewSettings.SEARCH_REGEX,
        defaultSettings.getSearchSettings().getSearchRegEx()));
    settings.getSearchSettings().setSearchSelectedNodesOnly(createBooleanSetting(
        rawSettings, CViewSettings.SEARCH_SELECTED_ONLY,
        defaultSettings.getSearchSettings().getSearchSelectedNodesOnly()));
    settings.getSearchSettings().setSearchVisibleNodesOnly(createBooleanSetting(
        rawSettings, CViewSettings.SEARCH_VISIBLE_ONLY,
        defaultSettings.getSearchSettings().getSearchVisibleNodesOnly()));
    settings.getMouseSettings().setZoomSensitivity(createIntegerSetting(
        rawSettings, CViewSettings.ZOOM_SENSIBILITY,
        defaultSettings.getMouseSettings().getZoomSensitivity()));
    settings.getDisplaySettings().setSimplifiedVariableAccess(createBooleanSetting(
        rawSettings, CViewSettings.SIMPLIFIED_VARIABLE_ACCESS,
        defaultSettings.getDisplaySettings().getSimplifiedVariableAccess()));

    return new Pair<Map<String, String>, ZyGraphViewSettings>(rawSettings, settings);
  }

}
