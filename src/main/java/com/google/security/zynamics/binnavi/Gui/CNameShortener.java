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
package com.google.security.zynamics.binnavi.Gui;

import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

/**
 * Helper class for shortening names.
 */
public final class CNameShortener {
  /**
   * Maximum size of names in characters.
   */
  private static final int MAXIMUM_SIZE = 30;

  /**
   * You are not supposed to instantiate this class.
   */
  private CNameShortener() {
  }

  /**
   * Shortens a view name.
   * 
   * @param view The view whose name is shortened.
   * 
   * @return The shortened view name.
   */
  public static String shorten(final INaviView view) {
    final String name = view.getName();

    return name.length() <= MAXIMUM_SIZE ? name : name.substring(0, MAXIMUM_SIZE) + "...";
  }
}
