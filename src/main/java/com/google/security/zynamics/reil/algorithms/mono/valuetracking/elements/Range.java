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
import java.util.HashSet;
import java.util.Set;

public class Range implements IValueElement {
  private final Literal m_lower;
  private final Literal m_upper;

  public Range(final Literal lower, final Literal upper) {
    m_lower = lower;
    m_upper = upper;
  }

  @Override
  public Range clone() {
    return new Range(m_lower, m_upper);
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Range) && ((Range) rhs).m_lower.equals(m_lower)
        && ((Range) rhs).m_upper.equals(m_upper);
  }

  @Override
  public BigInteger evaluate() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public IValueElement getSimplified() {
    // TODO Auto-generated method stub
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public Set<String> getVariables() {
    return new HashSet<String>();
  }

  @Override
  public int hashCode() {
    return 555555 * m_lower.hashCode() * m_upper.hashCode();
  }

  @Override
  public String toString() {
    return "[" + m_lower + " ... " + m_upper + "]";
  }
}
