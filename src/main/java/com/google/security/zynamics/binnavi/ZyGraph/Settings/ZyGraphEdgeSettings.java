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
import com.google.security.zynamics.binnavi.config.CallGraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.EdgeHidingMode;

/**
 * Contains graph edge settings.
 */
public final class ZyGraphEdgeSettings {
  /**
   * Flag that indicates whether multiple edges between the same nodes are displayed as just one
   * edge.
   */
  private boolean displayMultipleEdgesAsOne;

  /**
   * Determines whether edges are hidden.
   */
  private EdgeHidingMode edgeHidingMode;

  /**
   * Number of edges a graph must have before edges are hidden.
   */
  private int edgeHidingThreshold;

  /**
   * Flag that determines if bends are drawn.
   */
  private boolean drawSelectedBends;

  /**
   * Settings file object that provides the settings. This value can be null.
   */
  private final GraphSettingsConfigItem type;

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IZyGraphEdgeSettingsListener> listeners =
      new ListenerProvider<IZyGraphEdgeSettingsListener>();

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphEdgeSettings(final GraphSettingsConfigItem type) {
    this.type = Preconditions.checkNotNull(type, "IE02017: Type argument can't be null");
    if (type instanceof CallGraphSettingsConfigItem) {
      setDisplayMultipleEdgesAsOne(type.isMultipleEdgesAsOne());
    }
  }

  /**
   * Creates a new settings object by copying the information from another settings object.
   *
   * @param settings The source settings object.
   */
  public ZyGraphEdgeSettings(final ZyGraphEdgeSettings settings) {
    displayMultipleEdgesAsOne = settings.getDisplayMultipleEdgesAsOne();
    edgeHidingMode = settings.getEdgeHidingMode();
    edgeHidingThreshold = settings.getEdgeHidingThreshold();
    drawSelectedBends = settings.getDrawSelectedBends();
    type = null;
  }

  /**
   * Converts the numerical value of a configuration file edge hiding mode to an enumeration value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static EdgeHidingMode getEdgeHidingMode(final GraphSettingsConfigItem type) {
    try {
      return EdgeHidingMode.parseInt(type.getEdgeHidingMode());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);
      return EdgeHidingMode.HIDE_ON_THRESHOLD;
    }
  }

  /**
   * Adds a listener that is notified about changes in the graph view settings.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IZyGraphEdgeSettingsListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Returns whether multiple edges should be displayed as one or not.
   *
   * @return True, if multiple edges should be displayed as one. False, otherwise.
   */
  public boolean getDisplayMultipleEdgesAsOne() {
    return type == null ? displayMultipleEdgesAsOne : type.isMultipleEdgesAsOne();
  }

  /**
   * Returns the current draw selected bends setting.
   *
   * @return The current draw selected bends setting.
   */
  public boolean getDrawSelectedBends() {
    return type == null ? drawSelectedBends : type.isDrawBends();
  }

  /**
   * Returns the current edge hiding mode.
   *
   * @return The current edge hiding mode.
   */
  public EdgeHidingMode getEdgeHidingMode() {
    return type == null ? edgeHidingMode : getEdgeHidingMode(type);
  }

  /**
   * Returns the current edge hiding threshold.
   *
   * @return The current edge hiding threshold.
   */
  public int getEdgeHidingThreshold() {
    return type == null ? edgeHidingThreshold : type.getEdgeHidingThreshold();
  }

  /**
   * Removes a previously attached listener object.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IZyGraphEdgeSettingsListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Changes the current display multiple edges as one setting.
   *
   * @param value The new value of the display multiple edges as one setting.
   */
  public void setDisplayMultipleEdgesAsOne(final boolean value) {
    if (value == getDisplayMultipleEdgesAsOne()) {
      return;
    }
    if (type == null) {
      displayMultipleEdgesAsOne = value;
    } else {
      type.setMultipleEdgesAsOne(value);
    }

    for (final IZyGraphEdgeSettingsListener listener : listeners) {
      try {
        listener.changedDisplayMultipleEdgesAsOne(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current draw selected bends setting.
   *
   * @param value The new value of the draw selected bends setting.
   */
  public void setDrawSelectedBends(final boolean value) {
    if (value == getDrawSelectedBends()) {
      return;
    }
    if (type == null) {
      drawSelectedBends = value;
    } else {
      type.setDrawBends(true);
    }

    for (final IZyGraphEdgeSettingsListener listener : listeners) {
      try {
        listener.changedDrawSelectedBends(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current edge hiding mode setting.
   *
   * @param value The new value of the edge hiding mode setting.
   */
  public void setEdgeHidingMode(final EdgeHidingMode value) {
    Preconditions.checkNotNull(value, "IE00877: Edge hiding mode can't be null");
    if (value == getEdgeHidingMode()) {
      return;
    }

    if (type == null) {
      edgeHidingMode = value;
    } else {
      type.setEdgeHidingMode(value.ordinal());
    }

    for (final IZyGraphEdgeSettingsListener listener : listeners) {
      try {
        listener.changedEdgeHidingMode(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current edge hiding threshold setting.
   *
   * @param threshold The new value of the edge hiding threshold setting.
   */
  public void setEdgeHidingThreshold(final int threshold) {
    if (threshold == getEdgeHidingThreshold()) {
      return;
    }

    if (type == null) {
      edgeHidingThreshold = threshold;
    } else {
      type.setEdgeHidingThreshold(threshold);
    }

    for (final IZyGraphEdgeSettingsListener listener : listeners) {
      try {
        listener.changedEdgeHidingThreshold(threshold);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
