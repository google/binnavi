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
package com.google.security.zynamics.zylib.gui.zygraph.edges;

import java.awt.Color;

public interface IViewEdgeListener {
  default void addedBend(IViewEdge<?> edge, CBend path) {
  }

  default void changedColor(CViewEdge<?> edge, Color color) {
  }

  default void changedSelection(IViewEdge<?> edge, boolean selected) {
  }

  default void changedSourceX(CViewEdge<?> edge, double sourceX) {
  }

  default void changedSourceY(CViewEdge<?> edge, double sourceY) {
  }

  default void changedTargetX(CViewEdge<?> edge, double targetX) {
  }

  default void changedTargetY(CViewEdge<?> edge, double targetY) {
  }

  default void changedType(CViewEdge<?> edge, EdgeType type) {
  }

  default void changedVisibility(IViewEdge<?> edge, boolean visibility) {
  }

  default void clearedBends(IViewEdge<?> edge) {
  }

  default void insertedBend(IViewEdge<?> edge, int index, CBend path) {
  }

  default void removedBend(CViewEdge<?> edge, int index, CBend path) {
  }
}
