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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphDisplaySettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphEdgeSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphMouseSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphProximitySettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphSearchSettings;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.Settings.ZyGraphLayoutSettings;
import com.google.security.zynamics.zylib.gui.zygraph.AbstractZyGraphSettings;

import java.util.Map;

/**
 * View settings class that contains all the possible settings for a visible graph view.
 */
public final class ZyGraphViewSettings extends AbstractZyGraphSettings {
  /**
   * Contains the layout-related settings.
   */
  private final ZyGraphLayoutSettings m_layoutSettings;

  /**
   * Contains the proximity browsing-related settings.
   */
  private final ZyGraphProximitySettings m_proximitySettings;

  /**
   * Contains the search-related settings.
   */
  private final ZyGraphSearchSettings m_searchSettings;

  /**
   * Contains the edge-related settings.
   */
  private final ZyGraphEdgeSettings m_edgeSettings;

  /**
   * Contains the display-related settings.
   */
  private final ZyGraphDisplaySettings m_displaySettings;

  /**
   * Contains the mouse related-settings.
   */
  private final ZyGraphMouseSettings m_mouseSettings;

  /**
   * Workaround; remove once Case 874 is fixed
   */
  public Map<String, String> rawSettings;

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphViewSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE01527: Type argument can't be null");

    m_layoutSettings = new ZyGraphLayoutSettings(type);
    m_proximitySettings = new ZyGraphProximitySettings(type);
    m_searchSettings = new ZyGraphSearchSettings(type);
    m_edgeSettings = new ZyGraphEdgeSettings(type);
    m_displaySettings = new ZyGraphDisplaySettings(type);
    m_mouseSettings = new ZyGraphMouseSettings(type);
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphViewSettings(final ZyGraphViewSettings settings) {
    Preconditions.checkNotNull(settings, "IE00875: Settings argument can't be null");
    m_layoutSettings = new ZyGraphLayoutSettings(settings.getLayoutSettings());
    m_proximitySettings = new ZyGraphProximitySettings(settings.getProximitySettings());
    m_searchSettings = new ZyGraphSearchSettings(settings.getSearchSettings());
    m_edgeSettings = new ZyGraphEdgeSettings(settings.getEdgeSettings());
    m_displaySettings = new ZyGraphDisplaySettings(settings.getDisplaySettings());
    m_mouseSettings = new ZyGraphMouseSettings(settings.getMouseSettings());
  }

  @Override
  public ZyGraphDisplaySettings getDisplaySettings() {
    return m_displaySettings;
  }

  /**
   * Returns the edge-related settings.
   *
   * @return The edge-related settings.
   */
  public ZyGraphEdgeSettings getEdgeSettings() {
    return m_edgeSettings;
  }

  @Override
  public ZyGraphLayoutSettings getLayoutSettings() {
    return m_layoutSettings;
  }

  @Override
  public ZyGraphMouseSettings getMouseSettings() {
    return m_mouseSettings;
  }

  @Override
  public ZyGraphProximitySettings getProximitySettings() {
    return m_proximitySettings;
  }

  /**
   * Returns the search-related settings.
   *
   * @return The search-related settings.
   */
  public ZyGraphSearchSettings getSearchSettings() {
    return m_searchSettings;
  }
}
