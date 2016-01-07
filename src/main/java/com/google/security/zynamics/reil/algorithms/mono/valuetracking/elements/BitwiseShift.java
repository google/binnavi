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

import com.google.common.base.Preconditions;

public class BitwiseShift implements IValueElement {
  private final IValueElement m_lhs;

  private final IValueElement m_rhs;

  public BitwiseShift(final IValueElement lhs, final IValueElement rhs) {
    Preconditions.checkNotNull(lhs, "Error: lhs argument can not be null");
    Preconditions.checkNotNull(rhs, "Error: rhs argument can not be null");
    Preconditions.checkArgument(!(lhs instanceof Undefined),
        "Error: LHS side of an Addition can not be undefined");
    Preconditions.checkArgument(!(rhs instanceof Undefined),
        "Error: RHS side of an Addition can not be undefined");

    m_lhs = lhs;
    m_rhs = rhs;
  }

  @Override
  public BitwiseShift clone() {
    return new BitwiseShift(m_lhs.clone(), m_rhs.clone());
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof BitwiseShift) && ((BitwiseShift) rhs).m_lhs.equals(m_lhs)
        && ((BitwiseShift) rhs).m_rhs.equals(m_rhs);
  }

  @Override
  public BigInteger evaluate() {
    throw new IllegalStateException("Not yet implemented");
  }

  public IValueElement getLhs() {
    return m_lhs;
  }

  public IValueElement getRhs() {
    return m_rhs;
  }

  @Override
  public IValueElement getSimplified() {
    final IValueElement simplifiedLhs = m_lhs.getSimplified();
    final IValueElement simplifiedRhs = m_rhs.getSimplified();

    if ((simplifiedLhs instanceof Literal) && (simplifiedRhs instanceof Literal)) {
      final Literal lhs = (Literal) simplifiedLhs;
      final Literal rhs = (Literal) simplifiedRhs;

      final BigInteger lhsValue = lhs.getValue();
      final BigInteger rhsValue = rhs.getValue();

      if (lhsValue.compareTo(BigInteger.ZERO) == -1) {
        final BigInteger result = lhsValue.shiftLeft(rhsValue.intValue());

        return new Literal(result);
      } else {
        final BigInteger result = lhsValue.shiftRight(rhsValue.intValue());

        return new Literal(result);
      }
    } else {
      return clone();
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
    return 777 * m_lhs.hashCode() * m_rhs.hashCode();
  }

  @Override
  public String toString() {
    if (m_rhs instanceof Literal) {
      final BigInteger value = ((Literal) m_rhs).getValue();

      if (value.compareTo(BigInteger.ZERO) < 0) {
        return "(" + m_lhs + " >> " + value.abs() + ")";
      } else {
        return "(" + m_lhs + " << " + m_rhs + ")";
      }
    } else {
      return "(" + m_lhs + " SHIFT " + m_rhs + ")";
    }
  }
}
