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
package com.google.security.zynamics.binnavi.API.reil.mono;

import java.util.List;

// ! Interface for MonoREIL lattices.
/**
 * Interface that must be implemented by all classes that are used as lattices in the context of
 * MonoREIL.
 *
 * @param <LatticeElement> Type of the elements in the lattice.
 * @param <ObjectType> Type of the additional object of a state.
 */
public interface ILattice<LatticeElement extends ILatticeElement<LatticeElement>, ObjectType> {
  // ! Combines states.
  /**
   * Combines a number of states to a single lattice state.
   *
   * @param states The states to combine.
   *
   * @return The combined state.
   */
  LatticeElement combine(List<IInfluencingState<LatticeElement, ObjectType>> states);
}
