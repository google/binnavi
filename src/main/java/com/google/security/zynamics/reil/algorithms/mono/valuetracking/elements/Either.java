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

public class Either implements IValueElement {
  private final IValueElement m_lhs;

  private final IValueElement m_rhs;

  public Either(final IValueElement lhs, final IValueElement rhs) {
    m_lhs = lhs;
    m_rhs = rhs;
  }

  @Override
  public Either clone() {
    return new Either(m_lhs.clone(), m_rhs.clone());
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Either) && ((Either) rhs).m_lhs.equals(m_lhs)
        && ((Either) rhs).m_rhs.equals(m_rhs);
  }

  @Override
  public BigInteger evaluate() {
    throw new IllegalStateException();
  }

  public IValueElement getLhs() {
    return m_lhs;
  }

  public IValueElement getRhs() {
    return m_rhs;
  }

  @Override
  public IValueElement getSimplified() {
    if (m_lhs.equals(m_rhs)) {
      return m_lhs.clone();
    } else if ((m_lhs instanceof Undefined) || (m_rhs instanceof Undefined)) {
      return new Undefined();
    } else {
      return this;
    }
  }

  @Override
  public Set<String> getVariables() {
    final Set<String> variables = new HashSet<String>();
    variables.addAll(m_lhs.getVariables());
    variables.addAll(m_rhs.getVariables());
    return variables;
  }

  @Override
  public int hashCode() {
    return 555 * m_lhs.hashCode() * m_rhs.hashCode();
  }

  @Override
  public String toString() {
    return "(" + m_lhs + " || " + m_rhs + ")";
  }
}
