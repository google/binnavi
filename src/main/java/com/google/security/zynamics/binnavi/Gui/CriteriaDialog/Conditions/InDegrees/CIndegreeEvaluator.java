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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.InDegrees;

import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Used to evaluate indegree criteria maches.
 */
public final class CIndegreeEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CIndegreeEvaluator() {
  }

  /**
   * Evaluates an indegree criterium match on a node.
   *
   * @param node The node to check.
   * @param operator Defines the desired relation between the given indegree and the node indegree.
   * @param indegree The indegree to compare the node indegree to.
   *
   * @return True, if the node indegree matches the given indegree. False, otherwise.
   */
  public static boolean matches(final NaviNode node, final String operator, final int indegree) {
    if ("<".equals(operator)) {
      return node.getRawNode().getIncomingEdges().size() < indegree;
    }
    if ("=".equals(operator)) {
      return node.getRawNode().getIncomingEdges().size() == indegree;
    }
    if (">".equals(operator)) {
      return node.getRawNode().getIncomingEdges().size() > indegree;
    }

    throw new IllegalStateException("IE02202: Unknown operator " + operator);
  }
}
