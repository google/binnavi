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
package com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel;

import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;

/**
 * Interface for objects that want to be notified about events during debug tracing.
 */
public interface IToolbarPanelSynchronizerListener {
  /**
   * Invoked after a trace list could not be saved to the database.
   *
   * @param list The list that could not be saved.
   */
  void errorSavingTrace(TraceList list);
}
