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
package com.google.security.zynamics.binnavi.ZyGraph.Settings;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.HierarchicStyle;

/**
 * Contains graph settings for hierarchical layouts.
 */
public final class ZyGraphHierarchicalSettings {
  /**
   * Minimum number of pixels between layers.
   */
  private int m_minimumLayerDistance;

  /**
   * Minimum number of pixels between nodes.
   */
  private int m_minimumNodeDistance;

  /**
   * Minimum number of pixels between edges.
   */
  private int m_minimumEdgeDistance;

  /**
   * Minimum number of pixels between nodes and edges.
   */
  private int m_minimumNodeEdgeDistance;
  /**
   * Orientation of hierarchic layouting operations.
   */
  private HierarchicOrientation m_orientation;

  /**
   * Style of hierarchic layouting operations.
   */
  private HierarchicStyle m_style = HierarchicStyle.OCTLINEAR_OPTIMAL;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;


  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   * 
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphHierarchicalSettings(final GraphSettingsConfigItem type) {
    m_type = Preconditions.checkNotNull(type, "IE02018: Type argument can't be null");
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   * 
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphHierarchicalSettings(final ZyGraphHierarchicalSettings settings) {
    m_type = null;

    m_style = settings.getStyle();
    m_minimumEdgeDistance = settings.getMinimumEdgeDistance();
    m_minimumLayerDistance = settings.getMinimumLayerDistance();
    m_minimumNodeDistance = settings.getMinimumNodeDistance();
    m_minimumNodeEdgeDistance = settings.getMinimumNodeEdgeDistance();
    m_orientation = settings.getOrientation();
  }



  /**
   * Converts the numerical value of a configuration file hierarchic layout style type to an
   * enumeration value.
   * 
   * @param type The configuration file settings type that provides the numerical value.
   * 
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static HierarchicStyle getLayout(final GraphSettingsConfigItem type) {
    try {
      return HierarchicStyle.parseInt(type.getHierarchicEdgeRoutingStyle());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);

      return HierarchicStyle.OCTLINEAR_OPTIMAL;
    }
  }

  /**
   * Converts the numerical value of a configuration file heirarchic orientation to an enumeration
   * value.
   * 
   * @param type The configuration file settings type that provides the numerical value.
   * 
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static HierarchicOrientation getOrientation(final GraphSettingsConfigItem type) {
    try {
      return HierarchicOrientation.parseInt(type.getHierarchicOrientation());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);
      return HierarchicOrientation.VERTICAL;
    }
  }

  /**
   * Returns the current minimum edge distance.
   * 
   * @return The current minimum edge distance.
   */
  public int getMinimumEdgeDistance() {
    return m_type == null ? m_minimumEdgeDistance : m_type.getHierarchicMinimumEdgeDistance();
  }

  /**
   * Returns the current minimum layer distance.
   * 
   * @return The current minimum layer distance.
   */
  public int getMinimumLayerDistance() {
    return m_type == null ? m_minimumLayerDistance : m_type.getHierarchicMinimumLayerDistance();
  }

  /**
   * Returns the current minimum node distance.
   * 
   * @return The current minimum node distance.
   */
  public int getMinimumNodeDistance() {
    return m_type == null ? m_minimumNodeDistance : m_type.getHierarchicMinimumNodeDistance();
  }

  /**
   * Returns the current minimum node to edge distance.
   * 
   * @return the current minimum node to edge distance.
   */
  public int getMinimumNodeEdgeDistance() {
    return m_type == null ? m_minimumNodeEdgeDistance : m_type
        .getHierarchicMinimumNodeEdgeDistance();
  }

  /**
   * Returns the current orientation.
   * 
   * @return The current orientation.
   */
  public HierarchicOrientation getOrientation() {
    return m_type == null ? m_orientation : getOrientation(m_type);
  }

  /**
   * Returns the current hierarchic layouting style.
   * 
   * @return The current hierarchic layouting style.
   */
  public HierarchicStyle getStyle() {
    return m_type == null ? m_style : getLayout(m_type);
  }

  /**
   * Changes the current minimum hierarchic edge distance setting.
   * 
   * @param value The new value of the minimum hierarchic edge distance setting.
   */
  public void setMinimumEdgeDistance(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00882: Distance argument must not be negative");

    if (value == getMinimumEdgeDistance()) {
      return;
    }

    if (m_type == null) {
      m_minimumEdgeDistance = value;
    } else {
      m_type.setHierarchicMinimumEdgeDistance(value);
    }
  }

  /**
   * Changes the current minimum hierarchic layer distance setting.
   * 
   * @param value The new value of the minimum hierarchic layer distance setting.
   */
  public void setMinimumLayerDistance(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00883: Distance argument must not be negative");

    if (value == getMinimumLayerDistance()) {
      return;
    }

    if (m_type == null) {
      m_minimumLayerDistance = value;
    } else {
      m_type.setHierarchicMinimumLayerDistance(value);
    }
  }

  /**
   * Changes the current minimum hierarchic node distance setting.
   * 
   * @param value The new value of the minimum hierarchic node distance setting.
   */
  public void setMinimumNodeDistance(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00884: Distance argument must not be negative");

    if (value == getMinimumNodeDistance()) {
      return;
    }

    if (m_type == null) {
      m_minimumNodeDistance = value;
    } else {
      m_type.setHierarchicMinimumNodeDistance(value);
    }
  }

  /**
   * Changes the current minimum node to edge distance setting.
   * 
   * @param minNodeEdgeDistance the new minimum distance between nodes and edges
   */
  public void setMinimumNodeEdgeDistance(final Integer minNodeEdgeDistance) {
    Preconditions.checkArgument(minNodeEdgeDistance >= 0,
        "Error: Distance argument must not be negative");

    if (minNodeEdgeDistance == getMinimumNodeEdgeDistance()) {
      return;
    }

    if (m_type == null) {
      m_minimumNodeEdgeDistance = minNodeEdgeDistance;
    } else {
      m_type.setHierarchicMinimumNodeEdgeDistance(minNodeEdgeDistance);
    }
  }

  /**
   * Changes the current hierarchic orientation setting.
   * 
   * @param value The new value of the hierarchic orientation setting.
   */
  public void setOrientation(final HierarchicOrientation value) {
    Preconditions.checkNotNull(value, "IE00879: Orientation argument can't be null");

    if (value == getOrientation()) {
      return;
    }

    if (m_type == null) {
      m_orientation = value;
    } else {
      m_type.setHierarchicOrientation(value.ordinal());
    }
  }

  /**
   * Changes the current hierarchic layouting style setting.
   * 
   * @param value The new value of the hierarchic layouting style setting.
   */
  public void setStyle(final HierarchicStyle value) {
    Preconditions.checkNotNull(value, "IE00878: Style argument can't be null");

    if (value == getStyle()) {
      return;
    }

    if (m_type == null) {
      m_style = value;
    } else {
      m_type.setHierarchicEdgeRoutingStyle(value.ordinal());
    }
  }
}
