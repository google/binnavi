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
package com.google.security.zynamics.binnavi.Gui.HotkeyDialog;

import com.google.common.base.Preconditions;


/**
 * Wrapper class for displaying the right part of hotkey section titles in the table.
 */
public final class CRightTitle {
  /**
   * Right half of the hotkey section title
   */
  private final String m_string;

  /**
   * Creates a new wrapper object.
   *
   * @param string Right half of the hotkey section title.
   */
  public CRightTitle(final String string) {
    Preconditions.checkNotNull(string, "IE01818: String argument can not be null");

    m_string = string;
  }

  @Override
  public String toString() {
    return m_string;
  }
}
