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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph.Settings;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.IZyGraphLayoutSettingsListener;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphCircularSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphHierarchicalSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphOrthogonalSettings;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyLayoutCreator;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.settings.ILayoutSettings;

import y.layout.CanonicMultiStageLayouter;

/**
 * Class that encapsulates layout related graph settings.
 */
public final class ZyGraphLayoutSettings implements ILayoutSettings {
  /**
   * Number of nodes made visible in one step before a warning dialog is shown.
   */
  private int m_visibilityWarningThreshold;

  /**
   * Flag that determines whether automatic layouting is enabled.
   */
  private boolean m_automaticLayouting;

  /**
   * Number of nodes a native graph must have before automatic layouting is deactived.
   */
  private int m_autoLayoutDeactivationThreshold;

  private int m_animateLayoutNodeThreshold;

  private int m_animateLayoutEdgeThreshold;

  /**
   * Number of nodes a graph must have before a warning dialog is shown that layouting the graph can
   * take a while.
   */
  private int m_layoutCalculationTimeWarningThreshold;

  /**
   * Flag that determines if layout operations are animated.
   */
  private boolean m_layoutAnimation;

  /**
   * Default graph layout for layouting operations.
   */
  private LayoutStyle m_defaultGraphLayout;

  /**
   * Configuration file object that is synchronized with this settings class.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Currently active layouter.
   */
  private CanonicMultiStageLayouter m_currentLayouter;

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IZyGraphLayoutSettingsListener> m_listeners =
      new ListenerProvider<IZyGraphLayoutSettingsListener>();

  /**
   * Contains settings for circular layouting.
   */
  private final ZyGraphCircularSettings m_circularSettings;

  /**
   * Contains settings for hierarchical layouting.
   */
  private final ZyGraphHierarchicalSettings m_hierarchicalSettings;

  /**
   * Contains settings for orthogonal layouting.
   */
  private final ZyGraphOrthogonalSettings m_orthogonalSettings;

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphLayoutSettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02023: Type argument can't be null");

    m_type = type;

    m_circularSettings = new ZyGraphCircularSettings(type);
    m_hierarchicalSettings = new ZyGraphHierarchicalSettings(type);
    m_orthogonalSettings = new ZyGraphOrthogonalSettings(type);
  }

  /**
   * Creates a new settings type by copying the settings of another settings type.
   *
   * @param settings The settings type that provides the initial settings.
   */
  public ZyGraphLayoutSettings(final ZyGraphLayoutSettings settings) {
    m_type = null;

    m_defaultGraphLayout = settings.getDefaultGraphLayout();

    m_animateLayoutEdgeThreshold = settings.getAnimateLayoutEdgeThreshold();
    m_animateLayoutNodeThreshold = settings.getAnimateLayoutNodeThreshold();

    m_autoLayoutDeactivationThreshold = settings.getAutolayoutDeactivationThreshold();
    m_automaticLayouting = settings.getAutomaticLayouting();
    m_layoutAnimation = settings.getAnimateLayout();
    m_layoutCalculationTimeWarningThreshold = settings.getLayoutCalculationTimeWarningThreshold();
    m_visibilityWarningThreshold = settings.getVisibilityWarningTreshold();

    m_circularSettings = new ZyGraphCircularSettings(settings.getCircularSettings());
    m_hierarchicalSettings = new ZyGraphHierarchicalSettings(settings.getHierarchicalSettings());
    m_orthogonalSettings = new ZyGraphOrthogonalSettings(settings.getOrthogonalSettings());

    updateLayouter();
  }

  /**
   * Converts the numerical value of a configuration file default graph layout type to an
   * enumeration value.
   *
   * @param type The configuration file settings type that provides the numerical value.
   *
   * @return The corresponding enumeration value or a default value if the value from the
   *         configuration file is invalid.
   */
  private static LayoutStyle getDefaultLayout(final GraphSettingsConfigItem type) {
    try {
      return LayoutStyle.parseInt(type.getDefaultGraphLayout());
    } catch (final IllegalStateException e) {
      CUtilityFunctions.logException(e);

      return LayoutStyle.CIRCULAR;
    }
  }

  /**
   * Sets the current layouter.
   *
   * @param layouter The new layouter.
   */
  private void setCurrentLayouter(final CanonicMultiStageLayouter layouter) {
    m_currentLayouter =
        Preconditions.checkNotNull(layouter, "IE02249: Layouter argument can't be null");
  }

  /**
   * Updates the active graph layouter object depending on the current layouter settings.
   */
  private void updateLayouter() {
    if (getDefaultGraphLayout() == LayoutStyle.HIERARCHIC) {
      setCurrentLayouter(ZyLayoutCreator.getHierarchicLayout(this));
    } else if (getDefaultGraphLayout() == LayoutStyle.ORTHOGONAL) {
      setCurrentLayouter(ZyLayoutCreator.getOrthogonalLayout(this));
    } else {
      setCurrentLayouter(ZyLayoutCreator.getCircularLayout(this));
    }
  }

  /**
   * Adds a listener that is notified about changes in the layout settings.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IZyGraphLayoutSettingsListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public boolean getAnimateLayout() {
    return m_type == null ? m_layoutAnimation : m_type.isLayoutAnimation();
  }

  @Override
  public int getAnimateLayoutEdgeThreshold() {
    return m_type == null ? m_animateLayoutEdgeThreshold : m_type.getAnimateLayoutEdgeThreshold();
  }

  @Override
  public int getAnimateLayoutNodeThreshold() {
    return m_type == null ? m_animateLayoutNodeThreshold : m_type.getAnimateLayoutNodeThreshold();
  }

  /**
   * Returns the current autolayout deactivation threshold.
   *
   * @return The current autolayout deactivation threshold.
   */
  public int getAutolayoutDeactivationThreshold() {
    return m_type == null ? m_autoLayoutDeactivationThreshold : m_type
        .getAutoLayoutDeactivationThreshold();
  }

  @Override
  public boolean getAutomaticLayouting() {
    return m_type == null ? m_automaticLayouting : m_type.isAutomaticLayouting();
  }

  /**
   * Returns the circular layouting settings.
   *
   * @return The circular layouting settings.
   */
  public ZyGraphCircularSettings getCircularSettings() {
    return m_circularSettings;
  }

  /**
   * Returns the current layouter.
   *
   * @return The current layouter.
   */
  @Override
  public CanonicMultiStageLayouter getCurrentLayouter() {
    return m_currentLayouter;
  }

  /**
   * Returns the current default graph layout.
   *
   * @return The current default graph layout.
   */
  public LayoutStyle getDefaultGraphLayout() {
    return m_type == null ? m_defaultGraphLayout : getDefaultLayout(m_type);
  }

  /**
   * Returns the hierarchical layouting settings.
   *
   * @return The hierarchical layouting settings.
   */
  public ZyGraphHierarchicalSettings getHierarchicalSettings() {
    return m_hierarchicalSettings;
  }

  /**
   * Returns the current layout calculation time warning threshold.
   *
   * @return The current layout calculation time warning threshold.
   */
  public int getLayoutCalculationTimeWarningThreshold() {
    return m_type == null ? m_layoutCalculationTimeWarningThreshold : m_type
        .getLayoutCalculationThreshold();
  }

  /**
   * Returns the orthogonal layouting settings.
   *
   * @return The orthogonal layouting settings.
   */
  public ZyGraphOrthogonalSettings getOrthogonalSettings() {
    return m_orthogonalSettings;
  }

  /**
   * Returns the current visibility warning threshold.
   *
   * @return The current visibility warning threshold.
   */
  public int getVisibilityWarningTreshold() {
    return m_type == null ? m_visibilityWarningThreshold : m_type.getVisibilityWarningThreshold();
  }

  /**
   * Removes a previously attached listener object.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IZyGraphLayoutSettingsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the current animate layout setting.
   *
   * @param value The new value of the animate layout setting.
   */
  public void setAnimateLayout(final boolean value) {
    if (value == getAnimateLayout()) {
      return;
    }

    if (m_type == null) {
      m_layoutAnimation = value;
    } else {
      m_type.setLayoutAnimation(value);
    }
  }

  /**
   * Changes the current autolayout deactivation setting.
   *
   * @param value The new value of the autolayout deactivation setting.
   */
  public void setAutolayoutActivisionThreshold(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00901: Threshold value must not be negative");

    if (value == getAutolayoutDeactivationThreshold()) {
      return;
    }

    if (m_type == null) {
      m_autoLayoutDeactivationThreshold = value;
    } else {
      m_type.setAnimationSpeed(value);
    }
  }

  /**
   * Enables or disables automated layouting.
   *
   * @param value True, to enable automated layouting. False, to disable it.
   */
  public void setAutomaticLayouting(final boolean value) {
    if (value == getAutomaticLayouting()) {
      return;
    }

    if (m_type == null) {
      m_automaticLayouting = value;
    } else {
      m_type.setAutomaticLayouting(value);
    }

    for (final IZyGraphLayoutSettingsListener listener : m_listeners) {
      try {
        listener.changedAutomaticLayouting(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current default graph layout setting. Warning this code does not only change the
   * setting but also generates a complete new Layouter.
   *
   * @param value The new value of the default graph layout setting.
   */
  public void setDefaultGraphLayout(final LayoutStyle value) {
    Preconditions.checkNotNull(value, "IE00876: Graph layout argument can't be null");

    // ATTENTION: DO NOT RETURN ON value == getDefaultGraphLayout() BECAUSE LAYOUTER SETTINGS CAN
    // CHANGE

    if (m_type == null) {
      m_defaultGraphLayout = value;
    } else {
      m_type.setDefaultGraphLayout(value.ordinal());
    }

    updateLayouter();
  }

  /**
   * Changes the current layout calculation time warning threshold setting.
   *
   * @param value The new value of the layout calculation time warning threshold setting.
   */
  public void setLayoutCalculationTimeWarningThreshold(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00880: Threshold argument must not be negative");

    if (value == getLayoutCalculationTimeWarningThreshold()) {
      return;
    }

    if (m_type == null) {
      m_layoutCalculationTimeWarningThreshold = value;
    } else {
      m_type.setLayoutCalculationThreshold(value);
    }
  }

  /**
   * Changes the current visibility warning threshold setting.
   *
   * @param value The new value of the visibility warning threshold setting.
   */
  public void setVisibilityWarningThreshold(final int value) {
    Preconditions.checkArgument(value >= 0, "IE00893: Threshold argument must not be negative");

    if (value == getVisibilityWarningTreshold()) {
      return;
    }

    if (m_type == null) {
      m_visibilityWarningThreshold = value;
    } else {
      m_type.setVisibilityWarningThreshold(value);
    }
  }

}
