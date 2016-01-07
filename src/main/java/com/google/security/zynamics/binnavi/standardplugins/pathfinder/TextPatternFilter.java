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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import com.google.common.base.Preconditions;

/**
 * A filter class which allows to filter strings according to a specified pattern
 */
public class TextPatternFilter {
  /**
   * The pattern to be searched for
   */
  private final String m_pattern;

  public TextPatternFilter(final String pattern) {
    m_pattern =
        Preconditions.checkNotNull(pattern, "Error: pattern argument can not be null")
            .toLowerCase();
  }

  /**
   * 
   * @param text Text to check for a pattern
   * @return True if text matches the pattern and thus should be included in the list of tree nodes
   */
  public boolean matchesFilter(final String text) {
    final String lowText = text.toLowerCase();

    return (m_pattern.length() == 0) || lowText.contains(m_pattern);
  }
}
