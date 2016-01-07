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

import com.google.security.zynamics.binnavi.Gui.Debug.Notifier.CDebugEventNotifier;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger;

/**
 * Interface for objects that provide the debuggers available in a graph window.
 */
public interface IFrontEndDebuggerProvider {
  /**
   * Returns the currently selected debugger.
   *
   * @return The currently selected debugger.
   */
  IDebugger getCurrentSelectedDebugger();

  /**
   * Returns the event notifier used for a given debugger.
   *
   * @param debugger The debugger whose event notifier is returned.
   *
   * @return The event notifier of the debugger.
   */
  CDebugEventNotifier getNotifier(IDebugger debugger);

  /**
   * Returns the trace logger used for a given debugger.
   *
   * @param debugger The debugger whose trace logger is returned.
   *
   * @return The trace logger of the debugger.
   */
  TraceLogger getTraceLogger(IDebugger debugger);
}
