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
package com.google.security.zynamics.binnavi.Gui.Debug;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that is used to generate the fading color effect in tables for new process objects
 * (threads, memory modules, ...).
 *
 * @param <T> Type of the objects managed by the generator.
 */
public class CFadingColorGenerator<T> {
  /**
   * Objects and their fade color.
   */
  private final Map<T, Color> m_objects = new HashMap<T, Color>();

  /**
   * Returns the color to be used to display a given object.
   *
   * @param object The object whose color is determined.
   *
   * @return The color of the object or null if there is no such object.
   */
  public Color getColor(final T object) {
    return m_objects.get(object);
  }

  /**
   * Returns the next color for a given object.
   *
   * @param object The object whose next color is determined.
   *
   * @return The next color of the object.
   */
  public Color next(final T object) {
    if (!m_objects.containsKey(object)) {
      m_objects.put(object, Color.GREEN);

      return Color.GREEN;
    }

    final Color oldColor = m_objects.get(object);

    final int newLowest = Math.min((oldColor.getRGB() & 0xFF) + 0x40, 0xFF) & 0xFF;

    final int newMiddle = Math.min((oldColor.getRGB() & 0xFF0000) + 0x400000, 0xFF0000) & 0xFF0000;

    final Color newColor = new Color((oldColor.getRGB() & 0x00FF00) + newMiddle + newLowest);

    if (newColor == Color.WHITE) {
      m_objects.remove(object);
    } else {
      m_objects.put(object, newColor);
    }

    return newColor;
  }
}
