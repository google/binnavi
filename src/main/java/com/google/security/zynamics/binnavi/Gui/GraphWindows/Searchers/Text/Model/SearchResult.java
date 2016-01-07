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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model;

/**
 * Represents a single search result.
 *
 * TODO: Get rid of type Object; introduce proper result object types.
 */
public final class SearchResult {
  /**
   * Node or edge that was found.
   */
  private final Object m_object;

  /**
   * Line of the search result inside the result object.
   */
  private final int m_line;

  /**
   * Index into the search result line.
   */
  private final int m_index;

  /**
   * Length of the search result in characters.
   */
  private final int m_length;

  /**
   * Creates a new search result object.
   *
   * @param object Node or edge that was found.
   * @param line Line of the search result inside the result object.
   * @param index Index into the search result line.
   * @param length Length of the search result in characters.
   */
  public SearchResult(final Object object, final int line, final int index, final int length) {
    m_object = object;
    m_line = line;
    m_index = index;
    m_length = length;
  }

  /**
   * Returns the length of the search result.
   *
   * @return The length of the search result.
   */
  public int getLength() {
    return m_length;
  }

  /**
   * Returns the search result line index.
   *
   * @return The search result line index.
   */
  public int getLine() {
    return m_line;
  }

  /**
   * Returns the search result object.
   *
   * @return The search result object.
   */
  public Object getObject() {
    return m_object;
  }

  /**
   * Returns the search result position.
   *
   * @return The search result position.
   */
  public int getPosition() {
    return m_index;
  }
}
