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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.Actions;

import com.google.security.zynamics.zylib.general.ClipboardHelpers;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


/**
 * Action class used for copying stack values to the clipboard.
 */
public final class CCopyStackValueAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4792307925622132321L;

  /**
   * Value to copy to the clipboard.
   */
  private final String m_value;

  /**
   * Creates a new action object.
   *
   * @param value Value to copy to the clipboard.
   */
  public CCopyStackValueAction(final String value) {
    super(String.format("Copy '%s' to clipboard", value));

    m_value = value;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    ClipboardHelpers.copyToClipboard(m_value);
  }
}
