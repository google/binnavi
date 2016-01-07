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
 * Adapter class for graph edge listeners.
 */
public class ZyGraphEdgeSettingsListenerAdapter implements IZyGraphEdgeSettingsListener {
  @Override
  public void changedDisplayMultipleEdgesAsOne(final boolean value) {
    // Empty default implementation
  }

  @Override
  public void changedDrawSelectedBends(final boolean value) {
    // Empty default implementation
  }

  @Override
  public void changedEdgeHidingMode(final EdgeHidingMode value) {
    // Empty default implementation
  }

  @Override
  public void changedEdgeHidingThreshold(final int threshold) {
    // Empty default implementation
  }
}
