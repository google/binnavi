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
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IProximitySettingsListener;

/**
 * Contains settings for proximity browsing.
 */
public final class ZyGraphProximitySettings implements IProximitySettings {
  /**
   * Number of nodes a native graph must have before proximity browsing is enabled by default.
   */
  private int m_proximityBrowsingActivisionThreshold;

  /**
   * Flag that determines if proximity browsing is enabled.
   */
  private boolean m_proximityBrowsing;

  /**
   * Flag that determines whether proximity browsing is frozen.
   */
  private boolean m_proximityBrowsingFrozen = false;

  /**
   * Flag that determines whether proximity browsing is previewed.
   */
  private boolean m_ProximityPreview;

  /**
   * Number of children shown for a visible node during proximity browsing.
   */
  private int m_proximityBrowsingChildren;

  /**
   * Number of parents shown for a visible node during proximity browsing.
   */
  private int m_proximityBrowsingParents;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private GraphSettingsConfigItem m_type;

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IProximitySettingsListener> m_listeners =
      new ListenerProvider<IProximitySettingsListener>();

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphProximitySettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02025: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphProximitySettings(final ZyGraphProximitySettings settings) {
    m_proximityBrowsing = settings.getProximityBrowsing();
    m_proximityBrowsingActivisionThreshold = settings.getProximityBrowsingActivationThreshold();
    m_proximityBrowsingChildren = settings.getProximityBrowsingChildren();
    m_proximityBrowsingFrozen = settings.getProximityBrowsingFrozen();
    m_proximityBrowsingParents = settings.getProximityBrowsingParents();
    m_ProximityPreview = settings.getProximityBrowsingPreview();
  }

  @Override
  public void addListener(final IProximitySettingsListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean getProximityBrowsing() {
    return m_type == null ? m_proximityBrowsing : m_type.isProximityBrowsing();
  }

  /**
   * Returns the current proximity browser activation threshold.
   *
   * @return The current proximity browser activation threshold.
   */
  public int getProximityBrowsingActivationThreshold() {
    return m_type == null ? m_proximityBrowsingActivisionThreshold : m_type
        .getProximityBrowsingThreshold();
  }

  @Override
  public int getProximityBrowsingChildren() {
    return m_type == null ? m_proximityBrowsingChildren : m_type.getProximityBrowsingChildren();
  }

  @Override
  public boolean getProximityBrowsingFrozen() {
    return m_proximityBrowsingFrozen;
  }

  @Override
  public int getProximityBrowsingParents() {
    return m_type == null ? m_proximityBrowsingParents : m_type.getProximityBrowsingParents();
  }

  /**
   * Returns the current proximity browsing preview setting.
   *
   * @return The current proximity browsing preview setting.
   */
  public boolean getProximityBrowsingPreview() {
    return m_type == null ? m_ProximityPreview : m_type.isProximityBrowsingPreview();
  }

  @Override
  public void removeListener(final IProximitySettingsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the current proximity browsing setting.
   *
   * @param value The new value of the proximity browsing setting.
   */
  public void setProximityBrowsing(final boolean value) {
    if (value == getProximityBrowsing()) {
      return;
    }

    if (m_type == null) {
      m_proximityBrowsing = value;
    } else {
      m_type.setProximityBrowsing(value);
    }

    for (final IProximitySettingsListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because we are calling a listener function.
      try {
        listener.changedProximityBrowsing(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current proximity browsing activation threshold setting.
   *
   * @param value The new value of the proximity browsing activation threshold setting.
   */
  public void setProximityBrowsingActivationThreshold(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00889: Threshold argument must not be negative");

    if (value == getProximityBrowsingActivationThreshold()) {
      return;
    }

    if (m_type == null) {
      m_proximityBrowsingActivisionThreshold = value;
    } else {
      m_type.setProximityBrowsingThreshold(value);
    }
  }

  /**
   * Changes the current proximity browsing children setting.
   *
   * @param value The new value of the proximity browsing children setting.
   */
  public void setProximityBrowsingChildren(final int value) {
    Preconditions.checkArgument(value >= -1, "IE00890: Distance argument must not be less than -1");

    if (value == getProximityBrowsingChildren()) {
      return;
    }

    if (m_type == null) {
      m_proximityBrowsingChildren = value;
    } else {
      m_type.setProximityBrowsingChildren(value);
    }

    for (final IProximitySettingsListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because we are calling a listener function.
      try {
        listener.changedProximityBrowsingDepth(value, getProximityBrowsingParents());
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Enables or disables frozen proximity browsing.
   *
   * @param value True, to enable freeze mode. False, to disable it.
   */
  public void setProximityBrowsingFrozen(final boolean value) {
    if (value == getProximityBrowsingFrozen()) {
      return;
    }

    m_proximityBrowsingFrozen = value;

    for (final IProximitySettingsListener listener : m_listeners) {
      try {
        listener.changedProximityBrowsingFrozen(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current proximity browsing parents setting.
   *
   * @param value The new value of the proximity browsing parents setting.
   */
  public void setProximityBrowsingParents(final int value) {
    Preconditions.checkArgument(value >= -1, "IE00891: Distance argument must not be less than -1");

    if (value == getProximityBrowsingParents()) {
      return;
    }

    if (m_type == null) {
      m_proximityBrowsingParents = value;
    } else {
      m_type.setProximityBrowsingParents(value);
    }
  }

  /**
   * Changes the current proximity browsing preview setting.
   *
   * @param value The new value of the proximity browsing preview setting.
   */
  public void setProximityBrowsingPreview(final boolean value) {
    if (value == getProximityBrowsingPreview()) {
      return;
    }

    if (m_type == null) {
      m_ProximityPreview = value;
    } else {
      m_type.setProximityBrowsingPreview(value);
    }

    for (final IProximitySettingsListener listener : m_listeners) {
      try {
        listener.changedProximityBrowsingPreview(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
