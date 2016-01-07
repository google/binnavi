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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.OutDegree;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Used to evaluate out-degree criteria matches.
 */
public final class COutdegreeEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private COutdegreeEvaluator() {
  }

  /**
   * Evaluates an out-degree criteria match on a node.
   *
   * @param node The node to check.
   * @param operator Defines the desired relation between the given out-degree and the node
   *        out-degree.
   * @param outdegree The out-degree to compare the node out-degree to.
   *
   * @return True, if the node out-degree matches the given out-degree. False, otherwise.
   */
  public static boolean matches(final NaviNode node, final String operator, final int outdegree) {
    if ("<".equals(operator)) {
      return node.getRawNode().getOutgoingEdges().size() < outdegree;
    } else if ("=".equals(operator)) {
      return node.getRawNode().getOutgoingEdges().size() == outdegree;
    } else if (">".equals(operator)) {
      return node.getRawNode().getOutgoingEdges().size() > outdegree;
    }

    throw new IllegalStateException("IE02203: Unknown operator " + operator);
  }
}
