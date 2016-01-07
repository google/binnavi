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
package com.google.security.zynamics.binnavi.Gui;

import com.google.security.zynamics.zylib.gui.textfields.JTextFieldLimit;

import javax.swing.JTextPane;


/**
 * Input field that is limited to 30000 characters.
 */
public class CLimitedInputPane extends JTextPane {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2063080879169049120L;

  /**
   * Creates a new limited input field.
   * 
   * @param text Initial text of the input field.
   */
  public CLimitedInputPane(final String text) {
    super(new JTextFieldLimit(30000));

    setText(text);
  }
}
