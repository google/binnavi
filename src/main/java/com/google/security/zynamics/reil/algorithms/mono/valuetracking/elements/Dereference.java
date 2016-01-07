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
package com.google.security.zynamics.reil.algorithms.mono.valuetracking.elements;

import java.math.BigInteger;
import java.util.Set;

public class Dereference implements IValueElement {
  private final IValueElement m_value;

  public Dereference(final IValueElement value) {
    m_value = value;
  }

  @Override
  public Dereference clone() {
    return new Dereference(m_value.clone());
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Dereference) && ((Dereference) rhs).m_value.equals(m_value);
  }

  @Override
  public BigInteger evaluate() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IValueElement getSimplified() {
    return this;
  }

  @Override
  public Set<String> getVariables() {
    return m_value.getVariables();
  }

  @Override
  public int hashCode() {
    return 77777 * m_value.hashCode();
  }

  @Override
  public String toString() {
    return "@" + m_value;
  }

}
