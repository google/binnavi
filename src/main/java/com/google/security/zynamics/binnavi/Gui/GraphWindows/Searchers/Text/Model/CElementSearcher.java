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

import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Contains the code that can both be used for edge searching and node searching.
 */
public final class CElementSearcher {
  /**
   * You are not supposed to instantiate this class.
   */
  private CElementSearcher() {
  }

  /**
   * Performs a regular expression search on a single line.
   *
   * @param element The element to search through.
   * @param lineText The line text to search through.
   * @param lineCounter The index of the line in the node.
   * @param localSearchString The string to search for.
   * @param startPosition The start position of the search in the line.
   * @param caseSensitive True, to signal case sensitive search.
   * @param results If something is found, a search result is added to this list.
   *
   * @return The start position for the next iteration or -1 if nothing was found.
   */
  private static int doRegexSearch(final Object element,
      final String lineText,
      final int lineCounter,
      final String localSearchString,
      final int startPosition,
      final boolean caseSensitive,
      final List<SearchResult> results) {
    final Pattern pattern = caseSensitive ? Pattern.compile(localSearchString) : Pattern.compile(
        localSearchString, Pattern.CASE_INSENSITIVE);

    final Matcher matcher = pattern.matcher(lineText);
    final boolean found = matcher.find(startPosition);

    if (found) {
      final int start = matcher.start();
      final int end = matcher.end();

      if (start != end) {
        // Something was found
        results.add(new SearchResult(element, lineCounter, start, end - start));
      }

      if (matcher.end() == lineText.length()) {
        return -1;
      }

      if (start == end) {
        return end + 1;
      }

      return end;
    } else {
      return -1;
    }
  }

  /**
   * Performs a normal search on a single line.
   *
   * @param element The element to search through.
   * @param lineText The line text to search through.
   * @param lineCounter The index of the line in the node.
   * @param localSearchString The string to search for.
   * @param startPosition The start position of the search in the line.
   * @param caseSensitive True, to signal case sensitive search.
   * @param results If something is found, a search result is added to this list.
   *
   * @return The start position for the next iteration or -1 if nothing was found.
   */
  private static int doTextSearch(final Object element,
      final String lineText,
      final int lineCounter,
      final String localSearchString,
      final int startPosition,
      final boolean caseSensitive,
      final List<SearchResult> results) {
    final int index = lineText.indexOf(
        caseSensitive ? localSearchString : localSearchString.toLowerCase(), startPosition);

    if (index == -1) {
      return -1;
    } else {
      results.add(new SearchResult(element, lineCounter, index, localSearchString.length()));
      return index + localSearchString.length();
    }
  }

  /**
   * Performs a search on a label content.
   *
   * @param element The element to search through.
   * @param content The label content to search through.
   * @param searchString The string to search for.
   * @param regEx True, if regular expression search.
   * @param caseSensitive True, to signal case sensitive search.
   *
   * @return The start position for the next iteration or -1 if nothing was found.
   */
  public static List<SearchResult> search(final Object element, final ZyLabelContent content,
      final String searchString, final boolean regEx, final boolean caseSensitive) {
    final ArrayList<SearchResult> results = new ArrayList<SearchResult>();

    if ("".equals(searchString)) {
      return results;
    }

    int lineCounter = 0;

    for (final ZyLineContent lineContent : content) {
      final String lineText =
          caseSensitive ? lineContent.getText() : lineContent.getText().toLowerCase();

      int startPosition = 0;

      do {
        startPosition = regEx ? CElementSearcher.doRegexSearch(element,
            lineText,
            lineCounter,
            searchString,
            startPosition,
            caseSensitive,
            results) : CElementSearcher.doTextSearch(element,
            lineText,
            lineCounter,
            searchString,
            startPosition,
            caseSensitive,
            results);
      } while (startPosition != -1);

      ++lineCounter;
    }

    return results;
  }

}
