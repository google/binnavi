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
package com.google.security.zynamics.reil.algorithms.mono2.common.interfaces;

import com.google.security.zynamics.reil.algorithms.mono2.common.MonoReilSolverResult;
import com.google.security.zynamics.reil.algorithms.mono2.common.instructiongraph.interfaces.IInstructionGraphEdge;
import com.google.security.zynamics.zylib.general.Pair;


/**
 * Interface for the mono REIL abstract interpretation solver.
 * 
 * @author (timkornau@google.com)
 * 
 * @param <LatticeElementType>
 */
public interface IMonoReilSolver<LatticeElementType extends ILatticeElement<LatticeElementType>> {

  /**
   * The solve function interface.
   * 
   * @param transformationProvider The parameterized {@link ITransformationProvider} which
   *        encapsulates the logic on how to handle a transform from one state into the next.
   * @param initialStates The initial state for the solver. This is used to seed the analysis.
   * @param maximumIteration The maximum number of iterations that can be reached by this analysis,
   *        once this number is reached the analysis terminates without a result.
   * @return A {@link MonoReilSolverResult} which holds the collected information of the analysis.
   */
  public MonoReilSolverResult<LatticeElementType> solve(
      ITransformationProvider<LatticeElementType> transformationProvider,
      Iterable<Pair<IInstructionGraphEdge, LatticeElementType>> initialStates, int maximumIteration);
}
