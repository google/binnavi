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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import java.awt.Window;

import javax.swing.JPopupMenu;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel.Actions.CResumeThreadAction;
import com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel.Actions.CSuspendThreadAction;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

/**
 * Context menu of the thread information table.
 */
public final class CThreadInformationTableMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -6662728433347893934L;

  /**
   * Creates a new table menu object.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger The debugger used to manipulate the thread.
   * @param thread The clicked thread.
   */
  public CThreadInformationTableMenu(
      final Window parent, final IDebugger debugger, final TargetProcessThread thread) {
    Preconditions.checkNotNull(parent, "IE00648: Parent argument can not be null");

    Preconditions.checkNotNull(thread, "IE00650: Thread argument can not be null");

    if (debugger != null) {
      if (thread.getState() == ThreadState.RUNNING) {
        add(CActionProxy.proxy(new CSuspendThreadAction(thread)));
      } else {
        add(CActionProxy.proxy(new CResumeThreadAction(thread)));
      }
    }
  }
}
