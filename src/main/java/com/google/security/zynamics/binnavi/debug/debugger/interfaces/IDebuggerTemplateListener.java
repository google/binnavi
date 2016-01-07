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

/**
 * Interface that must be implemented by all objects that want to be notified about changes in
 * debugger templates.
 */
public interface IDebuggerTemplateListener {
  /**
   * Signals that the host string of a debugger changed.
   *
   * @param debugger The debugger whose host string changed.
   */
  void changedHost(DebuggerTemplate debugger);

  /**
   * Signals that the name of a debugger changed.
   *
   * @param debugger The debugger whose name changed.
   */
  void changedName(DebuggerTemplate debugger);

  /**
   * Signals that the port of a debugger changed.
   *
   * @param debugger The debugger whose port changed.
   */
  void changedPort(DebuggerTemplate debugger);
}
