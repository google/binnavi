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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.ForwardTracker;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CInstructionResult;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CResultColor;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CTracking;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker.CTrackingResult;
import com.google.security.zynamics.binnavi.disassembly.CInstruction;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.reil.Architecture;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.reil.algorithms.mono2.registertracking.RegisterTrackingOptions;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IOperandTree;
import com.google.security.zynamics.zylib.disassembly.IReference;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public final class CResultColorTest {
  @Test
  public void testFirst() throws InternalTranslationException {
    final MockSqlProvider mockProvider = new MockSqlProvider();

    final MockModule mockModule = new MockModule(mockProvider);

    // str 6, , t1

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID, "6", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode1, childNode1);

    final COperandTreeNode rootNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SYMBOL_ID, ",", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode2, childNode2);

    final COperandTreeNode rootNode3 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode3 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t1", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode3, childNode3);

    final COperandTree operand1 =
        new COperandTree(rootNode1, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand3 =
        new COperandTree(rootNode3, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands1 = Lists.newArrayList(operand1, operand2, operand3);

    final INaviInstruction instruction1 = new CInstruction(false, mockModule, new CAddress(1),
        "str", operands1, new byte[0], Architecture.REIL, mockProvider);

    // str 5, , t0

    final COperandTreeNode rootNode4 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode4 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID, "5", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode4, childNode4);

    final COperandTreeNode rootNode5 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode5 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SYMBOL_ID, ",", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode5, childNode5);

    final COperandTreeNode rootNode6 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode6 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t0", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode6, childNode6);

    final COperandTree operand4 =
        new COperandTree(rootNode4, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand5 =
        new COperandTree(rootNode5, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand6 =
        new COperandTree(rootNode6, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands2 = Lists.newArrayList(operand4, operand5, operand6);

    final INaviInstruction instruction2 = new CInstruction(false, mockModule, new CAddress(2),
        "str", operands2, new byte[0], Architecture.REIL, mockProvider);

    // add t0, t1, t2

    final COperandTreeNode rootNode7 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode7 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t0", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode7, childNode7);

    final COperandTreeNode rootNode8 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode8 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t1", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode8, childNode8);

    final COperandTreeNode rootNode9 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode9 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t2", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode9, childNode9);

    final COperandTree operand7 =
        new COperandTree(rootNode7, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand8 =
        new COperandTree(rootNode8, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand9 =
        new COperandTree(rootNode9, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands3 = Lists.newArrayList(operand7, operand8, operand9);

    final INaviInstruction instruction3 = new CInstruction(false, mockModule, new CAddress(3),
        "add", operands3, new byte[0], Architecture.REIL, mockProvider);

    final INaviView view = mockModule.getContent().getViewContainer().createView("", "");

    final List<INaviInstruction> instructions =
        Lists.newArrayList(instruction1, instruction2, instruction3);

    view.getContent().createCodeNode(null, instructions);

    final RegisterTrackingOptions options =
        new RegisterTrackingOptions(true, new HashSet<String>(), false, AnalysisDirection.UP);
    final CTrackingResult results = CTracking.track(view, instruction3, "t2", options);

    for (final CInstructionResult result : results.getResults()) {
      if (result.getInstruction() == instruction1) {
        assertEquals(Color.RED, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      } else if (result.getInstruction() == instruction2) {
        assertEquals(Color.CYAN, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      } else if (result.getInstruction() == instruction3) {
        assertEquals(Color.GREEN, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      }
    }
  }

  @Test
  public void testSecond() throws InternalTranslationException {
    final MockSqlProvider mockProvider = new MockSqlProvider();

    final MockModule mockModule = new MockModule(mockProvider);

    // str 6, , t0

    final COperandTreeNode rootNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode1 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_IMMEDIATE_INT_ID, "6", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode1, childNode1);

    final COperandTreeNode rootNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode2 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SYMBOL_ID, ",", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode2, childNode2);

    final COperandTreeNode rootNode3 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode3 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t0", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode3, childNode3);

    final COperandTree operand1 =
        new COperandTree(rootNode1, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand2 =
        new COperandTree(rootNode2, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand3 =
        new COperandTree(rootNode3, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands1 = Lists.newArrayList(operand1, operand2, operand3);

    final INaviInstruction instruction1 = new CInstruction(false, mockModule, new CAddress(1),
        "str", operands1, new byte[0], Architecture.REIL, mockProvider);

    // str t0, , t1

    final COperandTreeNode rootNode4 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode4 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t0", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode4, childNode4);

    final COperandTreeNode rootNode5 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode5 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SYMBOL_ID, ",", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode5, childNode5);

    final COperandTreeNode rootNode6 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode6 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t1", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode6, childNode6);

    final COperandTree operand4 =
        new COperandTree(rootNode4, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand5 =
        new COperandTree(rootNode5, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand6 =
        new COperandTree(rootNode6, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands2 = Lists.newArrayList(operand4, operand5, operand6);

    final INaviInstruction instruction2 = new CInstruction(false, mockModule, new CAddress(2),
        "str", operands2, new byte[0], Architecture.REIL, mockProvider);

    // str t1, , t2

    final COperandTreeNode rootNode7 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode7 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t1", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode7, childNode7);

    final COperandTreeNode rootNode8 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode8 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SYMBOL_ID, ",", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode8, childNode8);

    final COperandTreeNode rootNode9 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_SIZE_PREFIX_ID, "b4", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    final COperandTreeNode childNode9 =
        new COperandTreeNode(-1, IOperandTree.NODE_TYPE_REGISTER_ID, "t2", null,
            new ArrayList<IReference>(), mockProvider, mockModule.getTypeManager(), mockModule
                .getContent().getTypeInstanceContainer());
    COperandTreeNode.link(rootNode9, childNode9);

    final COperandTree operand7 =
        new COperandTree(rootNode7, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand8 =
        new COperandTree(rootNode8, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final COperandTree operand9 =
        new COperandTree(rootNode9, mockProvider, mockModule.getTypeManager(), mockModule
            .getContent().getTypeInstanceContainer());
    final List<COperandTree> operands3 = Lists.newArrayList(operand7, operand8, operand9);

    final INaviInstruction instruction3 = new CInstruction(false, mockModule, new CAddress(3),
        "str", operands3, new byte[0], Architecture.REIL, mockProvider);

    final INaviView view = mockModule.getContent().getViewContainer().createView("", "");

    final List<INaviInstruction> instructions =
        Lists.newArrayList(instruction1, instruction2, instruction3);

    view.getContent().createCodeNode(null, instructions);

    final RegisterTrackingOptions options =
        new RegisterTrackingOptions(true, new HashSet<String>(), false, AnalysisDirection.UP);
    final CTrackingResult results = CTracking.track(view, instruction3, "t2", options);

    for (final CInstructionResult result : results.getResults()) {
      if (result.getInstruction() == instruction1) {
        assertEquals(Color.RED, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      } else if (result.getInstruction() == instruction2) {
        assertEquals(Color.CYAN, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      } else if (result.getInstruction() == instruction3) {
        assertEquals(Color.GREEN, CResultColor.determineBackgroundColor(instruction3, "t2", result));
      }
    }
  }
}
