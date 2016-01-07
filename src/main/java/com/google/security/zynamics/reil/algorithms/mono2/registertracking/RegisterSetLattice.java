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

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.algorithms.mono2.common.interfaces.ILattice;

public class RegisterSetLattice implements ILattice<RegisterSetLatticeElement> {
  private static RegisterSetLatticeElement m_emptyElement = new RegisterSetLatticeElement();

  @Override
  public RegisterSetLatticeElement combine(final List<RegisterSetLatticeElement> inputs) {
    Preconditions.checkNotNull(inputs, "Error: inputs argument can not be null");

    final RegisterSetLatticeElement latticeElement = new RegisterSetLatticeElement();
    latticeElement.addAll(inputs);
    return latticeElement;
  }

  @Override
  public RegisterSetLatticeElement getMinimalElement() {
    return m_emptyElement;
  }

  @Override
  public boolean isSmallerEqual(final RegisterSetLatticeElement firstElement,
      final RegisterSetLatticeElement secondElement) {
    Preconditions.checkNotNull(firstElement, "Error: firstElement argument can not be null");
    Preconditions.checkNotNull(secondElement, "Error: secondElement argument can not be null");

    return firstElement.isSmallerEqual(secondElement);
  }
}
