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
package com.google.security.zynamics.zylib.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class contains stream utility functions.
 * 
 * @author cblichmann@google.com (Christian Blichmann)
 */
public class StreamUtils {
  private StreamUtils() {
    // Static methods only
  }

  /**
   * Reads lines of text from the specified {@link Reader}.
   * 
   * @param reader the reader to read lines from
   * @return a list containing the lines read. This list may be empty if there was no data to read.
   * @throws IOException if an IO error occurs.
   */
  public static List<String> readLinesFromReader(final Reader reader) throws IOException {
    try (BufferedReader br = new BufferedReader(reader)) {
      return br.lines().collect(Collectors.toList());
    }
  }
}
