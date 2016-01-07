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
package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerSelectionPanel;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.gui.DefaultWrapper;

/**
 * Used to encapsulate debugger objects for displaying them in the GUI.
 */
public final class CDebuggerWrapper extends DefaultWrapper<IDebugger> {
  /**
   * Creates a new debugger wrap.
   *
   * @param debugger The debugger to wrap.
   */
  public CDebuggerWrapper(final IDebugger debugger) {
    super(debugger);

    Preconditions.checkNotNull(debugger, "IE01368: Debugger can not be null");
  }

  @Override
  public String toString() {
    return getObject().toString();
  }
}
