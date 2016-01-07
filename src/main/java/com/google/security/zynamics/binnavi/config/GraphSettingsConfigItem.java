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
package com.google.security.zynamics.binnavi.config;

import com.google.security.zynamics.common.config.AbstractConfigItem;
import com.google.security.zynamics.common.config.TypedPropertiesWrapper;

public class GraphSettingsConfigItem extends AbstractConfigItem {
  private static final String AUTOMATIC_LAYOUTING = "AutomaticLayouting";
  private static final boolean AUTOMATIC_LAYOUTING_DEFAULT = true;
  private boolean automaticLayouting = AUTOMATIC_LAYOUTING_DEFAULT;

  private static final String PROXIMITY_BROWSING = "ProximityBrowsing";
  private static final boolean PROXIMITY_BROWSING_DEFAULT = true;
  private boolean proximityBrowsing = PROXIMITY_BROWSING_DEFAULT;

  private static final String PROXIMITY_BROWSING_PREVIEW = "ProximityBrowsingPreview";
  private static final boolean PROXIMITY_BROWSING_PREVIEW_DEFAULT = true;
  private boolean proximityBrowsingPreview = PROXIMITY_BROWSING_PREVIEW_DEFAULT;

  private static final String PROXIMITY_BROWSING_THRESHOLD = "ProximityBrowsingThreshold";
  private static final int PROXIMITY_BROWSING_THRESHOLD_DEFAULT = 100;
  private int proximityBrowsingThreshold = PROXIMITY_BROWSING_THRESHOLD_DEFAULT;

  private static final String AUTOLAYOUT_DEACTIVATION_THRESHOLD = "AutoLayoutDeactivationThreshold";
  private static final int AUTOLAYOUT_DEACTIVATION_THRESHOLD_DEFAULT = 100;
  private int autoLayoutDeactivationThreshold = AUTOLAYOUT_DEACTIVATION_THRESHOLD_DEFAULT;

  private static final String LAYOUT_CALCULATION_THRESHOLD = "LayoutCalculationThreshold";
  private static final int LAYOUT_CALCULATION_THRESHOLD_DEFAULT = 250;
  private int layoutCalculationThreshold = LAYOUT_CALCULATION_THRESHOLD_DEFAULT;

  private static final String DEFAULT_GRAPH_LAYOUT = "DefaultGraphLayout";
  private static final int DEFAULT_GRAPH_LAYOUT_DEFAULT = 0;
  private int defaultGraphLayout = DEFAULT_GRAPH_LAYOUT_DEFAULT;

  private static final String VISIBILITY_WARNING_THRESHOLD = "VisibilityWarningThreshold";
  private static final int VISIBILITY_WARNING_THRESHOLD_DEFAULT = 150;
  private int visibilityWarningThreshold = VISIBILITY_WARNING_THRESHOLD_DEFAULT;

  private static final String MULTIPLE_EDGES_AS_ONE = "MultipleEdgesAsOne";
  private static final boolean MULTIPLE_EDGES_AS_ONE_DEFAULT = true;
  private boolean multipleEdgesAsOne = MULTIPLE_EDGES_AS_ONE_DEFAULT;

  private static final String DRAW_BENDS = "DrawBends";
  private static final boolean DRAW_BENDS_DEFAULT = false;
  private boolean drawBends = DRAW_BENDS_DEFAULT;

  private static final String EDGE_HIDING_MODE = "EdgeHidingMode";
  private static final int EDGE_HIDING_MODE_DEFAULT = 2;
  private int edgeHidingMode = EDGE_HIDING_MODE_DEFAULT;

  private static final String EDGE_HIDING_THRESHOLD = "EdgeHidingThreshold";
  private static final int EDGE_HIDING_THRESHOLD_DEFAULT = 2000;
  private int edgeHidingThreshold = EDGE_HIDING_THRESHOLD_DEFAULT;

  private static final String HIERARCHIC_ORIENTATION = "HierarchicOrientation";
  private static final int HIERARCHIC_ORIENTATION_DEFAULT = 1;
  private int hierarchicOrientation = HIERARCHIC_ORIENTATION_DEFAULT;

  private static final String HIERARCHIC_EDGE_ROUTING_STYLE = "HierarchicEdgeRoutingStyle";
  private static final int HIERARCHIC_EDGE_ROUTING_STYLE_DEFAULT = 0;
  private int hierarchicEdgeRoutingStyle = HIERARCHIC_EDGE_ROUTING_STYLE_DEFAULT;

  private static final String HIERARCHIC_MINIMUM_LAYER_DISTANCE = "HierarchicMinimumLayerDistance";
  private static final int HIERARCHIC_MINIMUM_LAYER_DISTANCE_DEFAULT = 75;
  private int hierarchicMinimumLayerDistance = HIERARCHIC_MINIMUM_LAYER_DISTANCE_DEFAULT;

  private static final String HIERARCHIC_MINIMUM_NODE_DISTANCE = "HierarchicMinimumNodeDistance";
  private static final int HIERARCHIC_MINIMUM_NODE_DISTANCE_DEFAULT = 25;
  private int hierarchicMinimumNodeDistance = HIERARCHIC_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String HIERARCHIC_MINIMUM_EDGE_DISTANCE = "HierarchicMinimumEdgeDistance";
  private static final int HIERARCHIC_MINIMUM_EDGE_DISTANCE_DEFAULT = 25;
  private int hierarchicMinimumEdgeDistance = HIERARCHIC_MINIMUM_EDGE_DISTANCE_DEFAULT;

  private static final String HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE =
      "HierarchicMinimumNodeEdgeDistance";
  private static final int HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE_DEFAULT = 25;
  private int hierarchicMinimumNodeEdgeDistance = HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE_DEFAULT;

  private static final String ORTHOGONAL_ORIENTATION = "OrthogonalOrientation";
  private static final int ORTHOGONAL_ORIENTATION_DEFAULT = 0;
  private int orthogonalOrientation = ORTHOGONAL_ORIENTATION_DEFAULT;

  private static final String ORTHOGONAL_LAYOUT_STYLE = "OrthogonalLayoutStyle";
  private static final int ORTHOGONAL_LAYOUT_STYLE_DEFAULT = 0;
  private int orthogonalLayoutStyle = ORTHOGONAL_LAYOUT_STYLE_DEFAULT;

  private static final String ORTHOGONAL_MINIMUM_NODE_DISTANCE = "OrthogonalMinimumNodeDistance";
  private static final int ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int orthogonalMinimumNodeDistance = ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String CIRCULAR_LAYOUT_STYLE = "CircularLayoutStyle";
  private static final int CIRCULAR_LAYOUT_STYLE_DEFAULT = 1;
  private int circularLayoutStyle = CIRCULAR_LAYOUT_STYLE_DEFAULT;

  private static final String CIRCULAR_MINIMUM_NODE_DISTANCE = "CircularMinimumNodeDistance";
  private static final int CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT = 50;
  private int circularMinimumNodeDistance = CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT;

  private static final String SCROLL_SENSITIVITY = "ScrollSensitivity";
  private static final int SCROLL_SENSITIVITY_DEFAULT = 4;
  private int scrollSensitivity = SCROLL_SENSITIVITY_DEFAULT;

  private static final String ZOOM_SENSITIVITY = "ZoomSensitivity";
  private static final int ZOOM_SENSITIVITY_DEFAULT = 4;
  private int zoomSensitivity = ZOOM_SENSITIVITY_DEFAULT;

  private static final String MOUSE_WHEEL_ACTION = "MouseWheelAction";
  private static final int MOUSE_WHEEL_ACTION_DEFAULT = 0;
  private int mouseWheelAction = MOUSE_WHEEL_ACTION_DEFAULT;

  private static final String LAYOUT_ANIMATION = "LayoutAnimation";
  private static final boolean LAYOUT_ANIMATION_DEFAULT = true;
  private boolean layoutAnimation = LAYOUT_ANIMATION_DEFAULT;

  private static final String ANIMATION_SPEED = "AnimationSpeed";
  private static final int ANIMATION_SPEED_DEFAULT = 5;
  private int animationSpeed = ANIMATION_SPEED_DEFAULT;

  private static final String GRADIENT_BACKGROUND = "GradientBackground";
  private static final boolean GRADIENT_BACKGROUND_DEFAULT = true;
  private boolean gradientBackground = GRADIENT_BACKGROUND_DEFAULT;

  private static final String FUNCTION_NODE_INFORMATION = "FunctionNodeInformation";
  private static final boolean FUNCTION_NODE_INFORMATION_DEFAULT = true;
  private boolean functionNodeInformation = FUNCTION_NODE_INFORMATION_DEFAULT;

  private static final String PROXIMITY_BROWSING_CHILDREN = "ProximityBrowsingChildren";
  private static final int PROXIMITY_BROWSING_CHILDREN_DEFAULT = 2;
  private int proximityBrowsingChildren = PROXIMITY_BROWSING_CHILDREN_DEFAULT;

  private static final String PROXIMITY_BROWSING_PARENTS = "ProximityBrowsingParents";
  private static final int PROXIMITY_BROWSING_PARENTS_DEFAULT = 2;
  private int proximityBrowsingParents = PROXIMITY_BROWSING_PARENTS_DEFAULT;

  private static final String CASE_SENSITIVE_SEARCH = "CaseSensitiveSearch";
  private static final boolean CASE_SENSITIVE_SEARCH_DEFAULT = false;
  private boolean caseSensitiveSearch = CASE_SENSITIVE_SEARCH_DEFAULT;

  private static final String REGEX_SEARCH = "RegexSearch";
  private static final boolean REGEX_SEARCH_DEFAULT = false;
  private boolean regexSearch = REGEX_SEARCH_DEFAULT;

  private static final String SEARCH_SELECTED_NODES_ONLY = "SearchSelectedNodesOnly";
  private static final boolean SEARCH_SELECTED_NODES_ONLY_DEFAULT = false;
  private boolean searchSelectedNodesOnly = SEARCH_SELECTED_NODES_ONLY_DEFAULT;

  private static final String SEARCH_VISIBLE_NODES_ONLY = "SearchVisibleNodesOnly";
  private static final boolean SEARCH_VISIBLE_NODES_ONLY_DEFAULT = false;
  private boolean searchVisibleNodesOnly = SEARCH_VISIBLE_NODES_ONLY_DEFAULT;

  private static final String SIMPLIFIED_VARIABLE_ACCESS = "SimplifiedVariableAccess";
  private static final boolean SIMPLIFIED_VARIABLE_ACCESS_DEFAULT = false;
  private boolean simplifiedVariableAccess = SIMPLIFIED_VARIABLE_ACCESS_DEFAULT;

  private static final String ANIMATE_LAYOUT_EDGE_THRESHOLD = "AnimateLayoutEdgeThreshold";
  private static final int ANIMATE_LAYOUT_EDGE_THRESHOLD_DEFAULT = 30000;
  private int animateLayoutEdgeThreshold = ANIMATE_LAYOUT_EDGE_THRESHOLD_DEFAULT;

  private static final String ANIMATE_LAYOUT_NODE_THRESHOLD = "AnimateLayoutNodeThreshold";
  private static final int ANIMATE_LAYOUT_NODE_THRESHOLD_DEFAULT = 5000;
  private int animateLayoutNodeThreshold = ANIMATE_LAYOUT_NODE_THRESHOLD_DEFAULT;

  @Override
  public void load(final TypedPropertiesWrapper properties) {
    automaticLayouting = properties.getBoolean(AUTOMATIC_LAYOUTING, AUTOMATIC_LAYOUTING_DEFAULT);
    proximityBrowsing = properties.getBoolean(PROXIMITY_BROWSING, PROXIMITY_BROWSING_DEFAULT);
    proximityBrowsingPreview =
        properties.getBoolean(PROXIMITY_BROWSING_PREVIEW, PROXIMITY_BROWSING_PREVIEW_DEFAULT);
    proximityBrowsingThreshold =
        properties.getInteger(PROXIMITY_BROWSING_THRESHOLD, PROXIMITY_BROWSING_THRESHOLD_DEFAULT);
    autoLayoutDeactivationThreshold = properties.getInteger(
        AUTOLAYOUT_DEACTIVATION_THRESHOLD, AUTOLAYOUT_DEACTIVATION_THRESHOLD_DEFAULT);
    layoutCalculationThreshold =
        properties.getInteger(LAYOUT_CALCULATION_THRESHOLD, LAYOUT_CALCULATION_THRESHOLD_DEFAULT);
    defaultGraphLayout = properties.getInteger(DEFAULT_GRAPH_LAYOUT, DEFAULT_GRAPH_LAYOUT_DEFAULT);
    visibilityWarningThreshold =
        properties.getInteger(VISIBILITY_WARNING_THRESHOLD, VISIBILITY_WARNING_THRESHOLD_DEFAULT);
    multipleEdgesAsOne =
        properties.getBoolean(MULTIPLE_EDGES_AS_ONE, MULTIPLE_EDGES_AS_ONE_DEFAULT);
    drawBends = properties.getBoolean(DRAW_BENDS, DRAW_BENDS_DEFAULT);
    edgeHidingMode = properties.getInteger(EDGE_HIDING_MODE, EDGE_HIDING_MODE_DEFAULT);
    edgeHidingThreshold =
        properties.getInteger(EDGE_HIDING_THRESHOLD, EDGE_HIDING_THRESHOLD_DEFAULT);
    hierarchicOrientation =
        properties.getInteger(HIERARCHIC_ORIENTATION, HIERARCHIC_ORIENTATION_DEFAULT);
    hierarchicEdgeRoutingStyle =
        properties.getInteger(HIERARCHIC_EDGE_ROUTING_STYLE, HIERARCHIC_EDGE_ROUTING_STYLE_DEFAULT);
    hierarchicMinimumLayerDistance = properties.getInteger(
        HIERARCHIC_MINIMUM_LAYER_DISTANCE, HIERARCHIC_MINIMUM_LAYER_DISTANCE_DEFAULT);
    hierarchicMinimumNodeDistance = properties.getInteger(
        HIERARCHIC_MINIMUM_NODE_DISTANCE, HIERARCHIC_MINIMUM_NODE_DISTANCE_DEFAULT);
    hierarchicMinimumEdgeDistance = properties.getInteger(
        HIERARCHIC_MINIMUM_EDGE_DISTANCE, HIERARCHIC_MINIMUM_EDGE_DISTANCE_DEFAULT);
    hierarchicMinimumNodeEdgeDistance = properties.getInteger(
        HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE, HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE_DEFAULT);
    orthogonalOrientation =
        properties.getInteger(ORTHOGONAL_ORIENTATION, ORTHOGONAL_ORIENTATION_DEFAULT);
    orthogonalLayoutStyle =
        properties.getInteger(ORTHOGONAL_LAYOUT_STYLE, ORTHOGONAL_LAYOUT_STYLE_DEFAULT);
    orthogonalMinimumNodeDistance = properties.getInteger(
        ORTHOGONAL_MINIMUM_NODE_DISTANCE, ORTHOGONAL_MINIMUM_NODE_DISTANCE_DEFAULT);
    circularLayoutStyle =
        properties.getInteger(CIRCULAR_LAYOUT_STYLE, CIRCULAR_LAYOUT_STYLE_DEFAULT);
    circularMinimumNodeDistance = properties.getInteger(
        CIRCULAR_MINIMUM_NODE_DISTANCE, CIRCULAR_MINIMUM_NODE_DISTANCE_DEFAULT);
    scrollSensitivity = properties.getInteger(SCROLL_SENSITIVITY, SCROLL_SENSITIVITY_DEFAULT);
    zoomSensitivity = properties.getInteger(ZOOM_SENSITIVITY, ZOOM_SENSITIVITY_DEFAULT);
    mouseWheelAction = properties.getInteger(MOUSE_WHEEL_ACTION, MOUSE_WHEEL_ACTION_DEFAULT);
    layoutAnimation = properties.getBoolean(LAYOUT_ANIMATION, LAYOUT_ANIMATION_DEFAULT);
    animationSpeed = properties.getInteger(ANIMATION_SPEED, ANIMATION_SPEED_DEFAULT);
    gradientBackground = properties.getBoolean(GRADIENT_BACKGROUND, GRADIENT_BACKGROUND_DEFAULT);
    functionNodeInformation =
        properties.getBoolean(FUNCTION_NODE_INFORMATION, FUNCTION_NODE_INFORMATION_DEFAULT);
    proximityBrowsingChildren =
        properties.getInteger(PROXIMITY_BROWSING_CHILDREN, PROXIMITY_BROWSING_CHILDREN_DEFAULT);
    proximityBrowsingParents =
        properties.getInteger(PROXIMITY_BROWSING_PARENTS, PROXIMITY_BROWSING_PARENTS_DEFAULT);
    caseSensitiveSearch =
        properties.getBoolean(CASE_SENSITIVE_SEARCH, CASE_SENSITIVE_SEARCH_DEFAULT);
    regexSearch = properties.getBoolean(REGEX_SEARCH, REGEX_SEARCH_DEFAULT);
    searchSelectedNodesOnly =
        properties.getBoolean(SEARCH_SELECTED_NODES_ONLY, SEARCH_SELECTED_NODES_ONLY_DEFAULT);
    searchVisibleNodesOnly =
        properties.getBoolean(SEARCH_VISIBLE_NODES_ONLY, SEARCH_VISIBLE_NODES_ONLY_DEFAULT);
    simplifiedVariableAccess =
        properties.getBoolean(SIMPLIFIED_VARIABLE_ACCESS, SIMPLIFIED_VARIABLE_ACCESS_DEFAULT);
    animateLayoutEdgeThreshold =
        properties.getInteger(ANIMATE_LAYOUT_EDGE_THRESHOLD, ANIMATE_LAYOUT_EDGE_THRESHOLD_DEFAULT);
    animateLayoutNodeThreshold =
        properties.getInteger(ANIMATE_LAYOUT_NODE_THRESHOLD, ANIMATE_LAYOUT_NODE_THRESHOLD_DEFAULT);
  }

  @Override
  public void store(final TypedPropertiesWrapper properties) {
    properties.setBoolean(AUTOMATIC_LAYOUTING, automaticLayouting);
    properties.setBoolean(PROXIMITY_BROWSING, proximityBrowsing);
    properties.setBoolean(PROXIMITY_BROWSING_PREVIEW, proximityBrowsingPreview);
    properties.setInteger(PROXIMITY_BROWSING_THRESHOLD, proximityBrowsingThreshold);
    properties.setInteger(AUTOLAYOUT_DEACTIVATION_THRESHOLD, autoLayoutDeactivationThreshold);
    properties.setInteger(LAYOUT_CALCULATION_THRESHOLD, layoutCalculationThreshold);
    properties.setInteger(DEFAULT_GRAPH_LAYOUT, defaultGraphLayout);
    properties.setInteger(VISIBILITY_WARNING_THRESHOLD, visibilityWarningThreshold);
    properties.setBoolean(MULTIPLE_EDGES_AS_ONE, multipleEdgesAsOne);
    properties.setBoolean(DRAW_BENDS, drawBends);
    properties.setInteger(EDGE_HIDING_MODE, edgeHidingMode);
    properties.setInteger(EDGE_HIDING_THRESHOLD, edgeHidingThreshold);
    properties.setInteger(HIERARCHIC_ORIENTATION, hierarchicOrientation);
    properties.setInteger(HIERARCHIC_EDGE_ROUTING_STYLE, hierarchicEdgeRoutingStyle);
    properties.setInteger(HIERARCHIC_MINIMUM_LAYER_DISTANCE, hierarchicMinimumLayerDistance);
    properties.setInteger(HIERARCHIC_MINIMUM_NODE_DISTANCE, hierarchicMinimumNodeDistance);
    properties.setInteger(HIERARCHIC_MINIMUM_EDGE_DISTANCE, hierarchicMinimumEdgeDistance);
    properties.setInteger(HIERARCHIC_MINIMUM_NODE_EDGE_DISTANCE, hierarchicMinimumNodeEdgeDistance);
    properties.setInteger(ORTHOGONAL_ORIENTATION, orthogonalOrientation);
    properties.setInteger(ORTHOGONAL_LAYOUT_STYLE, orthogonalLayoutStyle);
    properties.setInteger(ORTHOGONAL_MINIMUM_NODE_DISTANCE, orthogonalMinimumNodeDistance);
    properties.setInteger(CIRCULAR_LAYOUT_STYLE, circularLayoutStyle);
    properties.setInteger(CIRCULAR_MINIMUM_NODE_DISTANCE, circularMinimumNodeDistance);
    properties.setInteger(SCROLL_SENSITIVITY, scrollSensitivity);
    properties.setInteger(ZOOM_SENSITIVITY, zoomSensitivity);
    properties.setInteger(MOUSE_WHEEL_ACTION, mouseWheelAction);
    properties.setBoolean(LAYOUT_ANIMATION, layoutAnimation);
    properties.setInteger(ANIMATION_SPEED, animationSpeed);
    properties.setBoolean(GRADIENT_BACKGROUND, gradientBackground);
    properties.setBoolean(FUNCTION_NODE_INFORMATION, functionNodeInformation);
    properties.setInteger(PROXIMITY_BROWSING_CHILDREN, proximityBrowsingChildren);
    properties.setInteger(PROXIMITY_BROWSING_PARENTS, proximityBrowsingParents);
    properties.setBoolean(CASE_SENSITIVE_SEARCH, caseSensitiveSearch);
    properties.setBoolean(REGEX_SEARCH, regexSearch);
    properties.setBoolean(SEARCH_SELECTED_NODES_ONLY, searchSelectedNodesOnly);
    properties.setBoolean(SEARCH_VISIBLE_NODES_ONLY, searchVisibleNodesOnly);
    properties.setBoolean(SIMPLIFIED_VARIABLE_ACCESS, simplifiedVariableAccess);
    properties.setInteger(ANIMATE_LAYOUT_EDGE_THRESHOLD, animateLayoutEdgeThreshold);
    properties.setInteger(ANIMATE_LAYOUT_NODE_THRESHOLD, animateLayoutNodeThreshold);
  }

  public boolean isAutomaticLayouting() {
    return automaticLayouting;
  }

  public void setAutomaticLayouting(final boolean value) {
    this.automaticLayouting = value;
  }

  public boolean isProximityBrowsing() {
    return proximityBrowsing;
  }

  public void setProximityBrowsing(final boolean value) {
    this.proximityBrowsing = value;
  }

  public boolean isProximityBrowsingPreview() {
    return proximityBrowsingPreview;
  }

  public void setProximityBrowsingPreview(final boolean value) {
    this.proximityBrowsingPreview = value;
  }

  public int getProximityBrowsingThreshold() {
    return proximityBrowsingThreshold;
  }

  public void setProximityBrowsingThreshold(final int value) {
    this.proximityBrowsingThreshold = value;
  }

  public int getAutoLayoutDeactivationThreshold() {
    return autoLayoutDeactivationThreshold;
  }

  public void setAutoLayoutDeactivationThreshold(final int value) {
    this.autoLayoutDeactivationThreshold = value;
  }

  public int getLayoutCalculationThreshold() {
    return layoutCalculationThreshold;
  }

  public void setLayoutCalculationThreshold(final int value) {
    this.layoutCalculationThreshold = value;
  }

  public int getDefaultGraphLayout() {
    return defaultGraphLayout;
  }

  public void setDefaultGraphLayout(final int value) {
    this.defaultGraphLayout = value;
  }

  public int getVisibilityWarningThreshold() {
    return visibilityWarningThreshold;
  }

  public void setVisibilityWarningThreshold(final int value) {
    this.visibilityWarningThreshold = value;
  }

  public boolean isMultipleEdgesAsOne() {
    return multipleEdgesAsOne;
  }

  public void setMultipleEdgesAsOne(final boolean value) {
    this.multipleEdgesAsOne = value;
  }

  public boolean isDrawBends() {
    return drawBends;
  }

  public void setDrawBends(final boolean value) {
    this.drawBends = value;
  }

  public int getEdgeHidingMode() {
    return edgeHidingMode;
  }

  public void setEdgeHidingMode(final int value) {
    this.edgeHidingMode = value;
  }

  public int getEdgeHidingThreshold() {
    return edgeHidingThreshold;
  }

  public void setEdgeHidingThreshold(final int value) {
    this.edgeHidingThreshold = value;
  }

  public int getHierarchicOrientation() {
    return hierarchicOrientation;
  }

  public void setHierarchicOrientation(final int value) {
    this.hierarchicOrientation = value;
  }

  public int getHierarchicEdgeRoutingStyle() {
    return hierarchicEdgeRoutingStyle;
  }

  public void setHierarchicEdgeRoutingStyle(final int value) {
    this.hierarchicEdgeRoutingStyle = value;
  }

  public int getHierarchicMinimumLayerDistance() {
    return hierarchicMinimumLayerDistance;
  }

  public void setHierarchicMinimumLayerDistance(final int value) {
    this.hierarchicMinimumLayerDistance = value;
  }

  public int getHierarchicMinimumNodeDistance() {
    return hierarchicMinimumNodeDistance;
  }

  public void setHierarchicMinimumNodeDistance(final int value) {
    this.hierarchicMinimumNodeDistance = value;
  }

  public int getHierarchicMinimumEdgeDistance() {
    return hierarchicMinimumEdgeDistance;
  }

  public void setHierarchicMinimumEdgeDistance(final int value) {
    this.hierarchicMinimumEdgeDistance = value;
  }

  public int getHierarchicMinimumNodeEdgeDistance() {
    return hierarchicMinimumNodeEdgeDistance;
  }

  public void setHierarchicMinimumNodeEdgeDistance(final int value) {
    this.hierarchicMinimumNodeEdgeDistance = value;
  }

  public int getOrthogonalOrientation() {
    return orthogonalOrientation;
  }

  public void setOrthogonalOrientation(final int value) {
    this.orthogonalOrientation = value;
  }

  public int getOrthogonalLayoutStyle() {
    return orthogonalLayoutStyle;
  }

  public void setOrthogonalLayoutStyle(final int value) {
    this.orthogonalLayoutStyle = value;
  }

  public int getOrthogonalMinimumNodeDistance() {
    return orthogonalMinimumNodeDistance;
  }

  public void setOrthogonalMinimumNodeDistance(final int value) {
    this.orthogonalMinimumNodeDistance = value;
  }

  public int getCircularLayoutStyle() {
    return circularLayoutStyle;
  }

  public void setCircularLayoutStyle(final int value) {
    this.circularLayoutStyle = value;
  }

  public int getCircularMinimumNodeDistance() {
    return circularMinimumNodeDistance;
  }

  public void setCircularMinimumNodeDistance(final int value) {
    this.circularMinimumNodeDistance = value;
  }

  public int getScrollSensitivity() {
    return scrollSensitivity;
  }

  public void setScrollSensitivity(final int value) {
    this.scrollSensitivity = value;
  }

  public int getZoomSensitivity() {
    return zoomSensitivity;
  }

  public void setZoomSensitivity(final int value) {
    this.zoomSensitivity = value;
  }

  public int getMouseWheelAction() {
    return mouseWheelAction;
  }

  public void setMouseWheelAction(final int value) {
    this.mouseWheelAction = value;
  }

  public boolean isLayoutAnimation() {
    return layoutAnimation;
  }

  public void setLayoutAnimation(final boolean value) {
    this.layoutAnimation = value;
  }

  public int getAnimationSpeed() {
    return animationSpeed;
  }

  public void setAnimationSpeed(final int value) {
    this.animationSpeed = value;
  }

  public boolean isGradientBackground() {
    return gradientBackground;
  }

  public void setGradientBackground(final boolean value) {
    this.gradientBackground = value;
  }

  public boolean isFunctionNodeInformation() {
    return functionNodeInformation;
  }

  public void setFunctionNodeInformation(final boolean value) {
    this.functionNodeInformation = value;
  }

  public int getProximityBrowsingChildren() {
    return proximityBrowsingChildren;
  }

  public void setProximityBrowsingChildren(final int value) {
    this.proximityBrowsingChildren = value;
  }

  public int getProximityBrowsingParents() {
    return proximityBrowsingParents;
  }

  public void setProximityBrowsingParents(final int value) {
    this.proximityBrowsingParents = value;
  }

  public boolean isCaseSensitiveSearch() {
    return caseSensitiveSearch;
  }

  public void setCaseSensitiveSearch(final boolean value) {
    this.caseSensitiveSearch = value;
  }

  public boolean isRegexSearch() {
    return regexSearch;
  }

  public void setRegexSearch(final boolean value) {
    this.regexSearch = value;
  }

  public boolean isSearchSelectedNodesOnly() {
    return searchSelectedNodesOnly;
  }

  public void setSearchSelectedNodesOnly(final boolean value) {
    this.searchSelectedNodesOnly = value;
  }

  public boolean isSearchVisibleNodesOnly() {
    return searchVisibleNodesOnly;
  }

  public void setSearchVisibleNodesOnly(final boolean value) {
    this.searchVisibleNodesOnly = value;
  }

  public boolean isSimplifiedVariableAccess() {
    return simplifiedVariableAccess;
  }

  public void setSimplifiedVariableAccess(final boolean value) {
    this.simplifiedVariableAccess = value;
  }

  public int getAnimateLayoutEdgeThreshold() {
    return animateLayoutEdgeThreshold;
  }

  public void setAnimateLayoutEdgeThreshold(final int value) {
    this.animateLayoutEdgeThreshold = value;
  }

  public int getAnimateLayoutNodeThreshold() {
    return animateLayoutNodeThreshold;
  }

  public void setAnimateLayoutNodeThreshold(final int value) {
    this.animateLayoutNodeThreshold = value;
  }
}
