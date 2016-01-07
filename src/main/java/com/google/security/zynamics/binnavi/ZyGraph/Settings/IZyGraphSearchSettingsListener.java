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

/**
 * Interface to be implemented by classes that want to be notified about changes in graph search
 * settings.
 */
public interface IZyGraphSearchSettingsListener {
  /**
   * Invoked after the case sensitive search setting changed.
   *
   * @param value The new value of the case sensitive search setting.
   */
  void changedSearchCaseSensitive(boolean value);

  /**
   * Invoked after the regex search setting changed.
   *
   * @param value The new value of the regex search setting.
   */
  void changedSearchRegEx(boolean value);

  /**
   * Invoked after the search selected nodes only setting changed.
   *
   * @param value The new value of the search selected nodes only setting.
   */
  void changedSearchSelectionNodesOnly(boolean value);

  /**
   * Invoked after the search visible nodes setting changed.
   *
   * @param value The new value of the search visible nodes setting.
   */
  void changedSearchVisibleNodesOnly(boolean value);

}
