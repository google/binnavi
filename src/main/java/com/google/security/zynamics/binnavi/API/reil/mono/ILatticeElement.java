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

// ! Interface for lattice elements.
/**
 * Interface that must be implemented by all lattice elements.
 *
 * @param <LatticeElement> Type of the lattice elements.
 */
public interface ILatticeElement<LatticeElement extends ILatticeElement<?>>
    extends com.google.security.zynamics.reil.algorithms.mono.interfaces.ILatticeElementMono1<LatticeElement> {
  // ! Compares two lattice elements.
  /**
   * Compares two lattice elements.
   *
   * @param rhs The right-hand-side lattice element of the comparison.
   *
   * @return True, if the two lattice elements are equal. False, otherwise.
   */
  @Override
  boolean equals(LatticeElement rhs);

  // ! Checks whether the element is less than the passed element.
  /**
   * Checks whether the element is less than the passed element.
   *
   *  Note that this function does not really have any effect on the calculation. It is only needed
   * for debugging purposes because it is used to make sure that state updates are monotonous. If
   * you do not want to have this additional error checking you can just return false from this
   * function (this is obviously not recommended).
   *
   * @param rhs The right-hand-side lattice element of the comparison.
   *
   * @return True, if the element is less than the given element. False, otherwise.
   */
  @Override
  boolean lessThan(LatticeElement rhs);
}
