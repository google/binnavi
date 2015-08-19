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
package com.google.security.zynamics.reil.algorithms.mono2.registertracking;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.zylib.general.Pair;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;
import java.util.TreeSet;

@RunWith(JUnit4.class)
public class ForwardRegisterTrackingTransformationProviderTest {
  public static RegisterSetLatticeElement createTaintedState(final String... registers) {
    final Set<String> taintedRegs = new TreeSet<String>();
    for (final String register : registers) {
      taintedRegs.add(register);
    }
    final Set<String> newlyTaintedRegs = new TreeSet<String>();
    final Set<String> untaintedRegs = new TreeSet<String>();
    final Set<String> readRegs = new TreeSet<String>();
    final Set<String> updatedRegs = new TreeSet<String>();

    return new RegisterSetLatticeElement(taintedRegs, newlyTaintedRegs, untaintedRegs, readRegs,
        updatedRegs);
  }

  @Before
  public void setUp() {
  }

  @Test
  public void testTransformAddBothInputRegisterAreTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("ebx", "ecx"));

    Assert.assertNull(transformationResult.second());
    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAddEmptyState() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, new RegisterSetLatticeElement());

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAddFirstInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAddSecondInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("ebx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndBothInputRegisterAreTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx", "ebx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndFirstInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndSecondInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndZeroFirstArgument() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, String.valueOf(0), OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndZeroSecondArgument() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, String.valueOf(0),
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndZeroSecondArgumentTeintedRegisterIsThirdArgument() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "eax", OperandSize.DWORD, String.valueOf(0),
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformBisz() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createBisz(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformBisz(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformBshFirstOperandTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createBsh(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformBsh(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformBshSecondOperandTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createBsh(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformBsh(instruction, createTaintedState("ebx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformDivFirstOperandTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createDiv(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformDiv(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformDivSecondOperandTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createDiv(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformDiv(instruction, createTaintedState("ebx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccFunctionCallClearAllIsCallFalse() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(true,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx", "isCall",
            "false");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("eax"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccFunctionCallClearAllIsCallTrue() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(true,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx", "isCall",
            "true");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("eax"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccFunctionCallClearSet() {
    final Set<String> cleared = new TreeSet<String>();
    cleared.add("ecx");
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false, cleared,
            false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx", "isCall",
            "true");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccNoFunctionCallClear() {
    final Set<String> cleared = new TreeSet<String>();
    cleared.add("ecx");
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false, cleared,
            false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccNoTaintconditionVariable() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformLdm() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createLdm(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformLdm(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformLdmFirstOperandTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createLdm(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformLdm(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformModBothInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMod(0, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMod(instruction, createTaintedState("ecx", "ebx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformModFirstInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMod(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMod(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformModSecondInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMod(0, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMod(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulFirstInputIsZero() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, String.valueOf("0"), OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulFirstInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulSecondInputIsZero() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, String.valueOf("0"),
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulSecondInputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulThirdOutputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "0", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformNop() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction = ReilHelpers.createNop(0);
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformOrFirstAllBits() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.BYTE, String.valueOf(0xFFL), OperandSize.BYTE, "ecx",
            OperandSize.BYTE, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformOrFirstinputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformOrSecondAllBits() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD,
            String.valueOf(0xFFFFFFFFL), OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformOrSecondinputRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.DWORD, "ebx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformStm() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createStm(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformStm(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformStr() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createStr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformStr(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformSubFirstInPutRegisterIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createSub(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformSub(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformSubIdenticalInput() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createSub(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformSub(instruction, createTaintedState("ecx", "eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformUndef() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction = ReilHelpers.createUndef(0, OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformUndef(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformUnknown() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction = ReilHelpers.createUnknown(0);
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformUndef(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformXorFirstInputOperandIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createXor(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformXor(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformXorSameOperands() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createXor(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformXor(instruction, createTaintedState("ecx", "eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformXorSecondInputOperandIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.DOWN));
    final ReilInstruction instruction =
        ReilHelpers.createXor(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformXor(instruction, createTaintedState("ecx"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }
}
