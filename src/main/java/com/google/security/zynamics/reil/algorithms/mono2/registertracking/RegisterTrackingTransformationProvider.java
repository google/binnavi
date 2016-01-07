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
package com.google.security.zynamics.reil.algorithms.mono2.registertracking;

import java.util.Set;
import java.util.TreeSet;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.OperandType;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.ReilOperand;
import com.google.security.zynamics.reil.algorithms.mono2.common.PerInstructionTransformationProvider;
import com.google.security.zynamics.reil.algorithms.mono2.common.enums.AnalysisDirection;
import com.google.security.zynamics.zylib.general.Pair;


public class RegisterTrackingTransformationProvider extends
    PerInstructionTransformationProvider<RegisterSetLatticeElement> {
  private final RegisterTrackingOptions m_trackingOptions;

  public RegisterTrackingTransformationProvider(final RegisterTrackingOptions options) {
    m_trackingOptions = options;
  }

  private String getMask(final OperandSize operandSize) {
    switch (operandSize) {
      case BYTE:
        return String.valueOf(0xFFL);
      case DWORD:
        return String.valueOf(0xFFFFFFFFL);
      case QWORD:
        return String.valueOf(0xFFFFFFFFFFFFFFFFL);
      case WORD:
        return String.valueOf(0xFFFFL);
      default:
        throw new IllegalStateException("Error: Unknown target size for truncate mask");
    }
  }

  private Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformNormalInstruction(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    if (m_trackingOptions.getAnalysisDirection() == AnalysisDirection.UP) {
      return transformNormalInstructionBackward(ins, state);
    } else {
      return transformNormalInstructionForward(ins, state);
    }
  }

  private Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformNormalInstructionBackward(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final ReilOperand in1 = ins.getFirstOperand();
    final ReilOperand in2 = ins.getSecondOperand();
    final ReilOperand out = ins.getThirdOperand();

    final Set<String> inputRegisters = new TreeSet<String>();

    if (in1.getType() == OperandType.REGISTER) {
      inputRegisters.add(in1.getValue());
    }
    if (in2.getType() == OperandType.REGISTER) {
      inputRegisters.add(in2.getValue());
    }

    final RegisterSetLatticeElement outputstate = state.copy();

    if (state.isTainted(out.getValue())) {
      if (inputRegisters.isEmpty()) {
        outputstate.untaint(out.getValue());
      } else {
        outputstate.untaint(out.getValue());
        outputstate.addReadReg(out.getValue());
        for (final String register : inputRegisters) {
          outputstate.taint(register);
        }
      }
    }

    // JCC is treated separately, so it is safe to return "null" for the false
    // edge of a conditional branch.
    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(outputstate, null);
  }

  private Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformNormalInstructionForward(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final ReilOperand in1 = ins.getFirstOperand();
    final ReilOperand in2 = ins.getSecondOperand();
    final ReilOperand out = ins.getThirdOperand();

    final Set<String> inputRegisters = new TreeSet<String>();

    if (in1.getType() == OperandType.REGISTER) {
      inputRegisters.add(in1.getValue());
    }
    if (in2.getType() == OperandType.REGISTER) {
      inputRegisters.add(in2.getValue());
    }

    // If the intersection of inputRegisters and state is empty, untaint the
    // output register. Else, taint the output register.

    final RegisterSetLatticeElement outputstate = state.copy();

    if (!state.isTainted(inputRegisters)) {
      outputstate.untaint(out.getValue());
    } else {
      for (final String register : inputRegisters) {
        if (state.isTainted(register)) {
          outputstate.addReadReg(register);
        }
      }

      outputstate.taint(out.getValue());
    }

    // JCC is treated separately, so it is safe to return "null" for the false
    // edge of a conditional branch.
    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(outputstate, null);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformAdd(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformAnd(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    if ((ins.getFirstOperand().getType() == OperandType.INTEGER_LITERAL)
        && ins.getFirstOperand().getValue().equalsIgnoreCase("0")) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    } else if ((ins.getSecondOperand().getType() == OperandType.INTEGER_LITERAL)
        && ins.getSecondOperand().getValue().equalsIgnoreCase("0")) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    }

    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformBisz(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformBsh(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformDiv(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformJcc(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final RegisterSetLatticeElement newstate = state.copy();

    if (ReilHelpers.isFunctionCall(ins)) {
      if (m_trackingOptions.clearsAllRegisters()) {
        newstate.untaintAll(state.getTaintedRegisters());
      } else {
        newstate.untaintAll(m_trackingOptions.getClearedRegisters());
      }
    }

    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newstate, newstate);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformLdm(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final RegisterSetLatticeElement newstate = state.copy();
    newstate.untaint(ins.getThirdOperand().getValue());
    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newstate, null);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformMod(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformMul(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    if ((ins.getFirstOperand().getType() == OperandType.INTEGER_LITERAL)
        && ins.getFirstOperand().getValue().equalsIgnoreCase("0")) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    } else if ((ins.getSecondOperand().getType() == OperandType.INTEGER_LITERAL)
        && ins.getSecondOperand().getValue().equalsIgnoreCase("0")) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    }

    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformNop(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformOr(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final OperandType operandOneType = ins.getFirstOperand().getType();
    final OperandType operandTwoType = ins.getSecondOperand().getType();
    final OperandSize operandOneSize = ins.getFirstOperand().getSize();
    final OperandSize operandTwoSize = ins.getSecondOperand().getSize();
    final OperandSize operandThreeSize = ins.getThirdOperand().getSize();
    final String operandOneValue = ins.getFirstOperand().getValue();
    final String operandTwoValue = ins.getSecondOperand().getValue();

    final String mask = getMask(operandThreeSize);

    if ((operandOneType == OperandType.INTEGER_LITERAL) && mask.equalsIgnoreCase(operandOneValue)
        && operandThreeSize.equals(operandTwoSize) && operandThreeSize.equals(operandOneSize)) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    } else if ((operandTwoType == OperandType.INTEGER_LITERAL)
        && mask.equalsIgnoreCase(operandTwoValue) && operandThreeSize.equals(operandTwoSize)
        && operandThreeSize.equals(operandOneSize)) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    }

    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformStm(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {

    final ReilOperand op1 = ins.getFirstOperand();
    if (op1.getType().equals(OperandType.REGISTER)) {
      if (state.isTainted(op1.getValue())) {
        final RegisterSetLatticeElement newState = state.copy();
        newState.addReadReg(op1.getValue());
        return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
      }
    }

    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(state, null);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformStr(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformSub(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    if (ins.getFirstOperand().getValue().equalsIgnoreCase(ins.getSecondOperand().getValue())) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    }

    return transformNormalInstruction(ins, state);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformUndef(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    final RegisterSetLatticeElement newstate = state.copy();
    newstate.untaint(ins.getThirdOperand().getValue());
    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newstate, null);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformUnknown(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(state, null);
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformUnknownOpcode(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    throw new IllegalArgumentException("Error: Unkown opcode when trying to calculate a transform");
  }

  @Override
  protected Pair<RegisterSetLatticeElement, RegisterSetLatticeElement> transformXor(
      final ReilInstruction ins, final RegisterSetLatticeElement state) {
    if (ins.getFirstOperand().getValue().equalsIgnoreCase(ins.getSecondOperand().getValue())) {
      final RegisterSetLatticeElement newState = state.copy();
      newState.untaint(ins.getThirdOperand().getValue());
      return new Pair<RegisterSetLatticeElement, RegisterSetLatticeElement>(newState, null);
    }
    return transformNormalInstruction(ins, state);
  }

}
