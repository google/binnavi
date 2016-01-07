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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

/**
 * Interface that must be implemented by objects that want to be notified about changes in register
 * tracking result containers.
 */
public interface ITrackingResultsListener {
  /**
   * Invoked after the current register tracking results changed.
   *
   * @param container The container that contains the register tracking result.
   * @param result The new register tracking result.
   */
  void updatedResult(CTrackingResultContainer container, CTrackingResult result);
}
