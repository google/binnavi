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

import com.google.common.base.Preconditions;

public class NullCheck implements IValueElement {
  private final IValueElement m_value;

  public NullCheck(final IValueElement element) {
    Preconditions.checkArgument(!(element instanceof Undefined),
        "Error: Element of a NullCheck can not be undefined");

    m_value = element.clone();
  }

  @Override
  public NullCheck clone() {
    return new NullCheck(m_value);
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof NullCheck) && ((NullCheck) rhs).m_value.equals(m_value);
  }

  @Override
  public BigInteger evaluate() {
    return m_value.evaluate().equals(BigInteger.ZERO) ? BigInteger.ONE : BigInteger.ZERO;
  }

  @Override
  public IValueElement getSimplified() {
    if (m_value instanceof Literal) {
      return new Literal(m_value.evaluate().equals(BigInteger.ZERO) ? BigInteger.ONE
          : BigInteger.ZERO);
    } else {
      return this;
    }
  }

  @Override
  public Set<String> getVariables() {
    return m_value.getVariables();
  }

  @Override
  public int hashCode() {
    return 5555 * m_value.hashCode();
  }

  @Override
  public String toString() {
    return "(" + m_value + " != 0)";
  }
}
