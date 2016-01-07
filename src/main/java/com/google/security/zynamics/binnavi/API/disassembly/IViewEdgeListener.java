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

import java.awt.Color;

// / Used to listen on view edges.
/**
 * Listener interface that must be implemented by all objects that want to be notified about changes
 * in a view edge.
 */
public interface IViewEdgeListener {
  // ! Signals the color of the view edge changed.
  /**
   * Invoked after the color of the view edge changed.
   *
   * @param edge The edge whose color changed.
   * @param color The new color of the edge.
   */
  void changedColor(ViewEdge edge, Color color);

  // ! Signals the visibility of the view edge changed.
  /**
   * Invoked after the visibility of the view edge changed.
   *
   * @param edge The edge whose visibility changed.
   * @param visibility The new visibility of the edge.
   */
  void changedVisibility(ViewEdge edge, boolean visibility);
}
