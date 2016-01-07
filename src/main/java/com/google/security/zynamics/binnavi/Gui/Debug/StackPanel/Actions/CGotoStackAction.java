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

import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.CStackMemoryProvider;
import com.google.security.zynamics.zylib.gui.JStackView.JStackView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;



/**
 * Action class to scroll the stack window to the current value of the stack pointer.
 */
public final class CGotoStackAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8195436728352490120L;

  /**
   * The stack view to scroll.
   */
  private final JStackView m_stackView;

  /**
   * Provides information about the current state of the stack.
   */
  private final CStackMemoryProvider m_model;

  /**
   * Creates a new action object.
   *
   * @param stackView The stack view to scroll.
   * @param model Provides information about the current state of the stack.
   */
  public CGotoStackAction(final JStackView stackView, final CStackMemoryProvider model) {
    super("Goto Stack Pointer");

    m_stackView = stackView;
    m_model = model;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    m_stackView.gotoOffset(m_model.getStackPointer());
  }
}
