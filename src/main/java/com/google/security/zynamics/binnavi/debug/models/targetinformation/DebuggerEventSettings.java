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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

/**
 * Represents the settings which can be set in the debugger events panel in the CDebuggerOptions
 * dialog.
 */
public class DebuggerEventSettings {
  private final boolean breakOnDllLoad;
  private final boolean breakOnDllUnload;

  /**
   * Creates a new instance of the debugger event settings class.
   *
   * @param breakOnDllLoad Specifies whether the debugger should break whenever a dll is loaded into
   *        the debuggee.
   * @param breakOnDllUnload Specifies whether the debugger should break whenever a dll is unloaded
   *        from the debuggee.
   */
  public DebuggerEventSettings(final boolean breakOnDllLoad, final boolean breakOnDllUnload) {
    this.breakOnDllLoad = breakOnDllLoad;
    this.breakOnDllUnload = breakOnDllUnload;
  }

  public boolean getBreakOnDllLoad() {
    return breakOnDllLoad;
  }

  public boolean getBreakOnDllUnload() {
    return breakOnDllUnload;
  }
}
