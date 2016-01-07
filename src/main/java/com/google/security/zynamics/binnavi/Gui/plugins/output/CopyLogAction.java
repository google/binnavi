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
package com.google.security.zynamics.binnavi.Gui.plugins.output;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextArea;

import com.google.security.zynamics.binnavi.Gui.plugins.output.implementations.CLogConsoleFunctions;


/**
 * Copies the log of the plugin log window to the clipboard.
 */
public final class CopyLogAction extends AbstractAction {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 7205444453838116310L;

  /**
   * The text area from which the log is copied.
   */
  private final JTextArea m_area;

  /**
   * Creates a new log action object.
   * 
   * @param area The text area from which the log is copied.
   */
  public CopyLogAction(final JTextArea area) {
    super("Copy");

    m_area = area;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    CLogConsoleFunctions.copy(m_area);
  }
}
