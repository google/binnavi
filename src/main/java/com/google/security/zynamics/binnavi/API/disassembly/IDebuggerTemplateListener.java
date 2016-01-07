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
package com.google.security.zynamics.binnavi.API.disassembly;

// / Used to listen on debugger templates.
/**
 * Interface that can be implemented by objects that want to be notified about changes in
 * DebuggerTemplate objects.
 */
public interface IDebuggerTemplateListener {
  // ! Signals changing debugger host strings.
  /**
   * Invoked after the host string of a debugger template changed.
   *
   * @param template The debugger template whose host string changed.
   * @param host The new host string.
   */
  void changedHost(DebuggerTemplate template, String host);

  // ! Signals changing debugger names.
  /**
   * Invoked after the name of a debugger template changed.
   *
   * @param template The debugger template whose name changed.
   * @param name The new name of the debugger.
   */
  void changedName(DebuggerTemplate template, String name);

  // ! Signals changing debugger ports.
  /**
   * Invoked after the port of a debugger template changed.
   *
   * @param template The debugger template whose port changed.
   * @param port The new port of the debugger.
   */
  void changedPort(DebuggerTemplate template, int port);
}
