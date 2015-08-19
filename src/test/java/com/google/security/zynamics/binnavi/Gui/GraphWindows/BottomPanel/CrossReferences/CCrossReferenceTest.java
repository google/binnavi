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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.CrossReferences.CCrossReference;
import com.google.security.zynamics.binnavi.disassembly.MockFunction;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class CCrossReferenceTest {
  @Test
  public void test1Simple() {
    final CCrossReference reference = new CCrossReference(new MockFunction(), new MockFunction());
    final CCrossReference reference2 = new CCrossReference(new MockFunction(), new MockFunction());

    assertFalse(reference.equals(reference2));
    assertTrue(reference.equals(reference));
  }

  @Test
  public void test2getCalledFunction() {
    final MockFunction function = new MockFunction();
    final CCrossReference reference = new CCrossReference(function, function);

    assertEquals(function, reference.getCalledFunction());
    assertEquals(function, reference.getCallingFunction());
  }
}
