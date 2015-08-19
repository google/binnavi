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
package com.google.security.zynamics.binnavi.Gui.CriteriaDialog.Conditions.Text;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.ZyGraph.CNaviNodeFactory;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CTextEvaluatorTest {
  @Test
  public void testComplete() {
    final NaviNode node = CNaviNodeFactory.get();

    assertFalse(CTextEvaluator.evaluate(node, "britzel", false, true));
    assertTrue(CTextEvaluator.evaluate(node, "Foo", false, true));
    assertFalse(CTextEvaluator.evaluate(node, "foo", false, true));
    assertTrue(CTextEvaluator.evaluate(node, "foo", false, false));

    assertTrue(CTextEvaluator.evaluate(node, "F*", true, true));
    assertFalse(CTextEvaluator.evaluate(node, "f*", true, true));

  }
}
