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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels;

import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.disassembly.IAddress;



/**
 * Listener adapter for the debug perspective model listener.
 */
public class CDebugPerspectiveModelListenerAdapter implements IDebugPerspectiveModelListener {
  @Override
  public void changedActiveAddress(final IAddress address, final boolean focusMemoryWindow) {
    // Empty default implementation
  }

  @Override
  public void changedActiveDebugger(final IDebugger oldDebugger, final IDebugger newDebugger) {
    // Empty default implementation
  }
}
