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

import java.util.List;

public interface ILattice<LatticeElementType extends ILatticeElement<LatticeElementType>> {
  /**
   * Combines a List of inputs into a single entity.
   * 
   * @param inputs The list of inputs.
   * 
   * @return The combined entity.
   */
  LatticeElementType combine(List<LatticeElementType> inputs);

  /**
   * Returns the specified minimal element of the defined lattice.
   * 
   * @return The minimal element.
   */
  LatticeElementType getMinimalElement();

  /**
   * Returns whether x <= y.
   * 
   * @param x
   * @param y
   * 
   * @return True if x <= y.
   */
  boolean isSmallerEqual(LatticeElementType x, LatticeElementType y);
}
