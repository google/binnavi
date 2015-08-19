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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Addition;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Symbol;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.AddTransformer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AddTransformerTest {
  @Test
  public void testAddConstants() {
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0x100, OperandSize.DWORD, "2", OperandSize.DWORD, "4",
            OperandSize.QWORD, "t0");

    final ValueTrackerElement state = new ValueTrackerElement();

    final ValueTrackerElement result = AddTransformer.transform(instruction, state);

    assertTrue(result.getState("t0") instanceof Literal);
    assertEquals(6, ((Literal) result.getState("t0")).getValue().longValue());
  }

  @Test
  public void testAddRegisterConstant() {
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0x100, OperandSize.DWORD, "t0", OperandSize.DWORD, "4",
            OperandSize.QWORD, "t1");

    final ValueTrackerElement state = new ValueTrackerElement();

    final ValueTrackerElement result = AddTransformer.transform(instruction, state);

    assertTrue(result.getState("t1") instanceof Addition);
    assertTrue(((Addition) result.getState("t1")).getLhs() instanceof Symbol);
    assertTrue(((Addition) result.getState("t1")).getRhs() instanceof Literal);
  }
}
