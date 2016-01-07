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
package com.google.security.zynamics.reil.algorithms.mono2.common;

import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILatticeElement;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ITransformationProvider;
import com.google.security.zynamics.zylib.general.Pair;


public abstract class PerInstructionTransformationProvider<LatticeElementType extends ILatticeElement<LatticeElementType>>
    implements ITransformationProvider<LatticeElementType> {

  protected abstract Pair<LatticeElementType, LatticeElementType> transformAdd(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformAnd(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformBisz(
      ReilInstruction ins, LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformBsh(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformDiv(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformJcc(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformLdm(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformMod(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformMul(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformNop(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformOr(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformStm(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformStr(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformSub(ReilInstruction ins,
      LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformUndef(
      ReilInstruction ins, LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformUnknown(
      ReilInstruction ins, LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformUnknownOpcode(
      ReilInstruction ins, LatticeElementType state);

  protected abstract Pair<LatticeElementType, LatticeElementType> transformXor(ReilInstruction ins,
      LatticeElementType state);

  @Override
  public Pair<LatticeElementType, LatticeElementType> transform(final IInstructionGraphNode n,
      final LatticeElementType state) {
    final ReilInstruction ins = n.getReilInstruction();
    final Integer mnemonic = ins.getMnemonicCode();

    switch (mnemonic) {
      case ReilHelpers._OPCODE_ADD:
        return transformAdd(ins, state);
      case ReilHelpers._OPCODE_AND:
        return transformAnd(ins, state);
      case ReilHelpers._OPCODE_BISZ:
        return transformBisz(ins, state);
      case ReilHelpers._OPCODE_BSH:
        return transformBsh(ins, state);
      case ReilHelpers._OPCODE_DIV:
        return transformDiv(ins, state);
      case ReilHelpers._OPCODE_JCC:
        return transformJcc(ins, state);
      case ReilHelpers._OPCODE_LDM:
        return transformLdm(ins, state);
      case ReilHelpers._OPCODE_MOD:
        return transformMod(ins, state);
      case ReilHelpers._OPCODE_MUL:
        return transformMod(ins, state);
      case ReilHelpers._OPCODE_NOP:
        return transformNop(ins, state);
      case ReilHelpers._OPCODE_OR:
        return transformOr(ins, state);
      case ReilHelpers._OPCODE_STM:
        return transformStm(ins, state);
      case ReilHelpers._OPCODE_STR:
        return transformStr(ins, state);
      case ReilHelpers._OPCODE_SUB:
        return transformSub(ins, state);
      case ReilHelpers._OPCODE_UNDEF:
        return transformUndef(ins, state);
      case ReilHelpers._OPCODE_UNKNOWN:
        return transformUnknown(ins, state);
      case ReilHelpers._OPCODE_XOR:
        return transformXor(ins, state);
      default:
        return transformUnknownOpcode(ins, state);
    }
  }
}
