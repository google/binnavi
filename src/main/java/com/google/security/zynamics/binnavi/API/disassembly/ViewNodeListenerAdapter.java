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

// / Adapter class for view nodes
/**
 * Adapter class that can be used by objects that want to listen on view nodes but only need to
 * process few events.
 */
public class ViewNodeListenerAdapter implements IViewNodeListener {
  @Override
  public void addedTag(final ViewNode node, final Tag tag) {
    // Adapter method
  }

  @Override
  public void changedBorderColor(final ViewNode viewNode, final Color color) {
    // Adapter method
  }

  @Override
  public void changedColor(final ViewNode node, final Color color) {
    // Adapter method
  }

  @Override
  public void changedParentGroup(final ViewNode node, final GroupNode parentGroup) {
    // Adapter method
  }

  @Override
  public void changedSelection(final ViewNode node, final boolean selected) {
    // Adapter method
  }

  @Override
  public void changedVisibility(final ViewNode node, final boolean visible) {
    // Adapter method
  }

  @Override
  public void changedX(final ViewNode node, final double xpos) {
    // Adapter method
  }

  @Override
  public void changedY(final ViewNode node, final double ypos) {
    // Adapter method
  }

  @Override
  public void removedTag(final ViewNode node, final Tag tag) {
    // Adapter method
  }
}
