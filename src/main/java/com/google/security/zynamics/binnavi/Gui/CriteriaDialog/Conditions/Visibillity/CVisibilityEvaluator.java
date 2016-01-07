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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Visibillity;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Used to evaluate visibility criteria maches.
 */
public final class CVisibilityEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CVisibilityEvaluator() {
  }

  /**
   * Evaluates a visibility criterium match on a node.
   *
   * @param node The node to check.
   * @param visibilityState The visibility state to compare to the visibility state of the node.
   *
   * @return True, if the visibility states match. False, otherwise.
   */
  public static boolean evaluate(final NaviNode node, final VisibilityState visibilityState) {
    return node.getRawNode().isVisible() == (visibilityState == VisibilityState.VISIBLE);
  }
}
