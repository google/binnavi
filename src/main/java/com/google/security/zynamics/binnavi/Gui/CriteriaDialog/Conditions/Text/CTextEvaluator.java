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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Model.CNodeSearcher;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

/**
 * Used to evaluate text criteria maches.
 */
public final class CTextEvaluator {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTextEvaluator() {
  }

  /**
   * Evaluates a text criterium match on a node.
   *
   * @param node The node to check.
   * @param text The text to compare to the node text.
   * @param regularExpression True, to signal regular expression search.
   * @param caseSensitive True, to signal case sensitive search.
   *
   * @return True, if the string was found in the node text. False, otherwise.
   */
  public static boolean evaluate(final NaviNode node, final String text,
      final boolean regularExpression, final boolean caseSensitive) {
    return CNodeSearcher.search(node, text, regularExpression, caseSensitive).size() != 0;
  }
}
