/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.ZyGraph.CNaviNodeFactory;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class COutDegreeEvaluatorTest {
  @Test
  public void testComplete() {
    final NaviNode node = CNaviNodeFactory.get();

    assertTrue(COutdegreeEvaluator.matches(node, "=", 0));
    assertFalse(COutdegreeEvaluator.matches(node, "<", 0));
    assertFalse(COutdegreeEvaluator.matches(node, ">", 0));
    assertFalse(COutdegreeEvaluator.matches(node, "=", 1));

  }

  @Test
  public void testInvalidOperator() {
    final NaviNode node = CNaviNodeFactory.get();

    try {
      COutdegreeEvaluator.matches(node, "!=", 1);
      fail();
    } catch (final IllegalStateException e) {
    }

    try {
      COutdegreeEvaluator.matches(node, "==", 1);
      fail();
    } catch (final IllegalStateException e) {
    }
  }
}
