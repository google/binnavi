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
package com.google.security.zynamics.binnavi.Gui.GraphWindows;

/**
 * Interface for switching between the different perspectives available in the graph windows.
 */
public interface IViewSwitcher {
  /**
   * Switches to the debug perspective.
   */
  void activateDebugView();

  /**
   * Switches to the standard perspective.
   */
  void activateStandardView();
}
