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

import com.google.security.zynamics.zylib.gui.CHexFormatter;

import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.text.DefaultFormatterFactory;


/**
 * Searcher class that can be used to search for hexadecimal strings.
 */
public final class HexSearcher implements ISearcher {
  /**
   * The formatter factory used by the input field where the search string is entered.
   */
  private final AbstractFormatterFactory m_factory =
      new DefaultFormatterFactory(new CHexFormatter());

  @Override
  public String getAlternativeName() {
    return "ASCII";
  }

  @Override
  public String getAlternativeString(final String text) {
    return com.google.security.zynamics.zylib.general.Convert.hexStringToAsciiString(text);
  }

  @Override
  public AbstractFormatterFactory getFormatterFactory() {
    return m_factory;
  }

  @Override
  public byte[] getSearchData(final String text) {
    return com.google.security.zynamics.zylib.general.Convert.hexStringToBytes(text);
  }

  @Override
  public String toString() {
    return "Hexadecimal";
  }
}
