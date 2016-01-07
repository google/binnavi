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
package com.google.security.zynamics.binnavi.Gui.Debug.SearchMemory;

import javax.swing.JFormattedTextField.AbstractFormatterFactory;

/**
 * Searcher class that can be used to search for ASCII strings.
 */
public final class AsciiSearcher implements ISearcher {
  @Override
  public String getAlternativeName() {
    return "Hex";
  }

  @Override
  public String getAlternativeString(final String text) {
    final StringBuffer output = new StringBuffer();

    for (int i = 0; i < text.length(); i++) {
      output.append(String.format("%02X ", (int) text.charAt(i)));
    }

    return output.toString();
  }

  @Override
  public AbstractFormatterFactory getFormatterFactory() {
    return null;
  }

  @Override
  public byte[] getSearchData(final String text) {
    final byte[] output = new byte[text.length()];

    for (int i = 0; i < output.length; i++) {
      output[i] = (byte) (text.charAt(i) & 0xFF);
    }

    return output;
  }

  @Override
  public String toString() {
    return "ASCII";
  }
}
