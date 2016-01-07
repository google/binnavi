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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.DebuggerProviderListener;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;

import java.util.List;

/**
 * Interface for debugger providers.
 */
public interface BackEndDebuggerProvider extends Iterable<IDebugger> {
  /**
   * Adds a listener object that is notified about changes in the debugger.
   *
   * @param listener The listener object to add.
   */
  void addListener(DebuggerProviderListener listener);

  /**
   * Returns the debugger used to debug a given module.
   *
   * @param module The module to check for.
   *
   * @return The debugger used to debug the module.
   */
  IDebugger getDebugger(INaviModule module);

  /**
   * Returns all debuggers provided by the debugger provider.
   *
   * @return All debuggers provided by the debugger provider.
   */
  List<IDebugger> getDebuggers();

  /**
   * Returns the debugged target.
   *
   * @return The debugged target.
   */
  DebugTargetSettings getDebugTarget();

  /**
   * Removes a listener object from the debugger provider.
   *
   * @param listener The listener to remove.
   */
  void removeListener(DebuggerProviderListener listener);
}
