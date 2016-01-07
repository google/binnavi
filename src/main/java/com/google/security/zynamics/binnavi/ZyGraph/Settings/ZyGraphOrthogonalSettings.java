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
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalOrientation;
import com.google.security.zynamics.zylib.gui.zygraph.layouters.OrthogonalStyle;

/**
 * Contains graph settings for orthogonal layouts.
 */
public final class ZyGraphOrthogonalSettings {
  /**
   * Orientation of orthogonal layouting operations.
   */
  private OrthogonalOrientation m_orientation;

  /**
   * Style of hierarchical layouting operations.
   */
  private OrthogonalStyle m_style;

  /**
   * Minimum number of pixels between orthogonally layouted nodes.
   */
  private int m_mininmumNodeDistance;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphOrthogonalSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02026: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphOrthogonalSettings(final ZyGraphOrthogonalSettings settings) {
    m_type = null;

    m_mininmumNodeDistance = settings.getMinimumNodeDistance();
    m_orientation = settings.getOrientation();
    m_style = settings.getStyle();
  }

  /**
   * Converts the numerical value of a configuration file orthogonal orientation to an enumeration
   * value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static OrthogonalOrientation getOrthogonalOrientation(final GraphSettingsConfigItem type) {
    try {
      return OrthogonalOrientation.parseInt(type.getOrthogonalOrientation());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);

      return OrthogonalOrientation.VERTICAL;
    }
  }

  /**
   * Converts the numerical value of a configuration file orthogonal layout style to an enumeration
   * value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static OrthogonalStyle getOrthogonalStyle(final GraphSettingsConfigItem type) {
    try {
      return OrthogonalStyle.parseInt(type.getOrthogonalLayoutStyle());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);

      return OrthogonalStyle.NORMAL;
    }
  }

  /**
   * Returns the current minimum orthogonal node distance.
   *
   * @return The current minimum orthogonal node distance.
   */
  public int getMinimumNodeDistance() {
    return m_type == null ? m_mininmumNodeDistance : m_type.getOrthogonalMinimumNodeDistance();
  }

  /**
   * Returns the current orthogonal orientation.
   *
   * @return The current orthogonal orientation.
   */
  public OrthogonalOrientation getOrientation() {
    return m_type == null ? m_orientation : getOrthogonalOrientation(m_type);
  }

  /**
   * Returns the current orthogonal layout style.
   *
   * @return The current orthogonal layout style.
   */
  public OrthogonalStyle getStyle() {
    return m_type == null ? m_style : getOrthogonalStyle(m_type);
  }

  /**
   * Changes the current minimum orthogonal node distance setting.
   *
   * @param value The new value of the minimum orthogonal node distance setting.
   */
  public void setMinimumNodeDistance(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00885: Distance argument must not be negative");

    if (value == getMinimumNodeDistance()) {
      return;
    }

    if (m_type == null) {
      m_mininmumNodeDistance = value;
    } else {
      m_type.setOrthogonalMinimumNodeDistance(value);
    }
  }

  /**
   * Changes the current orthogonal orientation setting.
   *
   * @param value The new value of the orthogonal orientation setting.
   */
  public void setOrientation(final OrthogonalOrientation value) {
    Preconditions.checkNotNull(value, "IE00888: Orientation argument must not be negative");

    if (value == getOrientation()) {
      return;
    }

    if (m_type == null) {
      m_orientation = value;
    } else {
      m_type.setOrthogonalOrientation(value.ordinal());
    }
  }

  /**
   * Changes the current orthogonal layout style setting.
   *
   * @param value The new value of the orthogonal layout style setting.
   */
  public void setStyle(final OrthogonalStyle value) {
    Preconditions.checkNotNull(value, "IE00887: Style argument must not be negative");

    if (value == getStyle()) {
      return;
    }

    if (m_type == null) {
      m_style = value;
    } else {
      m_type.setOrthogonalLayoutStyle(value.ordinal());
    }
  }
}
