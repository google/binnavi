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
package com.google.security.zynamics.binnavi.Gui.SettingsDialog;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.gui.JHint.JHintIcon;

/**
 * Class used to create hint icons that display helpful information when the mouse cursor hover over
 * them.
 */
public final class CHintCreator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CHintCreator() {
  }

  /**
   * Adds a hint icon to a component.
   * 
   * @param component The component the hint icon is added to.
   * @param message The message shown by the hint icon when the cursor hovers over it.
   * 
   * @return The new component that contains both the passed component and the hint icon.
   */
  public static Component createHintPanel(final Component component, final String message) {
    Preconditions.checkNotNull(component, "IE01256: Component argument can not be null");

    Preconditions.checkNotNull(message, "IE01257: Message argument can not be null");

    final JPanel panel = new JPanel(new BorderLayout());

    panel.add(component, BorderLayout.CENTER);

    final JHintIcon hintPopup = new JHintIcon(message);
    hintPopup.setBorder(new EmptyBorder(0, 3, 0, 0));
    panel.add(hintPopup, BorderLayout.EAST);

    return panel;
  }
}
