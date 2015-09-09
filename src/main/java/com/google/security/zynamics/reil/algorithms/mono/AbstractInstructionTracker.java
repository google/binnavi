/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.reil.algorithms.mono;

import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1;

/**
 * Dispatcher class that can be used as a base class for real instruction trackers.
 * 
 */
public abstract class AbstractInstructionTracker<LatticeElement extends ILatticeElementMono1<LatticeElement>>
    implements ITransformationProvider<InstructionGraphNode, LatticeElement> {

  protected LatticeElement transformAdd(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformAnd(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected abstract LatticeElement transformBinary(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState);

  protected LatticeElement transformBisz(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformBinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformBsh(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformDiv(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected abstract LatticeElement transformJcc(ReilInstruction instruction,
      LatticeElement currentState, LatticeElement incomingState);

  protected LatticeElement transformLdm(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformBinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformMod(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformMul(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected abstract LatticeElement transformNop(ReilInstruction instruction,
      LatticeElement currentState, LatticeElement incomingState);

  protected LatticeElement transformOr(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected abstract LatticeElement transformStm(ReilInstruction instruction,
      LatticeElement currentState, LatticeElement incomingState);

  protected LatticeElement transformStr(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformBinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformSub(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected abstract LatticeElement transformTrinary(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState);

  protected abstract LatticeElement transformUndef(ReilInstruction instruction,
      LatticeElement currentState, LatticeElement incomingState);

  protected abstract LatticeElement transformUnknown(ReilInstruction instruction,
      LatticeElement currentState, LatticeElement incomingState);

  protected LatticeElement transformUnknownOpcode(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  protected LatticeElement transformXor(final ReilInstruction instruction,
      final LatticeElement currentState, final LatticeElement incomingState) {
    return transformTrinary(instruction, currentState, incomingState);
  }

  @Override
  public LatticeElement transform(final InstructionGraphNode node,
      final LatticeElement currentState, final LatticeElement incomingState) // NOPMD by sp on
                                                                             // 04.11.08 14:19
  {
    final ReilInstruction instruction = node.getInstruction();

    switch (instruction.getMnemonic()) {
        case ReilHelpers.OPCODE_ADD:
            return transformAdd(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_AND:
            return transformAnd(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_BISZ:
            return transformBisz(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_BSH:
            return transformBsh(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_DIV:
            return transformDiv(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_JCC:
            return transformJcc(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_LDM:
            return transformLdm(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_MOD:
            return transformMod(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_MUL:
            return transformMul(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_NOP:
            return transformNop(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_OR:
            return transformOr(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_STM:
            return transformStm(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_STR:
            return transformStr(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_SUB:
            return transformSub(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_UNDEF:
            return transformUndef(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_UNKNOWN:
            return transformUnknown(instruction, currentState, incomingState);
        case ReilHelpers.OPCODE_XOR:
            return transformXor(instruction, currentState, incomingState);
        default:
            return transformUnknownOpcode(instruction, currentState, incomingState);
    } 
  }
}
