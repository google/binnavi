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
package com.google.security.zynamics.binnavi.Gui.StandardEditPanel;

import com.google.security.zynamics.binnavi.Help.IHelpInformation;

/**
 * Provides initialization information for a text field with context-sensitive help.
 * 
 * @param <T> The type of the wrapped value.
 */
public final class CDefaultFieldDescription<T> implements IFieldDescription<T> {
  /**
   * Initial value of the text field.
   */
  private final T m_value;

  /**
   * Context-sensitive help information of the field.
   */
  private final IHelpInformation m_help;

  /**
   * Creates a new field description object.
   * 
   * @param value Initial value of the text field.
   * @param help Context-sensitive help information of the field.
   */
  public CDefaultFieldDescription(final T value, final IHelpInformation help) {
    m_value = value;
    m_help = help;
  }

  @Override
  public IHelpInformation getHelp() {
    return m_help;
  }

  @Override
  public T getValue() {
    return m_value;
  }
}
