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

import java.text.ParseException;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.zylib.gui.zygraph.MouseWheelAction;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IMouseSettings;

/**
 * Contains mouse settings for the graph window.
 */
public final class ZyGraphMouseSettings implements IMouseSettings {
  /**
   * Sensitivity of the mouse wheel during scroll operations.
   */
  private int m_scrollSensitivity;

  /**
   * Sensitivity of the mouse wheel during zoom operations.
   */
  private int m_zoomSensitivity;

  /**
   * Behavior of the mouse wheel.
   */
  private MouseWheelAction m_mouseWheelAction;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphMouseSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02024: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphMouseSettings(final ZyGraphMouseSettings settings) {
    m_type = null;

    m_mouseWheelAction = settings.getMouseWheelAction();
    m_scrollSensitivity = settings.getScrollSensitivity();
    m_zoomSensitivity = settings.getZoomSensitivity();
  }

  /**
   * Converts the numerical value of a configuration file mouse wheel action to an enumeration
   * value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static MouseWheelAction getMousewheelAction(final GraphSettingsConfigItem type) {
    try {
      return MouseWheelAction.parseInt(type.getMouseWheelAction());
    } catch (final ParseException e) {
      CUtilityFunctions.logException(e);

      return MouseWheelAction.ZOOM;
    }
  }

  @Override
  public MouseWheelAction getMouseWheelAction() {
    return m_type == null ? m_mouseWheelAction : getMousewheelAction(m_type);
  }

  @Override
  public int getScrollSensitivity() {
    return m_type == null ? m_scrollSensitivity : m_type.getScrollSensitivity();
  }

  @Override
  public int getZoomSensitivity() {
    return m_type == null ? m_zoomSensitivity : m_type.getZoomSensitivity();
  }

  /**
   * Changes the current mouse wheel action setting.
   *
   * @param value The new value of the mouse wheel action setting.
   */
  public void setMousewheelAction(final MouseWheelAction value) {
    Preconditions.checkNotNull(value, "IE00886: Mouse wheel action argument can't be null");

    if (value == getMouseWheelAction()) {
      return;
    }

    if (m_type == null) {
      m_mouseWheelAction = value;
    } else {
      m_type.setMouseWheelAction(value.ordinal());
    }
  }

  /**
   * Changes the current scroll sensibility setting.
   *
   * @param value The new value of the scroll sensibility setting.
   */
  public void setScrollSensitivity(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00892: Sensitivity argument must not be negative");

    if (value == getScrollSensitivity()) {
      return;
    }

    if (m_type == null) {
      m_scrollSensitivity = value;
    } else {
      m_type.setScrollSensitivity(value);
    }
  }

  /**
   * Changes the current zoom sensitivity setting.
   *
   * @param value The new value of the zoom sensitivity setting.
   */
  public void setZoomSensitivity(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00900: Sensitivity argument must not be negative");

    if (value == getZoomSensitivity()) {
      return;
    }

    if (m_type == null) {
      m_zoomSensitivity = value;
    } else {
      m_type.setZoomSensitivity(value);
    }
  }
}
