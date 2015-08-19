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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter.CCallsDescription;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter.CSpecialInstruction;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter.CSpecialInstructionsModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.InstructionHighlighter.ISpecialInstructionsModelListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;


@RunWith(JUnit4.class)
public class CSpecialInstructionsModelTest {
  @Test
  public void test1Simple() {
    final CSpecialInstructionsModel model = new CSpecialInstructionsModel();

    final List<CSpecialInstruction> instructions = new ArrayList<CSpecialInstruction>();

    final CCallsDescription callsDescription = new CCallsDescription();
    final CSpecialInstruction instruction =
        new CSpecialInstruction(callsDescription, new com.google.security.zynamics.binnavi.disassembly.MockInstruction());
    instructions.add(instruction);
    model.setInstructions(instructions);

    assertEquals(instruction, model.getInstruction(0));

    assertTrue((model.getDescriptions().get(0)) instanceof CCallsDescription);

    assertEquals("nop", model.getInstructions().get(0).getInstruction().getMnemonic());

    @SuppressWarnings("unused")
    final ISpecialInstructionsModelListener listener;
  }
}
