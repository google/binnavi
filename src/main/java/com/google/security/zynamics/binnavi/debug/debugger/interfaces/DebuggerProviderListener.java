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
package com.google.security.zynamics.binnavi.debug.debugger.interfaces;

import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;

/**
 * Interface for objects that want to be notified about changes in debugger providers.
 */
public interface DebuggerProviderListener {
  /**
   * Invoked after a new debugger was added to the provider.
   *
   * @param provider The provider where the debugger was added.
   * @param debugger The debugger added to the provider.
   */
  void debuggerAdded(BackEndDebuggerProvider provider, IDebugger debugger);

  /**
   * Invoked after a debugger was removed from a debugger provider.
   *
   * @param provider The provider the debugger was removed from.
   * @param debugger The debugger that was removed.
   */
  void debuggerRemoved(BackEndDebuggerProvider provider, IDebugger debugger);
}
