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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui;

import javax.swing.JTextField;

import com.google.security.zynamics.binnavi.Help.IHelpInformation;
import com.google.security.zynamics.binnavi.Help.IHelpProvider;


/**
 * Input field for the graph search field.
 */
public final class CSearchInputField extends JTextField implements IHelpProvider {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5351082390430822699L;

  @Override
  public IHelpInformation getHelpInformation() {
    return new CSearchFieldHelp();
  }
}
