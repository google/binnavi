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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolver;
import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolverResult;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ITransformationProvider;
import com.google.security.zynamics.reil.yfileswrap.algorithms.mono2.common.instructiongraph.CReilInstructionGraph;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.ArrayList;
import java.util.List;

public final class RegisterTracker {
  private RegisterTracker() {
    // This class should not be instantiated.
  }

  /**
   * Function to do register tracking.
   * 
   * @param function The {@link ReilFunction} in which to do the register tracking.
   * @param startInstruction The {@link IInstruction} which is the start instruction.
   * @param trackedRegister The register to be tracked.
   * @param options The {@link RegisterTrackingOptions}.
   * 
   * @return The {@link MonoReilSolverResult} of the tracking.
   */
  public static MonoReilSolverResult<RegisterSetLatticeElement> track(final ReilFunction function,
      final IInstruction startInstruction, final String trackedRegister,
      final RegisterTrackingOptions options) {

    Preconditions.checkNotNull(function, "Error: function argument can not be null");
    Preconditions
        .checkNotNull(startInstruction, "Error: startInstruction argument can not be null");
    Preconditions.checkNotNull(trackedRegister, "Error: trackedRegister argument can not be null");
    Preconditions.checkNotNull(options, "Error: options argument can not be null");

    final CReilInstructionGraph instructionGraph = new CReilInstructionGraph(function.getGraph());
    final RegisterSetLatticeElement registerSetLatticeElement =
        new RegisterSetLatticeElement(trackedRegister);

    final MonoReilSolver<RegisterSetLatticeElement> monoReilSolver =
        new MonoReilSolver<RegisterSetLatticeElement>(instructionGraph,
            options.getAnalysisDirection(), new RegisterSetLattice());

    final Iterable<IInstructionGraphEdge> relevantEdges =
        options.trackIncoming() ? instructionGraph.getIncomingEdgesForAddress(startInstruction
            .getAddress()) : instructionGraph.getOutgoingEdgesForAddress(startInstruction
            .getAddress());
    final List<Pair<IInstructionGraphEdge, RegisterSetLatticeElement>> initialState =
        new ArrayList<Pair<IInstructionGraphEdge, RegisterSetLatticeElement>>();

    for (final IInstructionGraphEdge currentRelevantEdge : relevantEdges) {
      initialState.add(new Pair<IInstructionGraphEdge, RegisterSetLatticeElement>(
          currentRelevantEdge, registerSetLatticeElement));
    }

    final ITransformationProvider<RegisterSetLatticeElement> transformationProvider =
        new RegisterTrackingTransformationProvider(options);
    final MonoReilSolverResult<RegisterSetLatticeElement> solverResult =
        monoReilSolver.solve(transformationProvider, initialState, Integer.MAX_VALUE);

    return solverResult;
  }
}
