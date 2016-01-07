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

public class Subtraction implements IValueElement {
  private final IValueElement m_lhs;

  private final IValueElement m_rhs;

  public Subtraction(final IValueElement lhs, final IValueElement rhs) {
    Preconditions.checkArgument(!(lhs instanceof Undefined),
        "Error: LHS side of an Addition can not be undefined");
    Preconditions.checkArgument(!(rhs instanceof Undefined),
        "Error: RHS side of an Addition can not be undefined");

    m_lhs = lhs;
    m_rhs = rhs;
  }

  @Override
  public Subtraction clone() {
    return new Subtraction(m_lhs.clone(), m_rhs.clone());
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof Subtraction) && ((Subtraction) rhs).m_lhs.equals(m_lhs)
        && ((Subtraction) rhs).m_rhs.equals(m_rhs);
  }

  @Override
  public BigInteger evaluate() {
    return m_lhs.evaluate().subtract(m_rhs.evaluate());
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

    if (simplifiedRhs instanceof Literal) {
      if (simplifiedLhs instanceof Literal) {
        return new Literal(((Literal) simplifiedLhs).getValue().subtract(
            ((Literal) simplifiedRhs).getValue()));
      } else if ((simplifiedLhs instanceof Addition)
          && (((Addition) simplifiedLhs).getRhs() instanceof Literal)) {
        return new Addition(((Addition) simplifiedLhs).getLhs(), new Literal(
            ((Literal) ((Addition) simplifiedLhs).getRhs()).getValue().subtract(
                ((Literal) simplifiedRhs).getValue())));
      } else if ((simplifiedLhs instanceof Subtraction)
          && (((Subtraction) simplifiedLhs).getRhs() instanceof Literal)) {
        return new Subtraction(((Subtraction) simplifiedLhs).getLhs(), new Literal(
            ((Literal) ((Subtraction) simplifiedLhs).getRhs()).getValue().add(
                ((Literal) simplifiedRhs).getValue())));
      }
    }

    return new Subtraction(simplifiedLhs, simplifiedRhs);
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
    return m_lhs.hashCode() * m_rhs.hashCode() * m_rhs.hashCode();
  }

  @Override
  public String toString() {
    return "(" + m_lhs + " - " + m_rhs + ")";
  }
}
