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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs;

import com.google.common.base.Preconditions;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Simple OK button panel used in comment dialogs. Disposes the parent component passed as argument
 * automatically.
 */
public final class OKButtonPanel extends JPanel {

  private final JButton button;
  private static final String OK = "OK";
  private final Window parentComponent;

  /**
   * Creates a new {@link OKButtonPanel OK Button panel}. It automatically disposes the passed
   * argument on click.
   *
   * @param parentComponent The component where this button is placed on.
   */
  public OKButtonPanel(final Window parentComponent) {
    this.parentComponent = Preconditions.checkNotNull(
        parentComponent, "Error: parent component argument can not be null");
    setLayout(new BorderLayout());
    button = new JButton(OK);
    button.addActionListener(new OKButtonListener());
    final JPanel panel = new JPanel();
    panel.setBorder(new EmptyBorder(5, 5, 5, 5));
    panel.add(button);
    add(panel, BorderLayout.EAST);
  }

  private class OKButtonListener implements ActionListener {
    @Override
    public void actionPerformed(final ActionEvent event) {
      parentComponent.dispose();
    }
  }
}
