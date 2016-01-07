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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.AbstractInstructionTracker;
import com.google.security.zynamics.reil.algorithms.mono.WalkInformation;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.ILattice;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.AddTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.AndTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.BiszTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.BshTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.JccTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.LdmTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.OrTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.StateCombiner;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.StmTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.StrTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.SubTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.UndefTransformer;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers.XorTransformer;

import java.util.List;

public class ValueTrackerLattice extends AbstractInstructionTracker<ValueTrackerElement> implements
    ILattice<ValueTrackerElement, WalkInformation> {
  @Override
  protected ValueTrackerElement transformAdd(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return AddTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformAnd(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return AndTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformBinary(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  protected ValueTrackerElement transformBisz(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    return BiszTransformer.transform(instruction, combinedState);
  }

  @Override
  protected ValueTrackerElement transformBsh(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return BshTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformJcc(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    return JccTransformer.transform(combinedState);
  }

  @Override
  protected ValueTrackerElement transformLdm(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return LdmTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformNop(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  protected ValueTrackerElement transformOr(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return OrTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformStm(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    return StmTransformer.transform(instruction, combinedState);
  }

  @Override
  protected ValueTrackerElement transformStr(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return StrTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformSub(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return SubTransformer.transform(instruction, state);
  }

  @Override
  protected ValueTrackerElement transformTrinary(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    throw new IllegalStateException();
  }

  @Override
  protected ValueTrackerElement transformUndef(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    return UndefTransformer.transform(instruction, combinedState);
  }

  @Override
  protected ValueTrackerElement transformUnknown(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement combinedState) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  protected ValueTrackerElement transformXor(final ReilInstruction instruction,
      final ValueTrackerElement currentState, final ValueTrackerElement state) {
    return XorTransformer.transform(instruction, state);
  }

  @Override
  public ValueTrackerElement combine(
      final List<IInfluencingState<ValueTrackerElement, WalkInformation>> states) {
    switch (states.size()) {
        case 0:
            return new ValueTrackerElement();
        case 1:
            return states.get(0).getElement();
        default:
            return StateCombiner.combine(states);
    }
  }
}
