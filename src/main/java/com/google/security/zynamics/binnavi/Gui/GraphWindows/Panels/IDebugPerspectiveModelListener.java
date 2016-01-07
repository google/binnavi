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
 * This interface can be implemented by objects that want to be notified about relevant changes in
 * the debug GUI perspective.
 */
public interface IDebugPerspectiveModelListener {
  /**
   * Invoked after the active address of the debug GUI perspective changed.
   *
   * @param address The new active address.
   * @param focusMemoryWindow
   */
  void changedActiveAddress(IAddress address, boolean focusMemoryWindow);

  /**
   * Invoked after the active debugger of the debug GUI perspective changed.
   *
   * @param oldDebugger The previously active debugger.
   * @param newDebugger The new active debugger.
   */
  void changedActiveDebugger(IDebugger oldDebugger, IDebugger newDebugger);
}
