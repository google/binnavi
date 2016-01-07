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

import java.util.ArrayList;
import java.util.List;


/**
 * Class used to iterate over the results of a search operation.
 */
public final class CGraphSearchResultsCursor {
  /**
   * Flag that indicates whether search result iteration passed the last search result.
   */
  private boolean m_isAfterLast = false;

  /**
   * Flag that indicates whether reverse search result iteration passed the first search result.
   */
  private boolean m_isBeforeFirst = false;

  /**
   * Current index of the search result iteration.
   */
  private int m_currentSearchIndex = -1;

  /**
   * List of current search results, pruned to one search result per result object.
   */
  private final List<SearchResult> m_reducedResults = new ArrayList<SearchResult>();

  /**
   * List of unfiltered current results.
   */
  private List<SearchResult> m_results;

  /**
   * Fills the reduced results list by skipping more than one search result for any given result
   * object.
   */
  private void removeDuplicates() {
    // TODO: This function requires sorted result; get rid of this requirement.

    m_reducedResults.clear();

    Object last = null;

    for (final SearchResult r : m_results) {
      if (last != r.getObject()) {
        last = r.getObject();
        m_reducedResults.add(r);
      }
    }
  }

  /**
   * Resets the cursor and clears the current results.
   */
  public void clear() {
    m_reducedResults.clear();

    m_currentSearchIndex = 0;
  }

  /**
   * Returns the current search result during iteration.
   *
   * @return The current search result.
   */
  public SearchResult current() {
    return m_reducedResults.isEmpty() ? null : m_reducedResults.get(m_currentSearchIndex);
  }

  /**
   * Returns a flag that indicates whether search result iteration passed the last search result.
   *
   * @return True, if the last search result was passed.
   */
  public boolean isAfterLast() {
    return m_isAfterLast;
  }

  /**
   * Returns a flag that indicates whether reverse search result iteration passed the first search
   * result.
   *
   * @return True, if the first search result was passed.
   */
  public boolean isBeforeFirst() {
    return m_isBeforeFirst;
  }

  /**
   * Sets the iteration index of the current search result iteration.
   *
   * @param resultIndex The new search result index.
   */
  public void jumpTo(final int resultIndex) {
    m_currentSearchIndex = 0;

    SearchResult previousResult = m_results.get(0);

    for (int i = 1; i <= resultIndex; i++) {
      final SearchResult currentResult = m_results.get(i);

      if (previousResult.getObject() != currentResult.getObject()) {
        m_currentSearchIndex++;
      }

      previousResult = currentResult;
    }

    if (m_currentSearchIndex >= m_reducedResults.size()) {
      throw new IllegalStateException();
    }
  }

  /**
   * Moves to the current result to the next result. If there is no result left, the current result
   * remains the last result.
   */
  public void next() {
    if (current() == null) {
      return;
    }

    m_currentSearchIndex++;

    m_isBeforeFirst = false;
    m_isAfterLast = false;

    if (m_currentSearchIndex == m_reducedResults.size()) {
      m_currentSearchIndex = 0;

      m_isAfterLast = true;
    }
  }

  /**
   * Moves search result iteration to the previous search result.
   */
  public void previous() {
    if (current() == null) {
      return;
    }

    m_currentSearchIndex--;

    m_isBeforeFirst = false;
    m_isAfterLast = false;

    if (m_currentSearchIndex < 0) {
      m_currentSearchIndex = m_reducedResults.size() - 1;

      m_isBeforeFirst = true;
    }
  }

  /**
   * Resets the cursor to the beginning.
   */
  public void reset() {
    m_currentSearchIndex = 0;
  }

  /**
   * Sets the unfiltered results of a search operation.
   *
   * @param results The unfiltered results of a search operation.
   */
  public void setResults(final List<SearchResult> results) {
    m_results = new ArrayList<SearchResult>(results);

    removeDuplicates();
  }
}
