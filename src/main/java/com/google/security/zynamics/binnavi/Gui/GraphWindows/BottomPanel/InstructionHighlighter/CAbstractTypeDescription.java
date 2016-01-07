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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.awt.Color;



/**
 * Abstract base class for all kinds of type descriptions.
 */
public abstract class CAbstractTypeDescription implements ITypeDescription {
  /**
   * Currently used color to highlight instructions of this type.
   */
  private Color m_color;

  /**
   * The tooltip hint for instructions of this type.
   */
  private final String m_hint;

  /**
   * Flag that says whether highlighting of instructions of this type is enabled or disabled.
   */
  private boolean m_isEnabled = false;

  /**
   * Listeners that are notified about changes in the description.
   */
  private final ListenerProvider<ITypeDescriptionListener> m_listeners =
      new ListenerProvider<ITypeDescriptionListener>();

  /**
   * Creates a new description object.
   *
   * @param color Default highlighting color for this object.
   * @param hint The tooltip hint for instructions of this type.
   */
  protected CAbstractTypeDescription(final Color color, final String hint) {
    m_color = color;
    m_hint = hint;
  }

  @Override
  public void addListener(final ITypeDescriptionListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public Color getColor() {
    return m_color;
  }

  @Override
  public String getHint() {
    return m_hint;
  }

  @Override
  public boolean isEnabled() {
    return m_isEnabled;
  }

  @Override
  public void removeListener(final ITypeDescriptionListener listener) {
    m_listeners.removeListener(listener);
  }

  @Override
  public void setColor(final Color color) {
    m_color = color;

    for (final ITypeDescriptionListener listener : m_listeners) {
      try {
        listener.changedColor(color);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void setEnabled(final boolean enabled) {
    m_isEnabled = enabled;

    for (final ITypeDescriptionListener listener : m_listeners) {
      try {
        listener.changedStatus(enabled);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
