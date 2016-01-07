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

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettings;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettingsListener;

/**
 * Contains graph display settings.
 */
public final class ZyGraphDisplaySettings implements IDisplaySettings {
  /**
   * Flag that determines if a gradient background is shown in graph windows.
   */
  private boolean m_gradientBackground;

  /**
   * Flag that determines if magnifying mode is enabled.
   */
  private boolean m_magnifyingGlassMode;

  /**
   * Flag that determines if function information is shown in function nodes.
   */
  private boolean m_showFunctionNodeInformation;

  /**
   * Debuggers for which memory addresses are shown.
   */
  private final Set<IDebugger> m_showMemoryAddresses = new HashSet<IDebugger>();

  /**
   * Speed of graph animations.
   */
  private int m_animationSpeed;

  /**
   * Flag that determines whether variable access operands should be shown in simplified mode or
   * not.
   */
  private boolean m_simplifiedVariableAccess = false;

  /**
   * Settings file object that provides the settings. This value can be null.
   */
  private final GraphSettingsConfigItem m_type;

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IZyGraphDisplaySettingsListener> m_listeners =
      new ListenerProvider<IZyGraphDisplaySettingsListener>();

  /**
   * Listeners that are notified about changes in the graph settings.
   */
  private final ListenerProvider<IDisplaySettingsListener> m_glisteners =
      new ListenerProvider<IDisplaySettingsListener>();

  /**
   * Creates a new settings object backed by graph settings from the configuration file.
   *
   * @param type Graph settings from the configuration file.
   */
  public ZyGraphDisplaySettings(final GraphSettingsConfigItem type) {
    Preconditions.checkNotNull(type, "IE02016: Type argument can't be null");

    m_type = type;
  }

  /**
   * Creates a new settings object by copying the information from another settings object.
   *
   * @param settings The source settings object.
   */
  public ZyGraphDisplaySettings(final ZyGraphDisplaySettings settings) {
    m_type = null;

    m_animationSpeed = settings.getAnimationSpeed();
    m_gradientBackground = settings.getGradientBackground();
    m_showFunctionNodeInformation = settings.getFunctionNodeInformation();
    m_simplifiedVariableAccess = settings.getSimplifiedVariableAccess();
  }

  @Override
  public void addListener(final IDisplaySettingsListener listener) {
    m_glisteners.addListener(listener);
  }

  /**
   * Adds a listener that is notified about changes in the graph view settings.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IZyGraphDisplaySettingsListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public int getAnimationSpeed() {
    return m_type == null ? m_animationSpeed : m_type.getAnimationSpeed();
  }

  /**
   * Returns the current function node information switch.
   *
   * @return True, to show function node information. False, to hide it.
   */
  public boolean getFunctionNodeInformation() {
    return m_type == null ? m_showFunctionNodeInformation : m_type.isFunctionNodeInformation();
  }

  /**
   * Returns the current gradient background.
   *
   * @return The current gradient background.
   */
  public boolean getGradientBackground() {
    return m_type == null ? m_gradientBackground : m_type.isGradientBackground();
  }

  @Override
  public boolean getMagnifyingGlassMode() {
    return m_magnifyingGlassMode;
  }

  /**
   * Returns the current show memory addresses setting.
   *
   * @param debugger The debugger whose setting is determined.
   *
   * @return The current show memory addresses setting.
   */
  public boolean getShowMemoryAddresses(final IDebugger debugger) {
    return m_showMemoryAddresses.contains(debugger);
  }

  /**
   * Returns whether simplified variable access mode is switch on or off.
   *
   * @return A flag that says whether simplified variable access is on.
   */
  public boolean getSimplifiedVariableAccess() {
    return m_type == null ? m_simplifiedVariableAccess : m_type.isSimplifiedVariableAccess();
  }

  @Override
  public void removeListener(final IDisplaySettingsListener listener) {
    m_glisteners.removeListener(listener);
  }

  /**
   * Removes a listener that was previously notified about changes in the display settings.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IZyGraphDisplaySettingsListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Changes the current animation speed setting.
   *
   * @param animationSpeed The new value of the animation speed setting.
   */
  public void setAnimationSpeed(final int animationSpeed) {
    if (animationSpeed == getAnimationSpeed()) {
      return;
    }

    if (m_type == null) {
      m_animationSpeed = animationSpeed;
    } else {
      m_type.setAnimationSpeed(animationSpeed);
    }
  }

  /**
   * Sets the function node information switch.
   *
   * @param value True, to show function node information. False, to hide it.
   */
  public void setFunctionNodeInformation(final boolean value) {
    if (value == getFunctionNodeInformation()) {
      return;
    }

    if (m_type == null) {
      m_showFunctionNodeInformation = value;
    } else {
      m_type.setFunctionNodeInformation(value);
    }

    for (final IZyGraphDisplaySettingsListener listener : m_listeners) {
      try {
        listener.changedFunctionNodeInformation(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current gradient background setting.
   *
   * @param value The new value of the gradient background setting.
   */
  public void setGradientBackground(final boolean value) {
    if (value == getGradientBackground()) {
      return;
    }

    if (m_type == null) {
      m_gradientBackground = value;
    } else {
      m_type.setGradientBackground(value);
    }

    for (final IZyGraphDisplaySettingsListener listener : m_listeners) {
      try {
        listener.changedGradientBackground(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setMagnifyingGlassMode(final boolean value) {
    if (m_magnifyingGlassMode == value) {
      return;
    }

    m_magnifyingGlassMode = value;

    for (final IDisplaySettingsListener listener : m_glisteners) {
      try {
        listener.changedMagnifyingGlass(value);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the current show memory addresses setting.
   *
   * @param debugger The debugger for which the setting is changed.
   * @param selected The new value of the show memory addresses setting.
   */
  public void setShowMemoryAddresses(final IDebugger debugger, final boolean selected) {
    if (selected) {
      m_showMemoryAddresses.add(debugger);
    } else {
      m_showMemoryAddresses.remove(debugger);
    }

    for (final IZyGraphDisplaySettingsListener listener : m_listeners) {
      try {
        listener.changedShowMemoryAddresses(debugger, selected);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Changes the simplified variable access mode setting.
   *
   * @param simplified True, to turn on simplified variable access mode. False, to turn it off.
   */
  public void setSimplifiedVariableAccess(final boolean simplified) {
    if (simplified == getSimplifiedVariableAccess()) {
      return;
    }

    if (m_type == null) {
      m_simplifiedVariableAccess = simplified;
    } else {
      m_type.setSimplifiedVariableAccess(simplified);
    }

    for (final IZyGraphDisplaySettingsListener listener : m_listeners) {
      try {
        listener.changedSimplifiedVariableAccess(simplified);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
