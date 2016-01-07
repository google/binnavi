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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Actions;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.ClipboardHelpers;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action class that can be used to copy a register value to the clipboard.
 */
public final class CCopyRegisterValueAction extends AbstractAction {
  /**
   * Register value to copy to the clipboard.
   */
  private final String value;

  /**
   * Creates a new Goto Offset action object.
   *
   * @param value Register value to copy to the clipboard.
   */
  public CCopyRegisterValueAction(final String value) {
    super(String.format("Copy value %s to the clipboard", value));
    this.value = Preconditions.checkNotNull(value, "IE01482: Value argument can not be null");
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    ClipboardHelpers.copyToClipboard(value);
  }
}
