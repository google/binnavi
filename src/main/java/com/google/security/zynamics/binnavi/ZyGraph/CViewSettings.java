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
package com.google.security.zynamics.binnavi.ZyGraph;

/**
 * Contains string identifiers for graph view settings; these are the keys used for storing settings
 * values to the database.
 */
public final class CViewSettings {
  /**
   * Identifier key for storing the animation speed setting.
   */
  public static final String ANIMATION_SPEED = "animation_speed";

  /**
   * Identifier key for storing the automatic layouting setting.
   */
  public static final String AUTOMATIC_LAYOUTING = "automatic_layouting";

  /**
   * Identifier key for storing the autolayout threshold setting.
   */
  public static final String AUTOLAYOUT_THRESHOLD = "autolayout_treshold";

  /**
   * Identifier key for storing the circular layout style setting.
   */
  public static final String CIRCULAR_LAYOUT_STYLE = "circular_layout_style";

  /**
   * Identifier key for storing the display multiple edges as one setting.
   */
  public static final String DISPLAY_MULTIPLE_EDGES_AS_ONE = "multiple_edges_as_one";

  /**
   * Identifier key for storing function node information as one setting.
   */
  public static final String FUNCTION_NODE_INFORMATION = "function_node_information";

  /**
   * Identifier key for storing the gradient background setting.
   */
  public static final String GRADIENT_BACKGROUND = "gradient_background";

  /**
   * Identifier key for storing the hierarchic layouting style setting.
   */
  public static final String HIERARCHIC_LAYOUT_STYLE = "hierarchic_layout_style";

  /**
   * Identifier key for storing the layout animation setting.
   */
  public static final String LAYOUT_ANIMATION = "layout_animation";

  /**
   * Identifier key for storing the layout calculation threshold setting.
   */
  public static final String LAYOUT_CALCULATION_TRESHOLD = "layout_calculation_treshold";

  /**
   * Identifier key for storing the minimum circular node distance setting.
   */
  public static final String MINIMUM_CIRCULAR_NODE_DISTANCE = "minimum_circular_node_distance";

  /**
   * Identifier key for storing the minimum hierarchic edge distance setting.
   */
  // ESCA-JAVA0116:
  public static final String MINIMUM_HIERARCHIC_EDGE_DISTANCE = "minimum_hierarchic_edge_distance";

  /**
   * Identifier key for storing the minimum hierarchic layer distance setting.
   */
  public static final String MINIMUM_HIERARCHIC_LAYER_DISTANCE =
      "minimum_hierarchic_layer_distance";

  /**
   * Identifier key for storing the minimum hierarchic node distance setting.
   */
  public static final String MINIMUM_HIERARCHIC_NODE_DISTANCE = "minimum_hierarchic_node_distance";

  /**
   * Identifier key for storing the minimum orthogonal node distance setting.
   */
  public static final String MINIMUM_ORTHOGONAL_NODE_DISTANCE = "minimum_orthogonal_node_distance";

  /**
   * Identifier key for storing the mouse wheel action setting.
   */
  public static final String MOUSEWHEEL_ACTION = "mousewheel_action";

  /**
   * Identifier key for storing the orthogonal layout style setting.
   */
  public static final String ORTHOGONAL_LAYOUT_STYLE = "orthogonal_layout_style";

  /**
   * Identifier key for storing the orthogonal orientation setting.
   */
  public static final String ORTHOGONAL_ORIENTATION = "orthogonal_orientation";

  /**
   * Identifier key for storing the proximity browsing setting.
   */
  public static final String PROXIMITY_BROWSING = "proximity_browsing";

  /**
   * Identifier key for storing the proximity browsing threshold setting.
   */
  public static final String PROXIMITY_BROWSING_THRESHOLD = "proximity_browing_threshold";

  /**
   * Identifier key for storing the proximity browsing children setting.
   */
  public static final String PROXIMITY_BROWSING_CHILDREN = "proximity_browing_children";

  /**
   * Identifier key for storing the proximity browsing frozen setting.
   */
  public static final String PROXIMITY_BROWSING_FROZEN = "proximity_browing_frozen";

  /**
   * Identifier key for storing the proximity browsing parents setting.
   */
  public static final String PROXIMITY_BROWSING_PARENTS = "proximity_browing_parents";

  /**
   * Identifier key for storing the proximity browsing preview setting.
   */
  public static final String PROXIMITY_BROWSING_PREVIEW = "proximity_browing_preview";

  /**
   * Identifier key for storing the scroll sensibility setting.
   */
  public static final String SCROLL_SENSIBILITY = "scroll_sensibility";

  /**
   * Identifier key for storing the case sensitive search setting.
   */
  public static final String SEARCH_CASE_SENSITIVE = "search_case_sensitive";

  /**
   * Identifier key for storing the regular expression search setting.
   */
  public static final String SEARCH_REGEX = "search_regex";

  /**
   * Identifier key for storing the selected nodes only search setting.
   */
  public static final String SEARCH_SELECTED_ONLY = "search_selected_only";

  /**
   * Identifier key for storing the search visible nodes only setting.
   */
  public static final String SEARCH_VISIBLE_ONLY = "search_visible_only";

  /**
   * Identifier key for storing the zoom sensibility setting.
   */
  public static final String ZOOM_SENSIBILITY = "zoom_sensibility";

  /**
   * Identifier key for storing the simplified variable access value.
   */
  public static final String SIMPLIFIED_VARIABLE_ACCESS = "simplified_variable_access";

  /**
   * Private constructor because you are not supposed to instantiate this class.
   */
  private CViewSettings() {
    // You are not supposed to instantiate this class
  }
}
