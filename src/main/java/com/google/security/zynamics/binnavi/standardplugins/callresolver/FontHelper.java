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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Contains a helper function for finding the monospace font of the system.
 */
public final class FontHelper {
  /**
   * Determines the name of the monospace font of the system.
   * 
   * @return The name of the system monospace font.
   */
  public static String getMonospaceFont() {
    final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    final Font[] allfonts = env.getAllFonts();

    for (final Font font : allfonts) {
      if (font.getName().equals("Courier New")) {
        return "Courier New";
      }
    }

    return Font.MONOSPACED;
  }

}
