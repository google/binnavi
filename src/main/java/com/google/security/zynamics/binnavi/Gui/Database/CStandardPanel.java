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
package com.google.security.zynamics.binnavi.Gui.Database;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;


/**
 * Standard panel used for displaying information in the database panel.
 */
public final class CStandardPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 5931304205978461447L;

  /**
   * Creates a standard edit panel.
   * 
   * @param labelText Text of the label.
   * @param helpInformation Provides context-sensitive information for the label.
   * @param component Component to the right of the label.
   */
  public CStandardPanel(final String labelText, final IHelpInformation helpInformation,
      final JComponent component) {
    super(new BorderLayout());
    final JLabel label =
        helpInformation == null ? new JLabel(labelText)
            : new CHelpLabel(labelText, helpInformation);

    final int PREFERRED_LABEL_WIDTH = 110;
    final int PREFERRED_LABEL_HEIGHT = 25;

    label.setPreferredSize(new Dimension(PREFERRED_LABEL_WIDTH, PREFERRED_LABEL_HEIGHT));
    add(label, BorderLayout.WEST);
    add(component, BorderLayout.CENTER);
    setBorder(new EmptyBorder(0, 0, 0, 0));
  }
}
