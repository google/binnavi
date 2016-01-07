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
import com.google.common.collect.Sets;

public class BitwiseAnd implements IValueElement {
  private final IValueElement m_lhs;

  private final IValueElement m_rhs;

  public BitwiseAnd(final IValueElement lhs, final IValueElement rhs) {
    Preconditions.checkArgument(!(lhs instanceof Undefined),
        "Error: LHS side of an Addition can not be undefined");
    Preconditions.checkArgument(!(rhs instanceof Undefined),
        "Error: RHS side of an Addition can not be undefined");

    m_lhs = lhs;
    m_rhs = rhs;
  }

  @Override
  public BitwiseAnd clone() {
    return new BitwiseAnd(m_lhs.clone(), m_rhs.clone());
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof BitwiseAnd) && ((BitwiseAnd) rhs).m_lhs.equals(m_lhs)
        && ((BitwiseAnd) rhs).m_rhs.equals(m_rhs);
  }

  @Override
  public BigInteger evaluate() {
    return m_lhs.evaluate().and(m_rhs.evaluate());
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

    if (simplifiedLhs instanceof BitwiseAnd) {
      if (((BitwiseAnd) simplifiedLhs).getRhs().equals(simplifiedRhs)) {
        return simplifiedLhs;
      } else if (((BitwiseAnd) simplifiedLhs).getLhs().equals(simplifiedRhs)) {
        return simplifiedLhs;
      } else {
        return new BitwiseAnd(simplifiedLhs, simplifiedRhs);
      }
    } else if (simplifiedRhs instanceof BitwiseAnd) {
      if (((BitwiseAnd) simplifiedRhs).getRhs().equals(simplifiedLhs)) {
        return simplifiedRhs;
      } else if (((BitwiseAnd) simplifiedRhs).getLhs().equals(simplifiedLhs)) {
        return simplifiedRhs;
      } else {
        return new BitwiseAnd(simplifiedLhs, simplifiedRhs);
      }
    } else if ((simplifiedRhs instanceof Literal) && (simplifiedLhs instanceof Literal)) {
      return new Literal(((Literal) simplifiedLhs).getValue().and(
          ((Literal) simplifiedRhs).getValue()));
    }

    else {
      return new BitwiseAnd(simplifiedLhs, simplifiedRhs);
    }
  }

  @Override
  public Set<String> getVariables() {
    final HashSet<String> set = Sets.newHashSet(m_lhs.getVariables());
    set.addAll(m_rhs.getVariables());
    return set;
  }

  @Override
  public int hashCode() {
    return 7 * m_lhs.hashCode() * m_rhs.hashCode();
  }

  @Override
  public String toString() {
    return "(" + m_lhs + " & " + m_rhs + ")";
  }
}
