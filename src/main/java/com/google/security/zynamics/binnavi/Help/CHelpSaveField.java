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
package com.google.security.zynamics.binnavi.Help;

import com.google.security.zynamics.binnavi.Gui.SaveFields.CSaveField;

/**
 * Save text field class that can display context-sensitive help.
 */
public final class CHelpSaveField extends CSaveField implements IHelpProvider {
  /**
   * Provides context-sensitive help.
   */
  private final IHelpInformation information;

  /**
   * Creates a new save text field object.
   *
   * @param information Provides context-sensitive help.
   */
  public CHelpSaveField(final IHelpInformation information) {
    this("", information);
  }

  /**
   * Creates a new save text field object.
   *
   * @param text Initial value of the field.
   * @param information Provides context-sensitive help.
   */
  public CHelpSaveField(final String text, final IHelpInformation information) {
    super(text);
    this.information = information;
  }

  @Override
  public IHelpInformation getHelpInformation() {
    return information;
  }
}
