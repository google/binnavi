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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component.Navigation;

/**
 * Represents a single search result.
 */
public final class CSearchResult {
  /**
   * Start offset of the search result.
   */
  public final long m_offset; // NO_UCD

  /**
   * Length of the search result in bytes.
   */
  public final int m_length; // NO_UCD

  /**
   * Creates a new search result.
   * 
   * @param offset Start offset of the search result.
   * @param length Length of the search result in bytes.
   */
  public CSearchResult(final long offset, final int length) {
    m_offset = offset;
    m_length = length;
  }
}
