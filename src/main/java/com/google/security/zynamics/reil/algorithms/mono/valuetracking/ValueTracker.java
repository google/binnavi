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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.algorithms.mono.IStateVector;
import com.google.security.zynamics.reil.algorithms.mono.InstructionGraph;
import com.google.security.zynamics.reil.algorithms.mono.InstructionGraphNode;
import com.google.security.zynamics.reil.algorithms.mono.StateVector;

public class ValueTracker {
  private static InstructionGraph createInitialGraph(final ReilFunction reilFunction) {
    return InstructionGraph.create(reilFunction.getGraph());
  }

  private static StateVector<InstructionGraphNode, ValueTrackerElement> createInitialStateVector(
      final InstructionGraph instructionGraph) {
    final StateVector<InstructionGraphNode, ValueTrackerElement> stateVector =
        new StateVector<InstructionGraphNode, ValueTrackerElement>();

    for (final InstructionGraphNode node : instructionGraph) {
      stateVector.setState(node, new ValueTrackerElement());
    }

    return stateVector;
  }

  public static IStateVector<InstructionGraphNode, ValueTrackerElement> track(
      final ReilFunction function) {
    Preconditions.checkNotNull(function, "Error: function argument can not be null");

    // Translate the given graph to an instruction graph
    final InstructionGraph instructionGraph = createInitialGraph(function);

    final StateVector<InstructionGraphNode, ValueTrackerElement> stateVector =
        createInitialStateVector(instructionGraph);

    final ValueTrackerSolver tracker = new ValueTrackerSolver(instructionGraph, stateVector);

    return tracker.solve();
  }
}
