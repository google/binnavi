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

import javax.swing.JLabel;

import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;


/**
 * Listener class which is used to update the label when certain debug events occur.
 */
public class CDebugEventListener extends DebugEventListenerAdapter {
  private final JLabel m_label;

  /**
   * Creates a new debug event listener object.
   *
   * @param label The label to be updated.
   */
  public CDebugEventListener(final JLabel label) {
    m_label = label;
  }

  @Override
  public void receivedReply(final ResumeReply reply) {
    m_label.setText("Process is running");
  }
}
