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
import com.google.security.zynamics.zylib.gui.zygraph.layouters.CircularStyle;

/**
 * Contains settings for circular graph layouts.
 */
public final class ZyGraphCircularSettings {
  /**
   * Minimum number of pixels between circularly layouted nodes.
   */
  private int m_minimumNodeDistance;

  /**
   * Style of circular layouting operations.
   */
  private CircularStyle m_layout = CircularStyle.COMPACT;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphCircularSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02010: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphCircularSettings(final ZyGraphCircularSettings settings) {
    m_type = null;

    m_layout = settings.getStyle();
    m_minimumNodeDistance = settings.getMinimumNodeDistance();
  }

  /**
   * Converts the numerical value of a configuration file circular style type to an enumeration
   * value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static CircularStyle getCircularLayoutStyle(final GraphSettingsConfigItem type) {
    try {
      return CircularStyle.parseInt(type.getCircularLayoutStyle());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);

      return CircularStyle.COMPACT;
    }
  }

  /**
   * Returns the current minimum circular node distance.
   *
   * @return The current minimum circular node distance.
   */
  public int getMinimumNodeDistance() {
    return m_type == null ? m_minimumNodeDistance : m_type.getCircularMinimumNodeDistance();
  }

  /**
   * Returns the current circular layout style.
   *
   * @return The current circular layout style.
   */
  public CircularStyle getStyle() {
    return m_type == null ? m_layout : getCircularLayoutStyle(m_type);
  }

  /**
   * Changes the current minimum circular node distance setting.
   *
   * @param value The new value of the minimum circular node distance setting.
   */
  public void setMinimumNodeDistance(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00881: Distance argument must not be negative");

    if (value == getMinimumNodeDistance()) {
      return;
    }

    if (m_type == null) {
      m_minimumNodeDistance = value;
    } else {
      m_type.setCircularMinimumNodeDistance(value);
    }
  }

  /**
   * Changes the current circular style setting.
   *
   * @param value The new value of the circular style setting.
   */
  public void setStyle(final CircularStyle value) {
    Preconditions.checkNotNull(value, "IE00902: Style argument can't be null");

    if (value == getStyle()) {
      return;
    }

    if (m_type == null) {
      m_layout = value;
    } else {
      m_type.setCircularLayoutStyle(value.ordinal());
    }
  }
}
