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
package com.google.security.zynamics.zylib.strings;

/**
 * Small helper class for String related helper functions.
 */
public class StringHelper {
  /**
   * Counts how often a character appears in a given string.
   *
   * @param string The string to search through.
   * @param c The character to search for.
   *
   * @return Number of times the character appears in the string.
   */
  public static int count(final String string, final char c) {
    return (int) string.chars()
                  .filter(character -> character == c)
                  .count();
  }

  /**
   * Deprecated. Use replaceAll() method
   * of the java.lang.String class instead;
   * 
   * For example:
   * {@code
   * String initialString = "I teed some tews";
   * String resultString = initialString.replaceAll("t", "n");
   * //resultString will be: "I need some news"
   * }
   * 
   * Replaces all occurrences of a substring inside a string.
   *
   * @param inputLine The input string.
   * @param source The substring to be replaced.
   * @param target The replacement of the substring.
   *
   * @return The input line with all occurrences of source replaced by target.
   */
  @Deprecated
  public static String replaceAll(final String inputLine, final String source, final String target) {
    int index = inputLine.indexOf(source);

    String ret = inputLine;

    while (index != -1) {
      ret = ret.substring(0, index) + target + ret.substring(index + source.length());
      index = ret.indexOf(source);
    }

    return ret;
  }
}
