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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.security.zynamics.binnavi.Help.CHelpLabel;
import com.google.security.zynamics.binnavi.Help.IHelpInformation;


/**
 * Small helper class for panels that contain a label and an arbitrary other component.
 */
public final class CLabeledComponent extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7357451083830097279L;

  /**
   * Creates a new labeled component.
   * 
   * @param labelText The text of the label.
   * @param helpInfo Provides context-sensitive information for the created label.
   * @param component The component to add to the panel.
   */
  public CLabeledComponent(final String labelText, final IHelpInformation helpInfo,
      final JComponent component) {
    super(new BorderLayout(5, 5));

    final JLabel label = new CHelpLabel(labelText, helpInfo);

    label.setPreferredSize(new Dimension(170, 25));

    add(label, BorderLayout.WEST);
    add(component, BorderLayout.CENTER);
  }
}
