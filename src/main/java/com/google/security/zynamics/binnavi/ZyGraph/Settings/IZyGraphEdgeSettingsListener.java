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

import com.google.security.zynamics.zylib.gui.zygraph.EdgeHidingMode;

/**
 * Interface to be implemented by classes that want to be notified about changes in graph edge
 * settings.
 */
public interface IZyGraphEdgeSettingsListener {
  /**
   * Invoked after the display multiple edges as one setting changed.
   *
   * @param value The new value of the display multiple edges as one setting.
   */
  void changedDisplayMultipleEdgesAsOne(boolean value);

  /**
   * Invoked after the draw selected bends setting changed.
   *
   * @param value The new value of the draw selected bends setting.
   */
  void changedDrawSelectedBends(boolean value);

  /**
   * Invoked after the edge hiding mode setting changed.
   *
   * @param value The new value of the edge hiding mode setting.
   */
  void changedEdgeHidingMode(EdgeHidingMode value);

  /**
   * Invoked after the edge hiding threshold setting changed.
   *
   * @param threshold The new value of the edge hiding threshold setting.
   */
  void changedEdgeHidingThreshold(int threshold);

}
