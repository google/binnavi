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

import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager;

/**
 * This interface must be implemented by all objects that want to be notified about changes in the
 * debugger templates that are managed by a debugger template manager.
 */
public interface IDebuggerTemplateManagerListener {
  /**
   * Signals that a new debugger template was added to the debugger template manager.
   *
   * @param manager The manager to which the debugger template was added.
   * @param debugger The debugger template that was added.
   */
  void addedDebugger(DebuggerTemplateManager manager, DebuggerTemplate debugger);

  /**
   * Signals that an existing debugger template was removed from the debugger template manager.
   *
   * @param manager The manager from which the debugger template was removed.
   * @param debugger The debugger template that was removed.
   */
  void removedDebugger(DebuggerTemplateManager manager, DebuggerTemplate debugger);
}
