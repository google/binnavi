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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Set;
import java.util.TreeSet;

@RunWith(JUnit4.class)
public class BackwardRegisterTrackingTransformationProviderTest {

  public static RegisterSetLatticeElement createTaintedState(final String register) {
    final Set<String> taintedRegs = new TreeSet<String>();
    taintedRegs.add(register);
    final Set<String> newlyTaintedRegs = new TreeSet<String>();
    final Set<String> untaintedRegs = new TreeSet<String>();
    final Set<String> readRegs = new TreeSet<String>();
    final Set<String> updatedRegs = new TreeSet<String>();

    return new RegisterSetLatticeElement(taintedRegs, newlyTaintedRegs, untaintedRegs, readRegs,
        updatedRegs);
  }

  @Test
  public void testTransformAddEmptyState() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, new RegisterSetLatticeElement());

    Assert.assertNull(transformationResult.second());
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
  }

  @Test
  public void testTransformAddOutputIsTainted() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createAdd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    Assert.assertTrue(transformationResult.first().isTainted("ecx"));
    Assert.assertTrue(transformationResult.first().isTainted("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAnd() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));
    Assert.assertTrue(transformationResult.first().isTainted("ecx"));
    Assert.assertTrue(transformationResult.first().isTainted("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformAndZeroFirstArgument() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, String.valueOf(0), OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));

  }

  @Test
  public void testTransformAndZeroSecondArgument() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createAnd(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, String.valueOf(0),
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAnd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformBisz() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createBisz(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformBisz(instruction, createTaintedState("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
  }

  @Test
  public void testTransformBsh() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createBsh(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformBsh(instruction, createTaintedState("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
  }

  @Test
  public void testTransformDiv() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createDiv(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformDiv(instruction, createTaintedState("ecx"));
    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));

  }

  @Test
  public void testTransformJccFunctionCallClearAll() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(true,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx", "isCall",
            "true");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccFunctionCallClearSet() {
    final Set<String> cleared = new TreeSet<String>();
    cleared.add("ecx");
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false, cleared,
            false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx", "isCall",
            "true");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccNoFunctionCallClear() {
    final Set<String> cleared = new TreeSet<String>();
    cleared.add("ecx");
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false, cleared,
            false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformJccNoTaintconditionVariable() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createJcc(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformJcc(instruction, createTaintedState("ecx"));

    Assert.assertFalse(transformationResult.first().getTaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformLdm() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createLdm(0, OperandSize.DWORD, "eax", OperandSize.DWORD, "ecx");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformLdm(instruction, createTaintedState("ecx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMod() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createMod(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMod(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    Assert.assertTrue(transformationResult.first().isTainted("ecx"));
    Assert.assertTrue(transformationResult.first().isTainted("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());

  }

  @Test
  public void testTransformMul() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    Assert.assertTrue(transformationResult.first().isTainted("ecx"));
    Assert.assertTrue(transformationResult.first().isTainted("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformMulFirstZero() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, String.valueOf("0"),
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformMulSecondZero() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createMul(0, OperandSize.DWORD, String.valueOf("0"), OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformMul(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformNop() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction = ReilHelpers.createNop(0);
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformAdd(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
  }

  @Test
  public void testTransformOr() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformOrFirstAllBits() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD,
            String.valueOf(0xFFFFFFFFL), OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformOrSecondAllBits() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createOr(0, OperandSize.BYTE, String.valueOf(0xFFL), OperandSize.BYTE, "ecx",
            OperandSize.BYTE, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformOr(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformStm() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createStm(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformStm(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
  }

  @Test
  public void testTransformStr() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createStr(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformStr(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }

  @Test
  public void testTransformSub() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createSub(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformSub(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());
    Assert.assertFalse(transformationResult.first().isTainted("eax"));

    Assert.assertTrue(transformationResult.first().isTainted("ecx"));
    Assert.assertTrue(transformationResult.first().isTainted("ebx"));

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());

  }

  @Test
  public void testTransformSubIdenticalInput() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createSub(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformSub(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformUndef() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction = ReilHelpers.createUndef(0, OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformUndef(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformUnknown() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction = ReilHelpers.createUnknown(0);
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformUndef(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformXor() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createXor(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ebx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformXor(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getTaintedRegisters().contains("ebx"));
    Assert.assertTrue(transformationResult.first().getReadRegisters().contains("eax"));
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().contains("ecx"));
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
  }

  @Test
  public void testTransformXorSameOperands() {
    final RegisterTrackingTransformationProvider transformationProvider =
        new RegisterTrackingTransformationProvider(new RegisterTrackingOptions(false,
            new TreeSet<String>(), false, AnalysisDirection.UP));
    final ReilInstruction instruction =
        ReilHelpers.createXor(0, OperandSize.DWORD, "ecx", OperandSize.DWORD, "ecx",
            OperandSize.DWORD, "eax");
    final Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformationResult =
        transformationProvider.transformXor(instruction, createTaintedState("eax"));

    Assert.assertNull(transformationResult.second());

    transformationResult.first().onInstructionExit();

    Assert.assertTrue(transformationResult.first().getTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getReadRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getNewlyTaintedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUpdatedRegisters().isEmpty());
    Assert.assertTrue(transformationResult.first().getUntaintedRegisters().contains("eax"));
  }
}
