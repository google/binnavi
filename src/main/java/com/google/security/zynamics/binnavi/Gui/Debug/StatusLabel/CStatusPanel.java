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
package com.google.security.zynamics.binnavi.Gui.Debug.StatusLabel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Panel that contains a label that shows the latest debugger events.
 */
public final class CStatusPanel extends JPanel {
  /**
   * JDK 1.1 serialVersionUID
   */
  private static final long serialVersionUID = -4286616964784111625L;

  /**
   * Label where the debugger events are shown.
   */
  private final JLabel m_label = new JLabel();

  /**
   * Synchronizer that makes sure the label is updated on new events.
   */
  private final CStatusLabelSynchronizer m_synchronizer;

  /**
   * Creates a new status panel.
   *
   * @param debuggerProvider Provides the debuggers whose events are shown.
   */
  public CStatusPanel(final BackEndDebuggerProvider debuggerProvider) {
    super(new BorderLayout());

    Preconditions.checkNotNull(
        debuggerProvider, "IE1094: Debugger provider argument can not be null");

    m_label.setForeground(Color.BLACK);

    add(m_label);

    m_synchronizer = new CStatusLabelSynchronizer(m_label, debuggerProvider);
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_synchronizer.dispose();
  }
}
