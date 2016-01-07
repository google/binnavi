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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.transformers;

import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.algorithms.mono.WalkInformation;
import com.google.security.zynamics.reil.algorithms.mono.interfaces.IInfluencingState;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.ValueTrackerElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IAloc;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.IValueElement;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Literal;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Range;
import com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements.Undefined;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class StateCombiner {
  private static IValueElement combine(final IValueElement lhs, final IValueElement rhs) {
    if (lhs.equals(rhs)) {
      return lhs.clone();
    } else if ((lhs instanceof Literal) && (rhs instanceof Literal)) {
      final Literal llhs = (Literal) lhs;
      final Literal lrhs = (Literal) rhs;

      if (llhs.getValue().compareTo(lrhs.getValue()) == -1) {
        return new Range(llhs, lrhs);
      } else {
        return new Range(lrhs, llhs);
      }
    }
    // else if (lhs instanceof Addition && rhs instanceof Addition)
    // {
    // final Set<String> lhsVariables = lhs.getVariables();
    // final Set<String> rhsVariables = rhs.getVariables();
    //
    // if (lhsVariables.size() == 1 && lhsVariables.equals(rhsVariables))
    // {
    // final IValueElement simplifiedTreeLhs = lhs.getSimplified();
    // final IValueElement simplifiedTreeRhs = rhs.getSimplified();
    //
    // final BigInteger valueLhs = simplifiedTreeLhs.evaluate();
    // final BigInteger valueRhs = simplifiedTreeRhs.evaluate();
    //
    // final BigInteger lowerValue = valueLhs.compareTo(valueRhs) == -1 ? valueLhs : valueRhs;
    // final BigInteger higherValue = valueLhs == lowerValue ? valueRhs : valueLhs;
    //
    // return new Addition(new Symbol(new ArrayList<String>(lhsVariables).get(0)), new Range(new
    // Literal(lowerValue), new Literal(higherValue)));
    // }
    // else
    // {
    // return new Undefined();
    // // return new Either(lhs, rhs);
    // }
    // }
    else if ((lhs instanceof Undefined) || (rhs instanceof Undefined)) {
      return new Undefined();
    } else {
      return new Undefined();
      // return new Either(lhs, rhs);
    }
  }

  private static ValueTrackerElement combine(final ValueTrackerElement state1,
      final ValueTrackerElement state2) {
    final Map<IAloc, IValueElement> values1 = state1.getStates();
    final Map<IAloc, IValueElement> values2 = state2.getStates();

    final Map<IAloc, IValueElement> combinedState = new HashMap<IAloc, IValueElement>();
    final Set<ReilInstruction> combinedInfluences = state1.getInfluences();
    combinedInfluences.addAll(state2.getInfluences());

    final Map<String, Set<IAddress>> combinedWritten = new HashMap<String, Set<IAddress>>();
    combinedWritten.putAll(state1.getLastWritten());

    for (final Map.Entry<String, Set<IAddress>> lastWritten : state2.getLastWritten().entrySet()) {
      if (combinedWritten.containsKey(lastWritten.getKey())) {
        combinedWritten.get(lastWritten.getKey()).addAll(lastWritten.getValue());
      } else {
        combinedWritten.put(lastWritten.getKey(), new HashSet<IAddress>(lastWritten.getValue()));
      }
    }

    for (final Map.Entry<IAloc, IValueElement> entry : values1.entrySet()) {
      final IAloc aloc = entry.getKey();

      if (values2.containsKey(aloc)) {
        final IValueElement lhs = entry.getValue();
        final IValueElement rhs = values2.get(aloc);

        combinedState.put(aloc, combine(lhs, rhs));
      } else {
        combinedState.put(aloc, new Undefined());
      }
    }

    for (final Map.Entry<IAloc, IValueElement> entry : values2.entrySet()) {
      final IAloc aloc = entry.getKey();

      if (!values1.containsKey(aloc)) {
        combinedState.put(aloc, new Undefined());
      }
    }

    if ((combinedState.size() < state1.getStates().size())
        || (combinedState.size() < state2.getStates().size())) {
      throw new IllegalStateException();
    }

    return new ValueTrackerElement(combinedInfluences, combinedState, combinedWritten);
  }

  public static ValueTrackerElement combine(
      final List<IInfluencingState<ValueTrackerElement, WalkInformation>> states) {
    if (states.size() == 2) {
      final ValueTrackerElement state1 = states.get(0).getElement();
      final ValueTrackerElement state2 = states.get(1).getElement();

      if (state1.equals(state2)) {
        return state1.clone();
      } else {
        final ValueTrackerElement result = combine(state1, state2);

        if (result.getStates().size() < state1.getStates().size()) {
          System.out.println(state1);
          System.out.println(state2);
          System.out.println(result);
          throw new IllegalStateException();
        }

        if (result.getStates().size() < state2.getStates().size()) {
          System.out.println(state1);
          System.out.println(state2);
          System.out.println(result);
          throw new IllegalStateException();
        }

        return result;
      }
    } else {
      // TODO Auto-generated method stub
      throw new IllegalStateException("Not yet implemented");
    }
  }
}
