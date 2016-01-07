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
package com.google.security.zynamics.binnavi.ZyGraph.Settings;

import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;

/**
 * Interface to be implemented by classes that want to be notified about changes in graph display
 * settings.
 */
public interface IZyGraphDisplaySettingsListener {
  /**
   * Invoked after the switch for showing function node information was toggled.
   *
   * @param show True, to show function node information. False, to hide it.
   */
  void changedFunctionNodeInformation(boolean show);

  /**
   * Invoked after the gradient background setting changed.
   *
   * @param value The new value of the gradient background setting.
   */
  void changedGradientBackground(boolean value);

  /**
   * Invoked after the show memory addresses setting changed.
   *
   * @param debugger The debugger for which the setting changed.
   * @param selected The new value of the show memory addresses setting.
   */
  void changedShowMemoryAddresses(IDebugger debugger, boolean selected);

  /**
   * Invoked after the simplified variable access mode was toggled.
   *
   * @param value The new value of the simplified variable access mode.
   */
  void changedSimplifiedVariableAccess(boolean value);
}
