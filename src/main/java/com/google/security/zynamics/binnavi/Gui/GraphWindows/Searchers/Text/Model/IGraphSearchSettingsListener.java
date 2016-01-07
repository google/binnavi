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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model;

/**
 * Interface to be implemented by classes that want to be notified about changes in graph settings.
 */
public interface IGraphSearchSettingsListener {
  /**
   * Invoked after the setting for case sensitive search changed.
   */
  void changedCaseSensitive();

  /**
   * Invoked after the setting for selected nodes search changed.
   */
  void changedOnlySelected();

  /**
   * Invoked after the setting for visible nodes search changed.
   */
  void changedOnlyVisible();

  /**
   * Invoked after the setting for regular expression search changed.
   */
  void changedRegEx();
}
