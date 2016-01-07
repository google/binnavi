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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.NodeColor;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import java.awt.Color;


/**
 * Used to evaluate color criteria maches.
 */
public final class CColorEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CColorEvaluator() {
  }

  /**
   * Evaluates a color criterium match on a node.
   *
   * @param node The node to check.
   * @param color The color to compare the node color to.
   *
   * @return True, if the node color matches the given color. False, otherwise.
   */
  public static boolean evaluate(final NaviNode node, final Color color) {
    return node.getRawNode().getColor().equals(color);
  }
}
