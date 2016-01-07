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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Tag;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Used to evaluate tag criteria maches.
 */
public final class CTagEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTagEvaluator() {
  }

  /**
   * Evaluates a tag criterium match on a node.
   *
   * @param node The node to check.
   * @param any True, to match nodes tagged with any tag.
   * @param tag The tag to compare to the node tags. This argument can be null if any is true.
   *
   * @return True, if tags match. False, otherwise.
   */
  public static boolean evaluate(final NaviNode node, final boolean any, final CTag tag) {
    if (any) {
      return node.getRawNode().isTagged();
    }

    return node.getRawNode().isTagged(tag);
  }
}
